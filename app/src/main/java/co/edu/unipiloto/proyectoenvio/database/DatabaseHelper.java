package co.edu.unipiloto.proyectoenvio.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.content.Intent;
import android.util.Log;

import co.edu.unipiloto.proyectoenvio.model.CalificacionRequest;
import co.edu.unipiloto.proyectoenvio.services.NotificacionService;

import co.edu.unipiloto.proyectoenvio.model.Usuario;
import co.edu.unipiloto.proyectoenvio.model.Encomienda;
import co.edu.unipiloto.proyectoenvio.network.RetrofitClient;
import co.edu.unipiloto.proyectoenvio.network.UsuarioService;
import co.edu.unipiloto.proyectoenvio.network.EncomiendaService;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rimessa.db";
    private static final int DATABASE_VERSION = 9; // subimos versión por el cambio

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
    public static final String COLUMN_CELULAR = "celular";



    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_USUARIO + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_EMAIL + " TEXT NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_DIRECCION + " TEXT, " +
                    COLUMN_CELULAR + " TEXT, " +
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
    public static final String COLUMN_CALIFICACION = "calificacion";
    public static final String COLUMN_COMENTARIO = "comentario";


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
                    COLUMN_RECOLECTOR_ID + " TEXT, " +
                    COLUMN_CALIFICACION + " INTEGER DEFAULT 0, " +
                    COLUMN_COMENTARIO + " TEXT " +
                    ");";
    private UsuarioService usuarioService;
    private EncomiendaService encomiendaService;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        usuarioService = RetrofitClient.getClient().create(UsuarioService.class);
        encomiendaService = RetrofitClient.getClient().create(EncomiendaService.class);
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
                                   String direccion, String celular, String rol, String fechaNacimiento, String genero) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_USUARIO, usuario);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_DIRECCION, direccion);
        values.put(COLUMN_CELULAR, celular);
        values.put(COLUMN_ROL, rol);
        values.put(COLUMN_FECHA_NACIMIENTO, fechaNacimiento);
        values.put(COLUMN_GENERO, genero);

        long resultado = db.insert(TABLE_USERS, null, values);

        if (resultado != -1) {

            // 🔥 ENVIAR AL BACKEND
            Usuario u = new Usuario(nombre, usuario, email, password, direccion, celular, rol, fechaNacimiento, genero, null);

            usuarioService.crear(u).enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) { }
                @Override
                public void onFailure(Call<Usuario> call, Throwable t) { }
            });
        }

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

    public String obtenerRolUsuario(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ROL + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USUARIO + "=?", new String[]{usuario});
        String rol = null;
        if (cursor.moveToFirst()) {
            rol = cursor.getString(0);
        }
        cursor.close();
        return rol;
    }

    public boolean actualizarUsuario(String usuario, String nombre, String email, String password, String direccion, String celular) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_DIRECCION, direccion);
        values.put(COLUMN_CELULAR, celular);
        if (password != null && !password.isEmpty()) {
            values.put(COLUMN_PASSWORD, password);
        }

        int filas = db.update(TABLE_USERS, values, COLUMN_USUARIO + "=?", new String[]{usuario});

        if (filas > 0) {

            Usuario payload = new Usuario(nombre, usuario, email, password, direccion, celular, null, null, null, null);

            usuarioService.actualizar(usuario, payload).enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) { }
                @Override
                public void onFailure(Call<Usuario> call, Throwable t) { }
            });
        }

        return filas > 0;
    }


    public boolean actualizarFotoUsuario(String usuario, Bitmap bitmap) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        byte[] bytes = null;
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            bytes = stream.toByteArray();
            values.put(COLUMN_FOTO, bytes);
        } else {
            values.putNull(COLUMN_FOTO);
        }

        int filas = db.update(TABLE_USERS, values, COLUMN_USUARIO + "=?", new String[]{usuario});

        if (filas > 0 && bytes != null) {

            RequestBody body = RequestBody.create(MediaType.parse("image/*"), bytes);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", "foto.png", body);

            usuarioService.subirFoto(usuario, part).enqueue(new Callback<Usuario>() {
                @Override
                public void onResponse(Call<Usuario> call, Response<Usuario> response) { }
                @Override
                public void onFailure(Call<Usuario> call, Throwable t) { }
            });
        }

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

        ContentValues v = new ContentValues();
        v.put(COLUMN_GUIA, guia);
        v.put(COLUMN_REMITENTE, remitente);
        v.put(COLUMN_USUARIO_REMITENTE, usuarioRemitente);
        v.put(COLUMN_CELULAR_REMITENTE, celularRemitente);
        v.put(COLUMN_DIRECCION_REMITENTE_ACTUAL, direccionRemitenteActual);
        v.put(COLUMN_DESTINATARIO, destinatario);
        v.put(COLUMN_CELULAR_DESTINATARIO, celularDestinatario);
        v.put(COLUMN_DIRECCION_DESTINATARIO, direccionDestinatario);
        v.put(COLUMN_ESTADO, estado);
        v.put(COLUMN_FECHA_SOLICITUD, fechaSolicitud);
        v.put(COLUMN_FECHA_ENTREGA, fechaEntrega);
        v.put(COLUMN_PESO, peso);
        v.put(COLUMN_PRECIO, precio);
        v.put(COLUMN_RECOLECTOR_ID, recolectorId);

        long r = db.insert(TABLE_ENCOMIENDAS, null, v);

        if (r != -1) {

            Encomienda e = new Encomienda(
                    guia, remitente, usuarioRemitente, celularRemitente, direccionRemitenteActual,
                    destinatario, celularDestinatario, direccionDestinatario,
                    estado, fechaSolicitud, fechaEntrega, peso, precio, recolectorId,
                    null, null
            );

            encomiendaService.crear(e).enqueue(new Callback<Encomienda>() {
                @Override public void onResponse(Call<Encomienda> call, Response<Encomienda> response) { }
                @Override public void onFailure(Call<Encomienda> call, Throwable t) { }
            });
        }

        return r != -1;
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

    public Cursor getTodasLasEncomiendas() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ENCOMIENDAS, null, null, null, null, null, null);
    }

    // Obtener encomiendas asignadas a un recolector específico
    public Cursor getEncomiendasAsignadasARecolector(String recolectorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_ENCOMIENDAS,
                null,
                COLUMN_RECOLECTOR_ID + "=?",
                new String[]{recolectorId},
                null, null, null
        );
    }

    // Obtener encomiendas asignadas a un recolector específico y con estado SOLICITADO
    public Cursor getEncomiendasSolicitadasDeRecolector(String recolectorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_ENCOMIENDAS,
                null,
                COLUMN_RECOLECTOR_ID + "=? AND UPPER(" + COLUMN_ESTADO + ")=UPPER(?)",
                new String[]{recolectorId, "SOLICITADO"},
                null, null, null
        );
    }

    // Obtener encomiendas asignadas a un recolector específico y con estado RECOGIDO o EN_TRANSITO
    public Cursor getEncomiendasEntregarDeRecolector(String recolectorId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Selección: recolectorId = ? AND (estado = ? OR estado = ?)
        String selection = COLUMN_RECOLECTOR_ID + "=? AND (" + COLUMN_ESTADO + "=? OR " + COLUMN_ESTADO + "=?)";
        String[] selectionArgs = new String[]{recolectorId, "RECOGIDO", "EN_TRANSITO"};

        return db.query(
                TABLE_ENCOMIENDAS,
                null, // todas las columnas
                selection,
                selectionArgs,
                null, // groupBy
                null, // having
                null  // orderBy
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

    public boolean actualizarEstadoEncomienda(String guia, String nuevoEstado, Context context) {
        // ======================
        // 1. ACTUALIZACIÓN LOCAL
        // ======================
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ESTADO, nuevoEstado);

        int filas = db.update(TABLE_ENCOMIENDAS, values, COLUMN_GUIA + "=?", new String[]{guia});

        // Si no se actualizó en local, NO seguimos
        if (filas <= 0) return false;

        // ======================
        // 2. NOTIFICACIÓN LOCAL
        // ======================
        String mensaje = "";
        switch (nuevoEstado.toUpperCase()) {
            case "SOLICITADO":
                mensaje = "Tu encomienda #" + guia + " ha sido solicitada correctamente.";
                break;
            case "RECOGIDO":
                mensaje = "Tu encomienda #" + guia + " ha sido recogida por el mensajero.";
                break;
            case "EN_TRANSITO":
                mensaje = "Tu encomienda #" + guia + " está en camino hacia el destino.";
                break;
            case "ENTREGADO":
                mensaje = "Tu encomienda #" + guia + " ha sido entregada con éxito.";
                break;
        }

        if (!mensaje.isEmpty()) {
            Intent serviceIntent = new Intent(context, NotificacionService.class);
            serviceIntent.putExtra(NotificacionService.EXTRA_MENSAJE, mensaje);
            context.startService(serviceIntent);
        }

        // ======================
        // 3. SINCRONIZACIÓN BACKEND
        // ======================

        encomiendaService.actualizarEstado(guia, nuevoEstado).enqueue(new Callback<Encomienda>() {
            @Override
            public void onResponse(Call<Encomienda> call, Response<Encomienda> response) {
                if (!response.isSuccessful()) {
                    Log.e("SYNC", "Falló sincronización estado backend: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Encomienda> call, Throwable t) {
                Log.e("SYNC", "Error conectando con backend: " + t.getMessage());
            }
        });

        // ======================
        return true;
    }


    public boolean guardarCalificacion(String guia, int calificacion, String comentario) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues v = new ContentValues();
        v.put(COLUMN_CALIFICACION, calificacion);
        v.put(COLUMN_COMENTARIO, comentario);

        int filas = db.update(TABLE_ENCOMIENDAS, v, COLUMN_GUIA + "=?", new String[]{guia});

        if (filas > 0) {

            CalificacionRequest req = new CalificacionRequest(calificacion, comentario);

            encomiendaService.guardarCalificacion(guia, req)
                    .enqueue(new Callback<Encomienda>() {
                        @Override public void onResponse(Call<Encomienda> call, Response<Encomienda> response) { }
                        @Override public void onFailure(Call<Encomienda> call, Throwable t) { }
                    });
        }

        return filas > 0;
    }



    public Cursor getEncomiendasPorEstado(String estado) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM encomiendas WHERE UPPER(estado) = UPPER(?)", new String[]{estado});

    }

    public int contarUsuariosPorRol(String rol) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM usuarios WHERE LOWER(TRIM(rol)) = LOWER(TRIM(?))",
                new String[]{rol}
        );
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // ====== MÉTODOS DE ESTADÍSTICAS DE CALIFICACIONES ======

    // Promedio de todas las calificaciones (visión global)
    public double obtenerPromedioCalificacionGlobal() {
        SQLiteDatabase db = this.getReadableDatabase();
        double promedio = 0;

        Cursor cursor = db.query(
                TABLE_ENCOMIENDAS,
                new String[]{"AVG(" + COLUMN_CALIFICACION + ") AS promedio"},
                COLUMN_CALIFICACION + " > 0",
                null, null, null, null
        );

        if (cursor.moveToFirst()) {
            promedio = cursor.getDouble(cursor.getColumnIndexOrThrow("promedio"));
        }
        cursor.close();
        return promedio;
    }

    // Promedio de calificaciones de las encomiendas de un ciudadano (remitente)
    public double obtenerPromedioCalificacionPorUsuario(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        double promedio = 0;

        Cursor cursor = db.query(
                TABLE_ENCOMIENDAS,
                new String[]{"AVG(" + COLUMN_CALIFICACION + ") AS promedio"},
                COLUMN_USUARIO_REMITENTE + "=? AND " + COLUMN_CALIFICACION + " > 0",
                new String[]{usuario},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            promedio = cursor.getDouble(cursor.getColumnIndexOrThrow("promedio"));
        }
        cursor.close();
        return promedio;
    }

    // Promedio de calificaciones de las encomiendas asignadas a un recolector
    public double obtenerPromedioCalificacionPorRecolector(String recolectorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double promedio = 0;

        Cursor cursor = db.query(
                TABLE_ENCOMIENDAS,
                new String[]{"AVG(" + COLUMN_CALIFICACION + ") AS promedio"},
                COLUMN_RECOLECTOR_ID + "=? AND " + COLUMN_CALIFICACION + " > 0",
                new String[]{recolectorId},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            promedio = cursor.getDouble(cursor.getColumnIndexOrThrow("promedio"));
        }
        cursor.close();
        return promedio;
    }

    // Distribución global de calificaciones (para el asignador)
    public Map<Integer, Integer> obtenerDistribucionCalificaciones() {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<Integer, Integer> distribucion = new HashMap<>();

        Cursor cursor = db.query(
                TABLE_ENCOMIENDAS,
                new String[]{COLUMN_CALIFICACION, "COUNT(*) AS cantidad"},
                COLUMN_CALIFICACION + " > 0",
                null,
                COLUMN_CALIFICACION,
                null,
                COLUMN_CALIFICACION + " ASC"
        );

        while (cursor.moveToNext()) {
            int calif = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALIFICACION));
            int cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad"));
            distribucion.put(calif, cantidad);
        }
        cursor.close();
        return distribucion;
    }

    // Distribución de calificaciones de un recolector específico
    public Map<Integer, Integer> obtenerDistribucionCalificacionesPorRecolector(String recolectorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<Integer, Integer> distribucion = new HashMap<>();

        Cursor cursor = db.query(
                TABLE_ENCOMIENDAS,
                new String[]{COLUMN_CALIFICACION, "COUNT(*) AS cantidad"},
                COLUMN_RECOLECTOR_ID + "=? AND " + COLUMN_CALIFICACION + " > 0",
                new String[]{recolectorId},
                COLUMN_CALIFICACION,
                null,
                COLUMN_CALIFICACION + " ASC"
        );

        while (cursor.moveToNext()) {
            int calif = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALIFICACION));
            int cantidad = cursor.getInt(cursor.getColumnIndexOrThrow("cantidad"));
            distribucion.put(calif, cantidad);
        }
        cursor.close();
        return distribucion;
    }

// ====== ASIGNACIÓN DE RECOLECTORES ======

    // Obtener encomiendas sin recolector asignado
    public List<Map<String, String>> obtenerEncomiendasSinRecolector() {
        List<Map<String, String>> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_ENCOMIENDAS,
                new String[]{COLUMN_GUIA, COLUMN_DESTINATARIO, COLUMN_DIRECCION_DESTINATARIO, COLUMN_USUARIO_REMITENTE},
                COLUMN_RECOLECTOR_ID + " IS NULL OR " + COLUMN_RECOLECTOR_ID + " = ''",
                null,
                null, null,
                COLUMN_GUIA + " ASC"
        );

        while (cursor.moveToNext()) {
            Map<String, String> item = new HashMap<>();
            item.put("guia", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GUIA)));
            item.put("destinatario", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESTINATARIO)));
            item.put("direccion_destinatario", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIRECCION_DESTINATARIO)));
            item.put("usuario_remitente", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_REMITENTE)));
            lista.add(item);
        }
        cursor.close();
        return lista;
    }

    // Obtener lista de recolectores registrados
    public List<String> obtenerRecolectores() {
        List<String> recolectores = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USUARIO},
                COLUMN_ROL + "=?",
                new String[]{"Recolector de encomiendas"},
                null, null,
                COLUMN_USUARIO + " ASC"
        );

        while (cursor.moveToNext()) {
            recolectores.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USUARIO)));
        }
        cursor.close();
        return recolectores;
    }

    // Asignar un recolector a una encomienda
    public boolean asignarRecolectorAEncomienda(String guia, String recolectorId) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RECOLECTOR_ID, recolectorId);

        int filas = db.update(TABLE_ENCOMIENDAS, values, COLUMN_GUIA + "=?", new String[]{guia});

        if (filas > 0) {

            // 🔥 Sincronizar con el backend
            encomiendaService.asignar(guia, recolectorId)
                    .enqueue(new Callback<Encomienda>() {
                        @Override public void onResponse(Call<Encomienda> call, Response<Encomienda> response) { }
                        @Override public void onFailure(Call<Encomienda> call, Throwable t) { }
                    });
        }

        return filas > 0;
    }


    public void insertOrUpdateUsuarioFromBackend(Usuario u) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOMBRE, u.getNombre());
        values.put(COLUMN_USUARIO, u.getUsuario());
        values.put(COLUMN_EMAIL, u.getEmail());
        values.put(COLUMN_PASSWORD, u.getPassword());
        values.put(COLUMN_DIRECCION, u.getDireccion());
        values.put(COLUMN_CELULAR, u.getCelular());
        values.put(COLUMN_ROL, u.getRol());
        values.put(COLUMN_FECHA_NACIMIENTO, u.getFechaNacimiento());
        values.put(COLUMN_GENERO, u.getGenero());

        // Foto base64
        if (u.getFotoBase64() != null) {
            byte[] bytes = android.util.Base64.decode(u.getFotoBase64(), android.util.Base64.DEFAULT);
            values.put(COLUMN_FOTO, bytes);
        }

        db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void insertOrUpdateEncomiendaFromBackend(Encomienda e) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();

        v.put(COLUMN_GUIA, e.getNumeroGuia());
        v.put(COLUMN_REMITENTE, e.getRemitente());
        v.put(COLUMN_USUARIO_REMITENTE, e.getUsuarioRemitente());
        v.put(COLUMN_CELULAR_REMITENTE, e.getCelularRemitente());
        v.put(COLUMN_DIRECCION_REMITENTE_ACTUAL, e.getDireccionRemitenteActual());
        v.put(COLUMN_DESTINATARIO, e.getDestinatario());
        v.put(COLUMN_CELULAR_DESTINATARIO, e.getCelularDestinatario());
        v.put(COLUMN_DIRECCION_DESTINATARIO, e.getDireccionDestinatario());
        v.put(COLUMN_ESTADO, e.getEstado());
        v.put(COLUMN_FECHA_SOLICITUD, e.getFechaSolicitud());
        v.put(COLUMN_FECHA_ENTREGA, e.getFechaEntrega());
        v.put(COLUMN_PESO, e.getPeso());
        v.put(COLUMN_PRECIO, e.getPrecio());
        v.put(COLUMN_RECOLECTOR_ID, e.getRecolectorId());
        v.put(COLUMN_CALIFICACION, e.getCalificacion());
        v.put(COLUMN_COMENTARIO, e.getComentario());

        db.insertWithOnConflict(TABLE_ENCOMIENDAS, null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }



}


