package co.edu.unipiloto.proyectoenvio;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EnvioActivity extends AppCompatActivity {

    EditText edtRemitente, edtDireccion, edtCelular;
    Button btnRegistrarEnvio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio);

        edtRemitente = findViewById(R.id.edtRemitente);
        edtDireccion = findViewById(R.id.edtDireccion);
        edtCelular = findViewById(R.id.edtCelularRemitente);
        btnRegistrarEnvio = findViewById(R.id.btnRegistrarEnvio);

        btnRegistrarEnvio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarEnvio();
            }
        });
    }

    private void validarEnvio() {
        String nombre = edtRemitente.getText().toString().trim();
        String direccion = edtDireccion.getText().toString().trim();
        String celular = edtCelular.getText().toString().trim();

        if (!nombre.matches("[a-zA-Z ]+")) {
            Toast.makeText(this, "El nombre solo debe contener letras", Toast.LENGTH_SHORT).show();
            return;
        }
        if (direccion.isEmpty()) {
            Toast.makeText(this, "Ingrese la dirección", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!celular.matches("\\d{10}")) {
            Toast.makeText(this, "Ingrese un celular válido (10 dígitos)", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Envío registrado ✅", Toast.LENGTH_SHORT).show();
    }
}

