package co.edu.unipiloto.proyectoenvio.menus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.proyectoenvio.loginSignup.LoginActivity;
import co.edu.unipiloto.proyectoenvio.R;

public class SplashActivity extends AppCompatActivity {

    ImageButton btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        btnContinuar = findViewById(R.id.btnContinuar);

        btnContinuar.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Finaliza la actividad para que no regrese aquí con "Back"
        });
    }
}
