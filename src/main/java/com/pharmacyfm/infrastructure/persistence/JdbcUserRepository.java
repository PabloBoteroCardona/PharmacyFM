package com.pharmacyfm.infrastructure.persistence;

import com.pharmacyfm.domain.model.Role;
import com.pharmacyfm.domain.model.User;
import com.pharmacyfm.domain.port.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Adaptador JDBC para el puerto UserRepository.
 *
 * Accede a la tabla 'usuarios' para autenticación y gestión de cuentas.
 * La columna 'rol' se convierte al enum tipado Role mediante Role.from(),
 * garantizando que solo entren al dominio valores reconocidos.
 *
 * El método insert recibe la Connection externamente para participar en
 * la transacción de registro (usuario + paciente) que gestiona AuthService.
 */
public class JdbcUserRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcUserRepository.class);

    /**
     * {@inheritDoc}
     * Busca el usuario completo por email, mapeando 'rol' al enum Role.
     */
    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";

        try (Connection conn = SqliteConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Convertimos la fila en un objeto de dominio inmutable
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
            log.error("[JdbcUserRepository] Error en findByEmail: {}", e.getMessage());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Recupera solo el hash BCrypt para no cargar el User completo antes de validar.
     */
    @Override
    public String getPasswordHashByEmail(String email) {
        String sql = "SELECT password FROM usuarios WHERE email = ?";

        try (Connection conn = SqliteConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }

        } catch (SQLException e) {
            log.error("[JdbcUserRepository] Error en getPasswordHashByEmail: {}", e.getMessage());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Comprueba existencia de email sin cargar el usuario completo.
     */
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection conn = SqliteConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            log.error("[JdbcUserRepository] Error en existsByEmail: {}", e.getMessage());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * Inserta el usuario dentro de la transacción controlada por AuthService.
     * La conexión no se cierra aquí: la gestiona el llamante.
     */
    @Override
    public int insert(String email, String passwordHash, String nombre,
                      String telefono, String rol, Connection conn) throws SQLException {
        String sql = "INSERT INTO usuarios (email, password, nombre, telefono, rol) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, email);
            stmt.setString(2, passwordHash);
            stmt.setString(3, nombre);
            stmt.setString(4, telefono);
            stmt.setString(5, rol);
            stmt.executeUpdate();

            // Recuperamos el ID autogenerado por SQLite
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     * Reemplaza el hash de contraseña almacenado para un email.
     */
    @Override
    public boolean updatePassword(String email, String passwordHash) {
        String sql = "UPDATE usuarios SET password = ? WHERE email = ?";

        try (Connection conn = SqliteConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("[JdbcUserRepository] Error en updatePassword: {}", e.getMessage());
            return false;
        }
    }
}
