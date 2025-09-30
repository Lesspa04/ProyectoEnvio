package co.edu.unipiloto.proyectoenvio;

import android.database.Cursor;

import org.osmdroid.util.GeoPoint;

import java.util.Date;
import java.util.List;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class Encomiendas {

    public enum Estado {
        SOLICITADO,
        RECOGIDO,
        EN_TRANSITO,
        ENTREGADO
    }

    // --- Campos remitente ---
    private String numeroGuia;
    private String remitenteNombre;
    private String remitenteDireccion;
    private String remitenteCelular;

    // --- Campos destinatario ---
    private String destinatarioNombre;
    private String destinatarioDireccion;
    private String destinatarioCelular;

    // --- Datos de envío ---
    private Estado estado;
    private Date fechaSolicitada;
    private Date fechaEstimadaEntrega;
    private double peso;
    private double precio;

    // --- Opcionales ---
    private List<GeoPoint> ruta; // si existe
    private String recolectorId;  // puede ser null

    public Encomiendas(String numeroGuia,
                       String remitenteNombre,
                       String remitenteDireccion,
                       String remitenteCelular,
                       String destinatarioNombre,
                       String destinatarioDireccion,
                       String destinatarioCelular,
                       Estado estado,
                       Date fechaSolicitada,
                       Date fechaEstimadaEntrega,
                       double peso,
                       double precio,
                       List<GeoPoint> ruta,
                       String recolectorId) {
        this.numeroGuia = numeroGuia;
        this.remitenteNombre = remitenteNombre;
        this.remitenteDireccion = remitenteDireccion;
        this.remitenteCelular = remitenteCelular;
        this.destinatarioNombre = destinatarioNombre;
        this.destinatarioDireccion = destinatarioDireccion;
        this.destinatarioCelular = destinatarioCelular;
        this.estado = estado;
        this.fechaSolicitada = fechaSolicitada;
        this.fechaEstimadaEntrega = fechaEstimadaEntrega;
        this.peso = peso;
        this.precio = precio;
        this.ruta = ruta;
        this.recolectorId = recolectorId;
    }

    // Reconstruir desde cursor
    public static Encomiendas fromCursor(Cursor cursor) {
        String numeroGuia = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GUIA));

        String remitenteNombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMITENTE));
        String remitenteDireccion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIRECCION_REMITENTE_ACTUAL));
        String remitenteCelular = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CELULAR_REMITENTE));

        String destinatarioNombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESTINATARIO));
        String destinatarioDireccion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIRECCION_DESTINATARIO));
        String destinatarioCelular = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CELULAR_DESTINATARIO));

        String estadoStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ESTADO));
        Estado estado = Estado.valueOf(estadoStr);

        long fechaSolicitudMillis = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA_SOLICITUD)));
        long fechaEntregaMillis = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA_ENTREGA)));

        double peso = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PESO));
        double precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO));

        String recolectorId = null;
        int idxRecolector = cursor.getColumnIndex(DatabaseHelper.COLUMN_RECOLECTOR_ID);
        if (idxRecolector != -1) {
            recolectorId = cursor.getString(idxRecolector);
        }

        return new Encomiendas(
                numeroGuia,
                remitenteNombre,
                remitenteDireccion,
                remitenteCelular,
                destinatarioNombre,
                destinatarioDireccion,
                destinatarioCelular,
                estado,
                new Date(fechaSolicitudMillis),
                new Date(fechaEntregaMillis),
                peso,
                precio,
                null, // ruta aún no implementada
                recolectorId
        );
    }

    // --- Getters ---
    public String getNumeroGuia() { return numeroGuia; }
    public String getRemitenteNombre() { return remitenteNombre; }
    public String getRemitenteDireccion() { return remitenteDireccion; }
    public String getRemitenteCelular() { return remitenteCelular; }

    public String getDestinatarioNombre() { return destinatarioNombre; }
    public String getDestinatarioDireccion() { return destinatarioDireccion; }
    public String getDestinatarioCelular() { return destinatarioCelular; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Date getFechaSolicitada() { return fechaSolicitada; }
    public Date getFechaEstimadaEntrega() { return fechaEstimadaEntrega; }

    public double getPeso() { return peso; }
    public double getPrecio() { return precio; }

    public List<GeoPoint> getRuta() { return ruta; }
    public void setRuta(List<GeoPoint> ruta) { this.ruta = ruta; }

    public String getRecolectorId() { return recolectorId; }
    public void setRecolectorId(String recolectorId) { this.recolectorId = recolectorId; }
}
