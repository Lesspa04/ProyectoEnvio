package co.edu.unipiloto.proyectoenvio;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class RutasActivity extends AppCompatActivity {

    private MapView mapView;
    private DatabaseHelper dbHelper;
    private GeoPoint ubicacionUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_rutas);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mapView = findViewById(R.id.mapSolicitadas);
        mapView.setMultiTouchControls(true);

        dbHelper = new DatabaseHelper(this);

        // --- Obtener usuario actual desde SharedPreferences ---
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String usuarioActual = prefs.getString("usuario", null);

        if (usuarioActual == null) {
            Toast.makeText(this, "Error: No se encontró la sesión de usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Obtener dirección del usuario desde BD ---
        String direccionUsuario = obtenerDireccionUsuario(usuarioActual);
        if (direccionUsuario == null || direccionUsuario.isEmpty()) {
            Toast.makeText(this, "Tu dirección no está registrada.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Convertir dirección a coordenadas ---
        ubicacionUsuario = geocode(direccionUsuario);
        if (ubicacionUsuario == null) {
            Toast.makeText(this, "No se pudo obtener tu ubicación a partir de tu dirección.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Mostrar recolecciones ---
        mostrarRecoleccionesSolicitadas(ubicacionUsuario);
    }

    private String obtenerDireccionUsuario(String usuario) {
        Cursor cursor = dbHelper.obtenerUsuario(usuario);
        if (cursor != null && cursor.moveToFirst()) {
            String direccion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIRECCION));
            cursor.close();
            return direccion;
        }
        return null;
    }

    private void mostrarRecoleccionesSolicitadas(GeoPoint origen) {
        List<Encomiendas> lista = obtenerRecoleccionesSolicitadas();

        if (lista.isEmpty()) {
            Toast.makeText(this, "No hay recolecciones solicitadas.", Toast.LENGTH_SHORT).show();
            return;
        }

        Marker marcadorOrigen = new Marker(mapView);
        marcadorOrigen.setPosition(origen);
        marcadorOrigen.setTitle("Tu ubicación de origen");
        marcadorOrigen.setSnippet("Esta es tu dirección registrada.");
        marcadorOrigen.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        Drawable iconoAzul = ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.marker_default);
        if (iconoAzul != null) {
            iconoAzul = DrawableCompat.wrap(iconoAzul);
            DrawableCompat.setTint(iconoAzul, ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            marcadorOrigen.setIcon(iconoAzul);
        }

        marcadorOrigen.setAlpha(0.95f);
        mapView.getOverlays().add(marcadorOrigen);


        List<GeoPoint> puntos = new ArrayList<>();
        puntos.add(origen); // Punto inicial: dirección del usuario

        for (Encomiendas e : lista) {
            GeoPoint punto = geocode(e.getRemitenteDireccion());
            if (punto != null) {
                Marker marker = new Marker(mapView);
                marker.setPosition(punto);
                marker.setTitle("Guía: " + e.getNumeroGuia());

                marker.setOnMarkerClickListener((m, mv) -> {
                    Intent intent = new Intent(RutasActivity.this, RecoleccionDetalleActivity.class);
                    intent.putExtra("guia", e.getNumeroGuia());
                    startActivity(intent);
                    return true;
                });

                mapView.getOverlays().add(marker);
                puntos.add(punto);
            }
        }

        if (puntos.size() > 1) {
            new ObtenerRutaTask().execute(puntos.toArray(new GeoPoint[0]));
        }

        mapView.getController().setZoom(12.0);
        mapView.getController().setCenter(origen);
    }

    private List<Encomiendas> obtenerRecoleccionesSolicitadas() {
        List<Encomiendas> lista = new ArrayList<>();
        Cursor cursor = dbHelper.getEncomiendasPorEstado("SOLICITADO");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                lista.add(Encomiendas.fromCursor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return lista;
    }

    private GeoPoint geocode(String direccion) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(direccion, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                return new GeoPoint(addr.getLatitude(), addr.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== Tarea asíncrona para obtener y dibujar la ruta óptima =====
    private class ObtenerRutaTask extends AsyncTask<GeoPoint, Void, Polyline> {
        @Override
        protected Polyline doInBackground(GeoPoint... puntos) {
            try {
                StringBuilder urlBuilder = new StringBuilder("https://router.project-osrm.org/route/v1/driving/");
                for (int i = 0; i < puntos.length; i++) {
                    GeoPoint p = puntos[i];
                    urlBuilder.append(p.getLongitude()).append(",").append(p.getLatitude());
                    if (i < puntos.length - 1) urlBuilder.append(";");
                }
                urlBuilder.append("?overview=full&geometries=geojson&steps=false");

                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                JSONArray coordinates = json.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates");

                Polyline ruta = new Polyline();
                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray coord = coordinates.getJSONArray(i);
                    ruta.addPoint(new GeoPoint(coord.getDouble(1), coord.getDouble(0)));
                }
                ruta.setWidth(6f);
                return ruta;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Polyline ruta) {
            if (ruta != null) {
                mapView.getOverlays().add(ruta);
                mapView.invalidate();
            } else {
                Toast.makeText(RutasActivity.this, "No se pudo calcular la ruta óptima.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
