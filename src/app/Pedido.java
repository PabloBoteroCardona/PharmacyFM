package app;

/**
 * Modelo de datos que representa un Pedido de una fórmula magistral.
 * Esta clase consolida la información necesaria para ser visualizada en las tablas 
 * de administración y de usuario, uniendo datos del paciente y de la fórmula.
 */
public class Pedido {

    // Atributos privados para representar el estado y los detalles del pedido
    private int id;
    private int idPaciente;
    private String nombrePaciente;
    private String fecha;
    private String nombreFormula;
    private String estado;
    private int cantidad;
    private String unidad;
    private String observaciones;

    /**
     * Constructor completo para inicializar un Pedido con todos sus detalles.
     * Se utiliza para mapear los resultados de las consultas JOIN en la base de datos.
     */
    public Pedido(int id, int idPaciente, String nombrePaciente,
                  String fecha, String nombreFormula,
                  String estado, int cantidad, String unidad, String observaciones) {
        this.id = id;
        this.idPaciente = idPaciente;
        this.nombrePaciente = nombrePaciente;
        this.fecha = fecha;
        this.nombreFormula = nombreFormula;
        this.estado = estado;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.observaciones = observaciones;
    }

    // -------- MÉTODOS DE ACCESO (GETTERS) --------

    public int getId() { return id; }
    public int getIdPaciente() { return idPaciente; }
    public String getNombrePaciente() { return nombrePaciente; }
    public String getFecha() { return fecha; }
    public String getNombreFormula() { return nombreFormula; }
    public String getEstado() { return estado; }
    public int getCantidad() { return cantidad; }
    public String getUnidad() { return unidad; }
    public String getObservaciones() { return observaciones; }

    /**
     * Método de utilidad diseñado para la capa de presentación (UI).
     */
    public String getCantidadConUnidad() {
        return cantidad + " " + (unidad != null ? unidad : "");
    }

    // Permite la actualización del estado del pedido por parte del administrador
    public void setEstado(String estado) { this.estado = estado; }
}