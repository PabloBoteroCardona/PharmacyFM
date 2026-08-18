package com.pharmacyfm.domain.model;

/**
 * Modelo de dominio que representa una fórmula magistral del catálogo.
 *
 * Diseñada como clase inmutable (todos los campos son final y no hay setters).
 * Para "editar" una fórmula, la capa de UI crea una nueva instancia con los
 * valores actualizados y la pasa al servicio — nunca se muta el objeto en memoria.
 *
 * Se mantiene como clase (en lugar de record) para conservar los métodos
 * 'getNombre()', 'getPrecio()'… con prefijo 'get', requeridos por
 * PropertyValueFactory de JavaFX. En F4 se migrará a lambdas y podrá
 * convertirse a record.
 */
public final class Formula {

    private final int id;
    private final String nombre;
    private final String descripcion;
    private final double precio;

    /**
     * Constructor principal para fórmulas recuperadas de la base de datos.
     *
     * @param id          Identificador único autoincremental.
     * @param nombre      Nombre del preparado magistral.
     * @param descripcion Descripción y composición.
     * @param precio      Precio de venta al público.
     */
    public Formula(int id, String nombre, String descripcion, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    /**
     * Constructor de conveniencia para nuevas fórmulas aún sin persistir.
     * El id se fija a 0, que actúa como centinela de "nueva entidad".
     */
    public Formula(String nombre, String descripcion, double precio) {
        this(0, nombre, descripcion, precio);
    }

    public int getId()             { return id; }
    public String getNombre()      { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio()      { return precio; }

    /** Retorna true si la fórmula no ha sido persistida todavía (id == 0). */
    public boolean isNew() { return id == 0; }

    /**
     * Representación textual usada por los ComboBox de JavaFX para mostrar
     * el nombre de la fórmula en las listas desplegables.
     */
    @Override
    public String toString() {
        return nombre != null ? nombre : "(Sin nombre)";
    }
}
