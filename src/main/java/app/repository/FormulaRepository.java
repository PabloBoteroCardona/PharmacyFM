package app.repository;

import app.DatabaseConnection;
import app.Formula;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de fórmulas magistrales.
 * Implementa el patrón DAO (Data Access Object) para centralizar la persistencia 
 * de la entidad Formula, desacoplando la lógica de base de datos del resto de la app.
 */
public class FormulaRepository {

    /**
     * Consulta la base de datos para obtener el catálogo completo de fórmulas.
     * @return List de objetos Formula ordenados alfabéticamente.
     */
    public List<Formula> findAll() {
        List<Formula> lista = new ArrayList<>();
        String sql = "SELECT * FROM formulas ORDER BY nombre ASC";

        // Uso de try-with-resources para garantizar el cierre de recursos JDBC
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Mapeo del ResultSet al objeto modelo Formula
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
     * Registra una nueva fórmula en el sistema.
     * @param f Objeto Formula con los datos a persistir.
     * @return El ID generado por la base de datos o -1 en caso de error.
     */
    public int insert(Formula f) {
        String sql = "INSERT INTO formulas (nombre, descripcion, precio) VALUES (?, ?, ?)";

        // Se solicita el retorno de claves generadas para obtener el ID asignado por SQLite
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Asignación de parámetros mediante PreparedStatement para prevenir SQL Injection
            stmt.setString(1, f.getNombre());
            stmt.setString(2, f.getDescripcion());
            stmt.setDouble(3, f.getPrecio());
            stmt.executeUpdate();

            // Recuperación del ID autoincremental generado
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
     * Actualiza un registro existente en la tabla 'formulas'.
     * @return true si la operación afectó a alguna fila, false en caso contrario.
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
     * Elimina una fórmula de forma permanente según su identificador único.
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