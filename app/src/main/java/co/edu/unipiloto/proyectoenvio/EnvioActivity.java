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
import java.util.Random;

public class EnvioActivity extends AppCompatActivity {

    EditText edtRemitente, edtDireccion, edtCelular;
    Button btnRegistrarEnvio, btnDescargarPDF;
    TextView txtComprobante;
    String comprobanteTexto = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio);

        edtRemitente = findViewById(R.id.edtRemitente);
        edtDireccion = findViewById(R.id.edtDireccion);
        edtCelular = findViewById(R.id.edtCelularRemitente);
        btnRegistrarEnvio = findViewById(R.id.btnRegistrarEnvio);
        txtComprobante = findViewById(R.id.txtComprobante);
        btnDescargarPDF = findViewById(R.id.btnDescargarPDF);

        btnRegistrarEnvio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarEnvio();
            }
        });

        btnDescargarPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generarPDF();
            }
        });
    }

    private void validarEnvio() {
        String nombre = edtRemitente.getText().toString().trim();
        String direccion = edtDireccion.getText().toString().trim();
        String celular = edtCelular.getText().toString().trim();

        if (!nombre.matches("[a-zA-Z ]+")) {
            Toast.makeText(this, "El nombre solo debe contener letras", Toast.LENGTH_SHORT).show();
            return;
        }
        if (direccion.isEmpty()) {
            Toast.makeText(this, "Ingrese la dirección", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!celular.matches("\\d{10}")) {
            Toast.makeText(this, "Ingrese un celular válido (10 dígitos)", Toast.LENGTH_SHORT).show();
            return;
        }

        int guia = new Random().nextInt(900000) + 100000;

        comprobanteTexto = "Comprobante de Envío\n\n"
                + "Número de guía: " + guia + "\n"
                + "Remitente: " + nombre + "\n"
                + "Dirección: " + direccion + "\n"
                + "Celular: " + celular + "\n";

        txtComprobante.setText(comprobanteTexto);
        txtComprobante.setVisibility(View.VISIBLE);
        btnDescargarPDF.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Envío registrado", Toast.LENGTH_SHORT).show();
    }

    private void generarPDF() {
        if (comprobanteTexto.isEmpty()) {
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
                "ComprobanteEnvio.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar PDF", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }
}


