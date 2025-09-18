package co.edu.unipiloto.proyectoenvio;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SeguimientoActivity extends AppCompatActivity {

    EditText edtBuscarGuia;
    Button btnBuscarGuia, btnListarPorCelular;
    TextView tvResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento);

        edtBuscarGuia = findViewById(R.id.edtBuscarGuia);
        btnBuscarGuia = findViewById(R.id.btnBuscarGuia);
        btnListarPorCelular = findViewById(R.id.btnListarPorCelular);
        tvResultado = findViewById(R.id.tvResultado);

        btnBuscarGuia.setOnClickListener(v -> {
            String guia = edtBuscarGuia.getText().toString().trim();
            if (guia.isEmpty()) {
                Toast.makeText(this, "Ingrese número de guía", Toast.LENGTH_SHORT).show();
                return;
            }
            Encomiendas e = FakeBackend.getInstance().getEncomiendaPorGuia(guia);
            if (e == null) {
                tvResultado.setText("No se encontró la guía " + guia);
            } else {
                mostrarEncomienda(e);
            }
        });

        btnListarPorCelular.setOnClickListener(v -> {
            String celular = "3101234567";
            List<Encomiendas> list = FakeBackend.getInstance().getEncomiendasPorRemitenteCelular(celular);
            if (list.isEmpty()) {
                tvResultado.setText("No hay envíos asociados al celular " + celular);
            } else {
                StringBuilder sb = new StringBuilder();
                for (Encomiendas e : list) {
                    sb.append(e.getNumeroGuia()).append(" - ").append(e.getEstado().name()).append("\n");
                }
                tvResultado.setText(sb.toString());
            }
        });
    }

    private void mostrarEncomienda(Encomiendas e) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String texto = "Guía: " + e.getNumeroGuia() + "\n" +
                "Estado: " + e.getEstado().name() + "\n" +
                "Remitente: " + e.getRemitenteNombre() + "\n" +
                "Celular: " + e.getRemitenteCelular() + "\n" +
                "Fecha solicitada: " + sdf.format(e.getFechaSolicitada()) + "\n" +
                "Fecha estimada entrega: " + (e.getFechaEstimadaEntrega() != null ? sdf.format(e.getFechaEstimadaEntrega()) : "N/A") + "\n";

        switch (e.getEstado()) {
            case SOLICITADO:
                texto += "\nDescripción: Su paquete está solicitado y pendiente de recogida.";
                break;
            case RECOGIDO:
                texto += "\nDescripción: Su paquete fue recogido por el recolector.";
                break;
            case EN_TRANSITO:
                texto += "\nDescripción: Su paquete está en tránsito.";
                break;
            case ENTREGADO:
                texto += "\nDescripción: Su paquete fue entregado. Se envió notificación.";
                break;
        }
        tvResultado.setText(texto);
    }
}
