package com.sociallab.appgestion;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;

/**
 * DashboardActivity es la actividad principal después de iniciar sesión.
 * Permite agregar contraseñas, ver contraseñas y gestionar la cuenta.
 */
public class DashboardActivity extends AppCompatActivity {

    // Componentes de la interfaz de usuario
    private TextView welcomeTextView;
    private Button addPasswordButton, viewPasswordsButton, deleteAccountButton, logoutButton;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Inicializar Firebase y vistas
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeTextView = findViewById(R.id.welcomeTextView);
        addPasswordButton = findViewById(R.id.addPasswordButton);
        viewPasswordsButton = findViewById(R.id.viewPasswordsButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Configurar mensaje de bienvenida
        fetchAndSetWelcomeMessage();

        // Configurar acciones de los botones
        addPasswordButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AddPasswordActivity.class)));

        viewPasswordsButton.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ViewPasswordsActivity.class)));

        deleteAccountButton.setOnClickListener(v -> authenticateBeforeDelete());

        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
    }

    /**
     * Obtiene y configura el mensaje de bienvenida con el nombre de usuario.
     */
    private void fetchAndSetWelcomeMessage() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.contains("username") ? documentSnapshot.getString("username") : "Usuario";
                    welcomeTextView.setText("¡Hola " + username + "!, te damos la bienvenida a FortPass");
                })
                .addOnFailureListener(e -> welcomeTextView.setText("¡Hola, te damos la bienvenida a FortPass!"));
    }

    /**
     * Solicita autenticación biométrica o por PIN antes de eliminar la cuenta.
     */
    private void authenticateBeforeDelete() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                showDeleteAccountConfirmation();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(DashboardActivity.this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS || errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE) {
                    authenticateWithKeyguard();
                } else {
                    Toast.makeText(DashboardActivity.this, "Error de autenticación: " + errString, Toast.LENGTH_SHORT).show();
                }
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirmar Eliminación")
                .setSubtitle("Usa tu huella digital o PIN para continuar")
                .setNegativeButtonText("Cancelar")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Solicita autenticación con PIN, patrón o contraseña en caso de no tener biometría.
     */
    private void authenticateWithKeyguard() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguardManager != null && keyguardManager.isDeviceSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Confirmar Eliminación", "Ingresa tu PIN, patrón o contraseña para continuar");
            startActivityForResult(intent, 1);
        } else {
            Toast.makeText(this, "Configura un bloqueo de pantalla para proceder.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            showDeleteAccountConfirmation();
        } else {
            Toast.makeText(this, "Autenticación cancelada", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Muestra un cuadro de diálogo para confirmar la eliminación de la cuenta.
     */
    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Elimina la cuenta del usuario junto con sus datos de Firestore.
     */
    private void deleteAccount() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> mAuth.getCurrentUser().delete()
                        .addOnSuccessListener(aVoid1 -> {
                            SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                            editor.clear();
                            editor.apply();
                            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> showError("Error al eliminar cuenta: " + e.getMessage())))
                .addOnFailureListener(e -> showError("Error al eliminar datos del usuario: " + e.getMessage()));
    }

    /**
     * Muestra un cuadro de diálogo para confirmar el cierre de sesión.
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Cerrar Sesión", (dialog, which) -> logout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Cierra la sesión del usuario y lo redirige a la pantalla de inicio de sesión.
     */
    private void logout() {
        SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
        mAuth.signOut();
        startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
        finish();
    }

    /**
     * Muestra un cuadro de diálogo para notificar errores.
     *
     * @param message Mensaje de error a mostrar.
     */
    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
