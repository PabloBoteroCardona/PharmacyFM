package app.repository;

import app.DatabaseConnection;
import app.User;

import java.sql.*;

/**
 * Repositorio de usuarios.
 * Se encarga exclusivamente de las consultas y operaciones en la tabla 'usuarios'.
 * Centraliza el acceso a los datos de acceso y perfiles de usuario.
 */
public class UsuarioRepository {

    /**
     * Busca un usuario en la base de datos utilizando su dirección de email.
     * @param email El correo electrónico del usuario.
     * @return Un objeto User con los datos del perfil si se encuentra, o null si no existe.
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Evitamos inyección de código malicioso usando parámetros en la consulta
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Convertimos la fila de la base de datos en un objeto User
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
     * Recupera la contraseña encriptada (hash) asociada a un email.
     * Se utiliza durante el proceso de login para comparar con la clave introducida por el usuario.
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
     * Verifica si un correo electrónico ya está registrado en el sistema.
     * Útil para validar registros nuevos y evitar emails duplicados.
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Si el conteo es mayor a 0, el email ya existe
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
     * Crea un nuevo registro de usuario.
     * Nota: Recibe la conexión externa para poder realizar esta operación 
     * junto a la creación del perfil de paciente como una única acción.
     * @return El ID generado para el nuevo usuario o -1 si hubo un error.
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

            // Obtenemos el ID autoincremental que ha asignado la base de datos
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Modifica la contraseña de un usuario.
     * @param email El correo que identifica al usuario.
     * @param passwordHash El nuevo hash de la contraseña generado con BCrypt.
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