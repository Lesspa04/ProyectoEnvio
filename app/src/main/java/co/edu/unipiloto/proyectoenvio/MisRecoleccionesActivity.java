package co.edu.unipiloto.proyectoenvio;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.edu.unipiloto.proyectoenvio.database.FakeBackend;

public class MisRecoleccionesActivity extends AppCompatActivity {

    RecyclerView rvRecolecciones;
    RecoleccionesAdapter adapter;

    // Simulamos que este recolector tiene id "R1"
    private final String RECOLECTOR_ID = "R1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_recolecciones);

        rvRecolecciones = findViewById(R.id.rvRecolecciones);
        rvRecolecciones.setLayoutManager(new LinearLayoutManager(this));

        List<Encomiendas> lista = FakeBackend.getInstance().getEncomiendasPorRecolector(RECOLECTOR_ID);
        adapter = new RecoleccionesAdapter(lista, new RecoleccionesAdapter.OnItemClick() {
            @Override
            public void onClick(Encomiendas e) {
                // Abrir detalle
                Intent i = new Intent(MisRecoleccionesActivity.this, RecoleccionDetalleActivity.class);
                i.putExtra("guia", e.getNumeroGuia());
                startActivity(i);
            }
        });
        rvRecolecciones.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}
