package co.edu.unipiloto.proyectoenvio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EnvioActivity extends AppCompatActivity {

    EditText edtEmailRemitente, edtDestinatarioNombre, edtDestinatarioDireccion, edtDestinatarioCelular;
    Button btnRegistrarEnvio, btnDescargarPDF;
    TextView txtComprobante;
    String comprobanteTexto = "";

    // Variables para guardar info del envío recién creado
    long envioId = -1;
    String trackingGenerado = "";
    double precioEnvio = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio);

        edtEmailRemitente = findViewById(R.id.edtEmailRemitente);
        edtDestinatarioNombre = findViewById(R.id.edtDestinatarioNombre);
        edtDestinatarioDireccion = findViewById(R.id.edtDestinatarioDireccion);
        edtDestinatarioCelular = findViewById(R.id.edtDestinatarioCelular);
        btnRegistrarEnvio = findViewById(R.id.btnRegistrarEnvio);
        txtComprobante = findViewById(R.id.txtComprobante);
        btnDescargarPDF = findViewById(R.id.btnDescargarPDF);

        btnRegistrarEnvio.setOnClickListener(v -> registrarEnvio());
        btnDescargarPDF.setOnClickListener(v -> generarPDF());
    }

    private void registrarEnvio() {
        String emailRemitente = edtEmailRemitente.getText().toString().trim();
        String nombreDestinatario = edtDestinatarioNombre.getText().toString().trim();
        String direccionDestinatario = edtDestinatarioDireccion.getText().toString().trim();
        String celularDestinatario = edtDestinatarioCelular.getText().toString().trim();

        // Validar campos
        if (emailRemitente.isEmpty() || nombreDestinatario.isEmpty() ||
                direccionDestinatario.isEmpty() || celularDestinatario.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        DBHelper db = new DBHelper(this);
        long remitenteId = db.getRemitenteIdByEmail(emailRemitente);

        if (remitenteId <= 0) {
            Toast.makeText(this, "Registra el remitente antes de solicitar recolección", Toast.LENGTH_LONG).show();
            return;
        }

        // Crear destinatario
        long destinatarioId = db.insertDestinatario(nombreDestinatario, celularDestinatario, direccionDestinatario);

        // Crear envío
        trackingGenerado = "ENV" + System.currentTimeMillis();
        long now = System.currentTimeMillis();
        double peso = 1.5; // ejemplo
        precioEnvio = calcularPrecioSegunPeso(peso);

        envioId = db.insertEnvio(trackingGenerado, remitenteId, destinatarioId,
                direccionDestinatario, peso, "Solicitado", now, precioEnvio, null);

        if (envioId > 0) {
            comprobanteTexto = "Comprobante de Envío\n\n"
                    + "Número de guía: " + trackingGenerado + "\n"
                    + "Remitente: " + emailRemitente + "\n"
                    + "Destinatario: " + nombreDestinatario + "\n"
                    + "Dirección: " + direccionDestinatario + "\n"
                    + "Celular: " + celularDestinatario + "\n"
                    + "Precio: $" + precioEnvio + "\n";

            txtComprobante.setText(comprobanteTexto);
            txtComprobante.setVisibility(View.VISIBLE);
            btnDescargarPDF.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Solicitud creada. Guía: " + trackingGenerado, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error creando la solicitud", Toast.LENGTH_SHORT).show();
        }
    }

    private double calcularPrecioSegunPeso(double peso) {
        double precioBase = 10.0;
        double precioPorKilo = 2.0;
        return precioBase + (peso * precioPorKilo);
    }

    private void generarPDF() {
        if (comprobanteTexto.isEmpty() || envioId == -1) {
            Toast.makeText(this, "No hay comprobante generado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2001);
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        page.getCanvas().drawText(comprobanteTexto, 10, 25, new android.graphics.Paint());
        pdfDocument.finishPage(page);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "ComprobanteEnvio_" + trackingGenerado + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));

            // Guardar ruta PDF en BD
            DBHelper db = new DBHelper(this);
            db.updateEnvioEstadoByTracking(trackingGenerado, "Solicitado"); // estado no cambia, pero actualizamos pdf_path
            Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar PDF", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }
}

