package com.pharmacyfm.infrastructure.persistence;

import com.pharmacyfm.domain.model.Paciente;
import com.pharmacyfm.domain.port.PacienteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Adaptador JDBC para el puerto PacienteRepository.
 *
 * Implementa el acceso a la tabla 'pacientes'. El método insert participa
 * en la transacción de registro de AuthService (usuario + paciente atómicos),
 * por eso recibe la Connection en lugar de abrirla.
 *
 * El proveedor de conexiones es inyectable para facilitar los tests de integración.
 */
public class JdbcPacienteRepository implements PacienteRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcPacienteRepository.class);

    /** Factoría de conexiones. En producción usa SqliteConnectionProvider. */
    private final Supplier<Connection> connectionFactory;

    /** Constructor de producción. */
    public JdbcPacienteRepository() {
        this(() -> {
            try { return SqliteConnectionProvider.getConnection(); }
            catch (SQLException e) { throw new RuntimeException(e); }
        });
    }

    /** Constructor para tests: permite inyectar una conexión en memoria. */
    public JdbcPacienteRepository(Supplier<Connection> connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private Connection getConn() throws SQLException {
        try {
            return connectionFactory.get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException) throw (SQLException) e.getCause();
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     * Devuelve el perfil de paciente vinculado a un ID de usuario.
     */
    @Override
    public Paciente findByUserId(int idUsuario) {
        String sql = "SELECT * FROM pacientes WHERE id_usuario = ? LIMIT 1";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            log.error("[JdbcPacienteRepository] Error en findByUserId: {}", e.getMessage());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Devuelve todos los pacientes ordenados alfabéticamente.
     */
    @Override
    public List<Paciente> findAll() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes ORDER BY nombre";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }

        } catch (SQLException e) {
            log.error("[JdbcPacienteRepository] Error en findAll: {}", e.getMessage());
        }
        return lista;
    }

    /**
     * {@inheritDoc}
     * Inserta un paciente dentro de la transacción de registro de AuthService.
     */
    @Override
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
     * {@inheritDoc}
     * Actualiza los datos de contacto de un paciente.
     */
    @Override
    public boolean update(Paciente p) {
        String sql = "UPDATE pacientes SET nombre = ?, telefono = ?, email = ? WHERE id = ?";

        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getNombre());
            stmt.setString(2, p.getTelefono());
            stmt.setString(3, p.getEmail());
            stmt.setInt(4, p.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error("[JdbcPacienteRepository] Error en update: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Mapea una fila del ResultSet a un objeto Paciente inmutable.
     * Método privado compartido por findByUserId y findAll.
     *
     * @param rs ResultSet posicionado en la fila a leer.
     * @return Paciente construido con los datos de la fila.
     * @throws SQLException Si falla la lectura de alguna columna.
     */
    private Paciente mapRow(ResultSet rs) throws SQLException {
        return new Paciente(
                rs.getInt("id"),
                rs.getInt("id_usuario"),
                rs.getString("nombre"),
                rs.getString("telefono"),
                rs.getString("email")
        );
    }
}
