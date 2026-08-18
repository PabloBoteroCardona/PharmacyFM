package app.service;

import app.DatabaseConnection;
import app.repository.PacienteRepository;
import app.repository.UsuarioRepository;
import com.pharmacyfm.domain.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;

public class AuthService {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();

    public User login(String email, String password) {
        String hashGuardado = usuarioRepository.getPasswordHashByEmail(email);

        if (hashGuardado == null) return null;
        if (!BCrypt.checkpw(password, hashGuardado)) return null;

        return usuarioRepository.findByEmail(email);
    }

    public boolean registrarPaciente(String nombre, String email, String password, String telefono) {
        if (usuarioRepository.existsByEmail(email)) return false;

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int idUsuario = usuarioRepository.insert(email, passwordHash, nombre, telefono, "paciente", conn);
            if (idUsuario <= 0) { conn.rollback(); return false; }

            boolean pacienteInsertado = pacienteRepository.insert(idUsuario, nombre, telefono, email, conn);
            if (!pacienteInsertado) { conn.rollback(); return false; }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error registrando paciente: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println(ex.getMessage()); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { System.err.println(e.getMessage()); }
            }
        }
    }

    public boolean recuperarPassword(String email, String nuevaPassword) {
        if (!usuarioRepository.existsByEmail(email)) return false;
        String nuevoHash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt());
        return usuarioRepository.updatePassword(email, nuevoHash);
    }
}
