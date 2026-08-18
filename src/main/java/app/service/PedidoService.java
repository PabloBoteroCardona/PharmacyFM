package app.service;

import app.repository.PedidoRepository;
import com.pharmacyfm.domain.model.Pedido;

import java.util.List;

public class PedidoService {

    private final PedidoRepository pedidoRepository = new PedidoRepository();

    public List<Pedido> getPedidosByPaciente(int idPaciente) {
        return pedidoRepository.findByPacienteId(idPaciente);
    }

    public List<Pedido> getAllPedidos() {
        return pedidoRepository.findAll();
    }

    public boolean crearPedidoFormulaCatalogo(int idPaciente, int idFormula,
                                               int cantidad, String unidad, String observaciones) {
        if (cantidad <= 0) {
            System.err.println("La cantidad debe ser mayor que 0.");
            return false;
        }
        return pedidoRepository.insert(idPaciente, idFormula, null, cantidad, unidad, observaciones);
    }

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

    public boolean actualizarEstado(int idPedido, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            System.err.println("El estado no puede estar vacío.");
            return false;
        }
        return pedidoRepository.updateEstado(idPedido, nuevoEstado);
    }
}
