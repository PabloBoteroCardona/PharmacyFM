package com.pharmacyfm.domain.model;

/**
 * Modelo de dominio que representa el perfil de un paciente.
 *
 * Modelado como Java record: todos los campos son inmutables.
 * Accesores generados: id(), idUsuario(), nombre(), telefono(), email()
 *
 * Los métodos with* devuelven una nueva instancia con el campo modificado,
 * preservando la inmutabilidad del modelo (patrón copy-and-update).
 */
public record Paciente(int id, int idUsuario, String nombre, String telefono, String email) {

    /**
     * Devuelve una copia del paciente con el nombre actualizado.
     *
     * @param nuevoNombre Nuevo nombre a asignar.
     * @return Nueva instancia de Paciente con el nombre cambiado.
     */
    public Paciente withNombre(String nuevoNombre) {
        return new Paciente(id, idUsuario, nuevoNombre, telefono, email);
    }

    /**
     * Devuelve una copia del paciente con el teléfono actualizado.
     *
     * @param nuevoTelefono Nuevo número de teléfono.
     * @return Nueva instancia de Paciente con el teléfono cambiado.
     */
    public Paciente withTelefono(String nuevoTelefono) {
        return new Paciente(id, idUsuario, nombre, nuevoTelefono, email);
    }

    /**
     * Devuelve una copia del paciente con el email actualizado.
     *
     * @param nuevoEmail Nueva dirección de correo electrónico.
     * @return Nueva instancia de Paciente con el email cambiado.
     */
    public Paciente withEmail(String nuevoEmail) {
        return new Paciente(id, idUsuario, nombre, telefono, nuevoEmail);
    }
}
