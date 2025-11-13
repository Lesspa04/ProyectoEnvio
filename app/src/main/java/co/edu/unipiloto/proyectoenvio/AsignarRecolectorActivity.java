package co.edu.unipiloto.proyectoenvio;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Map;
import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class AsignarRecolectorActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout layoutEncomiendas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_recolector);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        dbHelper = new DatabaseHelper(this);
        layoutEncomiendas = findViewById(R.id.layoutEncomiendas);

        mostrarEncomiendasSinAsignar();
    }

    private void mostrarEncomiendasSinAsignar() {
        layoutEncomiendas.removeAllViews();
        List<Map<String, String>> encomiendas = dbHelper.obtenerEncomiendasSinRecolector();
        List<String> recolectores = dbHelper.obtenerRecolectores();

        if (encomiendas.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No hay encomiendas pendientes de asignar ✅");
            layoutEncomiendas.addView(tv);
            return;
        }

        for (Map<String, String> e : encomiendas) {
            View card = getLayoutInflater().inflate(R.layout.item_encomienda_asignar, null);
            TextView txtGuia = card.findViewById(R.id.txtGuia);
            TextView txtDestino = card.findViewById(R.id.txtDestino);
            TextView txtRemitente = card.findViewById(R.id.txtRemitente);
            Spinner spinnerRecolector = card.findViewById(R.id.spinnerRecolector);
            Button btnAsignar = card.findViewById(R.id.btnAsignar);

            // Mostrar la información con las claves reales del método corregido
            txtGuia.setText("Guía: " + e.get("guia"));
            txtDestino.setText("Destino: " + e.get("direccion_destinatario"));
            txtRemitente.setText("Remitente: " + e.get("usuario_remitente"));

            // Configurar spinner con los recolectores disponibles
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, recolectores);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRecolector.setAdapter(adapter);

            // Asignar evento de clic
            btnAsignar.setOnClickListener(v -> {
                String seleccionado = spinnerRecolector.getSelectedItem().toString();
                String guia = e.get("guia");

                dbHelper.asignarRecolectorAEncomienda(guia, seleccionado);
                Toast.makeText(this, "Encomienda #" + guia + " asignada a " + seleccionado, Toast.LENGTH_SHORT).show();

                mostrarEncomiendasSinAsignar(); // refrescar lista
            });

            layoutEncomiendas.addView(card);
        }
    }
}
