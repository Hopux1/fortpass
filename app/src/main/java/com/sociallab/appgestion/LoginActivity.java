package com.sociallab.appgestion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Actividad para gestionar el inicio de sesión de los usuarios en la aplicación.
 */
public class LoginActivity extends AppCompatActivity {

    // Declaración de vistas y variables
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private MaterialButton registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Referencias a las vistas del layout
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);
        registerButton = findViewById(R.id.registerButton);

        // Verificar si el usuario ya está autenticado
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            navigateToDashboardActivity(); // Redirigir al Dashboard si el usuario ya está autenticado
        }

        // Configurar el botón de inicio de sesión
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!validateInput(email, password)) return;

            // Intentar iniciar sesión con Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso
                            FirebaseUser user = mAuth.getCurrentUser();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();
                            navigateToDashboardActivity();
                        } else {
                            // Manejar errores de inicio de sesión
                            handleLoginError(task.getException(), email);
                        }
                    });
        });

        // Configurar el texto de "¿Olvidaste tu contraseña?"
        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Configurar el botón de "Registrar"
        registerButton.setOnClickListener(v -> navigateToRegisterActivity());
    }

    /**
     * Valida las credenciales ingresadas por el usuario.
     *
     * @param email    Correo electrónico ingresado.
     * @param password Contraseña ingresada.
     * @return `true` si las credenciales son válidas, `false` de lo contrario.
     */
    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("El email es obligatorio");
            emailEditText.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("La contraseña es obligatoria");
            passwordEditText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Redirige al usuario al Dashboard después de un inicio de sesión exitoso.
     */
    private void navigateToDashboardActivity() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Evitar regresar al LoginActivity
    }

    /**
     * Redirige al usuario a la actividad de registro.
     */
    private void navigateToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Maneja los errores de inicio de sesión y muestra mensajes adecuados al usuario.
     *
     * @param exception Excepción lanzada durante el intento de inicio de sesión.
     * @param email     Correo electrónico ingresado por el usuario.
     */
    private void handleLoginError(Exception exception, String email) {
        if (exception == null) {
            Toast.makeText(this, "Error desconocido. Inténtalo nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (exception instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(this, "El email ingresado no está registrado.", Toast.LENGTH_SHORT).show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            if (email.contains("@")) {
                Toast.makeText(this, "Contraseña incorrecta. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "El formato del email es incorrecto.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error de autenticación. Revisa tus credenciales.", Toast.LENGTH_SHORT).show();
        }

        // Limpiar el campo de contraseña en caso de error
        passwordEditText.setText("");
        passwordEditText.requestFocus();
    }
}
