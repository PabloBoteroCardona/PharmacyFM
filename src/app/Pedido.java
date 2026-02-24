package app;

public class Pedido {

    private int id;
    private int idPaciente;
    private String nombrePaciente;
    private String fecha;
    private String nombreFormula;
    private String estado;
    private int cantidad;
    private String unidad;
    private String observaciones;

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
     * Devuelve cantidad y unidad juntas para mostrar en la tabla.
     * Ejemplo: "180 Cápsulas"
     */
    public String getCantidadConUnidad() {
        return cantidad + " " + (unidad != null ? unidad : "");
    }

    public void setEstado(String estado) { this.estado = estado; }
}