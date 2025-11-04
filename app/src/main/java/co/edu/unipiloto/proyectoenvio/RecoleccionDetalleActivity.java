package co.edu.unipiloto.proyectoenvio;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class RecoleccionDetalleActivity extends AppCompatActivity {

    TextView tvGuia, tvRemitente, tvDireccionRem,
            tvDestinatario, tvDireccionDest,
            tvEstado;

    Button btnMarcarRecogida, btnMarcarEntrega, btnEmbalajeSeguro, btnEnviarCalificacion;

    MapView map;
    RatingBar ratingBar;
    MyLocationNewOverlay myLocationOverlay;
    Encomiendas encomienda;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_recoleccion_detalle);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarDetalle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        dbHelper = new DatabaseHelper(this);

        // Bind views
        tvGuia = findViewById(R.id.tvGuia);
        tvRemitente = findViewById(R.id.tvRemitente);
        tvDireccionRem = findViewById(R.id.tvDireccionRem);
        tvDestinatario = findViewById(R.id.tvDestinatario);
        tvDireccionDest = findViewById(R.id.tvDireccionDest);
        tvEstado = findViewById(R.id.tvEstado);
        btnEmbalajeSeguro = findViewById(R.id.btnEmbalajeSeguro);
        btnMarcarRecogida = findViewById(R.id.btnMarcarRecogida);
        btnMarcarEntrega = findViewById(R.id.btnMarcarEntrega);
        ratingBar = findViewById(R.id.ratingBar);
        btnEnviarCalificacion = findViewById(R.id.btnEnviarCalificacion);

        map = findViewById(R.id.mapDetalle);
        map.setMultiTouchControls(true);

        // --- 🔹 Control de botones según el rol del usuario ---
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String usuario = prefs.getString("usuario", null);
        String rolUsuario = dbHelper.obtenerRolUsuario(usuario);

// Por defecto ocultamos los botones hasta saber el rol
        btnEmbalajeSeguro.setVisibility(Button.GONE);
        btnMarcarRecogida.setVisibility(Button.GONE);
        btnMarcarEntrega.setVisibility(Button.GONE);

// Mostramos según el rol
        if (rolUsuario != null) {
            switch (rolUsuario.toLowerCase()) {
                case "recolector de encomiendas":
                    // El recolector puede embalar, marcar recogido y entregado
                    btnEmbalajeSeguro.setVisibility(Button.VISIBLE);
                    btnMarcarRecogida.setVisibility(Button.VISIBLE);
                    btnMarcarEntrega.setVisibility(Button.VISIBLE);
                    break;

                case "asignador de rutas":
                    // El asignador no debería modificar estados, solo visualizar
                    // (dejamos los botones ocultos)
                    break;

                case "ciudadano":
                default:
                    // El ciudadano no puede modificar el estado ni embalar, puede calificar
                    break;
            }
        }

        btnMarcarRecogida.setEnabled(false);
        btnMarcarEntrega.setEnabled(false);
        btnEmbalajeSeguro.setOnClickListener(v -> mostrarChecklistEmbalaje());


        // Obtener encomienda desde BD
        String guia = getIntent().getStringExtra("guia");
        encomienda = obtenerEncomiendaDesdeBD(guia);

        if (encomienda == null) {
            Toast.makeText(this, "Encomienda no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Mostrar calificación solo si la encomienda está ENTREGADA
        if (encomienda.getEstado() == Encomiendas.Estado.ENTREGADO) {
            ratingBar.setVisibility(View.VISIBLE);
            btnEnviarCalificacion.setVisibility(View.VISIBLE);

            if (encomienda.getCalificacion() > 0) {
                ratingBar.setRating(encomienda.getCalificacion());
                ratingBar.setIsIndicator(true);
                btnEnviarCalificacion.setEnabled(false);
            } else {
                btnEnviarCalificacion.setEnabled(true);
                ratingBar.setIsIndicator(false);
            }
        } else {
            ratingBar.setVisibility(View.GONE);
            btnEnviarCalificacion.setVisibility(View.GONE);
        }

        // Mostrar información en pantalla
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvGuia.setText("Guía: " + encomienda.getNumeroGuia());
        tvRemitente.setText("Remitente: " + encomienda.getRemitenteNombre());
        tvDireccionRem.setText("Dirección remitente: " + encomienda.getRemitenteDireccion());
        tvDestinatario.setText("Destinatario: " + encomienda.getDestinatarioNombre());
        tvDireccionDest.setText("Dirección destinatario: " + encomienda.getDestinatarioDireccion());
        tvEstado.setText("Estado: " + encomienda.getEstado().name());

        // Geocodificar direcciones
        GeoPoint remitentePoint = geocode(encomienda.getRemitenteDireccion());
        GeoPoint destinatarioPoint = geocode(encomienda.getDestinatarioDireccion());

        if (remitentePoint != null && destinatarioPoint != null) {
            // Marcadores
            Marker mRem = new Marker(map);
            mRem.setPosition(remitentePoint);
            mRem.setTitle("Remitente");
            map.getOverlays().add(mRem);

            Marker mDest = new Marker(map);
            mDest.setPosition(destinatarioPoint);
            mDest.setTitle("Destinatario");
            map.getOverlays().add(mDest);

            map.getController().setZoom(12.0);
            map.getController().setCenter(remitentePoint);

            // Trazar ruta real entre ambos puntos (usando OSRM)
            new ObtenerRutaTask().execute(remitentePoint, destinatarioPoint);

        } else {
            map.getController().setZoom(11.0);
            map.getController().setCenter(new GeoPoint(4.6482837, -74.0631496));
        }

        // Ubicación actual
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3001);
        }

        // Botones marcar recogida/entrega
        btnMarcarRecogida.setOnClickListener(v -> {
            dbHelper.actualizarEstadoEncomienda(encomienda.getNumeroGuia(), Encomiendas.Estado.RECOGIDO.name());
            encomienda.setEstado(Encomiendas.Estado.RECOGIDO);
            tvEstado.setText("Estado: " + encomienda.getEstado().name());
            Toast.makeText(this, "Encomienda marcada como recogida", Toast.LENGTH_SHORT).show();
        });

        btnMarcarEntrega.setOnClickListener(v -> {
            dbHelper.actualizarEstadoEncomienda(encomienda.getNumeroGuia(), Encomiendas.Estado.ENTREGADO.name());
            encomienda.setEstado(Encomiendas.Estado.ENTREGADO);
            tvEstado.setText("Estado: " + encomienda.getEstado().name());
            Toast.makeText(this, "Encomienda marcada como entregada", Toast.LENGTH_SHORT).show();
        });

        btnEnviarCalificacion.setOnClickListener(v -> {
            int rating = (int) ratingBar.getRating();
            if (rating == 0) {
                Toast.makeText(this, "Seleccione una calificación", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean guardado = dbHelper.guardarCalificacion(encomienda.getNumeroGuia(), rating);
            if (guardado) {
                encomienda.setCalificacion(rating);
                Toast.makeText(this, "¡Gracias por tu calificación!", Toast.LENGTH_SHORT).show();
                btnEnviarCalificacion.setEnabled(false);
                ratingBar.setIsIndicator(true); // Bloquea modificaciones
            } else {
                Toast.makeText(this, "Error al guardar la calificación", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void enableMyLocation() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(myLocationOverlay);
    }

    private Encomiendas obtenerEncomiendaDesdeBD(String guia) {
        Cursor cursor = dbHelper.obtenerEncomiendaPorGuia(guia);
        if (cursor != null && cursor.moveToFirst()) {
            Encomiendas e = Encomiendas.fromCursor(cursor);
            cursor.close();
            return e;
        }
        return null;
    }

    // Geocodificación de dirección a GeoPoint
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
                String urlStr = "https://router.project-osrm.org/route/v1/driving/"
                        + puntos[0].getLongitude() + "," + puntos[0].getLatitude()
                        + ";" + puntos[1].getLongitude() + "," + puntos[1].getLatitude()
                        + "?overview=full&geometries=geojson";

                URL url = new URL(urlStr);
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
                ruta.setColor(0xFF0000FF);
                return ruta;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(Polyline ruta) {
            if (ruta != null) {
                map.getOverlays().add(ruta);
                map.invalidate();
            } else {
                Toast.makeText(RecoleccionDetalleActivity.this, "No se pudo obtener la ruta.", Toast.LENGTH_SHORT).show();
            }
        }
    }

        @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 3001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalle, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ver_caracteristicas) {
            mostrarCaracteristicasEnvio(); return true;
        } return super.onOptionsItemSelected(item); }

    private void mostrarCaracteristicasEnvio() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        String mensaje = "Guía: " + encomienda.getNumeroGuia() + "\n"
                + "Celular remitente: " + encomienda.getRemitenteCelular() + "\n"
                + "Celular destinatario: " + encomienda.getDestinatarioCelular() + "\n"
                + "Peso: " + encomienda.getPeso() + " kg\n"
                + "Precio: $" + encomienda.getPrecio() + "\n"
                + "Fecha solicitud: " + sdf.format(encomienda.getFechaSolicitada()) + "\n"
                + "Fecha estimada de entrega: " + sdf.format(encomienda.getFechaEstimadaEntrega());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Características del envío")
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .show();
    }


    private void mostrarChecklistEmbalaje() {
        String[] pasos = {
                "Caja en buen estado",
                "Cinta de seguridad bien colocada",
                "Etiqueta visible y legible",
                "Protección interna adecuada"
        };

        boolean[] checkedItems = new boolean[pasos.length];

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar embalaje seguro")
                .setMultiChoiceItems(pasos, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    boolean todosMarcados = true;
                    for (boolean checked : checkedItems) {
                        if (!checked) {
                            todosMarcados = false;
                            break;
                        }
                    }
                    if (todosMarcados) {
                        Toast.makeText(this, "Embalaje confirmado", Toast.LENGTH_SHORT).show();
                        btnMarcarRecogida.setEnabled(true);
                        btnMarcarEntrega.setEnabled(true);
                    } else {
                        Toast.makeText(this, "Debe confirmar todos los pasos de embalaje", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

}
