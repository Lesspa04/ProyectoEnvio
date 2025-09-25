package co.edu.unipiloto.proyectoenvio.database;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import co.edu.unipiloto.proyectoenvio.Encomiendas;

public class FakeBackend {
    private static FakeBackend instance;
    private Map<String, Encomiendas> encomiendas;

    private FakeBackend() {
        encomiendas = new HashMap<>();
        seedSampleData();
    }

    public static synchronized FakeBackend getInstance() {
        if (instance == null) instance = new FakeBackend();
        return instance;
    }

    private void seedSampleData() {
        // Crear 3-4 encomiendas simuladas con rutas (GeoPoints)
        List<GeoPoint> ruta1 = new ArrayList<>();
        ruta1.add(new GeoPoint(4.6482837, -74.0631496)); // Chapinero
        ruta1.add(new GeoPoint(4.707982, -74.032536)); // Usaquén

        List<GeoPoint> ruta2 = new ArrayList<>();
        ruta2.add(new GeoPoint(4.744765, -74.077642)); // Suba
        ruta2.add(new GeoPoint(4.626556, -74.148291)); // Kennedy

        String r1 = generarGuia();
        String r2 = generarGuia();
        String r3 = generarGuia();

        Calendar cal = Calendar.getInstance();
        Date hoy = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        Date eta = cal.getTime();

        // Asignar recolectorId "R1" a las dos primeras, "R2" a la tercera
        encomiendas.put(r1, new Encomiendas(r1, "Juan Pérez", "Cra 15 #45-20", "3101234567",
                Encomiendas.Estado.SOLICITADO, hoy, eta, ruta1, "R1"));
        encomiendas.put(r2, new Encomiendas(r2, "María Gómez", "Cl 90 #12-34", "3209876543",
                Encomiendas.Estado.SOLICITADO, hoy, eta, ruta2, "R1"));
        // Sin ruta (todavía)
        encomiendas.put(r3, new Encomiendas(r3, "Carlos Ruiz", "Av 68 #34-56", "3115554433",
                Encomiendas.Estado.SOLICITADO, hoy, eta, null, "R2"));
    }

    private String generarGuia() {
        return "ENV-" + (new Random().nextInt(900000) + 100000);
    }

    // Obtener todas las encomiendas asignadas a un recolector
    public List<Encomiendas> getEncomiendasPorRecolector(String recolectorId) {
        List<Encomiendas> out = new ArrayList<>();
        for (Encomiendas e : encomiendas.values()) {
            if (recolectorId.equals(e.getRecolectorId())) out.add(e);
        }
        return out;
    }

    // Obtener por guia
    public Encomiendas getEncomiendaPorGuia(String guia) {
        return encomiendas.get(guia);
    }

    // Cambiar estado y persistir en memoria
    public void actualizarEstado(String guia, Encomiendas.Estado nuevoEstado) {
        Encomiendas e = encomiendas.get(guia);
        if (e != null) {
            e.setEstado(nuevoEstado);
        }
    }

    // Listado por remitente (por celular)
    public List<Encomiendas> getEncomiendasPorRemitenteCelular(String celular) {
        List<Encomiendas> out = new ArrayList<>();
        for (Encomiendas e : encomiendas.values()) {
            if (e.getRemitenteCelular().equals(celular)) out.add(e);
        }
        return out;
    }
}

