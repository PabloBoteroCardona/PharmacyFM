package com.pharmacyfm.domain.model;

/**
 * Modelo de dominio que representa la ficha de un paciente registrado.
 *
 * Diseñada como clase inmutable: todos los campos son final y no hay setters.
 * Las actualizaciones de datos personales se realizan mediante métodos 'with*'
 * que devuelven una nueva instancia con el campo modificado, manteniendo el
 * objeto original intacto hasta que el cambio se confirme en la base de datos.
 *
 * Se mantiene como clase (en lugar de record) para mantener compatibilidad
 * con PropertyValueFactory de JavaFX en la tabla de pacientes.
 */
public final class Paciente {

    private final int id;
    private final int idUsuario;   // Clave foránea hacia la tabla 'usuarios'
    private final String nombre;
    private final String telefono;
    private final String email;

    /**
     * Constructor completo, utilizado principalmente por el repositorio
     * al mapear filas de la base de datos.
     */
    public Paciente(int id, int idUsuario, String nombre, String telefono, String email) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }

    public int getId()          { return id; }
    public int getIdUsuario()   { return idUsuario; }
    public String getNombre()   { return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail()    { return email; }

    // ---- Métodos 'with' para actualizaciones inmutables ----
    // En lugar de mutar el objeto, devuelven una copia con el campo cambiado.
    // El objeto original permanece válido hasta que la operación de BD confirme el cambio.

    public Paciente withNombre(String nombre) {
        return new Paciente(id, idUsuario, nombre, telefono, email);
    }

    public Paciente withTelefono(String telefono) {
        return new Paciente(id, idUsuario, nombre, telefono, email);
    }

    public Paciente withEmail(String email) {
        return new Paciente(id, idUsuario, nombre, telefono, email);
    }
}
