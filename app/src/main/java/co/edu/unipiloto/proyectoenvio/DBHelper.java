package co.edu.unipiloto.proyectoenvio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "envios.db";
    private static final int DB_VERSION = 1;

    // Tablas
    private static final String T_REMITENTE = "remitente";
    private static final String T_DESTINATARIO = "destinatario";
    private static final String T_RECOLECTOR = "recolector";
    private static final String T_ENVIO = "envio";
    private static final String T_PAGO = "pago";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // remitente
        db.execSQL("CREATE TABLE " + T_REMITENTE + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT, email TEXT, telefono TEXT, direccion TEXT, created_at INTEGER)");

        // destinatario
        db.execSQL("CREATE TABLE " + T_DESTINATARIO + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT, telefono TEXT, direccion TEXT)");

        // recolector (simulado)
        db.execSQL("CREATE TABLE " + T_RECOLECTOR + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT, lat REAL, lon REAL, zona TEXT)");

        // envio
        db.execSQL("CREATE TABLE " + T_ENVIO + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tracking TEXT UNIQUE, remitente_id INTEGER, destinatario_id INTEGER," +
                "direccion_destino TEXT, peso REAL, estado TEXT, fecha_solicitud INTEGER," +
                "precio REAL, pdf_path TEXT, recolector_id INTEGER," +
                "FOREIGN KEY(remitente_id) REFERENCES " + T_REMITENTE + "(_id)," +
                "FOREIGN KEY(destinatario_id) REFERENCES " + T_DESTINATARIO + "(_id)," +
                "FOREIGN KEY(recolector_id) REFERENCES " + T_RECOLECTOR + "(_id))");

        // pago (simulado)
        db.execSQL("CREATE TABLE " + T_PAGO + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "envio_id INTEGER, comprobante TEXT, monto REAL, fecha INTEGER, metodo TEXT," +
                "FOREIGN KEY(envio_id) REFERENCES " + T_ENVIO + "(_id))");

        // Insertar recolectores de ejemplo (zonas / coordenadas)
        db.execSQL("INSERT INTO " + T_RECOLECTOR + " (nombre, lat, lon, zona) VALUES " +
                "('Recolector Norte', 4.735000, -74.080000, 'Norte')," +
                "('Recolector Centro', 4.648282, -74.247895, 'Centro')," +
                "('Recolector Sur', 4.560000, -74.170000, 'Sur')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // sencillo para la etapa inicial
        db.execSQL("DROP TABLE IF EXISTS " + T_PAGO);
        db.execSQL("DROP TABLE IF EXISTS " + T_ENVIO);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECOLECTOR);
        db.execSQL("DROP TABLE IF EXISTS " + T_DESTINATARIO);
        db.execSQL("DROP TABLE IF EXISTS " + T_REMITENTE);
        onCreate(db);
    }

    // --------- Remitente ----------
    public long insertRemitente(String nombre, String email, String telefono, String direccion, long createdAt) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("email", email);
        cv.put("telefono", telefono);
        cv.put("direccion", direccion);
        cv.put("created_at", createdAt);
        return db.insert(T_REMITENTE, null, cv);
    }

    public long getRemitenteIdByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_REMITENTE, new String[]{"_id"}, "email=?", new String[]{email}, null, null, null);
        if (c != null && c.moveToFirst()) {
            long id = c.getLong(0);
            c.close();
            return id;
        }
        if (c != null) c.close();
        return -1;
    }

    // --------- Destinatario ----------
    public long insertDestinatario(String nombre, String telefono, String direccion) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre", nombre);
        cv.put("telefono", telefono);
        cv.put("direccion", direccion);
        return db.insert(T_DESTINATARIO, null, cv);
    }

    // --------- Envio ----------
    public long insertEnvio(String tracking, long remitenteId, long destinatarioId,
                            String direccionDestino, double peso, String estado,
                            long fechaSolicitud, double precio, String pdfPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("tracking", tracking);
        cv.put("remitente_id", remitenteId);
        cv.put("destinatario_id", destinatarioId);
        cv.put("direccion_destino", direccionDestino);
        cv.put("peso", peso);
        cv.put("estado", estado);
        cv.put("fecha_solicitud", fechaSolicitud);
        cv.put("precio", precio);
        cv.put("pdf_path", pdfPath);
        return db.insert(T_ENVIO, null, cv);
    }

    public Cursor getEnvioByTracking(String tracking) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(T_ENVIO, null, "tracking=?", new String[]{tracking}, null, null, null);
    }

    public int updateEnvioEstadoByTracking(String tracking, String nuevoEstado) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", nuevoEstado);
        return db.update(T_ENVIO, cv, "tracking=?", new String[]{tracking});
    }

    public int asignarRecolectorAEnvio(long envioId, long recolectorId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("recolector_id", recolectorId);
        cv.put("estado", "Asignado");
        return db.update(T_ENVIO, cv, "_id=?", new String[]{String.valueOf(envioId)});
    }

    // --------- Recolector ----------
    public Cursor getAllRecolectores() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(T_RECOLECTOR, null, null, null, null, null, null);
    }

    // Encuentra recolector más cercano (distancia euclidiana simple)
    public long findNearestRecolectorId(double lat, double lon) {
        Cursor c = getAllRecolectores();
        long bestId = -1;
        double bestDist = Double.MAX_VALUE;
        if (c != null) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow("_id"));
                double rlat = c.getDouble(c.getColumnIndexOrThrow("lat"));
                double rlon = c.getDouble(c.getColumnIndexOrThrow("lon"));
                double dlat = rlat - lat;
                double dlon = rlon - lon;
                double dist = Math.sqrt(dlat * dlat + dlon * dlon);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestId = id;
                }
            }
            c.close();
        }
        return bestId;
    }

    // --------- Pago ----------
    public long insertPago(long envioId, String comprobante, double monto, long fecha, String metodo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("envio_id", envioId);
        cv.put("comprobante", comprobante);
        cv.put("monto", monto);
        cv.put("fecha", fecha);
        cv.put("metodo", metodo);
        return db.insert(T_PAGO, null, cv);
    }
}
