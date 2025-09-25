package co.edu.unipiloto.proyectoenvio;

import android.Manifest;
import android.content.pm.PackageManager;
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

import java.util.List;

import co.edu.unipiloto.proyectoenvio.database.FakeBackend;

public class RecoleccionDetalleActivity extends AppCompatActivity {

    TextView tvGuia, tvNombre, tvDireccion, tvEstado;
    Button btnMarcarRecogida, btnMarcarEntrega;
    MapView map;
    MyLocationNewOverlay myLocationOverlay;
    Encomiendas encomienda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializar osmdroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_recoleccion_detalle);

        tvGuia = findViewById(R.id.tvGuia);
        tvNombre = findViewById(R.id.tvNombre);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvEstado = findViewById(R.id.tvEstado);
        btnMarcarRecogida = findViewById(R.id.btnMarcarRecogida);
        btnMarcarEntrega = findViewById(R.id.btnMarcarEntrega);
        map = findViewById(R.id.mapDetalle);
        map.setMultiTouchControls(true);

        String guia = getIntent().getStringExtra("guia");
        encomienda = FakeBackend.getInstance().getEncomiendaPorGuia(guia);

        if (encomienda == null) {
            Toast.makeText(this, "Encomienda no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvGuia.setText("Guía: " + encomienda.getNumeroGuia());
        tvNombre.setText("Remitente: " + encomienda.getRemitenteNombre());
        tvDireccion.setText("Dirección: " + encomienda.getRemitenteDireccion());
        tvEstado.setText("Estado: " + encomienda.getEstado().name());

        // Mostrar ruta si existe
        List<GeoPoint> ruta = encomienda.getRuta();
        if (ruta != null && !ruta.isEmpty()) {
            // Centrar en el primer punto
            map.getController().setZoom(12.0);
            map.getController().setCenter(ruta.get(0));

            // Añadir markers y polyline
            for (GeoPoint p : ruta) {
                Marker m = new Marker(map);
                m.setPosition(p);
                m.setTitle("Ruta punto");
                map.getOverlays().add(m);
            }
            Polyline poly = new Polyline();
            poly.setPoints(ruta);
            poly.setWidth(6f);
            poly.setColor(0xFF0000FF);
            map.getOverlayManager().add(poly);
        } else {
            map.getController().setZoom(11.0);
            map.getController().setCenter(new GeoPoint(4.6482837, -74.0631496));
        }

        // Mostrar ubicación actual (pedir permisos si es necesario)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3001);
        }

        // Botón marcar recogida
        btnMarcarRecogida.setOnClickListener(v -> {
            FakeBackend.getInstance().actualizarEstado(encomienda.getNumeroGuia(), Encomiendas.Estado.RECOGIDO);
            tvEstado.setText("Estado: " + Encomiendas.Estado.RECOGIDO.name());
            Toast.makeText(this, "Encomienda marcada como recogida", Toast.LENGTH_SHORT).show();

        });

        // Botón marcar entrega
        btnMarcarEntrega.setOnClickListener(v -> {
            FakeBackend.getInstance().actualizarEstado(encomienda.getNumeroGuia(), Encomiendas.Estado.RECOGIDO);
            tvEstado.setText("Estado: " + Encomiendas.Estado.ENTREGADO.name());
            Toast.makeText(this, "Encomienda marcada como entregada", Toast.LENGTH_SHORT).show();

        });
    }

    private void enableMyLocation() {
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(myLocationOverlay);
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
