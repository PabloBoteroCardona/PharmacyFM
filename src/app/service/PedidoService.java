package app.service;

import app.Pedido;
import app.repository.PedidoRepository;

import java.util.List;

/**
 * Servicio de pedidos.
 * Gestiona la lógica de negocio relacionada con los pedidos de fórmulas.
 */
public class PedidoService {

    private final PedidoRepository pedidoRepository = new PedidoRepository();

    /**
     * Devuelve todos los pedidos de un paciente concreto.
     */
    public List<Pedido> getPedidosByPaciente(int idPaciente) {
        return pedidoRepository.findByPacienteId(idPaciente);
    }

    /**
     * Devuelve todos los pedidos de la farmacia.
     * Para uso exclusivo del administrador.
     */
    public List<Pedido> getAllPedidos() {
        return pedidoRepository.findAll();
    }

    /**
     * Crea un nuevo pedido de fórmula existente del catálogo.
     */
    public boolean crearPedidoFormulaCatalogo(int idPaciente, int idFormula,
                                               int cantidad, String unidad, String observaciones) {
        if (cantidad <= 0) {
            System.err.println("La cantidad debe ser mayor que 0.");
            return false;
        }

        return pedidoRepository.insert(idPaciente, idFormula, null, cantidad, unidad, observaciones);
    }

    /**
     * Crea un nuevo pedido de fórmula personalizada.
     */
    public boolean crearPedidoFormulaPersonalizada(int idPaciente, String nombreFormula,
                                                    int cantidad, String unidad, String observaciones) {
        if (cantidad <= 0) {
            System.err.println("La cantidad debe ser mayor que 0.");
            return false;
        }

        if (nombreFormula == null || nombreFormula.trim().isEmpty()) {
            System.err.println("El nombre de la fórmula personalizada no puede estar vacío.");
            return false;
        }

        return pedidoRepository.insert(idPaciente, null, nombreFormula, cantidad, unidad, observaciones);
    }

    /**
     * Actualiza el estado de un pedido.
     */
    public boolean actualizarEstado(int idPedido, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            System.err.println("El estado no puede estar vacío.");
            return false;
        }

        return pedidoRepository.updateEstado(idPedido, nuevoEstado);
    }
}