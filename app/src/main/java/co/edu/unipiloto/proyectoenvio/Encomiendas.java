package co.edu.unipiloto.proyectoenvio;

import org.osmdroid.util.GeoPoint;
import java.util.Date;
import java.util.List;

public class Encomiendas {
    public enum Estado {
        SOLICITADO,
        RECOGIDO,
        EN_TRANSITO,
        ENTREGADO
    }

    private String numeroGuia;
    private String remitenteNombre;
    private String remitenteDireccion;
    private String remitenteCelular;
    private Estado estado;
    private Date fechaSolicitada;
    private Date fechaEstimadaEntrega;
    // Si existe, ruta (lista de puntos) asignada
    private List<GeoPoint> ruta;
    // id del recolector asignado (simulado)
    private String recolectorId;

    public Encomiendas(String numeroGuia, String remitenteNombre, String remitenteDireccion,
                      String remitenteCelular, Estado estado, Date fechaSolicitada,
                      Date fechaEstimadaEntrega, List<GeoPoint> ruta, String recolectorId) {
        this.numeroGuia = numeroGuia;
        this.remitenteNombre = remitenteNombre;
        this.remitenteDireccion = remitenteDireccion;
        this.remitenteCelular = remitenteCelular;
        this.estado = estado;
        this.fechaSolicitada = fechaSolicitada;
        this.fechaEstimadaEntrega = fechaEstimadaEntrega;
        this.ruta = ruta;
        this.recolectorId = recolectorId;
    }

    // getters y setters
    public String getNumeroGuia() { return numeroGuia; }
    public String getRemitenteNombre() { return remitenteNombre; }
    public String getRemitenteDireccion() { return remitenteDireccion; }
    public String getRemitenteCelular() { return remitenteCelular; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public Date getFechaSolicitada() { return fechaSolicitada; }
    public Date getFechaEstimadaEntrega() { return fechaEstimadaEntrega; }
    public List<GeoPoint> getRuta() { return ruta; }
    public String getRecolectorId() { return recolectorId; }
}
