package co.edu.unipiloto.proyectoenvio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;


import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class RecoleccionDetalleActivity extends AppCompatActivity {

    TextView tvGuia, tvRemitente, tvCelularRem, tvDireccionRem,
            tvDestinatario, tvCelularDest, tvDireccionDest,
            tvEstado, tvPeso, tvPrecio, tvFechaSolicitud, tvFechaEntrega;

    Button btnMarcarRecogida, btnMarcarEntrega, btnEmbalajeSeguro;

    MapView map;
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
        map = findViewById(R.id.mapDetalle);
        map.setMultiTouchControls(true);

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

        // Mostrar información en pantalla
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvGuia.setText("Guía: " + encomienda.getNumeroGuia());
        tvRemitente.setText("Remitente: " + encomienda.getRemitenteNombre());
        tvDireccionRem.setText("Dirección remitente: " + encomienda.getRemitenteDireccion());
        tvDestinatario.setText("Destinatario: " + encomienda.getDestinatarioNombre());
        tvDireccionDest.setText("Dirección destinatario: " + encomienda.getDestinatarioDireccion());
        tvEstado.setText("Estado: " + encomienda.getEstado().name());

        // Geocodificar direcciones y pintar ruta
        GeoPoint remitentePoint = geocode(encomienda.getRemitenteDireccion());
        GeoPoint destinatarioPoint = geocode(encomienda.getDestinatarioDireccion());

        if (remitentePoint != null && destinatarioPoint != null) {
            List<GeoPoint> ruta = new ArrayList<>();
            ruta.add(remitentePoint);
            ruta.add(destinatarioPoint);

            map.getController().setZoom(12.0);
            map.getController().setCenter(remitentePoint);

            // Marcadores
            Marker mRem = new Marker(map);
            mRem.setPosition(remitentePoint);
            mRem.setTitle("Remitente");
            map.getOverlays().add(mRem);

            Marker mDest = new Marker(map);
            mDest.setPosition(destinatarioPoint);
            mDest.setTitle("Destinatario");
            map.getOverlays().add(mDest);

            // Línea de ruta
            Polyline poly = new Polyline();
            poly.setPoints(ruta);
            poly.setWidth(6f);
            poly.setColor(0xFF0000FF);
            map.getOverlayManager().add(poly);

        } else {
            // Centrar mapa por defecto si no se pudo geocodificar
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
