package co.edu.unipiloto.proyectoenvio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class MisRecoleccionesActivity extends AppCompatActivity {

    private RecyclerView rvRecolecciones;
    private RecoleccionesAdapter adapter;
    private DatabaseHelper dbHelper;
    private String usuarioActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_recolecciones);

        dbHelper = new DatabaseHelper(this);

        rvRecolecciones = findViewById(R.id.rvRecolecciones);
        rvRecolecciones.setLayoutManager(new LinearLayoutManager(this));

        // Obtener usuario actual de la sesión
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioActual = prefs.getString("usuario", null);

        // Cargar encomiendas del usuario
        List<Encomiendas> lista = cargarEncomiendasPorUsuario(usuarioActual);

        adapter = new RecoleccionesAdapter(lista, e -> {
            // Abrir detalle de la encomienda
            Intent i = new Intent(MisRecoleccionesActivity.this, RecoleccionDetalleActivity.class);
            i.putExtra("guia", e.getNumeroGuia());
            startActivity(i);
        });

        rvRecolecciones.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar lista desde la BD
        List<Encomiendas> lista = cargarEncomiendasPorUsuario(usuarioActual);
        adapter.updateData(lista);
    }

    /**
     * Carga todas las encomiendas del usuario actual
     */
    private List<Encomiendas> cargarEncomiendasPorUsuario(String usuario) {
        List<Encomiendas> lista = new ArrayList<>();
        if (usuario == null) return lista;

        // Obtener cursor filtrando por remitente (usuario actual)
        Cursor cursor = dbHelper.getEncomiendasPorUsuario(usuario);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Reconstruye el objeto Encomiendas desde el cursor
                Encomiendas e = Encomiendas.fromCursor(cursor);
                lista.add(e);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return lista;
    }
}
