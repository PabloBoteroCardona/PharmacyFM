package app.repository;

import app.DatabaseConnection;
import com.pharmacyfm.domain.model.EstadoPedido;
import com.pharmacyfm.domain.model.Pedido;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Pedido.
 *
 * Gestiona consultas complejas con JOIN entre las tablas 'pedidos',
 * 'pacientes' y 'formulas'. El mapeo de la columna 'estado' al enum
 * EstadoPedido garantiza que solo entren al dominio estados válidos.
 *
 * Soporta dos tipos de pedido:
 *   - Fórmula del catálogo (id_formula no nulo, formula_personalizada nulo).
 *   - Fórmula personalizada (id_formula nulo, formula_personalizada con texto).
 */
public class PedidoRepository {

    /**
     * Recupera el historial de pedidos de un paciente concreto,
     * ordenado del más reciente al más antiguo.
     *
     * @param idPaciente ID del paciente en la tabla 'pacientes'.
     * @return Lista de pedidos del paciente; lista vacía si no tiene ninguno.
     */
    public List<Pedido> findByPacienteId(int idPaciente) {
        List<Pedido> lista = new ArrayList<>();

        // JOIN con pacientes y LEFT JOIN con fórmulas (puede ser nulo si es personalizada)
        String sql =
            "SELECT p.id, p.fecha, p.estado, p.cantidad, p.unidad, p.observaciones, " +
            "       f.nombre AS nombre_formula, pac.nombre AS nombre_paciente, " +
            "       p.formula_personalizada " +
            "FROM pedidos p " +
            "JOIN pacientes pac ON pac.id = p.id_paciente " +
            "LEFT JOIN formulas f ON f.id = p.id_formula " +
            "WHERE p.id_paciente = ? " +
            "ORDER BY p.fecha DESC, p.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPaciente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToPedido(rs, idPaciente));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo pedidos por paciente: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Recupera todos los pedidos del sistema para el panel de administración,
     * ordenados del más reciente al más antiguo.
     *
     * @return Lista con todos los pedidos registrados; lista vacía si no hay ninguno.
     */
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapResultSetToPedido(rs, rs.getInt("id_paciente")));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo todos los pedidos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Registra un nuevo pedido en la base de datos.
     *
     * Acepta tanto fórmulas del catálogo (idFormula no nulo) como
     * fórmulas personalizadas (idFormula nulo, formulaPersonalizada con texto).
     * El estado inicial siempre es PENDIENTE.
     *
     * @param idPaciente          ID del paciente que realiza el pedido.
     * @param idFormula           ID de la fórmula del catálogo, o null si es personalizada.
     * @param formulaPersonalizada Nombre de la fórmula si no está en el catálogo.
     * @param cantidad            Unidades solicitadas (debe ser > 0).
     * @param unidad              Tipo de unidad (Cápsulas, Gramos, Mililitros…).
     * @param observaciones       Indicaciones adicionales del paciente o médico.
     * @return true si el pedido se insertó correctamente.
     */
    public boolean insert(int idPaciente, Integer idFormula, String formulaPersonalizada,
                          int cantidad, String unidad, String observaciones) {

        String sql =
            "INSERT INTO pedidos " +
            "(id_paciente, id_formula, formula_personalizada, cantidad, unidad, observaciones, fecha, estado) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Timestamp de creación del pedido en formato ISO local
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPaciente);

            // Si es fórmula personalizada, la FK de catálogo se deja como NULL explícito
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
            // Todo pedido nuevo comienza siempre en estado PENDIENTE
            stmt.setString(8, EstadoPedido.PENDIENTE.getLabel());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error insertando pedido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza el estado de un pedido existente.
     * El texto del estado debe ser un label válido de EstadoPedido.
     *
     * @param idPedido    ID del pedido a actualizar.
     * @param nuevoEstado Label del nuevo estado (ej. "En preparación").
     * @return true si se actualizó correctamente.
     */
    public boolean updateEstado(int idPedido, String nuevoEstado) {
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idPedido);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando estado del pedido: " + e.getMessage());
            return false;
        }
    }

    /**
     * Transforma una fila del ResultSet en un objeto Pedido de dominio.
     *
     * Aplica la lógica de prioridad de nombre de fórmula:
     *   1. Si existe nombre del catálogo → se usa ese.
     *   2. Si no → se usa el nombre personalizado introducido por el paciente.
     *   3. Si tampoco hay nombre personalizado → texto de sustitución.
     *
     * La columna 'estado' se convierte al enum EstadoPedido mediante from(),
     * lanzando excepción si la BD contiene un valor no reconocido.
     *
     * @param rs         ResultSet posicionado en la fila a mapear.
     * @param idPaciente ID del paciente que se asociará al pedido.
     * @return Objeto Pedido inmutable construido con los datos de la fila.
     * @throws SQLException Si falla la lectura de alguna columna del ResultSet.
     */
    private Pedido mapResultSetToPedido(ResultSet rs, int idPaciente) throws SQLException {
        String nombreFormula   = rs.getString("nombre_formula");
        String formulaPersonal = rs.getString("formula_personalizada");

        // Prioridad: catálogo > personalizada > placeholder
        if (nombreFormula == null || nombreFormula.isEmpty()) {
            nombreFormula = (formulaPersonal != null && !formulaPersonal.isEmpty())
                    ? formulaPersonal : "(fórmula personalizada)";
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
