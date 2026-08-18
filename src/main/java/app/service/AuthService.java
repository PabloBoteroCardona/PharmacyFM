package app.service;

import app.DatabaseConnection;
import app.repository.PacienteRepository;
import app.repository.UsuarioRepository;
import com.pharmacyfm.domain.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Servicio de autenticación y gestión de cuentas de usuario.
 *
 * Centraliza las operaciones de seguridad de la capa de presentación:
 * inicio de sesión con verificación BCrypt, registro de nuevos pacientes
 * y recuperación de contraseña olvidada.
 *
 * El método de registro ejecuta las inserciones en usuario y paciente
 * dentro de la misma transacción JDBC para garantizar atomicidad: si
 * alguna inserción falla, ambas se revierten con rollback.
 */
public class AuthService {

    // Repositorios que acceden a las tablas 'usuarios' y 'pacientes'
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();

    /**
     * Autentica a un usuario verificando su contraseña con BCrypt.
     *
     * El hash almacenado en la BD nunca se expone fuera de este método;
     * se compara internamente y se descarta inmediatamente.
     *
     * @param email    Dirección de correo del usuario.
     * @param password Contraseña en texto plano introducida en el formulario.
     * @return El objeto User inmutable si las credenciales son correctas,
     *         o null si el email no existe o la contraseña no coincide.
     */
    public User login(String email, String password) {
        // Recuperamos solo el hash; si el email no existe, devolvemos null de inmediato
        String hashGuardado = usuarioRepository.getPasswordHashByEmail(email);

        if (hashGuardado == null) return null;

        // BCrypt.checkpw compara la contraseña introducida contra el hash almacenado
        if (!BCrypt.checkpw(password, hashGuardado)) return null;

        // Credenciales válidas: cargamos y devolvemos el perfil completo del usuario
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Registra un nuevo paciente en el sistema mediante una transacción atómica.
     *
     * Pasos internos:
     *   1. Verifica que el email no esté duplicado.
     *   2. Genera el hash BCrypt de la contraseña.
     *   3. Inserta el usuario en la tabla 'usuarios'.
     *   4. Inserta el perfil en la tabla 'pacientes' usando el ID generado.
     *   5. Confirma la transacción (commit) o la revierte (rollback) si algo falla.
     *
     * @param nombre    Nombre completo del nuevo paciente.
     * @param email     Email único que también servirá como nombre de usuario.
     * @param password  Contraseña en texto plano (se hasheará antes de guardar).
     * @param telefono  Número de contacto del paciente.
     * @return true si el registro fue exitoso, false si el email ya existe o hubo error.
     */
    public boolean registrarPaciente(String nombre, String email, String password, String telefono) {
        // Comprobación de duplicidad antes de empezar la transacción
        if (usuarioRepository.existsByEmail(email)) return false;

        // Generamos el hash con BCrypt (10 rondas de sal por defecto)
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Iniciamos la transacción manual

            // Paso 1: Insertar el registro en la tabla de usuarios
            int idUsuario = usuarioRepository.insert(email, passwordHash, nombre, telefono, "paciente", conn);
            if (idUsuario <= 0) {
                conn.rollback();
                return false;
            }

            // Paso 2: Insertar el perfil de paciente vinculado al usuario recién creado
            boolean pacienteInsertado = pacienteRepository.insert(idUsuario, nombre, telefono, email, conn);
            if (!pacienteInsertado) {
                conn.rollback();
                return false;
            }

            // Todo correcto: confirmamos ambas inserciones como una sola unidad atómica
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error registrando paciente: " + e.getMessage());
            // Revertimos cualquier cambio parcial realizado antes del error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error en rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            // Restauramos autoCommit y cerramos la conexión siempre, tanto en éxito como en error
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error cerrando conexión transaccional: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Actualiza la contraseña de un usuario existente.
     *
     * No requiere conocer la contraseña anterior. Esta operación está
     * pensada para el flujo de «contraseña olvidada» donde se valida
     * la identidad del usuario por otro medio (ej. email de recuperación).
     *
     * @param email          Email del usuario cuya contraseña se cambia.
     * @param nuevaPassword  Nueva contraseña en texto plano (se hasheará).
     * @return true si la contraseña se actualizó, false si el email no existe.
     */
    public boolean recuperarPassword(String email, String nuevaPassword) {
        // Verificamos que el usuario exista antes de generar el hash
        if (!usuarioRepository.existsByEmail(email)) return false;

        String nuevoHash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt());
        return usuarioRepository.updatePassword(email, nuevoHash);
    }
}
