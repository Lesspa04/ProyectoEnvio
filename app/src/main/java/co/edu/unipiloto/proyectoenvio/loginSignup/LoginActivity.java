package co.edu.unipiloto.proyectoenvio.loginSignup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.proyectoenvio.database.DatabaseHelper;
import co.edu.unipiloto.proyectoenvio.R;
import co.edu.unipiloto.proyectoenvio.menus.MenuPrincipalActivity;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText edtUsuario, edtPassword;
    Button btnLogin, btnIrRegistro;

    DatabaseHelper dbHelper;  // Helper para BD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ⛔️ Si ya hay sesión iniciada, redirige al menú
        if (getSharedPreferences("sesion", MODE_PRIVATE).contains("usuario")) {
            startActivity(new Intent(this, MenuPrincipalActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        edtUsuario = findViewById(R.id.edtUsuario);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        btnLogin.setOnClickListener(v -> {
            String usuario = edtUsuario.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (usuario.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar usuario y contraseña en la BD
            boolean esValido = dbHelper.validarUsuario(usuario, pass);

            if (esValido) {
                Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();

                // Guardar sesión
                getSharedPreferences("sesion", MODE_PRIVATE)
                        .edit()
                        .putString("usuario", usuario)
                        .apply();

                // Redirigir a menú principal
                Intent intent = new Intent(LoginActivity.this, MenuPrincipalActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        });

        btnIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }
}

