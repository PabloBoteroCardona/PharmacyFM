package com.pharmacyfm.domain.model;

/**
 * Modelo de dominio que representa un pedido de fórmula magistral.
 *
 * Modelado como Java record: todos los campos son inmutables.
 * Accesores generados: id(), idPaciente(), nombrePaciente(), fecha(),
 *   nombreFormula(), estado(), cantidad(), unidad(), observaciones()
 *
 * El campo 'estado' es del tipo enum EstadoPedido, garantizando que
 * solo puedan existir valores válidos y conocidos por el dominio.
 *
 * La UI obtiene el texto legible del estado con: pedido.estado().getLabel()
 */
public record Pedido(
        int id,
        int idPaciente,
        String nombrePaciente,
        String fecha,
        String nombreFormula,
        EstadoPedido estado,
        int cantidad,
        String unidad,
        String observaciones
) {

    /**
     * Devuelve la cantidad y la unidad formateadas en una sola cadena.
     * Ejemplo: "2 Cápsulas", "1 Gramos".
     * Se usa en las columnas de tabla de la UI.
     *
     * @return Cadena "cantidad unidad" para mostrar en la vista.
     */
    public String cantidadConUnidad() {
        return cantidad + " " + (unidad != null ? unidad : "");
    }

    /**
     * Devuelve una nueva instancia de Pedido con el estado actualizado.
     * Preserva todos los demás campos intactos.
     *
     * @param nuevoEstado El nuevo estado del pedido.
     * @return Un Pedido idéntico al original pero con el estado cambiado.
     */
    public Pedido withEstado(EstadoPedido nuevoEstado) {
        return new Pedido(id, idPaciente, nombrePaciente, fecha, nombreFormula,
                          nuevoEstado, cantidad, unidad, observaciones);
    }
}
