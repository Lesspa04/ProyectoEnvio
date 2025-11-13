package co.edu.unipiloto.proyectoenvio;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;
import co.edu.unipiloto.proyectoenvio.services.DistanciaService;

public class RutasActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private MapView mapView;
    private DatabaseHelper dbHelper;
    private GeoPoint ubicacionUsuario;

    private DistanciaService distanciaService;
    private boolean bound = false;

    private LinearLayout layoutDistancias;
    private Button btnMostrarDistancias;

    private FusedLocationProviderClient fusedLocationClient;
    private List<Encomiendas> listaRecolecciones = new ArrayList<>();

    // Conexión al BoundService
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DistanciaService.LocalBinder binder = (DistanciaService.LocalBinder) service;
            distanciaService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, DistanciaService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }

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

        layoutDistancias = findViewById(R.id.layoutDistancias);
        btnMostrarDistancias = findViewById(R.id.btnMostrarDistancias);

        dbHelper = new DatabaseHelper(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String usuarioActual = prefs.getString("usuario", null);
        String rolUsuario = dbHelper.obtenerRolUsuario(usuarioActual);

        if (usuarioActual == null) {
            Toast.makeText(this, "Error: No se encontró la sesión de usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pedimos/obtenemos la ubicación actual del dispositivo. Si no hay permiso o no se pudo obtener,
        // hacemos fallback a la dirección registrada en la BD (como tú tenías antes).
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActualYMostrar(usuarioActual, rolUsuario);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Botón para mostrar distancias únicas
        btnMostrarDistancias.setOnClickListener(v -> {
            if (bound && distanciaService != null) {
                mostrarDistanciasUnicas(listaRecolecciones);
            } else {
                Toast.makeText(this, "Servicio de distancias no disponible.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void obtenerUbicacionActualYMostrar(String usuarioActual, String rolUsuario) {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            ubicacionUsuario = new GeoPoint(location.getLatitude(), location.getLongitude());
                            // Llamamos al método que dibuja en el mapa y obtiene la lista
                            listaRecolecciones = mostrarRecoleccionesSolicitadas(ubicacionUsuario, usuarioActual, rolUsuario);
                        } else {
                            // Si no hay lastLocation disponible, fallback a geocode de la dirección en BD
                            String direccionUsuario = obtenerDireccionUsuario(usuarioActual);
                            if (direccionUsuario == null || direccionUsuario.isEmpty()) {
                                Toast.makeText(this, "No se pudo obtener la ubicación actual ni la dirección registrada.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ubicacionUsuario = geocode(direccionUsuario);
                            if (ubicacionUsuario == null) {
                                Toast.makeText(this, "No se pudo obtener tu ubicación a partir de tu dirección.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            listaRecolecciones = mostrarRecoleccionesSolicitadas(ubicacionUsuario, usuarioActual, rolUsuario);
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        // fallback si ocurre un error
                        String direccionUsuario = obtenerDireccionUsuario(usuarioActual);
                        if (direccionUsuario == null || direccionUsuario.isEmpty()) {
                            Toast.makeText(this, "No se pudo obtener la ubicación actual ni la dirección registrada.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ubicacionUsuario = geocode(direccionUsuario);
                        if (ubicacionUsuario == null) {
                            Toast.makeText(this, "No se pudo obtener tu ubicación a partir de tu dirección.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        listaRecolecciones = mostrarRecoleccionesSolicitadas(ubicacionUsuario, usuarioActual, rolUsuario);
                    });
        } catch (SecurityException ex) {
            ex.printStackTrace();
            // fallback geocode
            String direccionUsuario = obtenerDireccionUsuario(usuarioActual);
            if (direccionUsuario == null || direccionUsuario.isEmpty()) {
                Toast.makeText(this, "No se pudo obtener la ubicación actual ni la dirección registrada.", Toast.LENGTH_SHORT).show();
                return;
            }
            ubicacionUsuario = geocode(direccionUsuario);
            if (ubicacionUsuario == null) {
                Toast.makeText(this, "No se pudo obtener tu ubicación a partir de tu dirección.", Toast.LENGTH_SHORT).show();
                return;
            }
            listaRecolecciones = mostrarRecoleccionesSolicitadas(ubicacionUsuario, usuarioActual, rolUsuario);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
                String usuarioActual = prefs.getString("usuario", null);
                String rolUsuario = dbHelper.obtenerRolUsuario(usuarioActual);
                obtenerUbicacionActualYMostrar(usuarioActual, rolUsuario);
            } else {
                // Usuario denegó permiso: fallback a la dirección en BD
                SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
                String usuarioActual = prefs.getString("usuario", null);
                String direccionUsuario = obtenerDireccionUsuario(usuarioActual);
                if (direccionUsuario == null || direccionUsuario.isEmpty()) {
                    Toast.makeText(this, "Permiso denegado y no hay dirección registrada.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ubicacionUsuario = geocode(direccionUsuario);
                if (ubicacionUsuario == null) {
                    Toast.makeText(this, "No se pudo obtener la ubicación a partir de la dirección registrada.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String rolUsuario = dbHelper.obtenerRolUsuario(usuarioActual);
                listaRecolecciones = mostrarRecoleccionesSolicitadas(ubicacionUsuario, usuarioActual, rolUsuario);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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

    private List<Encomiendas> mostrarRecoleccionesSolicitadas(GeoPoint origen, String usuarioActual, String rolUsuario) {
        List<Encomiendas> lista;

        if ("asignador de rutas".equalsIgnoreCase(rolUsuario)) {
            lista = obtenerRecoleccionesSolicitadas();
        } else if ("recolector de encomiendas".equalsIgnoreCase(rolUsuario)) {
            lista = obtenerEncomiendasAsignadasARecolector(usuarioActual);
        } else {
            Toast.makeText(this, "Rol no autorizado para ver rutas.", Toast.LENGTH_SHORT).show();
            return new ArrayList<>();
        }

        if (lista.isEmpty()) {
            Toast.makeText(this, "No hay recolecciones solicitadas.", Toast.LENGTH_SHORT).show();
            return lista;
        }

        // marcador de la ubicación actual (origen)
        Marker marcadorOrigen = new Marker(mapView);
        marcadorOrigen.setPosition(origen);
        marcadorOrigen.setTitle("Tu ubicación actual");
        marcadorOrigen.setSnippet("Coordenadas reales del dispositivo.");
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
        puntos.add(origen);

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

        return lista;
    }

    private void mostrarDistanciasUnicas(List<Encomiendas> lista) {
        layoutDistancias.removeAllViews();
        HashSet<String> direccionesVistas = new HashSet<>();

        for (Encomiendas e : lista) {
            String direccion = e.getRemitenteDireccion();
            if (direccion == null || direccion.isEmpty() || direccionesVistas.contains(direccion)) continue;

            GeoPoint punto = geocode(direccion);
            if (punto != null) {
                direccionesVistas.add(direccion);
                float distancia = distanciaService.calcularDistancia(ubicacionUsuario, punto);
                String texto = direccion + " → " + String.format("%.2f km", distancia / 1000f);

                TextView tv = new TextView(this);
                tv.setText(texto);
                tv.setPadding(8, 8, 8, 8);
                layoutDistancias.addView(tv);
            }
        }
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

    private List<Encomiendas> obtenerEncomiendasAsignadasARecolector(String recolectorId) {
        List<Encomiendas> lista = new ArrayList<>();
        Cursor cursor = dbHelper.getEncomiendasSolicitadasDeRecolector(recolectorId);
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
                ruta.setColor(ContextCompat.getColor(RutasActivity.this, android.R.color.holo_blue_dark));
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
