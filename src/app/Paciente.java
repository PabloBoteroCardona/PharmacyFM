package app;

/**
 * Modelo de datos que representa a un Paciente dentro del sistema.
 * Esta entidad vincula la información personal del paciente con su cuenta de usuario
 * mediante una relación de clave foránea.
 */
public class Paciente {

    // Atributos privados para garantizar el encapsulamiento de los datos personales
    private int id;
    private int idUsuario; // Referencia al ID de la tabla usuarios (Foreign Key)
    private String nombre;
    private String telefono;
    private String email;

    /**
     * Constructor completo para la creación de objetos de tipo Paciente.
     * Utilizado principalmente al recuperar datos de la base de datos a través de los repositorios.
     * * @param id Identificador único del paciente.
     * @param idUsuario Identificador del usuario asociado en el sistema de autenticación.
     * @param nombre Nombre completo del paciente.
     * @param telefono Número de contacto.
     * @param email Dirección de correo electrónico.
     */
    public Paciente(int id, int idUsuario, String nombre, String telefono, String email) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }

    // -------- MÉTODOS DE ACCESO (GETTERS Y SETTERS) --------

    public int getId() {
        return id;
    }

    // El idUsuario normalmente no se modifica una vez asignado para mantener la integridad
    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}