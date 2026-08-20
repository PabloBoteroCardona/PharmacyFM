package app.service;

import com.pharmacyfm.domain.model.Paciente;
import com.pharmacyfm.domain.port.PacienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de perfiles de paciente.
 *
 * Centraliza la lógica de negocio relacionada con los pacientes:
 * consultas, validaciones y actualizaciones de datos de contacto.
 *
 * AdminWindow y UserWindow deben usar este servicio en lugar de
 * acceder directamente al repositorio, respetando la regla de
 * dependencia de Clean Architecture: UI → Service → Port.
 */
public class PacienteService {

    private static final Logger log = LoggerFactory.getLogger(PacienteService.class);

    /** Puerto de acceso a datos de pacientes, inyectado por constructor. */
    private final PacienteRepository pacienteRepository;

    /**
     * Constructor principal: recibe el repositorio inyectado desde AppContext.
     * No hay constructor por defecto; la dependencia siempre llega desde fuera.
     *
     * @param pacienteRepository Implementación del puerto de pacientes.
     */
    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Devuelve la lista completa de pacientes, ordenada alfabéticamente.
     * Usado por el panel de administración para gestionar el directorio de pacientes.
     *
     * @return Lista de todos los pacientes; lista vacía si no hay ninguno.
     */
    public List<Paciente> getTodosPacientes() {
        log.debug("Cargando lista completa de pacientes");
        return pacienteRepository.findAll();
    }

    /**
     * Busca el perfil de paciente vinculado a un ID de usuario.
     * Devuelve Optional.empty() si el usuario no tiene perfil de paciente,
     * evitando que el llamante trabaje con referencias nulas.
     *
     * @param idUsuario ID del usuario autenticado.
     * @return Optional con el Paciente si existe; Optional.empty() si no.
     */
    public Optional<Paciente> getPacientePorUsuario(int idUsuario) {
        log.debug("Buscando perfil de paciente para idUsuario={}", idUsuario);
        Paciente p = pacienteRepository.findByUserId(idUsuario);
        return Optional.ofNullable(p);
    }

    /**
     * Actualiza los datos de contacto de un paciente existente tras validarlos.
     *
     * Validaciones:
     *   - El nombre no puede estar vacío.
     *   - El ID del paciente debe ser mayor que 0.
     *
     * @param p Paciente con los datos actualizados (creado mediante los métodos with* del modelo).
     * @return true si la actualización fue exitosa.
     */
    public boolean actualizarPaciente(Paciente p) {
        if (p.id() <= 0) {
            log.warn("Intento de actualizar paciente con ID inválido: {}", p.id());
            return false;
        }
        if (p.nombre() == null || p.nombre().trim().isEmpty()) {
            log.warn("Intento de actualizar paciente con nombre vacío, id={}", p.id());
            return false;
        }

        boolean ok = pacienteRepository.update(p);
        if (ok) {
            log.info("Paciente actualizado correctamente, id={}", p.id());
        } else {
            log.warn("No se encontró el paciente a actualizar, id={}", p.id());
        }
        return ok;
    }
}
