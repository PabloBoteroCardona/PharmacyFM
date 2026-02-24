package app;

public class User {

    private int id;
    private String email;
    private String nombre;
    private String telefono;
    private String rol;

    public User(int id, String email, String nombre, String telefono, String rol) {
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.telefono = telefono;
        this.rol = rol;
    }

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
