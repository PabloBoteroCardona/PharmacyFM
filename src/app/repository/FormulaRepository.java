package app.repository;

import app.DatabaseConnection;
import app.Formula;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de fórmulas magistrales.
 * Responsable exclusivamente de las operaciones de base de datos
 * relacionadas con la tabla 'formulas'.
 */
public class FormulaRepository {

    /**
     * Devuelve todas las fórmulas ordenadas alfabéticamente.
     */
    public List<Formula> findAll() {
        List<Formula> lista = new ArrayList<>();
        String sql = "SELECT * FROM formulas ORDER BY nombre ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Formula(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo fórmulas:");
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Inserta una nueva fórmula y devuelve el id generado.
     * Devuelve -1 si falla.
     */
    public int insert(Formula f) {
        String sql = "INSERT INTO formulas (nombre, descripcion, precio) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, f.getNombre());
            stmt.setString(2, f.getDescripcion());
            stmt.setDouble(3, f.getPrecio());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error insertando fórmula:");
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Actualiza los datos de una fórmula existente.
     */
    public boolean update(Formula f) {
        String sql = "UPDATE formulas SET nombre = ?, descripcion = ?, precio = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, f.getNombre());
            stmt.setString(2, f.getDescripcion());
            stmt.setDouble(3, f.getPrecio());
            stmt.setInt(4, f.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando fórmula:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina una fórmula por su id.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM formulas WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error eliminando fórmula:");
            e.printStackTrace();
            return false;
        }
    }
}