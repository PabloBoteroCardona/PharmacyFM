package app.repository;

import app.DatabaseConnection;
import com.pharmacyfm.domain.model.EstadoPedido;
import com.pharmacyfm.domain.model.Pedido;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PedidoRepository {

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
            System.err.println("Error obteniendo pedidos por paciente: " + e.getMessage());
        }
        return lista;
    }

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

    public boolean insert(int idPaciente, Integer idFormula, String formulaPersonalizada,
                          int cantidad, String unidad, String observaciones) {

        String sql =
            "INSERT INTO pedidos " +
            "(id_paciente, id_formula, formula_personalizada, cantidad, unidad, observaciones, fecha, estado) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

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
            stmt.setString(8, EstadoPedido.PENDIENTE.getLabel());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error insertando pedido: " + e.getMessage());
            return false;
        }
    }

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
                EstadoPedido.from(rs.getString("estado")),
                rs.getInt("cantidad"),
                rs.getString("unidad"),
                rs.getString("observaciones")
        );
    }
}
