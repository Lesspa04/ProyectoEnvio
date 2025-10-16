package co.edu.unipiloto.proyectoenvio.perfil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;

import co.edu.unipiloto.proyectoenvio.*;
import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;
import co.edu.unipiloto.proyectoenvio.loginSignup.LoginActivity;

public class PerfilActivity extends AppCompatActivity {

    TextInputEditText edtCelular, edtNombre, edtEmail, edtPassword;
    TextView tvDireccionSeleccionada;
    ImageView imgPerfil;
    Button btnGuardar, btnCambiarFoto, btnSeleccionarDireccion;

    private static final int PICK_IMAGE_REQUEST = 1;

    DatabaseHelper dbHelper;
    String usuarioLogueado;
    String direccionSeleccionada = "";

    ActivityResultLauncher<Intent> direccionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        dbHelper = new DatabaseHelper(this);

        edtNombre = findViewById(R.id.edtPerfilNombre);
        edtEmail = findViewById(R.id.edtPerfilEmail);
        edtCelular = findViewById(R.id.edtPerfilCelular);
        edtPassword = findViewById(R.id.edtPerfilPassword);
        tvDireccionSeleccionada = findViewById(R.id.tvDireccionSeleccionada);
        imgPerfil = findViewById(R.id.imgPerfil);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        btnSeleccionarDireccion = findViewById(R.id.btnSeleccionarDireccion);

        // Obtener usuario desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioLogueado = prefs.getString("usuario", null);

        if (usuarioLogueado == null) {
            Toast.makeText(this, "Sesión no encontrada. Redirigiendo al login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Cargar datos y foto
        cargarDatosUsuario(usuarioLogueado);

        // Selección de imagen
        btnCambiarFoto.setOnClickListener(v -> seleccionarImagen());

        // Lanzador para mapa
        direccionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        double lat = data.getDoubleExtra("latitud", 0.0);
                        double lon = data.getDoubleExtra("longitud", 0.0);

                        // Convertir lat/lon a dirección real usando Geocoder
                        new Thread(() -> {
                            String addr = obtenerDireccion(lat, lon);
                            runOnUiThread(() -> {
                                direccionSeleccionada = addr;
                                tvDireccionSeleccionada.setText(addr);
                            });
                        }).start();
                    }
                }
        );

        // Abrir mapa
        btnSeleccionarDireccion.setOnClickListener(v -> {
            Intent i = new Intent(this, MapaDireccionActivity.class);
            direccionLauncher.launch(i);
        });

        // Guardar cambios de texto
        btnGuardar.setOnClickListener(v -> {
            String nombre = edtNombre.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String direccion = direccionSeleccionada;
            String celular = edtCelular.getText().toString().trim();

            if (celular.isEmpty()) {
                Toast.makeText(this, "Debe ingresar un número de celular", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nombre.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Nombre y email no pueden estar vacíos", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean actualizado = dbHelper.actualizarUsuario(usuarioLogueado, nombre, email, password, direccion, celular);
            if (actualizado) {
                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("Range")
    private void cargarDatosUsuario(String usuario) {
        Cursor cursor = dbHelper.obtenerUsuario(usuario);
        if (cursor != null && cursor.moveToFirst()) {
            edtNombre.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOMBRE)));
            edtEmail.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL)));
            edtPassword.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD)));
            direccionSeleccionada = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DIRECCION));
            tvDireccionSeleccionada.setText(direccionSeleccionada);
            edtCelular.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CELULAR)));

            // Cargar foto
            Bitmap foto = dbHelper.obtenerFotoDesdeCursor(cursor);
            if (foto != null) {
                imgPerfil.setImageBitmap(foto);
            } else {
                imgPerfil.setImageResource(R.drawable.ic_launcher_background); // placeholder
            }

            cursor.close();
        }
    }

    private void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imgPerfil.setImageBitmap(bitmap);
                dbHelper.actualizarFotoUsuario(usuarioLogueado, bitmap); // guardar como BLOB
                Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Convierte lat/lon a dirección en texto
    private String obtenerDireccion(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocation(lat, lon, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                return direcciones.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Dirección desconocida";
    }
}
