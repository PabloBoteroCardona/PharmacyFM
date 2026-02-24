package app.repository;

import app.DatabaseConnection;
import app.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de pacientes.
 * Responsable exclusivamente de las operaciones de base de datos
 * relacionadas con la tabla 'pacientes'.
 */
public class PacienteRepository {

    /**
     * Devuelve el paciente asociado a un id de usuario.
     * Devuelve null si no existe.
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
            System.err.println("Error obteniendo paciente por id_usuario:");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Devuelve todos los pacientes ordenados por nombre.
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
     * Inserta un nuevo paciente asociado a un usuario.
     * Recibe la conexión activa para participar en la misma transacción.
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
     * Actualiza los datos de un paciente existente.
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