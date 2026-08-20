package com.pharmacyfm.domain.model;

/**
 * Modelo de dominio que representa una fórmula magistral del catálogo.
 *
 * Modelada como Java record: todos los campos son inmutables y los accesores
 * siguen la convención de records (sin prefijo "get"): id(), nombre(), etc.
 *
 * Constructor canónico: Formula(int id, String nombre, String descripcion, double precio)
 * Constructor de conveniencia: Formula(String nombre, String descripcion, double precio)
 *   → crea una fórmula nueva con id = 0 (todavía no persistida).
 *
 * toString() devuelve el nombre para que el ComboBox de selección lo muestre correctamente.
 */
public record Formula(int id, String nombre, String descripcion, double precio) {

    /**
     * Constructor de conveniencia para fórmulas nuevas antes de ser persistidas.
     * El ID se fija a 0; la BD asigna el ID definitivo al insertarse.
     *
     * @param nombre      Nombre de la fórmula magistral.
     * @param descripcion Descripción de sus componentes o uso.
     * @param precio      Precio de venta (0 o positivo).
     */
    public Formula(String nombre, String descripcion, double precio) {
        this(0, nombre, descripcion, precio);
    }

    /**
     * Indica si la fórmula aún no ha sido persistida en la base de datos.
     * Una fórmula es "nueva" cuando su ID es 0, es decir, cuando la BD
     * todavía no le ha asignado un identificador.
     *
     * @return true si id == 0 (no persistida), false si id > 0 (existe en BD).
     */
    public boolean isNew() {
        return id == 0;
    }

    /**
     * Devuelve el nombre de la fórmula como representación textual.
     * JavaFX usa toString() para mostrar los elementos en un ComboBox.
     */
    @Override
    public String toString() {
        return nombre != null ? nombre : "(Sin nombre)";
    }
}
