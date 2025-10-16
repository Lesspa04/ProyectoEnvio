package co.edu.unipiloto.proyectoenvio.menus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.proyectoenvio.EnvioActivity;
import co.edu.unipiloto.proyectoenvio.MisRecoleccionesActivity;
import co.edu.unipiloto.proyectoenvio.R;
import co.edu.unipiloto.proyectoenvio.RutasActivity;
import co.edu.unipiloto.proyectoenvio.SeguimientoActivity;
import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;
import co.edu.unipiloto.proyectoenvio.loginSignup.LoginActivity;
import co.edu.unipiloto.proyectoenvio.perfil.PerfilActivity;

public class MenuPrincipalActivity extends AppCompatActivity {

    Button btnEnvio, btnRutas, btnRecolecciones, btnSeguimiento;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        dbHelper = new DatabaseHelper(this);

        // 👇 Manejo del botón "Atrás"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true); // Minimiza la app
            }
        });

        // Recuperar usuario actual
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String usuario = prefs.getString("usuario", null);

        if (usuario == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inicializar botones
        btnEnvio = findViewById(R.id.btnEnvio);
        btnRutas = findViewById(R.id.btnRutas);
        btnRecolecciones = findViewById(R.id.btnRecolecciones);
        btnSeguimiento = findViewById(R.id.btnSeguimiento);

// Inicializar vistas
        ImageView imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        TextView tvNombreUsuario = findViewById(R.id.tvNombreUsuario);
        TextView tvRolUsuario = findViewById(R.id.tvRolUsuario);
        TextView btnOpciones = findViewById(R.id.btnOpciones);

// Cargar datos del usuario
        Cursor cursor = dbHelper.obtenerUsuario(usuario);
        if (cursor != null && cursor.moveToFirst()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE));
            String rol = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROL));
            tvNombreUsuario.setText(nombre);
            tvRolUsuario.setText("Rol: " + rol);

            // Verificar si la columna de la foto existe
            int fotoIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FOTO);
            Bitmap foto = null;

            if (fotoIndex != -1) { // la columna existe
                byte[] fotoBytes = cursor.getBlob(fotoIndex);
                if (fotoBytes != null && fotoBytes.length > 0) {
                    foto = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
                }
            }

            if (foto != null) {
                imgFotoPerfil.setImageBitmap(foto);
            } else {
                imgFotoPerfil.setImageResource(R.mipmap.ic_launcher_round); // o tu placeholder
            }

            cursor.close();
        }

// Menú emergente en ⋮
        btnOpciones.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
            popup.getMenu().add("Mi Perfil");
            popup.getMenu().add("Cerrar Sesión");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.contains("Perfil")) {
                    startActivity(new Intent(this, PerfilActivity.class));
                } else if (title.contains("Cerrar")) {
                    getSharedPreferences("sesion", MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                return true;
            });
            popup.show();
        });


        // Obtener el rol del usuario
        String rolUsuario = dbHelper.obtenerRolUsuario(usuario);
        if (rolUsuario == null) rolUsuario = "ciudadano"; // valor por defecto

        // 👇 Mostrar/Ocultar botones según el rol
        configurarBotonesPorRol(rolUsuario);

        // Asignar acciones a los botones visibles
        btnEnvio.setOnClickListener(v -> startActivity(new Intent(this, EnvioActivity.class)));
        btnRutas.setOnClickListener(v -> startActivity(new Intent(this, RutasActivity.class)));
        btnRecolecciones.setOnClickListener(v -> startActivity(new Intent(this, MisRecoleccionesActivity.class)));
        btnSeguimiento.setOnClickListener(v -> startActivity(new Intent(this, SeguimientoActivity.class)));

    }

    private void configurarBotonesPorRol(String rol) {
        // Primero ocultamos todos los botones de funciones
        btnEnvio.setVisibility(Button.GONE);
        btnRutas.setVisibility(Button.GONE);
        btnRecolecciones.setVisibility(Button.GONE);
        btnSeguimiento.setVisibility(Button.GONE);

        // Luego activamos según el rol
        switch (rol.toLowerCase()) {
            case "ciudadano":
                btnEnvio.setVisibility(Button.VISIBLE);
                btnRecolecciones.setVisibility(Button.VISIBLE);
                btnSeguimiento.setVisibility(Button.VISIBLE);
                btnRecolecciones.setText("Mis solicitudes");
                break;

            case "recolector de encomiendas":
                btnRutas.setVisibility(Button.VISIBLE);
                btnRecolecciones.setVisibility(Button.VISIBLE);
                btnSeguimiento.setVisibility(Button.VISIBLE);
                btnRecolecciones.setText("Mis recolecciones");
                break;

            case "asignador de rutas":
                btnRutas.setVisibility(Button.VISIBLE);
                break;
        }

    }
}
