package com.pharmacyfm.domain.port;

import com.pharmacyfm.domain.model.Paciente;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Puerto de salida (Output Port) para la persistencia de pacientes.
 *
 * El insert participa en la transacción de registro de AuthService,
 * por eso recibe la Connection externamente en lugar de abrir la suya.
 */
public interface PacienteRepository {

    /**
     * Busca el perfil de paciente vinculado a un usuario por su ID de usuario.
     *
     * @param idUsuario ID del usuario en la tabla 'usuarios'.
     * @return Paciente asociado, o null si no existe.
     */
    Paciente findByUserId(int idUsuario);

    /**
     * Recupera todos los pacientes registrados en el sistema.
     *
     * @return Lista de pacientes ordenada alfabéticamente; lista vacía si no hay ninguno.
     */
    List<Paciente> findAll();

    /**
     * Inserta un nuevo paciente dentro de una transacción externa.
     *
     * @param idUsuario ID del usuario recién creado.
     * @param nombre    Nombre completo del paciente.
     * @param telefono  Teléfono de contacto.
     * @param email     Email del paciente.
     * @param conn      Conexión transaccional gestionada por AuthService.
     * @return true si la inserción fue exitosa.
     * @throws SQLException Si falla el acceso a la base de datos.
     */
    boolean insert(int idUsuario, String nombre, String telefono,
                   String email, Connection conn) throws SQLException;

    /**
     * Actualiza los datos de contacto de un paciente existente.
     *
     * @param p Paciente con los datos actualizados (id > 0).
     * @return true si la fila fue modificada.
     */
    boolean update(Paciente p);
}
