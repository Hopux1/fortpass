package com.sociallab.appgestion;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity para agregar o editar contraseñas.
 * Incluye funcionalidad para guardar o actualizar contraseñas en Firebase Firestore.
 */
public class AddPasswordActivity extends AppCompatActivity {

    // Campos de entrada y botones
    private TextInputEditText siteNameEditText, usernameEditText, passwordEditText, notesEditText;
    private MaterialButton saveButton;

    // Firebase Auth y Firestore
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Variables de control para edición
    private boolean editMode = false;
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        siteNameEditText = findViewById(R.id.siteNameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        notesEditText = findViewById(R.id.notesEditText);
        saveButton = findViewById(R.id.saveButton);

        // Determinar si la actividad está en modo de edición
        Intent intent = getIntent();
        editMode = intent.getBooleanExtra("editMode", false);

        if (editMode) {
            // Cargar datos para edición
            documentId = intent.getStringExtra("documentId");
            siteNameEditText.setText(intent.getStringExtra("siteName"));
            usernameEditText.setText(intent.getStringExtra("username"));
            passwordEditText.setText(intent.getStringExtra("password"));
            notesEditText.setText(intent.getStringExtra("notes"));

            // Cambiar texto del botón y asignar acción de actualizar
            saveButton.setText("Actualizar Contraseña");
            saveButton.setOnClickListener(v -> updatePassword());
        } else {
            // Asignar acción de guardar
            saveButton.setOnClickListener(v -> savePassword());
        }
    }

    /**
     * Método para guardar una nueva contraseña.
     */
    private void savePassword() {
        // Obtener valores de los campos de entrada
        String siteName = siteNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        // Validar campos
        if (!validateFields(siteName, username, password)) return;

        try {
            // Obtener ID del usuario y clave de cifrado
            String userId = mAuth.getCurrentUser().getUid();
            String key = EncryptionUtil.generateKey(userId);

            // Crear mapa de datos cifrados
            Map<String, Object> passwordData = new HashMap<>();
            passwordData.put("siteName", EncryptionUtil.encrypt(siteName, key));
            passwordData.put("username", EncryptionUtil.encrypt(username, key));
            passwordData.put("password", EncryptionUtil.encrypt(password, key));
            passwordData.put("notes", EncryptionUtil.encrypt(notes, key));

            // Guardar en Firestore
            db.collection("users").document(userId).collection("passwords")
                    .add(passwordData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Contraseña guardada con éxito", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            Toast.makeText(this, "Error en el cifrado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Método para actualizar una contraseña existente.
     */
    private void updatePassword() {
        // Obtener valores de los campos de entrada
        String siteName = siteNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        // Validar campos
        if (!validateFields(siteName, username, password)) return;

        try {
            // Obtener ID del usuario y clave de cifrado
            String userId = mAuth.getCurrentUser().getUid();
            String key = EncryptionUtil.generateKey(userId);

            // Crear mapa de datos cifrados
            Map<String, Object> passwordData = new HashMap<>();
            passwordData.put("siteName", EncryptionUtil.encrypt(siteName, key));
            passwordData.put("username", EncryptionUtil.encrypt(username, key));
            passwordData.put("password", EncryptionUtil.encrypt(password, key));
            passwordData.put("notes", EncryptionUtil.encrypt(notes, key));

            // Actualizar en Firestore
            db.collection("users").document(userId).collection("passwords").document(documentId)
                    .set(passwordData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            Toast.makeText(this, "Error en el cifrado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Valida los campos obligatorios.
     * @param siteName Nombre del sitio o aplicación.
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return true si todos los campos son válidos, false en caso contrario.
     */
    private boolean validateFields(String siteName, String username, String password) {
        if (TextUtils.isEmpty(siteName)) {
            siteNameEditText.setError("El nombre del sitio es obligatorio");
            return false;
        }
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("El nombre de usuario es obligatorio");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("La contraseña es obligatoria");
            return false;
        }
        return true;
    }
}
