package com.sociallab.appgestion;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Actividad para gestionar la funcionalidad de recuperación de contraseñas.
 * Permite al usuario enviar un enlace de restablecimiento a su correo electrónico registrado.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    // Declaración de variables para las vistas y FirebaseAuth
    private EditText emailEditText;
    private Button resetPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Inicializar FirebaseAuth para manejar la autenticación
        mAuth = FirebaseAuth.getInstance();

        // Referencias a las vistas del layout
        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        // Configuración del listener para el botón de restablecimiento
        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            // Validar el campo de correo electrónico
            if (!isValidEmail(email)) {
                return; // Si la validación falla, se detiene el flujo
            }

            // Enviar el enlace de restablecimiento de contraseña
            sendResetPasswordEmail(email);
        });
    }

    /**
     * Valida si el correo electrónico ingresado es válido.
     *
     * @param email El correo electrónico a validar.
     * @return `true` si es válido, de lo contrario `false`.
     */
    private boolean isValidEmail(String email) {
        // Verificar si el campo está vacío
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("El correo electrónico es obligatorio");
            emailEditText.requestFocus();
            return false;
        }

        // Verificar el formato del correo electrónico
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Formato de correo electrónico no válido");
            emailEditText.requestFocus();
            return false;
        }

        return true; // El correo es válido
    }

    /**
     * Envía un enlace de restablecimiento de contraseña al correo electrónico ingresado.
     *
     * @param email El correo electrónico al cual enviar el enlace.
     */
    private void sendResetPasswordEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Notificar al usuario que el enlace fue enviado
                        Toast.makeText(this, "Enlace de restablecimiento enviado a " + email, Toast.LENGTH_LONG).show();
                        finish(); // Cierra la actividad
                    } else {
                        // Manejar errores en el envío
                        Toast.makeText(this, "Error al enviar el enlace: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
