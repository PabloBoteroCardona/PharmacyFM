package app.repository;

import app.DatabaseConnection;
import app.User;

import java.sql.*;

/**
 * Repositorio de usuarios.
 * Responsable exclusivamente de las operaciones de base de datos
 * relacionadas con la tabla 'usuarios'.
 */
public class UsuarioRepository {

    /**
     * Busca un usuario por su email.
     * Devuelve el User si existe, null si no.
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("nombre"),
                            rs.getString("telefono"),
                            rs.getString("rol")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error buscando usuario por email:");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Devuelve el hash de contraseña almacenado para un email dado.
     * Devuelve null si el email no existe.
     */
    public String getPasswordHashByEmail(String email) {
        String sql = "SELECT password FROM usuarios WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo hash de contraseña:");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Comprueba si ya existe un usuario registrado con ese email.
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error comprobando existencia de email:");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     * Devuelve el id generado, o -1 si falla.
     */
    public int insert(String email, String passwordHash, String nombre, String telefono, String rol, Connection conn) throws SQLException {
        String sql = "INSERT INTO usuarios (email, password, nombre, telefono, rol) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, email);
            stmt.setString(2, passwordHash);
            stmt.setString(3, nombre);
            stmt.setString(4, telefono);
            stmt.setString(5, rol);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Actualiza la contraseña de un usuario dado su email.
     * Recibe el hash ya generado con BCrypt.
     * Devuelve true si se actualizó correctamente.
     */
    public boolean updatePassword(String email, String passwordHash) {
        String sql = "UPDATE usuarios SET password = ? WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando contraseña:");
            e.printStackTrace();
            return false;
        }
    }
}