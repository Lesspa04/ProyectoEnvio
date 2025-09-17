package co.edu.unipiloto.proyectoenvio;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginRegisterActivity extends AppCompatActivity {

    // Campos de la UI
    private EditText edtNombre, edtEmail, edtCelular, edtPassword;
    private Button btnRegistrar, btnIniciarSesion;
    private ImageButton btnVerPassword;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        // Inicializa la UI
        edtNombre = findViewById(R.id.edtNombre);
        edtEmail = findViewById(R.id.edtEmail);
        edtCelular = findViewById(R.id.edtCelular);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnVerPassword = findViewById(R.id.btnVerPassword);

        // Inicializa DBHelper
        dbHelper = new DBHelper(this);

        // Acción para el botón "Registrar"
        btnRegistrar.setOnClickListener(v -> registrarRemitente());

        // Acción para el botón "Iniciar Sesión"
        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        // Mostrar/ocultar contraseña
        btnVerPassword.setOnClickListener(v -> {
            if (edtPassword.getInputType() == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                edtPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            } else {
                edtPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    // Método para registrar un remitente
    private void registrarRemitente() {
        // Obtener datos de los campos
        String nombre = edtNombre.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String celular = edtCelular.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validación de campos
        if (nombre.isEmpty() || email.isEmpty() || celular.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el email ya existe
        long remitenteId = dbHelper.getRemitenteIdByEmail(email);
        if (remitenteId != -1) {
            Toast.makeText(this, "Este correo ya está registrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Registrar el nuevo remitente
        long createdAt = System.currentTimeMillis();
        long result = dbHelper.insertRemitente(nombre, email, celular, password, createdAt);

        // Mostrar mensaje según el resultado
        if (result > 0) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para iniciar sesión
    private void iniciarSesion() {
        // Obtener email y contraseña
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa tu email y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el email existe
        long remitenteId = dbHelper.getRemitenteIdByEmail(email);
        if (remitenteId == -1) {
            Toast.makeText(this, "Este correo no está registrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí podrías validar la contraseña con la base de datos, por ahora asumimos que la contraseña es correcta
        Toast.makeText(this, "Bienvenido de nuevo", Toast.LENGTH_SHORT).show();

        // Redirigir a otra actividad si es necesario (ejemplo)
        // Intent intent = new Intent(this, MainActivity.class);
        // startActivity(intent);
    }
}
