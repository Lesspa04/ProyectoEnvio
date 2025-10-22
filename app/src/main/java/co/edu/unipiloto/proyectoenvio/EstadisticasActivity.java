package co.edu.unipiloto.proyectoenvio;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.ValueFormatter;

import android.content.SharedPreferences;
import java.util.*;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class EstadisticasActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    TextView tvTitulo, tvContenido;
    BarChart barChart;
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        dbHelper = new DatabaseHelper(this);

        tvTitulo = findViewById(R.id.tvTituloEstadisticas);
        tvContenido = findViewById(R.id.tvContenidoEstadisticas);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String usuario = prefs.getString("usuario", null);
        String rol = dbHelper.obtenerRolUsuario(usuario);

        if (rol == null) rol = "ciudadano";

        mostrarEstadisticasPorRol(usuario, rol);
    }

    private void mostrarEstadisticasPorRol(String usuario, String rol) {
        StringBuilder sb = new StringBuilder();

        barChart.setVisibility(android.view.View.GONE);
        pieChart.setVisibility(android.view.View.GONE);

        switch (rol.toLowerCase()) {
            case "ciudadano":
                tvTitulo.setText("Estadísticas del usuario");
                mostrarEstadisticasCiudadano(usuario, sb);
                break;

            case "recolector de encomiendas":
                tvTitulo.setText("Estadísticas del recolector");
                mostrarEstadisticasRecolector(usuario, sb);
                break;

            case "asignador de rutas":
                tvTitulo.setText("Estadísticas del asignador de rutas");
                mostrarEstadisticasAsignador(usuario, sb);
                break;
        }

        tvContenido.setText(sb.toString());
    }

    // 🟢 CIUDADANO
    private void mostrarEstadisticasCiudadano(String usuario, StringBuilder sb) {
        Cursor cursor = dbHelper.getEncomiendasPorUsuario(usuario);
        int total = cursor.getCount();
        int entregadas = 0, pendientes = 0;
        double gastoTotal = 0;

        HashMap<String, Integer> barrios = new HashMap<>();

        while (cursor.moveToNext()) {
            String estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ESTADO));
            double precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO));
            gastoTotal += precio;
            if (estado.equalsIgnoreCase("entregado")) entregadas++;
            else pendientes++;
        }

        double gastoPromedio = total > 0 ? gastoTotal / total : 0;

        sb.append("Total de encomiendas: ").append(total).append("\n");
        sb.append("Entregadas: ").append(entregadas).append("\n");
        sb.append("Pendientes: ").append(pendientes).append("\n");
        sb.append("Gasto total: $").append(String.format("%.2f", gastoTotal)).append("\n");
        sb.append("Gasto promedio por envío: $").append(String.format("%.2f", gastoPromedio)).append("\n\n");

        cursor.close();

        pieChart.setVisibility(android.view.View.VISIBLE);
        mostrarPieChart(pieChart, entregadas, pendientes);
    }

    // 🟡 RECOLECTOR
    private void mostrarEstadisticasRecolector(String usuario, StringBuilder sb) {
        Cursor cursor = dbHelper.getTodasLasEncomiendas();
        int entregadas = 0, pendientes = 0;

        while (cursor.moveToNext()) {
            String estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ESTADO));

            if (estado != null) {
                if (estado.equalsIgnoreCase("entregado")) entregadas++;
                else pendientes++;
            }
        }
        cursor.close();

        sb.append("Encomiendas entregadas: ").append(entregadas).append("\n");
        sb.append("Encomiendas pendientes: ").append(pendientes).append("\n");
        sb.append("Promedio semanal: ").append(String.format("%.2f", entregadas / 7.0)).append(" entregas\n");

        pieChart.setVisibility(android.view.View.VISIBLE);
        mostrarPieChart(pieChart, entregadas, pendientes);

    }


    // 🔵 ASIGNADOR DE RUTAS
    private void mostrarEstadisticasAsignador(String usuario, StringBuilder sb) {
        int numUsuarios = dbHelper.contarUsuariosPorRol("ciudadano");
        int numRecolectores = dbHelper.contarUsuariosPorRol("recolector de encomiendas");

        Cursor cursor = dbHelper.getTodasLasEncomiendas();
        int pendientes = 0, entregadas = 0;

        while (cursor.moveToNext()) {
            String estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ESTADO));
            if (estado.equalsIgnoreCase("entregado")) entregadas++;
            else pendientes++;
        }
        cursor.close();

        sb.append("Usuarios registrados: ").append(numUsuarios).append("\n");
        sb.append("Recolectores registrados: ").append(numRecolectores).append("\n");
        sb.append("Encomiendas pendientes: ").append(pendientes).append("\n");
        sb.append("Encomiendas entregadas: ").append(entregadas).append("\n");

        // Construir datos para el gráfico de barras
        HashMap<String, Integer> datos = new HashMap<>();
        datos.put("Usuarios", numUsuarios);
        datos.put("Recolectores", numRecolectores);
        datos.put("Pendientes", pendientes);
        datos.put("Entregadas", entregadas);

        barChart.setVisibility(android.view.View.VISIBLE);
        mostrarBarChart(barChart, datos, "Estadísticas generales");
    }

    // ==================== GRÁFICAS ====================
    private void mostrarBarChart(BarChart chart, Map<String, Integer> datos, String titulo) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : datos.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue()));
            etiquetas.add(entry.getKey());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, titulo);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        // Ajustar color del texto según modo
        int textColor = (getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES
                ? android.graphics.Color.WHITE
                : android.graphics.Color.BLACK;
        dataSet.setValueTextColor(textColor);
        chart.getXAxis().setTextColor(textColor);
        chart.getAxisLeft().setTextColor(textColor);
        chart.getAxisRight().setTextColor(textColor);
        chart.getLegend().setTextColor(textColor);

        BarData data = new BarData(dataSet);
        chart.setData(data);
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (etiquetas.size() > 0)
                    return etiquetas.get((int) (value % etiquetas.size()));
                else
                    return "";
            }
        });
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.animateY(1500);
        chart.invalidate();
    }

    private void mostrarPieChart(PieChart chart, int entregadas, int pendientes) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(entregadas, "Entregadas"));
        entries.add(new PieEntry(pendientes, "Pendientes"));

        PieDataSet dataSet = new PieDataSet(entries, "Estado de encomiendas");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);

        int textColor = (getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES
                ? android.graphics.Color.WHITE
                : android.graphics.Color.BLACK;
        dataSet.setValueTextColor(textColor);
        chart.getLegend().setTextColor(textColor);
        chart.setEntryLabelColor(textColor);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.animateY(1500);
        chart.invalidate();
    }
}

