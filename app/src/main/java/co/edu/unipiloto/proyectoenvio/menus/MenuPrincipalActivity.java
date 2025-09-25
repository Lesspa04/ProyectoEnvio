package co.edu.unipiloto.proyectoenvio.menus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.proyectoenvio.EnvioActivity;
import co.edu.unipiloto.proyectoenvio.MisRecoleccionesActivity;
import co.edu.unipiloto.proyectoenvio.R;
import co.edu.unipiloto.proyectoenvio.RutasActivity;
import co.edu.unipiloto.proyectoenvio.SeguimientoActivity;
import co.edu.unipiloto.proyectoenvio.loginSignup.LoginActivity;
import co.edu.unipiloto.proyectoenvio.perfil.PerfilActivity;

public class MenuPrincipalActivity extends AppCompatActivity {

    Button btnEnvio, btnRutas, btnRecolecciones, btnSeguimiento, btnCerrarSesion, btnMiPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        // 👇 Manejo moderno del botón "Atrás"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Aquí decides qué hacer al presionar "Atrás"
                moveTaskToBack(true); // Minimiza la app
            }
        });

        // Recuperar nombre de usuario (si quieres usarlo más adelante)
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String usuario = prefs.getString("usuario", null);

        // Si no hay sesión, redirige al login
        if (usuario == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Botones normales
        btnEnvio = findViewById(R.id.btnEnvio);
        btnRutas = findViewById(R.id.btnRutas);
        btnRecolecciones = findViewById(R.id.btnRecolecciones);
        btnSeguimiento = findViewById(R.id.btnSeguimiento);

        // Botones nuevos
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnMiPerfil = findViewById(R.id.btnMiPerfil);

        btnEnvio.setOnClickListener(v ->
                startActivity(new Intent(this, EnvioActivity.class)));

        btnRutas.setOnClickListener(v ->
                startActivity(new Intent(this, RutasActivity.class)));

        btnRecolecciones.setOnClickListener(v ->
                startActivity(new Intent(this, MisRecoleccionesActivity.class)));

        btnSeguimiento.setOnClickListener(v ->
                startActivity(new Intent(this, SeguimientoActivity.class)));

        btnMiPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        btnCerrarSesion.setOnClickListener(v -> {
            // Borrar sesión
            getSharedPreferences("sesion", MODE_PRIVATE).edit().clear().apply();

            // Volver al login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

}
