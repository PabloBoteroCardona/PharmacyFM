package com.pharmacyfm.domain.port;

import com.pharmacyfm.domain.model.User;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Puerto de salida (Output Port) para la persistencia de usuarios.
 *
 * Expone solo las operaciones que la lógica de negocio (AuthService)
 * necesita realmente. Oculta los detalles de tabla SQL y tipos JDBC.
 *
 * El método insert recibe una Connection externa para participar en
 * la transacción de registro (usuario + paciente) controlada por AuthService.
 */
public interface UserRepository {

    /**
     * Busca y devuelve el usuario completo asociado a un email.
     *
     * @param email Email del usuario a buscar.
     * @return Objeto User si existe; null si no se encuentra.
     */
    User findByEmail(String email);

    /**
     * Obtiene el hash BCrypt de la contraseña almacenada para un email.
     * Se usa en la validación de login sin exponer el objeto User antes
     * de verificar credenciales.
     *
     * @param email Email del usuario.
     * @return Hash BCrypt, o null si el email no existe.
     */
    String getPasswordHashByEmail(String email);

    /**
     * Comprueba si un email ya está registrado.
     *
     * @param email Email a verificar.
     * @return true si el email existe en la base de datos.
     */
    boolean existsByEmail(String email);

    /**
     * Inserta un nuevo usuario dentro de una transacción externa.
     * La conexión es gestionada por el llamante (AuthService); este método
     * no la abre ni la cierra.
     *
     * @param email        Email único del nuevo usuario.
     * @param passwordHash Hash BCrypt de su contraseña.
     * @param nombre       Nombre completo.
     * @param telefono     Teléfono de contacto.
     * @param rol          Texto del rol: "admin" o "paciente".
     * @param conn         Conexión transaccional proporcionada por AuthService.
     * @return ID autogenerado por la BD, o -1 si hubo error.
     * @throws SQLException Si falla el acceso a la base de datos.
     */
    int insert(String email, String passwordHash, String nombre,
               String telefono, String rol, Connection conn) throws SQLException;

    /**
     * Actualiza el hash de contraseña de un usuario existente.
     *
     * @param email          Email del usuario.
     * @param passwordHash   Nuevo hash BCrypt.
     * @return true si la contraseña fue actualizada.
     */
    boolean updatePassword(String email, String passwordHash);
}
