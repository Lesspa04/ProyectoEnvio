package co.edu.unipiloto.proyectoenvio.perfil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import co.edu.unipiloto.proyectoenvio.*;
import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;
import co.edu.unipiloto.proyectoenvio.loginSignup.LoginActivity;

public class PerfilActivity extends AppCompatActivity {

    TextInputEditText edtNombre, edtEmail, edtPassword;
    ImageView imgPerfil;
    Button btnGuardar, btnCambiarFoto;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imagenSeleccionada;

    DatabaseHelper dbHelper;
    String usuarioLogueado; // Este valor deberías pasarlo desde Login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        dbHelper = new DatabaseHelper(this);

        edtNombre = findViewById(R.id.edtPerfilNombre);
        edtEmail = findViewById(R.id.edtPerfilEmail);
        edtPassword = findViewById(R.id.edtPerfilPassword);
        imgPerfil = findViewById(R.id.imgPerfil);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);

        // Recoger usuario logueado (puede venir de sesión, Intent, etc.)
        usuarioLogueado = getIntent().getStringExtra("usuario");

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String usuarioLogueado = prefs.getString("usuario", null);

        if (usuarioLogueado == null) {
            Toast.makeText(this, "Sesión no encontrada. Redirigiendo al login...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }


        cargarDatosUsuario(usuarioLogueado);

        btnCambiarFoto.setOnClickListener(v -> seleccionarImagen());

        btnGuardar.setOnClickListener(v -> {
            String nombre = edtNombre.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (nombre.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Nombre y email no pueden estar vacíos", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean actualizado = dbHelper.actualizarUsuario(usuarioLogueado, nombre, email, password);
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
            edtNombre.setText(cursor.getString(cursor.getColumnIndex("nombre")));
            edtEmail.setText(cursor.getString(cursor.getColumnIndex("email")));
            edtPassword.setText(cursor.getString(cursor.getColumnIndex("password")));
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
            imagenSeleccionada = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagenSeleccionada);
                imgPerfil.setImageBitmap(bitmap);
                // Podrías guardar la URI en la base de datos también
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
