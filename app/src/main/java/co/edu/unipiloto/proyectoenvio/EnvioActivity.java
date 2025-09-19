package co.edu.unipiloto.proyectoenvio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

public class EnvioActivity extends AppCompatActivity {

    TextInputEditText edtRemitente, edtCelular, edtPeso;
    TextView tvDireccionSeleccionada, txtComprobante;
    Button btnRegistrarEnvio, btnDescargarPDF, btnSeleccionarDireccion, btnPagar;

    double latitud = 0, longitud = 0;
    String comprobanteTexto = "";

    // Nuevo: usar launcher para mapa
    private final ActivityResultLauncher<Intent> direccionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            latitud = result.getData().getDoubleExtra("latitud", 0);
                            longitud = result.getData().getDoubleExtra("longitud", 0);
                            tvDireccionSeleccionada.setText("Lat: " + latitud + ", Lon: " + longitud);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio);

        edtRemitente = findViewById(R.id.edtRemitente);
        edtCelular = findViewById(R.id.edtCelularRemitente);
        edtPeso = findViewById(R.id.edtPeso);
        tvDireccionSeleccionada = findViewById(R.id.tvDireccionSeleccionada);
        txtComprobante = findViewById(R.id.txtComprobante);

        btnRegistrarEnvio = findViewById(R.id.btnRegistrarEnvio);
        btnDescargarPDF = findViewById(R.id.btnDescargarPDF);
        btnSeleccionarDireccion = findViewById(R.id.btnSeleccionarDireccion);
        btnPagar = findViewById(R.id.btnPagar);

        btnSeleccionarDireccion.setOnClickListener(v -> {
            Intent i = new Intent(EnvioActivity.this, MapaDireccionActivity.class);
            direccionLauncher.launch(i);
        });

        btnRegistrarEnvio.setOnClickListener(v -> validarEnvio());
        btnDescargarPDF.setOnClickListener(v -> generarPDF());
        btnPagar.setOnClickListener(v -> simularPago());
    }

    private void validarEnvio() {
        String nombre = edtRemitente.getText().toString().trim();
        String celular = edtCelular.getText().toString().trim();
        String pesoStr = edtPeso.getText().toString().trim();

        if (!nombre.matches("[a-zA-Z ]+")) {
            Toast.makeText(this, "El nombre solo debe contener letras", Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitud == 0 && longitud == 0) {
            Toast.makeText(this, "Seleccione la dirección en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!celular.matches("\\d{10}")) {
            Toast.makeText(this, "Ingrese un celular válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pesoStr.isEmpty()) {
            Toast.makeText(this, "Ingrese el peso estimado", Toast.LENGTH_SHORT).show();
            return;
        }

        double peso = Double.parseDouble(pesoStr);
        double precio = peso * 2000; // 2000 pesos por kilo

        // Fecha estimada (entre 2 y 5 días)
        int dias = new Random().nextInt(4) + 2;
        Calendar fechaEntrega = Calendar.getInstance();
        fechaEntrega.add(Calendar.DAY_OF_MONTH, dias);
        String fechaEntregaStr = fechaEntrega.get(Calendar.DAY_OF_MONTH) + "/" +
                (fechaEntrega.get(Calendar.MONTH) + 1) + "/" + fechaEntrega.get(Calendar.YEAR);

        int guia = new Random().nextInt(900000) + 100000;

        comprobanteTexto = "Comprobante de Envío\n\n"
                + "Número de guía: " + guia + "\n"
                + "Remitente: " + nombre + "\n"
                + "Celular: " + celular + "\n"
                + "Coordenadas: " + latitud + ", " + longitud + "\n"
                + "Peso: " + peso + " kg\n"
                + "Precio: $" + precio + "\n"
                + "Fecha estimada de entrega: " + fechaEntregaStr + "\n";

        txtComprobante.setText(comprobanteTexto);
        txtComprobante.setVisibility(TextView.VISIBLE);
        btnDescargarPDF.setVisibility(Button.VISIBLE);
        btnPagar.setVisibility(Button.VISIBLE);

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

        File file = new File(getExternalFilesDir(null), "ComprobanteEnvio.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF guardado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar PDF", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }

    private void simularPago() {
        Toast.makeText(this, "Simulación de pago realizada con éxito", Toast.LENGTH_LONG).show();
    }
}
