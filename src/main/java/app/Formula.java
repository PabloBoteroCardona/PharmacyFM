package app;

/**
 * Modelo de datos que representa una Fórmula Magistral.
 * Esta clase se utiliza para transportar los datos entre la base de datos y la interfaz de usuario.
 */
public class Formula {

    // Atributos privados para cumplir con el principio de encapsulamiento
    private int id;
    private String nombre;
    private String descripcion;
    private double precio;

    /**
     * Constructor completo para objetos recuperados de la base de datos.
     * @param id Identificador único autoincremental.
     */
    public Formula(int id, String nombre, String descripcion, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    /**
     * Constructor para nuevas instancias que aún no han sido persistidas.
     * Útil para capturar datos desde formularios de creación.
     */
    public Formula(String nombre, String descripcion, double precio) {
        this.id = 0;  // El valor 0 se usa como flag para indicar que es un registro nuevo
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    // -------- MÉTODOS DE ACCESO (GETTERS Y SETTERS) --------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    /**
     * Sobrescritura del método toString para facilitar la integración con 
     * componentes de JavaFX como ComboBox y ListView.
     */
    @Override
    public String toString() {
        return nombre != null ? nombre : "(Sin nombre)";
    }
}