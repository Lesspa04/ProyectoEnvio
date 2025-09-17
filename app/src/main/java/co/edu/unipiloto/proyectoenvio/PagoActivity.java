package co.edu.unipiloto.proyectoenvio;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PagoActivity extends AppCompatActivity {

    private EditText edtTracking, edtMonto;
    private Button btnPagar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);

        edtTracking = findViewById(R.id.edtTrackingPago);
        edtMonto = findViewById(R.id.edtMontoPago);
        btnPagar = findViewById(R.id.btnPagar);

        btnPagar.setOnClickListener(v -> procesarPago());
    }

    private void procesarPago() {
        String tracking = edtTracking.getText().toString().trim();
        String montoStr = edtMonto.getText().toString().trim();

        if (tracking.isEmpty() || montoStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto = Double.parseDouble(montoStr);

        DBHelper db = new DBHelper(this);
        long envioId = -1;

        // Buscar envio por tracking
        try (var cursor = db.getEnvioByTracking(tracking)) {
            if (cursor != null && cursor.moveToFirst()) {
                envioId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            }
        }

        if (envioId == -1) {
            Toast.makeText(this, "Envío no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String comprobante = "PAY-" + System.currentTimeMillis();
        long now = System.currentTimeMillis();

        long pagoId = db.insertPago(envioId, comprobante, monto, now, "Simulado");

        if (pagoId > 0) {
            db.updateEnvioEstadoByTracking(tracking, "Pagado");
            Toast.makeText(this, "Pago exitoso. Comprobante: " + comprobante, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error al registrar pago", Toast.LENGTH_SHORT).show();
        }
    }
}
