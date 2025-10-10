package co.edu.unipiloto.proyectoenvio.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rimessa.db";
    private static final int DATABASE_VERSION = 6; // subimos versión por el cambio

    // ====== TABLA USUARIOS ======
    public static final String TABLE_USERS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_USUARIO = "usuario";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_DIRECCION = "direccion";
    public static final String COLUMN_ROL = "rol";
    public static final String COLUMN_FECHA_NACIMIENTO = "fecha_nacimiento";
    public static final String COLUMN_GENERO = "genero";
    public static final String COLUMN_FOTO = "foto"; // BLOB
    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_USUARIO + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_EMAIL + " TEXT NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_DIRECCION + " TEXT, " +
                    COLUMN_ROL + " TEXT NOT NULL, " +
                    COLUMN_FECHA_NACIMIENTO + " TEXT NOT NULL, " +
                    COLUMN_GENERO + " TEXT NOT NULL, " +
                    COLUMN_FOTO + " BLOB" + // agregamos foto
                    ");";

    // ====== TABLA ENCOMIENDAS ======
    public static final String TABLE_ENCOMIENDAS = "encomiendas";
    public static final String COLUMN_GUIA = "numero_guia";

    // Remitente
    public static final String COLUMN_REMITENTE = "remitente";
    public static final String COLUMN_USUARIO_REMITENTE = "usuario_remitente"; // nueva columna
    public static final String COLUMN_CELULAR_REMITENTE = "celular_remitente";
    public static final String COLUMN_DIRECCION_REMITENTE_ACTUAL = "direccion_remitente_actual";

    // Destinatario
    public static final String COLUMN_DESTINATARIO = "destinatario";
    public static final String COLUMN_CELULAR_DESTINATARIO = "celular_destinatario";
    public static final String COLUMN_DIRECCION_DESTINATARIO = "direccion_destinatario";

    // Datos de envío
    public static final String COLUMN_ESTADO = "estado";
    public static final String COLUMN_FECHA_SOLICITUD = "fecha_solicitud";
    public static final String COLUMN_FECHA_ENTREGA = "fecha_entrega";
    public static final String COLUMN_PESO = "peso";
    public static final String COLUMN_PRECIO = "precio";
    public static final String COLUMN_RECOLECTOR_ID = "recolector_id";

    private static final String TABLE_CREATE_ENCOMIENDAS =
            "CREATE TABLE " + TABLE_ENCOMIENDAS + " (" +
                    COLUMN_GUIA + " TEXT PRIMARY KEY, " +
                    COLUMN_REMITENTE + " TEXT NOT NULL, " +
                    COLUMN_USUARIO_REMITENTE + " TEXT NOT NULL, " +
                    COLUMN_CELULAR_REMITENTE + " TEXT NOT NULL, " +
                    COLUMN_DIRECCION_REMITENTE_ACTUAL + " TEXT NOT NULL, " +
                    COLUMN_DESTINATARIO + " TEXT NOT NULL, " +
                    COLUMN_CELULAR_DESTINATARIO + " TEXT NOT NULL, " +
                    COLUMN_DIRECCION_DESTINATARIO + " TEXT NOT NULL, " +
                    COLUMN_ESTADO + " TEXT NOT NULL, " +
                    COLUMN_FECHA_SOLICITUD + " TEXT, " +
                    COLUMN_FECHA_ENTREGA + " TEXT, " +
                    COLUMN_PESO + " REAL, " +
                    COLUMN_PRECIO + " REAL, " +
                    COLUMN_RECOLECTOR_ID + " TEXT " +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_ENCOMIENDAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENCOMIENDAS);
        onCreate(db);
    }

    // ====== MÉTODOS USUARIOS ======
    public boolean insertarUsuario(String nombre, String usuario, String email, String password,
                                   String direccion, String rol, String fechaNacimiento, String genero) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_USUARIO, usuario);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_DIRECCION, direccion);
        values.put(COLUMN_ROL, rol);
        values.put(COLUMN_FECHA_NACIMIENTO, fechaNacimiento);
        values.put(COLUMN_GENERO, genero);

        long resultado = db.insert(TABLE_USERS, null, values);
        return resultado != -1;
    }

    public boolean validarUsuario(String usuario, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columnas = { COLUMN_ID };
        String selection = COLUMN_USUARIO + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { usuario, password };
        Cursor cursor = db.query(TABLE_USERS, columnas, selection, selectionArgs, null, null, null);
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    public Cursor obtenerUsuario(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USERS,
                null,
                COLUMN_USUARIO + " = ?",
                new String[]{usuario},
                null, null, null
        );
    }

    public boolean actualizarUsuario(String usuario, String nombre, String email, String password, String direccion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_DIRECCION, direccion);

        if (password != null && !password.isEmpty()) {
            values.put(COLUMN_PASSWORD, password);
        }

        int filasActualizadas = db.update(
                TABLE_USERS,
                values,
                COLUMN_USUARIO + " = ?",
                new String[]{usuario}
        );

        return filasActualizadas > 0;
    }

    public boolean actualizarFotoUsuario(String usuario, Bitmap bitmap) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if(bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            values.put(COLUMN_FOTO, stream.toByteArray());
        } else {
            values.putNull(COLUMN_FOTO);
        }
        int filas = db.update(TABLE_USERS, values, COLUMN_USUARIO + "=?", new String[]{usuario});
        return filas > 0;
    }

    // Método para obtener Bitmap desde cursor
    public Bitmap obtenerFotoDesdeCursor(Cursor cursor) {
        byte[] fotoBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_FOTO));
        if(fotoBytes != null) {
            return BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
        }
        return null;
    }

    // ====== MÉTODOS ENCOMIENDAS ======
    public boolean insertarEncomienda(String guia,
                                      String remitente, String usuarioRemitente, String celularRemitente, String direccionRemitenteActual,
                                      String destinatario, String celularDestinatario, String direccionDestinatario,
                                      String estado, String fechaSolicitud, String fechaEntrega,
                                      double peso, double precio, String recolectorId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GUIA, guia);
        values.put(COLUMN_REMITENTE, remitente);
        values.put(COLUMN_USUARIO_REMITENTE, usuarioRemitente);
        values.put(COLUMN_CELULAR_REMITENTE, celularRemitente);
        values.put(COLUMN_DIRECCION_REMITENTE_ACTUAL, direccionRemitenteActual);
        values.put(COLUMN_DESTINATARIO, destinatario);
        values.put(COLUMN_CELULAR_DESTINATARIO, celularDestinatario);
        values.put(COLUMN_DIRECCION_DESTINATARIO, direccionDestinatario);
        values.put(COLUMN_ESTADO, estado);
        values.put(COLUMN_FECHA_SOLICITUD, fechaSolicitud);
        values.put(COLUMN_FECHA_ENTREGA, fechaEntrega);
        values.put(COLUMN_PESO, peso);
        values.put(COLUMN_PRECIO, precio);
        values.put(COLUMN_RECOLECTOR_ID, recolectorId);

        long resultado = db.insert(TABLE_ENCOMIENDAS, null, values);
        return resultado != -1;
    }

    public Cursor getEncomiendasPorUsuario(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_ENCOMIENDAS,
                null,
                COLUMN_USUARIO_REMITENTE + " = ?",
                new String[]{usuario},
                null, null,
                COLUMN_FECHA_SOLICITUD + " DESC"
        );
    }

    public Cursor getEncomiendasPorRemitenteCelular(String celular) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_ENCOMIENDAS,
                null,
                COLUMN_CELULAR_REMITENTE + "=?",
                new String[]{celular},
                null, null, null
        );
    }

    public Cursor obtenerEncomiendaPorGuia(String guia) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_ENCOMIENDAS,
                null,
                COLUMN_GUIA + "=?",
                new String[]{guia},
                null, null, null
        );
    }

    public boolean actualizarEstadoEncomienda(String guia, String nuevoEstado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ESTADO, nuevoEstado);

        int filas = db.update(TABLE_ENCOMIENDAS, values,
                COLUMN_GUIA + "=?", new String[]{guia});
        return filas > 0;
    }

    public Cursor getEncomiendasPorEstado(String estado) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM encomiendas WHERE estado = ?", new String[]{estado});
    }

}

