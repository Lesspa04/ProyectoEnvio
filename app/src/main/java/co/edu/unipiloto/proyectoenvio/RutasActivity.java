package co.edu.unipiloto.proyectoenvio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class RutasActivity extends AppCompatActivity {

    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_rutas);

        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);

        // Pedir permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1001);
        }

        GeoPoint chapinero = new GeoPoint(4.6482837, -74.0631496);
        GeoPoint suba = new GeoPoint(4.744765, -74.077642);
        GeoPoint usaquen = new GeoPoint(4.707982, -74.032536);
        GeoPoint kennedy = new GeoPoint(4.626556, -74.148291);

        map.getController().setZoom(11.0);
        map.getController().setCenter(chapinero);

        addMarker(chapinero, "Solicitud - Chapinero");
        addMarker(suba, "Solicitud - Suba");
        addMarker(usaquen, "Solicitud - Usaquén");
        addMarker(kennedy, "Solicitud - Kennedy");

        List<GeoPoint> points = new ArrayList<>();
        points.add(chapinero);
        points.add(usaquen);
        points.add(suba);
        points.add(kennedy);

        Polyline line = new Polyline();
        line.setPoints(points);
        line.setWidth(8f);
        line.setColor(0xFF0000FF); // azul
        map.getOverlayManager().add(line);
    }

    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setTitle(title);
        map.getOverlays().add(marker);
    }
}

