package app;

public class Formula {

    private int id;
    private String nombre;
    private String descripcion;
    private double precio;

    // Constructor con id (fórmulas ya guardadas en la base de datos)
    public Formula(int id, String nombre, String descripcion, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    // Constructor sin id (para crear nuevas)
    public Formula(String nombre, String descripcion, double precio) {
        this.id = 0;  // 0 indica "aún no guardado en BD"
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    // -------- GETTERS Y SETTERS --------

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

    // Muy importante: para mostrar en ComboBox
    @Override
    public String toString() {
        return nombre != null ? nombre : "(Sin nombre)";
    }
}
