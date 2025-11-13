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
    private String rolUsuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_recolecciones);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        dbHelper = new DatabaseHelper(this);

        rvRecolecciones = findViewById(R.id.rvRecolecciones);
        rvRecolecciones.setLayoutManager(new LinearLayoutManager(this));

        // Obtener usuario actual y su rol desde la sesión
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioActual = prefs.getString("usuario", null);
        rolUsuario = dbHelper.obtenerRolUsuario(usuarioActual);

        // Cargar encomiendas según el rol
        List<Encomiendas> lista = cargarEncomiendasSegunRol(usuarioActual, rolUsuario);

        adapter = new RecoleccionesAdapter(lista, e -> {
            Intent i = new Intent(MisRecoleccionesActivity.this, RecoleccionDetalleActivity.class);
            i.putExtra("guia", e.getNumeroGuia());
            startActivity(i);
        });

        rvRecolecciones.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Encomiendas> lista = cargarEncomiendasSegunRol(usuarioActual, rolUsuario);
        adapter.updateData(lista);
    }

    /**
     * Carga las encomiendas dependiendo del rol del usuario.
     * - Si es ciudadano → solo las suyas
     * - Si es recolector → todas
     */
    private List<Encomiendas> cargarEncomiendasSegunRol(String usuario, String rol) {
        List<Encomiendas> lista = new ArrayList<>();
        if (usuario == null) return lista;

        Cursor cursor;

        if ("asignador de rutas".toLowerCase().equalsIgnoreCase(rol)) {
            // 🔹 Aignador ve todas las encomiendas
            cursor = dbHelper.getTodasLasEncomiendas();
        }  else if (rol.equalsIgnoreCase("recolector de encomiendas")) {
            // Recolector ve las encomiendas aignadas
            cursor = dbHelper.getEncomiendasAsignadasARecolector(usuario);
        } else {
            // 🔹 Ciudadano ve solo las suyas
            cursor = dbHelper.getEncomiendasPorUsuario(usuario);
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Encomiendas e = Encomiendas.fromCursor(cursor);
                lista.add(e);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return lista;
    }
}
