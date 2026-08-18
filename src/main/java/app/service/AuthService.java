package app.service;

import com.pharmacyfm.domain.port.PacienteRepository;
import com.pharmacyfm.domain.port.UserRepository;
import com.pharmacyfm.domain.model.User;
import com.pharmacyfm.infrastructure.persistence.JdbcPacienteRepository;
import com.pharmacyfm.infrastructure.persistence.JdbcUserRepository;
import com.pharmacyfm.infrastructure.persistence.SqliteConnectionProvider;
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
 *
 * Depende de los puertos domain.port.UserRepository y domain.port.PacienteRepository;
 * las implementaciones concretas se inyectarán por constructor en F3.
 */
public class AuthService {

    // Tipados como puertos de dominio — el servicio no conoce la tecnología subyacente
    private final UserRepository userRepository;
    private final PacienteRepository pacienteRepository;

    /**
     * Constructor por defecto: cablea las implementaciones JDBC concretas.
     * En F3 se sustituirá por inyección de dependencias real.
     */
    public AuthService() {
        this.userRepository     = new JdbcUserRepository();
        this.pacienteRepository = new JdbcPacienteRepository();
    }

    /**
     * Constructor para tests: permite inyectar implementaciones alternativas (dobles de test).
     *
     * @param userRepository     Implementación del puerto de usuarios.
     * @param pacienteRepository Implementación del puerto de pacientes.
     */
    public AuthService(UserRepository userRepository, PacienteRepository pacienteRepository) {
        this.userRepository     = userRepository;
        this.pacienteRepository = pacienteRepository;
    }

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
        String hashGuardado = userRepository.getPasswordHashByEmail(email);

        if (hashGuardado == null) return null;

        // BCrypt.checkpw compara la contraseña introducida contra el hash almacenado
        if (!BCrypt.checkpw(password, hashGuardado)) return null;

        // Credenciales válidas: cargamos y devolvemos el perfil completo del usuario
        return userRepository.findByEmail(email);
    }

    /**
     * Registra un nuevo paciente en el sistema mediante una transacción atómica.
     *
     * Pasos internos:
     *   1. Verifica que el email no esté duplicado.
     *   2. Genera el hash BCrypt de la contraseña.
     *   3. Inserta el usuario en la tabla 'usuarios'.
     *   4. Inserta el perfil en la tabla 'pacientes' usando el ID generado.
     *   5. Confirma (commit) o revierte (rollback) ambas inserciones.
     *
     * @param nombre    Nombre completo del nuevo paciente.
     * @param email     Email único que también servirá como nombre de usuario.
     * @param password  Contraseña en texto plano (se hasheará antes de guardar).
     * @param telefono  Número de contacto del paciente.
     * @return true si el registro fue exitoso, false si el email ya existe o hubo error.
     */
    public boolean registrarPaciente(String nombre, String email, String password, String telefono) {
        // Comprobación de duplicidad antes de empezar la transacción
        if (userRepository.existsByEmail(email)) return false;

        // Generamos el hash con BCrypt (10 rondas de sal por defecto)
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        Connection conn = null;
        try {
            conn = SqliteConnectionProvider.getConnection();
            conn.setAutoCommit(false); // Iniciamos la transacción manual

            // Paso 1: Insertar el registro en la tabla de usuarios
            int idUsuario = userRepository.insert(email, passwordHash, nombre, telefono, "paciente", conn);
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
            System.err.println("[AuthService] Error registrando paciente: " + e.getMessage());
            // Revertimos cualquier cambio parcial realizado antes del error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("[AuthService] Error en rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            // Restauramos autoCommit y cerramos la conexión siempre, en éxito y en error
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[AuthService] Error cerrando conexión: " + e.getMessage());
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
        if (!userRepository.existsByEmail(email)) return false;

        String nuevoHash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt());
        return userRepository.updatePassword(email, nuevoHash);
    }
}
