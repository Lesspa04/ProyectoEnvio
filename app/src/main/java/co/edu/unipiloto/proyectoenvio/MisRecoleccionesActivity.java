package co.edu.unipiloto.proyectoenvio;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MisRecoleccionesActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnMarcarRecogido;
    private ArrayList<String> listaEnvios;
    private ArrayList<String> listaTrackings;
    private ArrayAdapter<String> adapter;
    private String trackingSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_recolecciones);

        listView = findViewById(R.id.listRecolecciones);
        btnMarcarRecogido = findViewById(R.id.btnMarcarRecogido);

        listaEnvios = new ArrayList<>();
        listaTrackings = new ArrayList<>();

        cargarEnviosAsignados();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            trackingSeleccionado = listaTrackings.get(position);
            Toast.makeText(this, "Seleccionaste: " + trackingSeleccionado, Toast.LENGTH_SHORT).show();
        });

        btnMarcarRecogido.setOnClickListener(v -> marcarComoRecogido());
    }

    private void cargarEnviosAsignados() {
        DBHelper db = new DBHelper(this);
        Cursor c = db.getEnvioByTracking("%"); // consulta general, puedes crear un método específico

        // 🚨 Mejor opción: crea un método en DBHelper que liste envíos con estado "Asignado".
        Cursor cursor = db.getReadableDatabase().query("envio", null,
                "estado=?", new String[]{"Asignado"}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String tracking = cursor.getString(cursor.getColumnIndexOrThrow("tracking"));
                String direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion_destino"));
                listaTrackings.add(tracking);
                listaEnvios.add("Guía: " + tracking + "\nDirección: " + direccion);
            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaEnvios);
        listView.setAdapter(adapter);
    }

    private void marcarComoRecogido() {
        if (trackingSeleccionado == null) {
            Toast.makeText(this, "Selecciona un envío primero", Toast.LENGTH_SHORT).show();
            return;
        }

        DBHelper db = new DBHelper(this);
        int updated = db.updateEnvioEstadoByTracking(trackingSeleccionado, "Recogida");
        if (updated > 0) {
            Toast.makeText(this, "Envío " + trackingSeleccionado + " marcado como recogido", Toast.LENGTH_SHORT).show();
            listaEnvios.clear();
            listaTrackings.clear();
            cargarEnviosAsignados();
            adapter.notifyDataSetChanged();
        }
    }
}
