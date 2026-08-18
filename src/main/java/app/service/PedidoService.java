package app.service;

import app.repository.PedidoRepository;
import com.pharmacyfm.domain.model.Pedido;

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
 */
public class PedidoService {

    // Acceso a datos de pedidos; se instancia aquí hasta que F3 aplique inyección de constructor
    private final PedidoRepository pedidoRepository = new PedidoRepository();

    /**
     * Devuelve el historial de pedidos de un paciente concreto,
     * ordenado del más reciente al más antiguo.
     *
     * @param idPaciente ID del paciente en la tabla 'pacientes'.
     * @return Lista de pedidos del paciente; lista vacía si no tiene ninguno.
     */
    public List<Pedido> getPedidosByPaciente(int idPaciente) {
        return pedidoRepository.findByPacienteId(idPaciente);
    }

    /**
     * Devuelve todos los pedidos del sistema para el panel de administración.
     *
     * @return Lista con todos los pedidos registrados; lista vacía si no hay ninguno.
     */
    public List<Pedido> getAllPedidos() {
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
            System.err.println("La cantidad debe ser mayor que 0.");
            return false;
        }
        // idFormula se pasa como Integer; null indica fórmula personalizada — aquí nunca es null
        return pedidoRepository.insert(idPaciente, idFormula, null, cantidad, unidad, observaciones);
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
            System.err.println("La cantidad debe ser mayor que 0.");
            return false;
        }

        // El nombre de la fórmula es obligatorio cuando no se selecciona del catálogo
        if (nombreFormula == null || nombreFormula.trim().isEmpty()) {
            System.err.println("El nombre de la fórmula personalizada no puede estar vacío.");
            return false;
        }

        // idFormula es null porque la fórmula no existe en el catálogo
        return pedidoRepository.insert(idPaciente, null, nombreFormula, cantidad, unidad, observaciones);
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
            System.err.println("El estado no puede estar vacío.");
            return false;
        }
        return pedidoRepository.updateEstado(idPedido, nuevoEstado);
    }
}
