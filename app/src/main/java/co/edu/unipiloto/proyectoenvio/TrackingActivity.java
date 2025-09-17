package co.edu.unipiloto.proyectoenvio;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TrackingActivity extends AppCompatActivity {

    private EditText edtTrackingConsulta;
    private Button btnConsultar;
    private TextView txtResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        edtTrackingConsulta = findViewById(R.id.edtTrackingConsulta);
        btnConsultar = findViewById(R.id.btnConsultarTracking);
        txtResultado = findViewById(R.id.txtResultadoTracking);

        btnConsultar.setOnClickListener(v -> consultarEnvio());
    }

    private void consultarEnvio() {
        String tracking = edtTrackingConsulta.getText().toString().trim();

        if (tracking.isEmpty()) {
            Toast.makeText(this, "Ingresa un número de guía", Toast.LENGTH_SHORT).show();
            return;
        }

        DBHelper db = new DBHelper(this);
        try (Cursor c = db.getEnvioByTracking(tracking)) {
            if (c != null && c.moveToFirst()) {
                String estado = c.getString(c.getColumnIndexOrThrow("estado"));
                long fecha = c.getLong(c.getColumnIndexOrThrow("fecha_solicitud"));
                double precio = c.getDouble(c.getColumnIndexOrThrow("precio"));

                txtResultado.setText("Estado: " + estado
                        + "\nFecha solicitud: " + fecha
                        + "\nPrecio: $" + precio);
            } else {
                Toast.makeText(this, "No se encontró el envío", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
