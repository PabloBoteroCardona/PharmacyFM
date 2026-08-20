package com.pharmacyfm.domain.port;

import com.pharmacyfm.domain.model.Pedido;

import java.util.List;

/**
 * Puerto de salida (Output Port) para la persistencia de pedidos.
 *
 * Abstrae las operaciones de consulta y actualización sobre los pedidos
 * de fórmulas magistrales. Los dos métodos insert diferencian a nivel
 * semántico los dos tipos de pedido (catálogo vs. personalizado).
 */
public interface PedidoRepository {

    /**
     * Devuelve el historial de pedidos de un paciente concreto.
     *
     * @param idPaciente ID del paciente.
     * @return Lista de pedidos ordenada de más reciente a más antiguo; lista vacía si no hay ninguno.
     */
    List<Pedido> findByPacienteId(int idPaciente);

    /**
     * Devuelve todos los pedidos del sistema.
     *
     * @return Lista completa de pedidos; lista vacía si no hay ninguno.
     */
    List<Pedido> findAll();

    /**
     * Registra un nuevo pedido aceptando tanto fórmulas de catálogo como personalizadas.
     *
     * @param idPaciente           ID del paciente que realiza el pedido.
     * @param idFormula            ID de la fórmula del catálogo, o null para fórmulas personalizadas.
     * @param formulaPersonalizada Nombre de la fórmula si no está en el catálogo; null si es de catálogo.
     * @param cantidad             Número de unidades (debe ser > 0).
     * @param unidad               Tipo de unidad (Cápsulas, Gramos, Mililitros…).
     * @param observaciones        Notas adicionales del médico o paciente.
     * @return true si el pedido se registró correctamente.
     */
    boolean insert(int idPaciente, Integer idFormula, String formulaPersonalizada,
                   int cantidad, String unidad, String observaciones);

    /**
     * Actualiza el estado de un pedido existente.
     *
     * @param idPedido    ID del pedido a modificar.
     * @param nuevoEstado Label del nuevo estado (debe ser un valor válido de EstadoPedido).
     * @return true si el estado fue actualizado.
     */
    boolean updateEstado(int idPedido, String nuevoEstado);
}
