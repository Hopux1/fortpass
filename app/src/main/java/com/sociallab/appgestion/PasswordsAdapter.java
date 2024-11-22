package com.sociallab.appgestion;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

/**
 * Adaptador para manejar y mostrar una lista de contraseñas en un RecyclerView.
 */
public class PasswordsAdapter extends RecyclerView.Adapter<PasswordsAdapter.ViewHolder> {

    private final ArrayList<Map<String, String>> passwordList; // Lista de contraseñas
    private final Context context; // Contexto para manejar actividades y recursos

    /**
     * Constructor del adaptador.
     *
     * @param passwordList Lista de contraseñas para mostrar.
     * @param context      Contexto de la aplicación.
     */
    public PasswordsAdapter(ArrayList<Map<String, String>> passwordList, Context context) {
        this.passwordList = passwordList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el diseño de cada elemento en el RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_password, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener los datos de la contraseña actual
        Map<String, String> passwordData = passwordList.get(position);

        // Establecer los valores en las vistas correspondientes
        holder.siteTextView.setText("Sitio: " + passwordData.get("siteName"));
        holder.usernameTextView.setText("Usuario: " + passwordData.get("username"));
        holder.passwordTextView.setText("Contraseña: " + passwordData.get("password"));
        holder.notesTextView.setText("Notas: " + passwordData.get("notes"));

        // Configurar el botón de edición
        holder.editButton.setOnClickListener(v -> {
            // Iniciar la actividad para editar la contraseña
            Intent intent = new Intent(context, AddPasswordActivity.class);
            intent.putExtra("editMode", true);
            intent.putExtra("documentId", passwordData.get("documentId"));
            intent.putExtra("siteName", passwordData.get("siteName"));
            intent.putExtra("username", passwordData.get("username"));
            intent.putExtra("password", passwordData.get("password"));
            intent.putExtra("notes", passwordData.get("notes"));
            context.startActivity(intent);
        });

        // Configurar el botón de eliminación
        holder.deleteButton.setOnClickListener(v -> {
            String documentId = passwordData.get("documentId");

            if (documentId != null) {
                // Mostrar un diálogo de confirmación antes de eliminar
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Eliminar Contraseña")
                        .setMessage("¿Estás seguro de que deseas eliminar esta contraseña?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            // Eliminar la contraseña de Firestore
                            db.collection("users")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .collection("passwords")
                                    .document(documentId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Notificar al usuario y actualizar la lista
                                        Toast.makeText(context, "Contraseña eliminada con éxito", Toast.LENGTH_SHORT).show();
                                        passwordList.remove(position);
                                        notifyItemRemoved(position);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                // Mostrar un mensaje de error si el ID del documento no está disponible
                Toast.makeText(context, "ID de documento no encontrado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        // Retornar el tamaño de la lista de contraseñas
        return passwordList.size();
    }

    /**
     * Clase interna para representar cada elemento del RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView siteTextView, usernameTextView, passwordTextView, notesTextView;
        Button editButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inicializar vistas del diseño del elemento
            siteTextView = itemView.findViewById(R.id.siteTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            passwordTextView = itemView.findViewById(R.id.passwordTextView);
            notesTextView = itemView.findViewById(R.id.notesTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
