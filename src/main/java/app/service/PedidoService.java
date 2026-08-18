package app.service;

import com.pharmacyfm.domain.port.PedidoRepository;
import com.pharmacyfm.domain.model.Pedido;
import com.pharmacyfm.infrastructure.persistence.JdbcPedidoRepository;

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
 * Depende del puerto domain.port.PedidoRepository; en F3 la implementación
 * concreta se inyectará desde fuera en lugar de instanciarse aquí.
 */
public class PedidoService {

    // Tipado como puerto de dominio — el servicio no conoce la tecnología subyacente
    private final PedidoRepository pedidoRepository;

    /**
     * Constructor por defecto: cablea la implementación JDBC concreta.
     * En F3 se sustituirá por inyección de dependencias desde el llamante.
     */
    public PedidoService() {
        this.pedidoRepository = new JdbcPedidoRepository();
    }

    /**
     * Constructor para tests: permite inyectar un doble de test (mock/stub).
     *
     * @param pedidoRepository Implementación alternativa del puerto.
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
            System.err.println("[PedidoService] La cantidad debe ser mayor que 0.");
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
            System.err.println("[PedidoService] La cantidad debe ser mayor que 0.");
            return false;
        }

        // El nombre de la fórmula es obligatorio cuando no se selecciona del catálogo
        if (nombreFormula == null || nombreFormula.trim().isEmpty()) {
            System.err.println("[PedidoService] El nombre de la fórmula personalizada no puede estar vacío.");
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
            System.err.println("[PedidoService] El estado no puede estar vacío.");
            return false;
        }
        return pedidoRepository.updateEstado(idPedido, nuevoEstado);
    }
}
