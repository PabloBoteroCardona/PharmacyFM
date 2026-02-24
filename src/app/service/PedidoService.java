package app.service;

import app.Pedido;
import app.repository.PedidoRepository;

import java.util.List;

/**
 * Servicio de pedidos.
 * Se encarga de la lógica de negocio para gestionar las solicitudes de fórmulas,
 * actuando como intermediario entre la interfaz y la base de datos.
 */
public class PedidoService {

    private final PedidoRepository pedidoRepository = new PedidoRepository();

    /**
     * Recupera el historial de pedidos de un paciente específico para su área personal.
     */
    public List<Pedido> getPedidosByPaciente(int idPaciente) {
        return pedidoRepository.findByPacienteId(idPaciente);
    }

    /**
     * Recupera todos los pedidos de la farmacia. 
     * Este método lo utiliza el perfil administrador para la gestión global.
     */
    public List<Pedido> getAllPedidos() {
        return pedidoRepository.findAll();
    }

    /**
     * Crea un pedido utilizando una fórmula que ya existe en el catálogo de la farmacia.
     * @return true si el pedido se registró correctamente.
     */
    public boolean crearPedidoFormulaCatalogo(int idPaciente, int idFormula,
                                               int cantidad, String unidad, String observaciones) {
        // Validación básica: no permitimos pedidos sin cantidad
        if (cantidad <= 0) {
            System.err.println("La cantidad debe ser mayor que 0.");
            return false;
        }

        // Enviamos al repositorio el ID de la fórmula y dejamos el nombre personalizado en null
        return pedidoRepository.insert(idPaciente, idFormula, null, cantidad, unidad, observaciones);
    }

    /**
     * Crea un pedido para una fórmula que no está en el catálogo (personalizada).
     * @return true si la solicitud se guardó correctamente.
     */
    public boolean crearPedidoFormulaPersonalizada(int idPaciente, String nombreFormula,
                                                     int cantidad, String unidad, String observaciones) {
        // Validamos que haya una cantidad válida
        if (cantidad <= 0) {
            System.err.println("La cantidad debe ser mayor que 0.");
            return false;
        }

        // Validamos que el paciente haya descrito qué fórmula necesita
        if (nombreFormula == null || nombreFormula.trim().isEmpty()) {
            System.err.println("El nombre de la fórmula personalizada no puede estar vacío.");
            return false;
        }

        // Enviamos al repositorio el nombre de la fórmula y dejamos el ID del catálogo en null
        return pedidoRepository.insert(idPaciente, null, nombreFormula, cantidad, unidad, observaciones);
    }

    /**
     * Modifica el estado de un pedido existente (ej. de 'Pendiente' a 'En preparación').
     */
    public boolean actualizarEstado(int idPedido, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            System.err.println("El estado no puede estar vacío.");
            return false;
        }

        return pedidoRepository.updateEstado(idPedido, nuevoEstado);
    }
}