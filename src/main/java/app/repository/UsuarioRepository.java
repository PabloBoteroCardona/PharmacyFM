package app.repository;

import app.DatabaseConnection;
import com.pharmacyfm.domain.model.Role;
import com.pharmacyfm.domain.model.User;

import java.sql.*;

public class UsuarioRepository {

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
                            Role.from(rs.getString("rol"))
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error buscando usuario por email: " + e.getMessage());
        }
        return null;
    }

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
            System.err.println("Error obteniendo hash de contraseña: " + e.getMessage());
        }
        return null;
    }

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
            System.err.println("Error comprobando existencia de email: " + e.getMessage());
        }
        return false;
    }

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

    public boolean updatePassword(String email, String passwordHash) {
        String sql = "UPDATE usuarios SET password = ? WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando contraseña: " + e.getMessage());
            return false;
        }
    }
}
