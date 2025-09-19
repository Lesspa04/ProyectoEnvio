package co.edu.unipiloto.proyectoenvio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText edtUsuario, edtPassword;
    Button btnLogin, btnIrRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsuario = findViewById(R.id.edtUsuario);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        btnLogin.setOnClickListener(v -> {
            String usuario = edtUsuario.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (usuario.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();
            }
        });

        btnIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }
}
