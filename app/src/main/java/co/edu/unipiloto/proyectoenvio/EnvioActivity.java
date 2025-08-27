package co.edu.unipiloto.proyectoenvio;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EnvioActivity extends AppCompatActivity {

    EditText etNombreRemitente, etDireccionEnvio, etDireccionDestinatario, etCelularRemitente;
    Button btnRegistrarEnvio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio);

        etNombreRemitente = findViewById(R.id.etNombreRemitente);
        etDireccionEnvio = findViewById(R.id.etDireccionEnvio);
        etDireccionDestinatario = findViewById(R.id.etDireccionDestinatario);
        etCelularRemitente = findViewById(R.id.etCelularRemitente);
        btnRegistrarEnvio = findViewById(R.id.btnRegistrarEnvio);

        btnRegistrarEnvio.setOnClickListener(v -> {
            String datos = "Envío registrado de: " + etNombreRemitente.getText().toString();
            Toast.makeText(EnvioActivity.this, datos, Toast.LENGTH_LONG).show();
        });
    }
}
