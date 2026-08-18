package app.repository;

import app.DatabaseConnection;
import app.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de pacientes.
 * Centraliza todas las operaciones de persistencia para la entidad Paciente.
 * Sigue el patrón DAO para separar la lógica de acceso a datos de la interfaz.
 */
public class PacienteRepository {

    /**
     * Busca un paciente basándose en su identificador de usuario vinculado.
     * @param idUsuario ID del usuario en la tabla 'usuarios'.
     * @return Objeto Paciente si existe, o null en caso contrario.
     */
    public Paciente findByUserId(int idUsuario) {
        String sql = "SELECT * FROM pacientes WHERE id_usuario = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Seteo de parámetros para evitar ataques de SQL Injection
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Mapeo manual del registro de la BD al objeto modelo
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
            System.err.println("Error obteniendo paciente por id_usuario:");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Recupera la lista completa de pacientes registrados.
     * @return List de pacientes ordenados alfabéticamente por nombre.
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
            System.err.println("Error obteniendo la lista de pacientes:");
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Inserta un nuevo registro de paciente.
     * Nota técnica: Recibe el objeto Connection de forma externa para permitir
     * que esta operación forme parte de una transacción atómica (ej. crear usuario + paciente).
     */
    public boolean insert(int idUsuario, String nombre, String telefono, String email, Connection conn) throws SQLException {
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
     * @param p Objeto Paciente con los datos actualizados.
     * @return true si la actualización fue exitosa.
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
            System.err.println("Error actualizando paciente:");
            e.printStackTrace();
            return false;
        }
    }
}