package com.sociallab.appgestion;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Actividad para registrar nuevos usuarios.
 */
public class RegisterActivity extends AppCompatActivity {

    // Campos de entrada y botones
    private TextInputEditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private MaterialButton registerButton, backToLoginButton;
    private CheckBox acceptPoliciesCheckBox;
    private TextView readPoliciesTextView;

    // Firebase Auth y Firestore
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referenciar vistas del layout
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);
        acceptPoliciesCheckBox = findViewById(R.id.acceptPoliciesCheckBox);
        readPoliciesTextView = findViewById(R.id.readPoliciesTextView);

        // Configurar acción para el botón de registro
        registerButton.setOnClickListener(v -> registerUser());

        // Acción para volver al inicio de sesión
        backToLoginButton.setOnClickListener(v -> finish());

        // Acción para leer políticas
        readPoliciesTextView.setOnClickListener(v -> openPolicies());
    }

    /**
     * Lógica para registrar un usuario.
     */
    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validaciones de entrada
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Por favor, ingresa un nombre de usuario");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Por favor, ingresa un email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Por favor, ingresa una contraseña");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Las contraseñas no coinciden");
            return;
        }
        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }
        if (!acceptPoliciesCheckBox.isChecked()) {
            Toast.makeText(this, "Debes aceptar las políticas para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Registrar usuario en Firebase
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();

                        // Guardar datos adicionales del usuario en Firestore
                        saveUserDataToFirestore(user.getUid(), username, email);
                    } else {
                        Toast.makeText(this, "Fallo en el registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Guarda los datos adicionales del usuario en Firestore.
     *
     * @param userId   ID del usuario en Firebase
     * @param username Nombre de usuario
     * @param email    Correo electrónico
     */
    private void saveUserDataToFirestore(String userId, String username, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Abre la actividad para leer las políticas.
     */
    private void openPolicies() {
        Intent intent = new Intent(this, PoliciesActivity.class);
        startActivity(intent);
    }
}
