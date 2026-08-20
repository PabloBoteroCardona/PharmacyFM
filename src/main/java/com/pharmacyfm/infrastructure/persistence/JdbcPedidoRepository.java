package com.pharmacyfm.infrastructure.persistence;

import com.pharmacyfm.domain.model.EstadoPedido;
import com.pharmacyfm.domain.model.Pedido;
import com.pharmacyfm.domain.port.PedidoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Adaptador JDBC para el puerto PedidoRepository.
 *
 * Gestiona las consultas sobre la tabla 'pedidos', que incluye JOINs con
 * 'pacientes' y LEFT JOIN con 'formulas' (el JOIN es LEFT porque los pedidos
 * personalizados no tienen referencia a la tabla de catálogo).
 *
 * La columna 'estado' se convierte al enum EstadoPedido mediante from(),
 * lanzando IllegalArgumentException si la BD contiene un valor no reconocido.
 *
 * El proveedor de conexiones es inyectable para facilitar los tests de integración.
 */
public class JdbcPedidoRepository implements PedidoRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcPedidoRepository.class);

    /** Formato de fecha/hora usado al insertar nuevos pedidos en SQLite. */
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Factoría de conexiones. En producción usa SqliteConnectionProvider. */
    private final Supplier<Connection> connectionFactory;

    /** Constructor de producción. */
    public JdbcPedidoRepository() {
        this(() -> {
            try { return SqliteConnectionProvider.getConnection(); }
            catch (SQLException e) { throw new RuntimeException(e); }
        });
    }

    /** Constructor para tests: permite inyectar una conexión en memoria. */
    public JdbcPedidoRepository(Supplier<Connection> connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private Connection getConn() throws SQLException {
        try {
            return connectionFactory.get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException) throw (SQLException) e.getCause();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     * Devuelve el historial de un paciente, del más reciente al más antiguo.
     */
    @Override
    public List<Pedido> findByPacienteId(int idPaciente) {
        List<Pedido> lista = new ArrayList<>();

        String sql =
            "SELECT p.id, p.fecha, p.estado, p.cantidad, p.unidad, p.observaciones, " +
            "       f.nombre AS nombre_formula, pac.nombre AS nombre_paciente, " +
            "       p.formula_personalizada " +
            "FROM pedidos p " +
            "JOIN pacientes pac ON pac.id = p.id_paciente " +
            "LEFT JOIN formulas f ON f.id = p.id_formula " +
            "WHERE p.id_paciente = ? " +
            "ORDER BY p.fecha DESC, p.id DESC";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPaciente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs, idPaciente));
                }
            }

        } catch (SQLException e) {
            log.error("[JdbcPedidoRepository] Error en findByPacienteId: {}", e.getMessage());
        }
        return lista;
    }

    /**
     * {@inheritDoc}
     * Devuelve todos los pedidos del sistema para el panel de administración.
     */
    @Override
    public List<Pedido> findAll() {
        List<Pedido> lista = new ArrayList<>();

        String sql =
            "SELECT p.id, p.fecha, p.estado, p.cantidad, p.unidad, p.observaciones, " +
            "       f.nombre AS nombre_formula, pac.nombre AS nombre_paciente, " +
            "       pac.id AS id_paciente, p.formula_personalizada " +
            "FROM pedidos p " +
            "JOIN pacientes pac ON pac.id = p.id_paciente " +
            "LEFT JOIN formulas f ON f.id = p.id_formula " +
            "ORDER BY p.fecha DESC, p.id DESC";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs, rs.getInt("id_paciente")));
            }

        } catch (SQLException e) {
            log.error("[JdbcPedidoRepository] Error en findAll: {}", e.getMessage());
        }
        return lista;
    }

    /**
     * {@inheritDoc}
     * Inserta un nuevo pedido con estado inicial PENDIENTE.
     * Acepta tanto fórmulas de catálogo (idFormula no nulo) como personalizadas.
     */
    @Override
    public boolean insert(int idPaciente, Integer idFormula, String formulaPersonalizada,
                          int cantidad, String unidad, String observaciones) {

        String sql =
            "INSERT INTO pedidos " +
            "(id_paciente, id_formula, formula_personalizada, cantidad, unidad, observaciones, fecha, estado) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Registramos la fecha y hora exacta de creación del pedido
        String fecha = LocalDateTime.now().format(FORMATO_FECHA);

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPaciente);

            // Si es personalizado, la FK de catálogo se deja como NULL explícito
            if (idFormula == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, idFormula);
            }

            stmt.setString(3, formulaPersonalizada);
            stmt.setInt(4, cantidad);
            stmt.setString(5, unidad);
            stmt.setString(6, observaciones);
            stmt.setString(7, fecha);
            // Todo pedido nuevo comienza en estado PENDIENTE
            stmt.setString(8, EstadoPedido.PENDIENTE.getLabel());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("[JdbcPedidoRepository] Error en insert: {}", e.getMessage());
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * Actualiza el estado de un pedido existente.
     */
    @Override
    public boolean updateEstado(int idPedido, String nuevoEstado) {
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idPedido);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("[JdbcPedidoRepository] Error en updateEstado: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Transforma una fila del ResultSet en un Pedido de dominio inmutable.
     *
     * Lógica de prioridad para el nombre de fórmula:
     *   1. Nombre del catálogo (LEFT JOIN con formulas).
     *   2. Nombre personalizado escrito por el paciente.
     *   3. Texto sustituto "(fórmula personalizada)".
     *
     * @param rs         ResultSet posicionado en la fila a mapear.
     * @param idPaciente ID del paciente asociado al pedido.
     * @return Objeto Pedido inmutable.
     * @throws SQLException Si falla la lectura de alguna columna.
     */
    private Pedido mapRow(ResultSet rs, int idPaciente) throws SQLException {
        String nombreFormula   = rs.getString("nombre_formula");
        String formulaPersonal = rs.getString("formula_personalizada");

        // Aplicamos la prioridad: catálogo > personalizada > placeholder
        if (nombreFormula == null || nombreFormula.isEmpty()) {
            nombreFormula = (formulaPersonal != null && !formulaPersonal.isEmpty())
                    ? formulaPersonal
                    : "(fórmula personalizada)";
        }

        return new Pedido(
                rs.getInt("id"),
                idPaciente,
                rs.getString("nombre_paciente"),
                rs.getString("fecha"),
                nombreFormula,
                EstadoPedido.from(rs.getString("estado")),
                rs.getInt("cantidad"),
                rs.getString("unidad"),
                rs.getString("observaciones")
        );
    }
}
