package app.service;

import com.pharmacyfm.domain.port.PacienteRepository;
import com.pharmacyfm.domain.port.UserRepository;
import com.pharmacyfm.domain.model.User;
import com.pharmacyfm.infrastructure.persistence.SqliteConnectionProvider;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

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
 * Todas las dependencias se inyectan por constructor desde AppContext.
 */
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Puerto de acceso a datos de usuarios. */
    private final UserRepository userRepository;

    /** Puerto de acceso a datos de pacientes (para el registro transaccional). */
    private final PacienteRepository pacienteRepository;

    /**
     * Constructor principal: recibe las dependencias inyectadas desde AppContext.
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
     * @return Optional con el User si las credenciales son correctas,
     *         o Optional.empty() si el email no existe o la contraseña no coincide.
     */
    public Optional<User> login(String email, String password) {
        log.debug("Intento de login para email={}", email);

        // Recuperamos solo el hash; si el email no existe, devolvemos vacío de inmediato
        Optional<String> hashGuardado = userRepository.getPasswordHashByEmail(email);

        if (hashGuardado.isEmpty()) {
            log.warn("Login fallido: email no registrado={}", email);
            return Optional.empty();
        }

        // BCrypt.checkpw compara la contraseña introducida contra el hash almacenado
        if (!BCrypt.checkpw(password, hashGuardado.get())) {
            log.warn("Login fallido: contraseña incorrecta para email={}", email);
            return Optional.empty();
        }

        Optional<User> user = userRepository.findByEmail(email);
        log.info("Login exitoso para email={}, rol={}", email,
                user.map(u -> u.role().toString()).orElse("desconocido"));
        return user;
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
        if (userRepository.existsByEmail(email)) {
            log.warn("Registro rechazado: email ya registrado={}", email);
            return false;
        }

        // Generamos el hash con BCrypt (10 rondas de sal por defecto)
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        Connection conn = null;
        try {
            conn = SqliteConnectionProvider.getConnection();
            conn.setAutoCommit(false); // Iniciamos la transacción manual

            // Paso 1: Insertar el registro en la tabla de usuarios
            int idUsuario = userRepository.insert(email, passwordHash, nombre, telefono, "paciente", conn);
            if (idUsuario <= 0) {
                log.error("Error insertando usuario en la transacción de registro, email={}", email);
                conn.rollback();
                return false;
            }

            // Paso 2: Insertar el perfil de paciente vinculado al usuario recién creado
            boolean pacienteInsertado = pacienteRepository.insert(idUsuario, nombre, telefono, email, conn);
            if (!pacienteInsertado) {
                log.error("Error insertando perfil de paciente, idUsuario={}", idUsuario);
                conn.rollback();
                return false;
            }

            // Todo correcto: confirmamos ambas inserciones como una sola unidad atómica
            conn.commit();
            log.info("Registro exitoso de nuevo paciente, email={}", email);
            return true;

        } catch (SQLException e) {
            log.error("Error registrando paciente email={}: {}", email, e.getMessage());
            // Revertimos cualquier cambio parcial realizado antes del error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    log.error("Error en rollback: {}", ex.getMessage());
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
                    log.error("Error cerrando conexión transaccional: {}", e.getMessage());
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
        if (!userRepository.existsByEmail(email)) {
            log.warn("Recuperación de contraseña rechazada: email no existe={}", email);
            return false;
        }

        String nuevoHash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt());
        boolean ok = userRepository.updatePassword(email, nuevoHash);
        if (ok) log.info("Contraseña actualizada para email={}", email);
        return ok;
    }
}
