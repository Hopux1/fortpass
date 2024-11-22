package com.sociallab.appgestion;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class ViewPasswordsActivity extends AppCompatActivity {

    // Componentes de la UI
    private RecyclerView passwordsRecyclerView;
    private PasswordsAdapter passwordsAdapter;
    private FloatingActionButton fabAddPassword;
    private ImageView refreshButton;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Lista de contraseñas
    private ArrayList<Map<String, String>> passwordList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_passwords);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Configurar vistas
        setupRecyclerView();
        setupButtons();

        // Autenticar y cargar contraseñas
        authenticateAndLoadPasswords();
    }

    /**
     * Configura el RecyclerView para mostrar las contraseñas.
     */
    private void setupRecyclerView() {
        passwordsRecyclerView = findViewById(R.id.recyclerViewPasswords);
        passwordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        passwordsAdapter = new PasswordsAdapter(passwordList, this);
        passwordsRecyclerView.setAdapter(passwordsAdapter);
    }

    /**
     * Configura los botones flotantes y de refrescar.
     */
    private void setupButtons() {
        // Botón para agregar contraseñas
        fabAddPassword = findViewById(R.id.fabAddPassword);
        fabAddPassword.setOnClickListener(v -> {
            Intent intent = new Intent(ViewPasswordsActivity.this, AddPasswordActivity.class);
            startActivity(intent);
        });

        // Botón para refrescar la lista
        refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(v -> refreshPasswords());
    }

    /**
     * Realiza la autenticación (biométrica o Keyguard) y carga las contraseñas.
     */
    private void authenticateAndLoadPasswords() {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(ViewPasswordsActivity.this, "Autenticación exitosa", Toast.LENGTH_SHORT).show();
                fetchPasswords();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(ViewPasswordsActivity.this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS || errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE) {
                    authenticateWithKeyguard();
                } else {
                    Toast.makeText(ViewPasswordsActivity.this, "Error de autenticación: " + errString, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación Requerida")
                .setSubtitle("Usa tu huella digital o PIN para continuar")
                .setNegativeButtonText("Cancelar")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Utiliza Keyguard como método de autenticación alternativo.
     */
    private void authenticateWithKeyguard() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguardManager != null && keyguardManager.isDeviceSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(
                    "Autenticación Requerida",
                    "Ingresa tu PIN, patrón o contraseña para continuar"
            );
            startActivityForResult(intent, 1);
        } else {
            Toast.makeText(this, "Configura un bloqueo de pantalla para continuar.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Autenticación exitosa", Toast.LENGTH_SHORT).show();
                fetchPasswords();
            } else {
                Toast.makeText(this, "Autenticación cancelada", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Recupera las contraseñas almacenadas en Firestore y las desencripta.
     */
    private void fetchPasswords() {
        passwordList.clear();
        String userId = mAuth.getCurrentUser().getUid();

        try {
            String key = EncryptionUtil.generateKey(userId);

            db.collection("users").document(userId).collection("passwords")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                try {
                                    Map<String, String> passwordData = new HashMap<>();
                                    passwordData.put("siteName", EncryptionUtil.decrypt(doc.getString("siteName"), key));
                                    passwordData.put("username", EncryptionUtil.decrypt(doc.getString("username"), key));
                                    passwordData.put("password", EncryptionUtil.decrypt(doc.getString("password"), key));
                                    passwordData.put("notes", EncryptionUtil.decrypt(doc.getString("notes"), key));
                                    passwordData.put("documentId", doc.getId());
                                    passwordList.add(passwordData);
                                } catch (Exception e) {
                                    Toast.makeText(this, "Error al descifrar datos.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            passwordsAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No se encontraron contraseñas.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error al generar la clave: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Refresca la lista de contraseñas.
     */
    private void refreshPasswords() {
        fetchPasswords();
        Toast.makeText(this, "Lista actualizada.", Toast.LENGTH_SHORT).show();
    }
}
