package com.pharmacyfm.domain.model;

/**
 * Modelo de dominio que representa un pedido de fórmula magistral.
 *
 * Diseñado como clase inmutable: todos los campos son final.
 * El estado se gestiona mediante el enum EstadoPedido, garantizando
 * que solo puedan existir estados válidos y conocidos.
 *
 * Proporciona dos accesores para el estado:
 *   - getEstadoPedido(): devuelve el enum tipado (uso interno y en servicios).
 *   - getEstado(): devuelve el label como String (compatibilidad con PropertyValueFactory).
 */
public final class Pedido {

    private final int id;
    private final int idPaciente;
    private final String nombrePaciente;  // Desnormalizado para evitar JOINs en la vista
    private final String fecha;
    private final String nombreFormula;   // Puede ser del catálogo o personalizada
    private final EstadoPedido estado;
    private final int cantidad;
    private final String unidad;
    private final String observaciones;

    /**
     * Constructor principal, utilizado por el repositorio al mapear
     * los resultados de las consultas JOIN sobre pedidos, pacientes y fórmulas.
     */
    public Pedido(int id, int idPaciente, String nombrePaciente, String fecha,
                  String nombreFormula, EstadoPedido estado, int cantidad,
                  String unidad, String observaciones) {
        this.id = id;
        this.idPaciente = idPaciente;
        this.nombrePaciente = nombrePaciente;
        this.fecha = fecha;
        this.nombreFormula = nombreFormula;
        this.estado = estado;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.observaciones = observaciones;
    }

    public int getId()                    { return id; }
    public int getIdPaciente()            { return idPaciente; }
    public String getNombrePaciente()     { return nombrePaciente; }
    public String getFecha()              { return fecha; }
    public String getNombreFormula()      { return nombreFormula; }

    /** Devuelve el enum tipado — para uso en lógica de negocio y servicios. */
    public EstadoPedido getEstadoPedido() { return estado; }

    /** Devuelve el label legible — requerido por PropertyValueFactory de JavaFX. */
    public String getEstado()             { return estado.getLabel(); }

    public int getCantidad()              { return cantidad; }
    public String getUnidad()             { return unidad; }
    public String getObservaciones()      { return observaciones; }

    /**
     * Cadena formateada "cantidad unidad" para mostrar en columnas de la tabla.
     * Ejemplo: "2 Cápsulas", "1 Gramos".
     */
    public String getCantidadConUnidad() {
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
