package co.edu.unipiloto.proyectoenvio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnLogin, btnEnvio, btnRutas, btnMisRecolecciones, btnPago, btnTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLoginRegister);
        btnEnvio = findViewById(R.id.btnEnvio);
        btnRutas = findViewById(R.id.btnRutas);
        btnMisRecolecciones = findViewById(R.id.btnMisRecolecciones);
        btnPago = findViewById(R.id.btnPago);
        btnTracking = findViewById(R.id.btnTracking);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginRegisterActivity.class)));

        btnEnvio.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EnvioActivity.class)));

        btnRutas.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RutasActivity.class)));

        btnMisRecolecciones.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MisRecoleccionesActivity.class)));

        btnPago.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PagoActivity.class)));

        btnTracking.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TrackingActivity.class)));
    }
}
