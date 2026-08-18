package app;

/**
 * Modelo de datos que representa un Usuario autenticado en el sistema.
 * Esta clase se utiliza para gestionar la sesión activa y determinar los
 * permisos de acceso según el rol asignado (Admin o Paciente).
 */
public class User {

    // Atributos privados para representar la identidad y el perfil del usuario
    private int id;
    private String email;
    private String nombre;
    private String telefono;
    private String rol;

    /**
     * Constructor completo para inicializar un Usuario tras una autenticación exitosa.
     * @param id Identificador único en la base de datos.
     * @param email Correo electrónico utilizado como credencial de acceso.
     * @param nombre Nombre completo del usuario para personalización de la interfaz.
     * @param telefono Número de contacto registrado.
     * @param rol Define los privilegios dentro de la aplicación (ej: 'admin', 'paciente').
     */
    public User(int id, String email, String nombre, String telefono, String rol) {
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.telefono = telefono;
        this.rol = rol;
    }

    // -------- MÉTODOS DE ACCESO (GETTERS) --------
    // Nota: No se incluyen Setters para asegurar que los datos de sesión 
    // permanezcan inmutables durante el ciclo de vida del objeto en memoria.

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getRol() {
        return rol;
    }
}