package co.edu.unipiloto.proyectoenvio.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class DistanciaService extends Service {

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public DistanciaService getService() {
            return DistanciaService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Calcula distancia en metros usando Haversine
    public float calcularDistancia(GeoPoint origen, GeoPoint destino) {
        double lat1 = Math.toRadians(origen.getLatitude());
        double lon1 = Math.toRadians(origen.getLongitude());
        double lat2 = Math.toRadians(destino.getLatitude());
        double lon2 = Math.toRadians(destino.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return (float) (6371000 * c); // metros
    }

    // Devuelve el punto más cercano de una lista
    public GeoPoint obtenerPuntoMasCercano(GeoPoint origen, List<GeoPoint> puntos) {
        GeoPoint masCercano = null;
        float minDist = Float.MAX_VALUE;
        for (GeoPoint p : puntos) {
            float dist = calcularDistancia(origen, p);
            if (dist < minDist) {
                minDist = dist;
                masCercano = p;
            }
        }
        return masCercano;
    }
}

