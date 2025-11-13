package co.edu.unipiloto.proyectoenvio;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.ValueFormatter;

import android.content.SharedPreferences;
import java.util.*;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class EstadisticasActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    TextView tvTitulo, tvContenido;
    BarChart barChart, barChartCalificaciones;
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
        barChartCalificaciones = findViewById(R.id.barChartCalificaciones);
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
        barChartCalificaciones.setVisibility(View.GONE);

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

        double promedioCalificacion = dbHelper.obtenerPromedioCalificacionPorUsuario(usuario);
        sb.append("Calificación promedio de tus encomiendas: \n")
                .append(String.format("%.1f ★", promedioCalificacion))
                .append("\n\n");

        cursor.close();

        pieChart.setVisibility(android.view.View.VISIBLE);
        mostrarPieChart(pieChart, entregadas, pendientes);
    }

    // 🟡 RECOLECTOR
    private void mostrarEstadisticasRecolector(String usuario, StringBuilder sb) {
        Cursor cursor = dbHelper.getEncomiendasAsignadasARecolector(usuario);
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
        double promedioCalificacion = dbHelper.obtenerPromedioCalificacionPorRecolector(usuario);
        sb.append("Calificación promedio de tus entregas: ")
                .append(String.format("%.1f ★", promedioCalificacion))
                .append("\n\n");

        Map<Integer, Integer> distribucion = dbHelper.obtenerDistribucionCalificacionesPorRecolector(usuario);
        if (!distribucion.isEmpty()) {
            barChart.setVisibility(android.view.View.VISIBLE);
            mostrarBarChartCalificaciones(barChart, distribucion, "Calificaciones de tus entregas");
        }

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

        double promedioGlobal = dbHelper.obtenerPromedioCalificacionGlobal();
        sb.append("Calificación promedio global del servicio: ")
                .append(String.format("%.1f ★", promedioGlobal))
                .append("\n");

        Map<Integer, Integer> distribucion = dbHelper.obtenerDistribucionCalificaciones();

        if (!distribucion.isEmpty()) {
            barChartCalificaciones.setVisibility(android.view.View.VISIBLE);
            mostrarBarChartCalificaciones(barChartCalificaciones, distribucion, "Distribución de calificaciones globales");
        }

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

        // Colores dinámicos según modo oscuro/claro
        int textColor = (getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES
                ? android.graphics.Color.WHITE
                : android.graphics.Color.BLACK;

        dataSet.setValueTextColor(textColor);

        // Configurar ejes
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(textColor);
        xAxis.setPosition(XAxis.XAxisPosition.TOP); // 🔼 etiquetas arriba
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // 🔹 evita etiquetas duplicadas
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(etiquetas.size()); // 🔹 una etiqueta por barra
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < etiquetas.size()) {
                    return etiquetas.get((int) value);
                } else {
                    return "";
                }
            }
        });

        chart.getAxisLeft().setTextColor(textColor);
        chart.getAxisRight().setTextColor(textColor);
        chart.getLegend().setTextColor(textColor);

        // Configurar datos y estilo del gráfico
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);
        chart.setData(data);

        chart.setFitBars(true);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
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

    private void mostrarBarChartCalificaciones(BarChart chart, Map<Integer, Integer> datos, String titulo) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        int i = 0;

        for (int calif = 1; calif <= 5; calif++) {
            int cantidad = datos.getOrDefault(calif, 0);
            entries.add(new BarEntry(i, cantidad));
            etiquetas.add(calif + "★");
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, titulo);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

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

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(4.5f);

        // ✅ AQUÍ el cambio importante
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < etiquetas.size()) {
                    return etiquetas.get(index);
                }
                return "";
            }
        });

        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.animateY(1500);
        chart.invalidate();
    }


}

