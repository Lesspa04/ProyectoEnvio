package co.edu.unipiloto.proyectoenvio.loginSignup;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;
import co.edu.unipiloto.proyectoenvio.MapaDireccionActivity;
import co.edu.unipiloto.proyectoenvio.R;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText edtNombre, edtUsuario, edtEmail, edtPassword, edtConfirmPassword;
    TextView tvDireccionSeleccionada;
    Spinner spinnerRol;
    RadioGroup radioGroupGenero;
    Button btnFechaNacimiento, btnRegistrar, btnSeleccionarDireccion;
    int anio, mes, dia;
    Calendar fechaNacimiento;

    private ActivityResultLauncher<Intent> direccionLauncher;

    // Aquí el helper de base de datos
    DatabaseHelper dbHelper;

    // Guardamos la dirección como texto real
    private String direccionSeleccionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializamos helper BD
        dbHelper = new DatabaseHelper(this);

        edtNombre = findViewById(R.id.edtNombre);
        edtUsuario = findViewById(R.id.edtUsuario);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        spinnerRol = findViewById(R.id.spinnerRol);
        radioGroupGenero = findViewById(R.id.radioGroupGenero);
        btnFechaNacimiento = findViewById(R.id.btnFechaNacimiento);
        btnSeleccionarDireccion = findViewById(R.id.btnSeleccionarDireccion);
        tvDireccionSeleccionada = findViewById(R.id.tvDireccionSeleccionada);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        // Lanzador para mapa
        direccionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        double lat = data.getDoubleExtra("latitud", 0.0);
                        double lon = data.getDoubleExtra("longitud", 0.0);

                        // Convertir lat/lon a dirección real
                        new Thread(() -> {
                            String addr = obtenerDireccion(lat, lon);
                            runOnUiThread(() -> {
                                direccionSeleccionada = addr;
                                tvDireccionSeleccionada.setText(addr);
                            });
                        }).start();
                    }
                }
        );

        btnSeleccionarDireccion.setOnClickListener(v -> {
            Intent i = new Intent(RegisterActivity.this, MapaDireccionActivity.class);
            direccionLauncher.launch(i);
        });

        // Roles en Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapter);

        // Fecha de nacimiento
        btnFechaNacimiento.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            anio = calendar.get(Calendar.YEAR);
            mes = calendar.get(Calendar.MONTH);
            dia = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                fechaNacimiento = Calendar.getInstance();
                fechaNacimiento.set(year, month, dayOfMonth);
                btnFechaNacimiento.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }, anio, mes, dia);
            dialog.show();
        });

        // Botón registrar
        btnRegistrar.setOnClickListener(v -> validarCampos());
    }

    private void validarCampos() {
        String nombre = edtNombre.getText().toString().trim();
        String usuario = edtUsuario.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        if (nombre.isEmpty() || usuario.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPass)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!esPasswordSegura(password)) {
            new AlertDialog.Builder(this)
                    .setTitle("Contraseña insegura")
                    .setMessage("La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un símbolo.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        if (fechaNacimiento != null) {
            Calendar hoy = Calendar.getInstance();
            int edad = hoy.get(Calendar.YEAR) - fechaNacimiento.get(Calendar.YEAR);
            if (hoy.get(Calendar.DAY_OF_YEAR) < fechaNacimiento.get(Calendar.DAY_OF_YEAR)) {
                edad--;
            }
            if (edad < 18) {
                Toast.makeText(this, "Debe ser mayor de 18 años", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "Seleccione fecha de nacimiento", Toast.LENGTH_SHORT).show();
            return;
        }

        int generoId = radioGroupGenero.getCheckedRadioButtonId();
        if (generoId == -1) {
            Toast.makeText(this, "Seleccione género", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton rbGenero = findViewById(generoId);
        String genero = rbGenero.getText().toString();

        if (direccionSeleccionada.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar una dirección en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }

        String rol = spinnerRol.getSelectedItem().toString();

        // Intentamos insertar el usuario en la base
        boolean inserto = dbHelper.insertarUsuario(
                nombre, usuario, email, password, direccionSeleccionada, rol,
                btnFechaNacimiento.getText().toString(), genero);

        if (inserto) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_LONG).show();
            // Redirigir a LoginActivity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error al registrar, el usuario puede existir", Toast.LENGTH_LONG).show();
        }
    }

    private boolean esPasswordSegura(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
        return password.matches(regex);
    }

    // Convierte lat/lon a dirección en texto
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
}
