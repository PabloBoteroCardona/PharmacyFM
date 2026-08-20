package com.pharmacyfm.domain.model;

/**
 * Enum que representa los posibles estados del ciclo de vida de un pedido.
 * Cada constante lleva asociado el texto visible que se muestra en la interfaz
 * y que se almacena en la base de datos, garantizando consistencia en ambos extremos.
 *
 * Sustituye los Strings literales dispersos en la UI y los repositorios
 * ("Pendiente", "En preparación"…) por un tipo fuertemente tipado.
 */
public enum EstadoPedido {

    PENDIENTE("Pendiente"),
    EN_PREPARACION("En preparación"),
    LISTO("Listo"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    /** Texto legible almacenado en la BD y mostrado en la interfaz de usuario. */
    private final String label;

    EstadoPedido(String label) {
        this.label = label;
    }

    /** Devuelve el texto visible del estado (ej. "En preparación"). */
    public String getLabel() {
        return label;
    }

    /**
     * Convierte un String de la base de datos en el Enum correspondiente.
     * La comparación es insensible a mayúsculas/minúsculas y elimina espacios extra.
     *
     * @param value Texto del estado leído de la columna 'estado' de la tabla pedidos.
     * @return El EstadoPedido correspondiente.
     * @throws IllegalArgumentException si el valor es nulo o no coincide con ningún estado.
     */
    public static EstadoPedido from(String value) {
        if (value == null) throw new IllegalArgumentException("El estado no puede ser nulo");
        for (EstadoPedido e : values()) {
            if (e.label.equalsIgnoreCase(value.trim())) return e;
        }
        throw new IllegalArgumentException("Estado desconocido: " + value);
    }

    /**
     * Permite usar el enum directamente en ComboBox y TableView de JavaFX,
     * mostrando el label legible en lugar del nombre de la constante.
     */
    @Override
    public String toString() {
        return label;
    }
}
