package app.service;

import app.DatabaseConnection;
import app.User;
import app.repository.PacienteRepository;
import app.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Servicio de autenticación.
 * Gestiona el login, el registro y la recuperación de contraseña.
 */
public class AuthService {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();

    /**
     * Intenta autenticar a un usuario con email y contraseña.
     * Devuelve el User si las credenciales son correctas, null si no.
     */
    public User login(String email, String password) {
        String hashGuardado = usuarioRepository.getPasswordHashByEmail(email);

        if (hashGuardado == null) {
            return null;
        }

        if (!BCrypt.checkpw(password, hashGuardado)) {
            return null;
        }

        return usuarioRepository.findByEmail(email);
    }

    /**
     * Registra un nuevo paciente.
     * Inserta en 'usuarios' y en 'pacientes' dentro de la misma transacción.
     * Devuelve true si el registro fue exitoso, false si el email ya existe o hay error.
     */
    public boolean registrarPaciente(String nombre, String email, String password, String telefono) {
        if (usuarioRepository.existsByEmail(email)) {
            return false;
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int idUsuario = usuarioRepository.insert(email, passwordHash, nombre, telefono, "paciente", conn);

            if (idUsuario <= 0) {
                conn.rollback();
                return false;
            }

            boolean pacienteInsertado = pacienteRepository.insert(idUsuario, nombre, telefono, email, conn);

            if (!pacienteInsertado) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error registrando paciente:");
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Recupera la contraseña de un usuario.
     * Comprueba que el email existe y actualiza la contraseña con BCrypt.
     * Devuelve true si se actualizó correctamente, false si el email no existe.
     */
    public boolean recuperarPassword(String email, String nuevaPassword) {
        // 1. Comprobar que el email existe en la BD
        if (!usuarioRepository.existsByEmail(email)) {
            return false;
        }

        // 2. Hashear la nueva contraseña
        String nuevoHash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt());

        // 3. Actualizar en la BD
        return usuarioRepository.updatePassword(email, nuevoHash);
    }
}