package co.edu.unipiloto.proyectoenvio.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rimessa.db";
    private static final int DATABASE_VERSION = 1;

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

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_USUARIO + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_EMAIL + " TEXT NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_DIRECCION + " TEXT, " +
                    COLUMN_ROL + " TEXT NOT NULL, " +
                    COLUMN_FECHA_NACIMIENTO + " TEXT NOT NULL, " +
                    COLUMN_GENERO + " TEXT NOT NULL" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Aquí puedes manejar actualizaciones de esquema en el futuro
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Método para insertar usuario
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
        return resultado != -1;  // true si se insertó, false si hubo error
    }

    // Método para verificar si usuario existe y password es correcto
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

    // Obtener datos de un usuario específico
    public Cursor obtenerUsuario(String usuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USERS,
                null, // Todas las columnas
                COLUMN_USUARIO + " = ?",
                new String[]{usuario},
                null,
                null,
                null
        );
    }

    // Actualizar datos de un usuario (nombre, email, y si aplica contraseña)
    public boolean actualizarUsuario(String usuario, String nombre, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_EMAIL, email);

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

}
