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
 * Centraliza la lógica de negocio para el control de acceso,
 * incluyendo validación de credenciales, registro y seguridad de contraseñas.
 */
public class AuthService {

    // Dependencias necesarias para interactuar con las tablas de usuarios y pacientes
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final PacienteRepository pacienteRepository = new PacienteRepository();

    /**
     * Proceso de inicio de sesión.
     * Compara la contraseña introducida con el hash de la base de datos de forma segura.
     * @return El objeto User si la autenticación es válida, null en caso contrario.
     */
    public User login(String email, String password) {
        // Obtenemos la contraseña encriptada almacenada para este email
        String hashGuardado = usuarioRepository.getPasswordHashByEmail(email);

        if (hashGuardado == null) {
            return null; // El usuario no existe
        }

        // Verificamos si la contraseña coincide usando la librería BCrypt
        if (!BCrypt.checkpw(password, hashGuardado)) {
            return null; // Contraseña incorrecta
        }

        // Si todo está bien, devolvemos el perfil completo del usuario
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Gestiona el registro de nuevos pacientes en el sistema.
     * Utiliza un sistema de transacciones para asegurar que los datos se guarden 
     * correctamente en las dos tablas relacionadas (usuarios y pacientes).
     */
    public boolean registrarPaciente(String nombre, String email, String password, String telefono) {
        // Validación previa para evitar correos duplicados
        if (usuarioRepository.existsByEmail(email)) {
            return false;
        }

        // Encriptamos la contraseña antes de guardarla por seguridad
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            // Iniciamos una transacción manual (desactivamos el guardado automático)
            // Esto garantiza que, si falla un paso, no se guarde nada a medias.
            conn.setAutoCommit(false);

            // 1. Insertamos el usuario con el rol de 'paciente'
            int idUsuario = usuarioRepository.insert(email, passwordHash, nombre, telefono, "paciente", conn);

            if (idUsuario <= 0) {
                conn.rollback(); // Error en el primer paso: deshacemos cambios
                return false;
            }

            // 2. Insertamos la ficha de paciente vinculada al usuario creado
            boolean pacienteInsertado = pacienteRepository.insert(idUsuario, nombre, telefono, email, conn);

            if (!pacienteInsertado) {
                conn.rollback(); // Error en el segundo paso: deshacemos cambios para no dejar datos huérfanos
                return false;
            }

            // Si ambos pasos han tenido éxito, confirmamos definitivamente los cambios
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error registrando paciente:");
            e.printStackTrace();
            // En caso de excepción, intentamos deshacer cualquier cambio pendiente
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            // Limpieza de recursos y restauración del estado de la conexión
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
     * Proceso de recuperación o cambio de contraseña.
     * @param email Identificador del usuario.
     * @param nuevaPassword Texto plano de la nueva clave que será encriptada.
     */
    public boolean recuperarPassword(String email, String nuevaPassword) {
        // Verificamos que el usuario realmente existe en el sistema
        if (!usuarioRepository.existsByEmail(email)) {
            return false;
        }

        // Generamos un nuevo hash de seguridad para la nueva contraseña
        String nuevoHash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt());

        // Actualizamos el registro en la base de datos
        return usuarioRepository.updatePassword(email, nuevoHash);
    }
}
