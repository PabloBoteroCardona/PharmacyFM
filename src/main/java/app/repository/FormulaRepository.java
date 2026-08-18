package app.repository;

import app.DatabaseConnection;
import com.pharmacyfm.domain.model.Formula;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Formula.
 *
 * Implementa el patrón DAO (Data Access Object): centraliza todas las
 * operaciones CRUD sobre la tabla 'formulas', aislando el resto de la
 * aplicación de los detalles de la base de datos.
 *
 * Devuelve objetos del modelo de dominio (com.pharmacyfm.domain.model.Formula),
 * nunca filas o tipos JDBC. Las colecciones vacías se devuelven como List vacías,
 * nunca como null.
 */
public class FormulaRepository {

    /**
     * Recupera el catálogo completo de fórmulas magistrales ordenado por nombre.
     *
     * @return Lista de fórmulas; lista vacía si no hay ninguna registrada.
     */
    public List<Formula> findAll() {
        List<Formula> lista = new ArrayList<>();
        String sql = "SELECT * FROM formulas ORDER BY nombre ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Mapeamos cada fila de la BD al modelo de dominio inmutable
                lista.add(new Formula(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo fórmulas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Inserta una nueva fórmula en la base de datos.
     * El ID de la fórmula recibida se ignora (debe ser 0 para fórmulas nuevas).
     *
     * @param f Fórmula a persistir con los datos del formulario de creación.
     * @return El ID autoincremental asignado por la BD, o -1 si ocurrió un error.
     */
    public int insert(Formula f) {
        String sql = "INSERT INTO formulas (nombre, descripcion, precio) VALUES (?, ?, ?)";

        // RETURN_GENERATED_KEYS permite recuperar el ID asignado automáticamente
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
            System.err.println("Error insertando fórmula: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Actualiza los datos de una fórmula existente identificada por su ID.
     *
     * @param f Fórmula con los datos actualizados (debe tener id > 0).
     * @return true si la actualización afectó al menos una fila.
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
            System.err.println("Error actualizando fórmula: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina permanentemente una fórmula del catálogo por su ID.
     *
     * @param id Identificador único de la fórmula a eliminar.
     * @return true si se eliminó la fórmula, false si el ID no existía.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM formulas WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error eliminando fórmula: " + e.getMessage());
            return false;
        }
    }
}
