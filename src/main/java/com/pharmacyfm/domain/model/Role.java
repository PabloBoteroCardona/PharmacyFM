package com.pharmacyfm.domain.model;

/**
 * Enum que representa los roles disponibles dentro de la aplicación.
 * Sustituye el uso de Strings mágicos ("admin", "paciente") por tipos seguros
 * que el compilador puede verificar, eliminando errores de typo en tiempo de ejecución.
 */
public enum Role {

    ADMIN,
    PACIENTE;

    /**
     * Convierte un String proveniente de la base de datos en el Enum correspondiente.
     * Lanza una excepción tipada si el valor es nulo o no reconocido, en lugar de
     * retornar null silenciosamente.
     *
     * @param value Texto del rol almacenado en la columna 'rol' de la tabla usuarios.
     * @return El Role correspondiente al valor recibido.
     * @throws IllegalArgumentException si el valor es nulo o desconocido.
     */
    public static Role from(String value) {
        if (value == null) throw new IllegalArgumentException("El rol no puede ser nulo");
        return switch (value.toLowerCase().trim()) {
            case "admin"    -> ADMIN;
            case "paciente" -> PACIENTE;
            default -> throw new IllegalArgumentException("Rol desconocido: " + value);
        };
    }
}
