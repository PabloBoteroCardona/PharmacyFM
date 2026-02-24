package app.repository;

import app.DatabaseConnection;
import app.Pedido;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de pedidos.
 * Responsable exclusivamente de las operaciones de base de datos
 * relacionadas con la tabla 'pedidos'.
 */
public class PedidoRepository {

    /**
     * Devuelve todos los pedidos de un paciente concreto.
     */
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPaciente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToPedido(rs, idPaciente));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo pedidos por paciente:");
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Devuelve todos los pedidos. Para uso exclusivo del administrador.
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
            System.err.println("Error obteniendo todos los pedidos:");
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Inserta un nuevo pedido con unidad.
     */
    public boolean insert(int idPaciente, Integer idFormula, String formulaPersonalizada,
                          int cantidad, String unidad, String observaciones) {

        String sql =
            "INSERT INTO pedidos " +
            "(id_paciente, id_formula, formula_personalizada, cantidad, unidad, observaciones, fecha, estado) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String fecha  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPaciente);

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
            stmt.setString(8, "Pendiente");

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error insertando pedido:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza el estado de un pedido.
     */
    public boolean updateEstado(int idPedido, String nuevoEstado) {
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idPedido);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando estado del pedido:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convierte un ResultSet en un objeto Pedido.
     */
    private Pedido mapResultSetToPedido(ResultSet rs, int idPaciente) throws SQLException {
        String nombreFormula   = rs.getString("nombre_formula");
        String formulaPersonal = rs.getString("formula_personalizada");

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
                rs.getString("estado"),
                rs.getInt("cantidad"),
                rs.getString("unidad"),
                rs.getString("observaciones")
        );
    }
}