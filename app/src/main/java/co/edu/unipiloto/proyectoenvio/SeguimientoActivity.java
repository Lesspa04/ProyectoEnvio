package co.edu.unipiloto.proyectoenvio;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class SeguimientoActivity extends AppCompatActivity {

    EditText edtBuscarGuia, edtPrecioMin, edtPrecioMax, edtFechaFiltro;
    Button btnBuscarGuia, btnListarPorUsuario, btnAplicarFiltros;
    TextView tvResultado;
    Spinner spinnerEstado;
    LinearLayout layoutFiltros;

    private DatabaseHelper dbHelper;
private String usuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        dbHelper = new DatabaseHelper(this);

        edtBuscarGuia = findViewById(R.id.edtBuscarGuia);
        btnBuscarGuia = findViewById(R.id.btnBuscarGuia);
        btnListarPorUsuario = findViewById(R.id.btnListarPorUsuario);
        tvResultado = findViewById(R.id.tvResultado);
        layoutFiltros = findViewById(R.id.layoutFiltros);
        spinnerEstado = findViewById(R.id.spinnerEstado);
        edtPrecioMin = findViewById(R.id.edtPrecioMin);
        edtPrecioMax = findViewById(R.id.edtPrecioMax);
        edtFechaFiltro = findViewById(R.id.edtFechaFiltro);
        btnAplicarFiltros = findViewById(R.id.btnAplicarFiltros);

        edtFechaFiltro.setFocusable(false);
        edtFechaFiltro.setClickable(true);

        edtFechaFiltro.setOnClickListener(v -> {
            // Obtener fecha actual
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    SeguimientoActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        // Formatear fecha seleccionada (yyyy-MM-dd)
                        String fechaSeleccionada = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                        edtFechaFiltro.setText(fechaSeleccionada);
                    },
                    year, month, day
            );
            datePicker.show();
        });

        btnAplicarFiltros.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
            usuario = prefs.getString("usuario", null);
            if (usuario == null) {
                Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
                return;
            }

            String estadoSeleccionado = spinnerEstado.getSelectedItem().toString();
            if (estadoSeleccionado.equalsIgnoreCase("Todos")) {
                estadoSeleccionado = null; // para que no filtre por estado
            }

            Double precioMin = null;
            Double precioMax = null;
            try {
                if (!edtPrecioMin.getText().toString().trim().isEmpty()) {
                    precioMin = Double.parseDouble(edtPrecioMin.getText().toString());
                }
                if (!edtPrecioMax.getText().toString().trim().isEmpty()) {
                    precioMax = Double.parseDouble(edtPrecioMax.getText().toString());
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Verifica los precios ingresados", Toast.LENGTH_SHORT).show();
                return;
            }

            String fechaSeleccionada = edtFechaFiltro.getText().toString().trim();
            if (fechaSeleccionada.isEmpty()) {
                fechaSeleccionada = null;
            }

            cargarHistorial(usuario, estadoSeleccionado, precioMin, precioMax, fechaSeleccionada);
        });

        // Buscar guía específica
        btnBuscarGuia.setOnClickListener(v -> {
            String guia = edtBuscarGuia.getText().toString().trim();
            if (guia.isEmpty()) {
                Toast.makeText(this, "Ingrese número de guía", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = dbHelper.obtenerEncomiendaPorGuia(guia);
            if (cursor != null && cursor.moveToFirst()) {
                Encomiendas e = cursorToEncomienda(cursor);
                mostrarEncomienda(e);
                cursor.close();
            } else {
                tvResultado.setText("No se encontró la guía " + guia);
            }
        });

        // Listar por usuario
        btnListarPorUsuario.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
            usuario = prefs.getString("usuario", null);
            if (usuario == null) {
                Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
                return;
            }
            layoutFiltros.setVisibility(View.VISIBLE); // mostrar filtros
            cargarHistorial(usuario, null, null, null, null);
        });
        btnListarPorUsuario.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
            usuario = prefs.getString("usuario", null);
            if (usuario == null) {
                Toast.makeText(this, "No hay usuario logueado", Toast.LENGTH_SHORT).show();
                return;
            }
            layoutFiltros.setVisibility(View.VISIBLE); // mostrar filtros
            cargarHistorial(usuario, null, null, null, null);
        });
    }


    private void cargarHistorial(String usuario, String estado, Double precioMin, Double precioMax, String fechaStr) {
        Cursor cursor = dbHelper.getEncomiendasPorUsuario(usuario);
        List<Encomiendas> list = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Encomiendas e = cursorToEncomienda(cursor);

                boolean coincide = true;

                if (estado != null && !e.getEstado().name().equalsIgnoreCase(estado))
                    coincide = false;

                if (precioMin != null && e.getPrecio() < precioMin)
                    coincide = false;

                if (precioMax != null && e.getPrecio() > precioMax)
                    coincide = false;

                if (fechaStr != null && !fechaStr.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String fechaEncomienda = sdf.format(e.getFechaSolicitada());
                    if (!fechaEncomienda.equals(fechaStr))
                        coincide = false;
                }

                if (coincide) list.add(e);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (list.isEmpty()) {
            tvResultado.setText("No hay envíos con los filtros seleccionados.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Encomiendas e : list) {
                sb.append("Guía: ").append(e.getNumeroGuia())
                        .append(" | Estado: ").append(e.getEstado().name())
                        .append(" | Precio: $").append(e.getPrecio())
                        .append(" | Fecha: ").append(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(e.getFechaSolicitada()))
                        .append("\n");
            }
            tvResultado.setText(sb.toString());
        }
    }

    // Convierte una fila del cursor en objeto Encomiendas
    private Encomiendas cursorToEncomienda(Cursor cursor) {
        String guia = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GUIA));

        // Remitente
        String remitenteNombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMITENTE));
        String remitenteDireccion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIRECCION_REMITENTE_ACTUAL));
        String remitenteCelular = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CELULAR_REMITENTE));

        // Destinatario
        String destinatarioNombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESTINATARIO));
        String destinatarioDireccion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIRECCION_DESTINATARIO));
        String destinatarioCelular = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CELULAR_DESTINATARIO));

        String estadoStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ESTADO));
        Encomiendas.Estado estado = Encomiendas.Estado.valueOf(estadoStr);

        long fechaSolicitudMillis = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA_SOLICITUD)));
        long fechaEntregaMillis = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA_ENTREGA)));

        double peso = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PESO));
        double precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECIO));
        int calificacion = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION));

        String comentario = "";
        int idxComentario = cursor.getColumnIndex(DatabaseHelper.COLUMN_COMENTARIO);
        if (idxComentario != -1) {
            comentario = cursor.getString(idxComentario);
        }

        return new Encomiendas(
                guia,
                remitenteNombre,
                remitenteDireccion,
                remitenteCelular,
                destinatarioNombre,
                destinatarioDireccion,
                destinatarioCelular,
                estado,
                new Date(fechaSolicitudMillis),
                new Date(fechaEntregaMillis),
                peso,
                precio,
                calificacion,
                comentario,
                null, // ruta aún no implementada
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RECOLECTOR_ID))
        );
    }


    private void mostrarEncomienda(Encomiendas e) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String texto = "Guía: " + e.getNumeroGuia() + "\n" +
                "Estado: " + e.getEstado().name() + "\n" +
                "Remitente: " + e.getRemitenteNombre() + "\n" +
                "Destinatario: " + e.getDestinatarioNombre() + "\n" +
                "Celular Destinatario: " + e.getDestinatarioCelular() + "\n" +
                "Fecha solicitada: " + sdf.format(e.getFechaSolicitada()) + "\n" +
                "Fecha estimada entrega: " + (e.getFechaEstimadaEntrega() != null ? sdf.format(e.getFechaEstimadaEntrega()) : "N/A") + "\n";

        switch (e.getEstado()) {
            case SOLICITADO:
                texto += "\nDescripción: Su paquete está solicitado y pendiente de recogida.";
                break;
            case RECOGIDO:
                texto += "\nDescripción: Su paquete fue recogido por el recolector.";
                break;
            case EN_TRANSITO:
                texto += "\nDescripción: Su paquete está en tránsito.";
                break;
            case ENTREGADO:
                texto += "\nDescripción: Su paquete fue entregado. Se envió notificación.";
                break;
        }
        tvResultado.setText(texto);
    }
}
