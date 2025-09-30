package co.edu.unipiloto.proyectoenvio;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class SeguimientoActivity extends AppCompatActivity {

    EditText edtBuscarGuia;
    Button btnBuscarGuia, btnListarPorUsuario;
    TextView tvResultado;

    private DatabaseHelper dbHelper;
private String usuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento);

        dbHelper = new DatabaseHelper(this);

        edtBuscarGuia = findViewById(R.id.edtBuscarGuia);
        btnBuscarGuia = findViewById(R.id.btnBuscarGuia);
        btnListarPorUsuario = findViewById(R.id.btnListarPorUsuario); // Reusa el botón
        tvResultado = findViewById(R.id.tvResultado);

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
            // Obtener usuario actual de la sesión
            SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
            usuario = prefs.getString("usuario", null);
            Cursor cursor = dbHelper.getEncomiendasPorUsuario(usuario);

            List<Encomiendas> list = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToEncomienda(cursor));
                } while (cursor.moveToNext());
                cursor.close();
            }

            if (list.isEmpty()) {
                tvResultado.setText("No hay envíos asociados al usuario " + usuario);
            } else {
                StringBuilder sb = new StringBuilder();
                for (Encomiendas e : list) {
                    sb.append(e.getNumeroGuia())
                            .append(" - ")
                            .append(e.getEstado().name())
                            .append("\n");
                }
                tvResultado.setText(sb.toString());
            }
        });
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
                null, // ruta (puedes cargar GeoPoints si tienes)
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
