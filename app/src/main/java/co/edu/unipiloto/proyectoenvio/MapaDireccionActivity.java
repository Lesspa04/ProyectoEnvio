package co.edu.unipiloto.proyectoenvio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.events.MapEventsReceiver;

public class MapaDireccionActivity extends AppCompatActivity {

    private MapView mapView;
    private GeoPoint selectedPoint;
    private Marker marker;
    private Button btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_mapa_direccion);

        mapView = findViewById(R.id.map);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(new GeoPoint(4.60971, -74.08175));

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                if (marker != null) {
                    mapView.getOverlays().remove(marker);
                }
                marker = new Marker(mapView);
                marker.setPosition(p);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker);
                mapView.invalidate();

                selectedPoint = p;
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        mapView.getOverlays().add(eventsOverlay);

        btnConfirmar.setOnClickListener(v -> {
            if (selectedPoint != null) {
                Intent i = new Intent();
                i.putExtra("latitud", selectedPoint.getLatitude());
                i.putExtra("longitud", selectedPoint.getLongitude());
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }
}
