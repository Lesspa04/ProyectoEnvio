package co.edu.unipiloto.proyectoenvio;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginRegisterActivity extends AppCompatActivity {

    EditText edtNombre, edtCelular, edtPassword;
    Button btnRegistrar, btnIniciar;
    ImageButton btnVerPassword;
    boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        edtNombre = findViewById(R.id.edtNombre);
        edtCelular = findViewById(R.id.edtCelular);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnIniciar = findViewById(R.id.btnIniciarSesion);
        btnVerPassword = findViewById(R.id.btnVerPassword);

        // Mostrar / ocultar contraseña
        btnVerPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(passwordVisible){
                    edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVisible = false;
                } else {
                    edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVisible = true;
                }
                edtPassword.setSelection(edtPassword.length()); // cursor al final
            }
        });

        // Botón Registrar
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCampos();
            }
        });

        // Botón Iniciar sesión
        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCampos();
            }
        });
    }

    private void validarCampos() {
        String nombre = edtNombre.getText().toString().trim();
        String celular = edtCelular.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!nombre.matches("[a-zA-Z ]+")) {
            Toast.makeText(this, "El nombre solo debe contener letras", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!celular.matches("\\d{10}")) {
            Toast.makeText(this, "Ingrese un número de celular válido (10 dígitos)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6 || !password.matches(".*[A-Z].*") || !password.matches(".*\\d.*")) {
            Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres, una mayúscula y un número", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Validación correcta ✅", Toast.LENGTH_SHORT).show();
    }
}


