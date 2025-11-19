package co.edu.unipiloto.proyectoenvio.model;

public class Encomienda {
    private String numeroGuia;

    private String remitente;
    private String usuarioRemitente;
    private String celularRemitente;
    private String direccionRemitenteActual;

    private String destinatario;
    private String celularDestinatario;
    private String direccionDestinatario;

    private String estado;
    private String fechaSolicitud;
    private String fechaEntrega;

    private double peso;
    private double precio;

    private String recolectorId;

    private Integer calificacion;
    private String comentario;

    public Encomienda() {
    }

    public Encomienda(String numeroGuia, String remitente, String usuarioRemitente, String celularRemitente, String direccionRemitenteActual, String destinatario, String celularDestinatario, String direccionDestinatario, String estado, String fechaSolicitud, String fechaEntrega, double peso, double precio, String recolectorId, Integer calificacion, String comentario) {
        this.numeroGuia = numeroGuia;
        this.remitente = remitente;
        this.usuarioRemitente = usuarioRemitente;
        this.celularRemitente = celularRemitente;
        this.direccionRemitenteActual = direccionRemitenteActual;
        this.destinatario = destinatario;
        this.celularDestinatario = celularDestinatario;
        this.direccionDestinatario = direccionDestinatario;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaEntrega = fechaEntrega;
        this.peso = peso;
        this.precio = precio;
        this.recolectorId = recolectorId;
        this.calificacion = calificacion;
        this.comentario = comentario;
    }

    // getters y setters

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    public String getUsuarioRemitente() {
        return usuarioRemitente;
    }

    public void setUsuarioRemitente(String usuarioRemitente) {
        this.usuarioRemitente = usuarioRemitente;
    }

    public String getCelularRemitente() {
        return celularRemitente;
    }

    public void setCelularRemitente(String celularRemitente) {
        this.celularRemitente = celularRemitente;
    }

    public String getDireccionRemitenteActual() {
        return direccionRemitenteActual;
    }

    public void setDireccionRemitenteActual(String direccionRemitenteActual) {
        this.direccionRemitenteActual = direccionRemitenteActual;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getCelularDestinatario() {
        return celularDestinatario;
    }

    public void setCelularDestinatario(String celularDestinatario) {
        this.celularDestinatario = celularDestinatario;
    }

    public String getDireccionDestinatario() {
        return direccionDestinatario;
    }

    public void setDireccionDestinatario(String direccionDestinatario) {
        this.direccionDestinatario = direccionDestinatario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(String fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getRecolectorId() {
        return recolectorId;
    }

    public void setRecolectorId(String recolectorId) {
        this.recolectorId = recolectorId;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
