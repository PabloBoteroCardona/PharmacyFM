package app.repository;

import app.DatabaseConnection;
import com.pharmacyfm.domain.model.Role;
import com.pharmacyfm.domain.model.User;

import java.sql.*;

/**
 * Repositorio de acceso a datos para la entidad Usuario.
 *
 * Responsabilidad única: centraliza todas las operaciones SQL
 * sobre la tabla 'usuarios'. Ninguna otra clase debe ejecutar
 * consultas sobre esa tabla directamente.
 *
 * Todas las consultas usan PreparedStatement con marcadores '?'
 * para prevenir inyección SQL.
 */
public class UsuarioRepository {

    /**
     * Busca un usuario completo por su dirección de email.
     * Mapea la columna 'rol' al enum tipado Role mediante Role.from().
     *
     * @param email Correo electrónico del usuario a buscar.
     * @return El objeto User si existe, o null si no se encuentra.
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Convertimos la fila del ResultSet en un objeto de dominio inmutable
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

    /**
     * Obtiene únicamente el hash BCrypt almacenado para un email.
     * Se usa durante el login para comparar con la contraseña introducida
     * sin cargar el objeto User completo antes de validar credenciales.
     *
     * @param email Correo del usuario cuya contraseña se quiere verificar.
     * @return El hash BCrypt, o null si el email no existe.
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
            System.err.println("Error obteniendo hash de contraseña: " + e.getMessage());
        }
        return null;
    }

    /**
     * Comprueba si un email ya está registrado en el sistema.
     * Se utiliza antes de insertar un nuevo usuario para evitar duplicados.
     *
     * @param email Email a comprobar.
     * @return true si ya existe un usuario con ese email, false en caso contrario.
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
            System.err.println("Error comprobando existencia de email: " + e.getMessage());
        }
        return false;
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * Recibe la conexión externamente para poder participar en una transacción
     * controlada desde AuthService (registro de usuario + paciente en una sola
     * operación atómica). No cierra la conexión recibida.
     *
     * @param email        Email único del nuevo usuario.
     * @param passwordHash Hash BCrypt de la contraseña.
     * @param nombre       Nombre completo.
     * @param telefono     Número de contacto.
     * @param rol          Texto del rol: "admin" o "paciente".
     * @param conn         Conexión transaccional proporcionada por el llamante.
     * @return El ID autoincremental asignado por la BD, o -1 si hubo un error.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
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

            // Recuperamos el ID que la BD ha asignado automáticamente
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Actualiza la contraseña almacenada para un usuario identificado por email.
     * El hash debe generarse con BCrypt antes de llamar a este método.
     *
     * @param email        Email del usuario cuya contraseña se actualiza.
     * @param passwordHash Nuevo hash BCrypt de la contraseña.
     * @return true si se actualizó al menos una fila, false si el usuario no existe.
     */
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
