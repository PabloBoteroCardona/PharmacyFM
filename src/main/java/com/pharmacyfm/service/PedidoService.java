package com.pharmacyfm.service;

import com.pharmacyfm.domain.port.PedidoRepository;
import com.pharmacyfm.domain.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Servicio de gestión de pedidos de fórmulas magistrales.
 *
 * Encapsula la lógica de negocio de los pedidos:
 *   - Separación entre pedidos de catálogo y pedidos personalizados.
 *   - Validaciones de cantidad y nombre de fórmula antes de persistir.
 *   - Delegación de cambios de estado al repositorio.
 *
 * Oculta la firma del repositorio (que acepta Integer idFormula nullable)
 * exponiendo dos métodos semánticamente distintos para cada tipo de pedido.
 *
 * Todas las dependencias se inyectan por constructor desde AppContext.
 */
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    /** Puerto de acceso a datos de pedidos, inyectado por constructor. */
    private final PedidoRepository pedidoRepository;

    /**
     * Constructor principal: recibe el repositorio inyectado desde AppContext.
     *
     * @param pedidoRepository Implementación del puerto de pedidos.
     */
    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Devuelve el historial de pedidos de un paciente concreto,
     * ordenado del más reciente al más antiguo.
     *
     * @param idPaciente ID del paciente en la tabla 'pacientes'.
     * @return Lista de pedidos del paciente; lista vacía si no tiene ninguno.
     */
    public List<Pedido> getPedidosByPaciente(int idPaciente) {
        log.debug("Cargando pedidos de idPaciente={}", idPaciente);
        return pedidoRepository.findByPacienteId(idPaciente);
    }

    /**
     * Devuelve todos los pedidos del sistema para el panel de administración.
     *
     * @return Lista con todos los pedidos registrados; lista vacía si no hay ninguno.
     */
    public List<Pedido> getAllPedidos() {
        log.debug("Cargando todos los pedidos del sistema");
        return pedidoRepository.findAll();
    }

    /**
     * Crea un pedido para una fórmula que existe en el catálogo.
     *
     * @param idPaciente    ID del paciente que realiza el pedido.
     * @param idFormula     ID de la fórmula seleccionada del catálogo (debe ser > 0).
     * @param cantidad      Número de unidades solicitadas (debe ser > 0).
     * @param unidad        Tipo de unidad (Cápsulas, Gramos, Mililitros…).
     * @param observaciones Instrucciones adicionales del médico o del paciente.
     * @return true si el pedido se registró correctamente.
     */
    public boolean crearPedidoFormulaCatalogo(int idPaciente, int idFormula,
                                              int cantidad, String unidad, String observaciones) {
        // La cantidad negativa o cero no tiene sentido como solicitud de fármaco
        if (cantidad <= 0) {
            log.warn("Pedido rechazado: cantidad inválida={}", cantidad);
            return false;
        }
        boolean ok = pedidoRepository.insert(idPaciente, idFormula, null, cantidad, unidad, observaciones);
        if (ok) log.info("Pedido creado (catálogo): idPaciente={}, idFormula={}", idPaciente, idFormula);
        else    log.error("Error creando pedido de catálogo: idPaciente={}", idPaciente);
        return ok;
    }

    /**
     * Crea un pedido para una fórmula que no está en el catálogo (fórmula personalizada).
     *
     * @param idPaciente    ID del paciente que realiza el pedido.
     * @param nombreFormula Nombre descriptivo de la fórmula personalizada.
     * @param cantidad      Número de unidades solicitadas (debe ser > 0).
     * @param unidad        Tipo de unidad (Cápsulas, Gramos, Mililitros…).
     * @param observaciones Instrucciones adicionales del médico o del paciente.
     * @return true si el pedido se registró correctamente.
     */
    public boolean crearPedidoFormulaPersonalizada(int idPaciente, String nombreFormula,
                                                   int cantidad, String unidad, String observaciones) {
        if (cantidad <= 0) {
            log.warn("Pedido personalizado rechazado: cantidad inválida={}", cantidad);
            return false;
        }

        // El nombre de la fórmula es obligatorio cuando no se selecciona del catálogo
        if (nombreFormula == null || nombreFormula.trim().isEmpty()) {
            log.warn("Pedido personalizado rechazado: nombre de fórmula vacío");
            return false;
        }

        // idFormula es null porque la fórmula no existe en el catálogo
        boolean ok = pedidoRepository.insert(idPaciente, null, nombreFormula, cantidad, unidad, observaciones);
        if (ok) log.info("Pedido creado (personalizado): idPaciente={}, nombre='{}'", idPaciente, nombreFormula);
        else    log.error("Error creando pedido personalizado: idPaciente={}", idPaciente);
        return ok;
    }

    /**
     * Actualiza el estado de un pedido existente (ej. de PENDIENTE a EN_PREPARACION).
     * El label del estado debe ser un valor reconocido por EstadoPedido.from().
     *
     * @param idPedido    ID del pedido a actualizar.
     * @param nuevoEstado Label del nuevo estado (ej. "Listo", "Entregado").
     * @return true si el estado se actualizó correctamente.
     */
    public boolean actualizarEstado(int idPedido, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            log.warn("Actualización de estado rechazada: estado vacío para idPedido={}", idPedido);
            return false;
        }
        boolean ok = pedidoRepository.updateEstado(idPedido, nuevoEstado);
        if (ok) log.info("Estado actualizado a '{}' para idPedido={}", nuevoEstado, idPedido);
        else    log.warn("No se encontró el pedido a actualizar, idPedido={}", idPedido);
        return ok;
    }
}
