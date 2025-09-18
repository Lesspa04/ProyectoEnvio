package co.edu.unipiloto.proyectoenvio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnLogin, btnEnvio, btnRutas, btnRecolecciones, btnSeguimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLoginRegister);
        btnEnvio = findViewById(R.id.btnEnvio);
        btnRutas = findViewById(R.id.btnRutas);
        btnRecolecciones = findViewById(R.id.btnRecolecciones);
        btnSeguimiento = findViewById(R.id.btnSeguimiento);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginRegisterActivity.class))
        );

        btnEnvio.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EnvioActivity.class))
        );

        btnRutas.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RutasActivity.class))
        );

        btnRecolecciones.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MisRecoleccionesActivity.class))
        );

        btnSeguimiento.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SeguimientoActivity.class))
        );
    }
}
