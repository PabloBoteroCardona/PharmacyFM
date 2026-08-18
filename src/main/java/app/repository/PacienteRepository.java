package app.repository;

import app.DatabaseConnection;
import com.pharmacyfm.domain.model.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad Paciente.
 *
 * Centraliza todas las operaciones sobre la tabla 'pacientes'.
 * Los métodos que forman parte de una transacción externa (como el registro)
 * reciben la conexión por parámetro y no la cierran — esa responsabilidad
 * recae en el llamante (AuthService).
 */
public class PacienteRepository {

    /**
     * Busca el perfil de paciente vinculado a un ID de usuario concreto.
     * Se usa al iniciar sesión para cargar los datos del paciente autenticado.
     *
     * @param idUsuario ID del usuario en la tabla 'usuarios' (clave foránea).
     * @return El Paciente correspondiente, o null si no existe perfil asociado.
     */
    public Paciente findByUserId(int idUsuario) {
        String sql = "SELECT * FROM pacientes WHERE id_usuario = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Paciente(
                            rs.getInt("id"),
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getString("telefono"),
                            rs.getString("email")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo paciente por id_usuario: " + e.getMessage());
        }
        return null;
    }

    /**
     * Recupera todos los pacientes registrados, ordenados alfabéticamente.
     * Utilizado por el panel de administración para gestionar la lista completa.
     *
     * @return Lista de pacientes; lista vacía si no hay ninguno.
     */
    public List<Paciente> findAll() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Paciente(
                        rs.getInt("id"),
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("email")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo la lista de pacientes: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Inserta un nuevo perfil de paciente dentro de una transacción externa.
     *
     * Recibe la conexión desde AuthService para garantizar que la creación
     * del usuario y la del paciente sean atómicas: si una falla, ambas
     * se deshacen con un rollback.
     *
     * @param idUsuario ID del usuario recién creado en la tabla 'usuarios'.
     * @param nombre    Nombre completo del paciente.
     * @param telefono  Número de contacto.
     * @param email     Dirección de correo electrónico.
     * @param conn      Conexión transaccional proporcionada por AuthService.
     * @return true si la inserción fue exitosa.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public boolean insert(int idUsuario, String nombre, String telefono,
                          String email, Connection conn) throws SQLException {
        String sql = "INSERT INTO pacientes (id_usuario, nombre, telefono, email) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setString(2, nombre);
            stmt.setString(3, telefono);
            stmt.setString(4, email);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza los datos de contacto de un paciente existente.
     * Recibe el objeto Paciente completo con los nuevos valores ya aplicados
     * mediante los métodos with* del modelo de dominio inmutable.
     *
     * @param p Paciente con los datos actualizados (debe tener id > 0).
     * @return true si la actualización afectó al menos una fila.
     */
    public boolean update(Paciente p) {
        String sql = "UPDATE pacientes SET nombre = ?, telefono = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getNombre());
            stmt.setString(2, p.getTelefono());
            stmt.setString(3, p.getEmail());
            stmt.setInt(4, p.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando paciente: " + e.getMessage());
            return false;
        }
    }
}
