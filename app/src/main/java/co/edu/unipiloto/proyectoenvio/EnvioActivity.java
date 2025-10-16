package co.edu.unipiloto.proyectoenvio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;


import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;

public class EnvioActivity extends AppCompatActivity {

    TextInputEditText edtCelularRemitente, edtDestinatario, edtCelularDestinatario, edtPeso;
    TextView tvDireccionRemitente, tvDireccionDestinatario, txtComprobante;
    Button btnSeleccionarDireccionRemitente, btnSeleccionarDireccionDestinatario;
    Button btnRegistrarEnvio, btnDescargarPDF, btnPagar, btnCompartir;

    double latRemitente = 0, lonRemitente = 0;
    double latDestinatario = 0, lonDestinatario = 0;
    String direccionRemitenteActual = "", direccionDestinatario = "";
    String comprobanteTexto = "";
    String ultimoNumeroGuia = "";

    private DatabaseHelper dbHelper;
    private String remitenteNombre = "";
    private String usuarioActual = "";

    private final int REQUEST_CREATE_PDF = 1001;

    // Launchers para seleccionar direcciones
    private final ActivityResultLauncher<Intent> direccionRemitenteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    latRemitente = result.getData().getDoubleExtra("latitud", 0);
                    lonRemitente = result.getData().getDoubleExtra("longitud", 0);
                    new Thread(() -> {
                        String addr = obtenerDireccion(latRemitente, lonRemitente);
                        runOnUiThread(() -> {
                            direccionRemitenteActual = addr;
                            tvDireccionRemitente.setText(addr);
                        });
                    }).start();
                }
            });

    private final ActivityResultLauncher<Intent> direccionDestinatarioLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    latDestinatario = result.getData().getDoubleExtra("latitud", 0);
                    lonDestinatario = result.getData().getDoubleExtra("longitud", 0);
                    new Thread(() -> {
                        String addr = obtenerDireccion(latDestinatario, lonDestinatario);
                        runOnUiThread(() -> {
                            direccionDestinatario = addr;
                            tvDireccionDestinatario.setText(addr);
                        });
                    }).start();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        dbHelper = new DatabaseHelper(this);

        // Bind Views
        edtCelularRemitente = findViewById(R.id.edtCelularRemitente);
        edtDestinatario = findViewById(R.id.edtDestinatario);
        edtCelularDestinatario = findViewById(R.id.edtCelularDestinatario);
        edtPeso = findViewById(R.id.edtPeso);

        tvDireccionRemitente = findViewById(R.id.tvDireccionRemitente);
        tvDireccionDestinatario = findViewById(R.id.tvDireccionDestinatario);
        txtComprobante = findViewById(R.id.txtComprobante);

        btnSeleccionarDireccionRemitente = findViewById(R.id.btnSeleccionarDireccionRemitente);
        btnSeleccionarDireccionDestinatario = findViewById(R.id.btnSeleccionarDireccionDestinatario);
        btnRegistrarEnvio = findViewById(R.id.btnRegistrarEnvio);
        btnDescargarPDF = findViewById(R.id.btnDescargarPDF);
        btnPagar = findViewById(R.id.btnPagar);
        btnCompartir = findViewById(R.id.btnCompartir);


        // Listeners
        btnSeleccionarDireccionRemitente.setOnClickListener(v -> {
            Intent i = new Intent(EnvioActivity.this, MapaDireccionActivity.class);
            direccionRemitenteLauncher.launch(i);
        });

        btnSeleccionarDireccionDestinatario.setOnClickListener(v -> {
            Intent i = new Intent(EnvioActivity.this, MapaDireccionActivity.class);
            direccionDestinatarioLauncher.launch(i);
        });

        btnRegistrarEnvio.setOnClickListener(v -> validarEnvio());
        btnDescargarPDF.setOnClickListener(v -> solicitarUbicacionPDF());
        btnPagar.setOnClickListener(v -> simularPago());
        btnCompartir.setOnClickListener(v -> compartirFormulario());

        // Cargar remitente desde BD
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        usuarioActual = prefs.getString("usuario", null);
        if (usuarioActual != null) {
            Cursor cursor = dbHelper.obtenerUsuario(usuarioActual);
            if (cursor.moveToFirst()) {
                remitenteNombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE));
                int idxDir = cursor.getColumnIndex(DatabaseHelper.COLUMN_DIRECCION);
                if (idxDir != -1) {
                    String dirGuardada = cursor.getString(idxDir);
                    if (dirGuardada != null) {
                        direccionRemitenteActual = dirGuardada;
                        tvDireccionRemitente.setText(dirGuardada);
                    }
                }
            }
            cursor.close();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_envio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_notificacion) {
            crearNotificacionEnvio();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void validarEnvio() {
        String celularRemitente = edtCelularRemitente.getText().toString().trim();
        String destinatario = edtDestinatario.getText().toString().trim();
        String celularDestinatario = edtCelularDestinatario.getText().toString().trim();
        String pesoStr = edtPeso.getText().toString().trim();

        if (remitenteNombre.isEmpty()) {
            Toast.makeText(this, "Error: usuario remitente no identificado", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!celularRemitente.matches("\\d{10}")) {
            Toast.makeText(this, "Ingrese un celular válido del remitente", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!destinatario.matches("[a-zA-Z ]+")) {
            Toast.makeText(this, "El destinatario solo debe contener letras", Toast.LENGTH_SHORT).show();
            return;
        }
        if (direccionRemitenteActual.isEmpty()) {
            Toast.makeText(this, "Seleccione la dirección del remitente", Toast.LENGTH_SHORT).show();
            return;
        }
        if (direccionDestinatario.isEmpty()) {
            Toast.makeText(this, "Seleccione la dirección del destinatario", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!celularDestinatario.matches("\\d{10}")) {
            Toast.makeText(this, "Ingrese un celular válido del destinatario", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pesoStr.isEmpty()) {
            Toast.makeText(this, "Ingrese el peso estimado", Toast.LENGTH_SHORT).show();
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(pesoStr);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Peso inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio = peso * 2000;
        int dias = new Random().nextInt(4) + 2;
        Calendar fechaEntrega = Calendar.getInstance();
        fechaEntrega.add(Calendar.DAY_OF_MONTH, dias);

        long fechaSolicitud = System.currentTimeMillis();
        long fechaEntregaMillis = fechaEntrega.getTimeInMillis();

        int guia = new Random().nextInt(900000) + 100000;
        String numeroGuia = "ENV-" + guia;
        ultimoNumeroGuia = numeroGuia;

        // Inserción en BD
        boolean insertado = dbHelper.insertarEncomienda(
                numeroGuia,
                remitenteNombre,
                usuarioActual,
                celularRemitente,
                direccionRemitenteActual,
                destinatario,
                celularDestinatario,
                direccionDestinatario,
                Encomiendas.Estado.SOLICITADO.name(),
                String.valueOf(fechaSolicitud),
                String.valueOf(fechaEntregaMillis),
                peso,
                precio,
                null
        );

        if (!insertado) {
            Toast.makeText(this, "Error al guardar la encomienda en la base de datos", Toast.LENGTH_SHORT).show();
            return;
        }

        String fechaEntregaStr = fechaEntrega.get(Calendar.DAY_OF_MONTH) + "/" +
                (fechaEntrega.get(Calendar.MONTH) + 1) + "/" + fechaEntrega.get(Calendar.YEAR);

        comprobanteTexto = "Comprobante de Envío\n\n"
                + "Número de guía: " + numeroGuia + "\n"
                + "Remitente: " + remitenteNombre + " (" + celularRemitente + ")\n"
                + "Dirección remitente: " + direccionRemitenteActual + "\n"
                + "Destinatario: " + destinatario + " (" + celularDestinatario + ")\n"
                + "Dirección destino: " + direccionDestinatario + "\n"
                + "Peso: " + peso + " kg\n"
                + "Precio: $" + precio + "\n"
                + "Fecha estimada de entrega: " + fechaEntregaStr + "\n";

        txtComprobante.setText(comprobanteTexto);
        txtComprobante.setVisibility(TextView.VISIBLE);
        btnDescargarPDF.setVisibility(Button.VISIBLE);
        btnPagar.setVisibility(Button.VISIBLE);
        btnCompartir.setVisibility(Button.VISIBLE);

        Toast.makeText(this, "Envío registrado correctamente", Toast.LENGTH_SHORT).show();
    }

    private void crearNotificacionEnvio() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }
        // Validar que haya un envío registrado
        if (ultimoNumeroGuia == null || ultimoNumeroGuia.isEmpty()) {
            Toast.makeText(this, "Debe registrar un envío antes de notificar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear canal de notificación (solo se ejecuta una vez)
        String channelId = "envio_channel";
        String channelName = "Notificaciones de Envío";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId,
                    channelName,
                    android.app.NotificationManager.IMPORTANCE_HIGH
            );
            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        androidx.core.app.NotificationCompat.Builder builder =
                new androidx.core.app.NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Nuevo envío registrado")
                        .setContentText("Guía " + ultimoNumeroGuia + " registrada exitosamente.")
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        Intent intent = new Intent(this, RutasActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );
        builder.setContentIntent(pendingIntent);

        // Mostrar notificación
        androidx.core.app.NotificationManagerCompat notificationManager =
                androidx.core.app.NotificationManagerCompat.from(this);
        notificationManager.notify(new Random().nextInt(1000), builder.build());

        Toast.makeText(this, "Notificación creada", Toast.LENGTH_SHORT).show();
    }

    private void compartirFormulario() {
        if (comprobanteTexto.isEmpty()) {
            Toast.makeText(this, "No hay información para compartir", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Detalles del envío");
        intent.putExtra(Intent.EXTRA_TEXT, comprobanteTexto);

        startActivity(Intent.createChooser(intent, "Compartir vía"));
    }


    private String obtenerDireccion(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocation(lat, lon, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                return direcciones.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Dirección desconocida";
    }

    // --- PDF con elección de ubicación ---
    private void solicitarUbicacionPDF() {
        if (comprobanteTexto.isEmpty()) {
            Toast.makeText(this, "No hay comprobante generado", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "Comprobante_" + (ultimoNumeroGuia.isEmpty() ? System.currentTimeMillis() : ultimoNumeroGuia) + ".pdf";
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, REQUEST_CREATE_PDF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CREATE_PDF && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                PdfDocument pdfDocument = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(612, 792, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);

                Canvas canvas = page.getCanvas();
                int xMargin = 50;
                int y = 50;

// --- Título con fondo ---
                Paint titlePaint = new Paint();
                titlePaint.setTextSize(26f);
                titlePaint.setFakeBoldText(true);
                titlePaint.setColor(Color.WHITE);

// Fondo azul para el título
                Paint rectPaint = new Paint();
                rectPaint.setColor(Color.parseColor("#3949AB"));
                rectPaint.setStyle(Paint.Style.FILL);
                canvas.drawRect(xMargin - 10, y - 30, 562, y + 10, rectPaint);

                canvas.drawText("Comprobante de Envío", xMargin, y, titlePaint);
                y += 50;

// --- Separador ---
                Paint separator = new Paint();
                separator.setColor(Color.GRAY);
                separator.setStrokeWidth(2);
                canvas.drawLine(xMargin, y, 562, y, separator);
                y += 20;

// --- Cuerpo del comprobante ---
                Paint bodyPaint = new Paint();
                bodyPaint.setTextSize(16f);
                bodyPaint.setColor(Color.BLACK);
                bodyPaint.setAntiAlias(true);

                String[] lineas = comprobanteTexto.split("\n");
                for (int i = 1; i < lineas.length; i++) { // Saltar la primera línea
                    canvas.drawText(lineas[i], xMargin, y, bodyPaint);
                    y += 28; // Ajusta espaciado según necesidad
                }

// Otra línea separadora al final
                y += 10;
                canvas.drawLine(xMargin, y, 562, y, separator);

                pdfDocument.finishPage(page);

// Guardar PDF
                OutputStream os = getContentResolver().openOutputStream(data.getData());
                pdfDocument.writeTo(os);
                os.close();
                pdfDocument.close();


                Toast.makeText(this, "PDF guardado correctamente", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al guardar PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void simularPago() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 50, 60, 20);
        // Título
        TextView titulo = new TextView(this);
        titulo.setText("Pasarela de Pago");
        titulo.setTextSize(20f);
        titulo.setPadding(0, 0, 0, 20);
        titulo.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);

        TextView lblTarjeta = new TextView(this);
        lblTarjeta.setText("Número de tarjeta:");
        lblTarjeta.setTextColor(Color.DKGRAY);

        TextInputEditText txtTarjeta = new TextInputEditText(this);
        txtTarjeta.setHint("1234 5678 9012 3456");
        txtTarjeta.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        TextView lblCVV = new TextView(this);
        lblCVV.setText("CVV:");
        lblCVV.setTextColor(Color.DKGRAY);

        TextInputEditText txtCVV = new TextInputEditText(this);
        txtCVV.setHint("123");
        txtCVV.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        TextView lblFecha = new TextView(this);
        lblFecha.setText("Fecha de expiración:");
        lblFecha.setTextColor(Color.DKGRAY);

        TextInputEditText txtFecha = new TextInputEditText(this);
        txtFecha.setHint("MM/AA");
        txtFecha.setInputType(android.text.InputType.TYPE_CLASS_DATETIME);

        layout.addView(titulo);
        layout.addView(lblTarjeta);
        layout.addView(txtTarjeta);
        layout.addView(lblCVV);
        layout.addView(txtCVV);
        layout.addView(lblFecha);
        layout.addView(txtFecha);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("Pagar", (d, which) -> {
                    String tarjeta = txtTarjeta.getText() != null ? txtTarjeta.getText().toString() : "";
                    String cvv = txtCVV.getText() != null ? txtCVV.getText().toString() : "";
                    String fecha = txtFecha.getText() != null ? txtFecha.getText().toString() : "";

                    if (tarjeta.length() < 16 || cvv.length() < 3 || fecha.isEmpty()) {
                        Toast.makeText(this, "Datos de pago inválidos", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Pago procesado con éxito", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancelar", (d, which) -> d.dismiss())
                .create();

        dialog.show();

    }
}
