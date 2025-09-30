package co.edu.unipiloto.proyectoenvio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    Button btnMarcarRecogida, btnMarcarEntrega;
    MapView map;
    MyLocationNewOverlay myLocationOverlay;
    Encomiendas encomienda;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_recoleccion_detalle);

        dbHelper = new DatabaseHelper(this);

        // Bind views
        tvGuia = findViewById(R.id.tvGuia);
        tvRemitente = findViewById(R.id.tvRemitente);
        tvCelularRem = findViewById(R.id.tvCelularRem);
        tvDireccionRem = findViewById(R.id.tvDireccionRem);
        tvDestinatario = findViewById(R.id.tvDestinatario);
        tvCelularDest = findViewById(R.id.tvCelularDest);
        tvDireccionDest = findViewById(R.id.tvDireccionDest);
        tvEstado = findViewById(R.id.tvEstado);
        tvPeso = findViewById(R.id.tvPeso);
        tvPrecio = findViewById(R.id.tvPrecio);
        tvFechaSolicitud = findViewById(R.id.tvFechaSolicitud);
        tvFechaEntrega = findViewById(R.id.tvFechaEntrega);

        btnMarcarRecogida = findViewById(R.id.btnMarcarRecogida);
        btnMarcarEntrega = findViewById(R.id.btnMarcarEntrega);
        map = findViewById(R.id.mapDetalle);
        map.setMultiTouchControls(true);

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
        tvCelularRem.setText("Celular: " + encomienda.getRemitenteCelular());
        tvDireccionRem.setText("Dirección: " + encomienda.getRemitenteDireccion());
        tvDestinatario.setText("Destinatario: " + encomienda.getDestinatarioNombre());
        tvCelularDest.setText("Celular: " + encomienda.getDestinatarioCelular());
        tvDireccionDest.setText("Dirección: " + encomienda.getDestinatarioDireccion());
        tvEstado.setText("Estado: " + encomienda.getEstado().name());
        tvPeso.setText("Peso: " + encomienda.getPeso() + " kg");
        tvPrecio.setText("Precio: $" + encomienda.getPrecio());
        tvFechaSolicitud.setText("Fecha solicitud: " + sdf.format(encomienda.getFechaSolicitada()));
        tvFechaEntrega.setText("Fecha estimada entrega: " + sdf.format(encomienda.getFechaEstimadaEntrega()));

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
}
