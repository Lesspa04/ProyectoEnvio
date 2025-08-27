package co.edu.unipiloto.proyectoenvio;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroActivity extends AppCompatActivity {

    EditText etNombre, etCelular, etPassword;
    Button btnRegistrar;
    ImageButton btnTogglePassword;
    boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etNombre = findViewById(R.id.etNombre);
        etCelular = findViewById(R.id.etCelular);
        etPassword = findViewById(R.id.etPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);

        btnTogglePassword.setOnClickListener(v -> {
            if (passwordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
                passwordVisible = false;
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                passwordVisible = true;
            }
            etPassword.setSelection(etPassword.length());
        });

        btnRegistrar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String celular = etCelular.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (!validarNombre(nombre)) {
                etNombre.setError("El nombre solo debe contener letras y espacios");
                return;
            }
            if (!validarCelular(celular)) {
                etCelular.setError("Número de celular inválido");
                return;
            }
            if (!validarPassword(password)) {
                etPassword.setError("La contraseña debe tener mínimo 8 caracteres, incluir mayúsculas, minúsculas, número y símbolo");
                return;
            }

            Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private boolean validarNombre(String nombre) {
        return !nombre.isEmpty() && nombre.matches("^[a-zA-Z ]+$");
    }

    private boolean validarCelular(String celular) {
        return !celular.isEmpty() && celular.matches("^[0-9]{10,}$"); // mínimo 10 dígitos
    }

    private boolean validarPassword(String password) {
        return !password.isEmpty() &&
                password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$");
    }
}

