package com.example.androidfirebase;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView servicesRecyclerView;
    private RecyclerView usersRecyclerView;
    private ServicesAdapter servicesAdapter;
    private UsersAdapter usersAdapter;
    private List<Services> serviceList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Инициализация для услуг
        servicesRecyclerView = findViewById(R.id.recycler_view);
        servicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        servicesAdapter = new ServicesAdapter(serviceList, new ServicesAdapter.OnServicesClickListener() {
            @Override
            public void onEditClick(Services service) {
                openEditServicesDialog(service);
            }

            @Override
            public void onDeleteClick(Services service) {
                deleteServices(service);
            }
        });
        servicesRecyclerView.setAdapter(servicesAdapter);

        Button addServiceButton = findViewById(R.id.add_service_button);
        addServiceButton.setOnClickListener(v -> openAddServiceDialog());

        // Инициализация для пользователей
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersAdapter = new UsersAdapter(userList, new UsersAdapter.OnUsersClickListener() {
            @Override
            public void onEditClick(User user) {
                openEditUserDialog(user);
            }

            @Override
            public void onDeleteClick(User user) {
                deleteUser(user);
            }
        });
        usersRecyclerView.setAdapter(usersAdapter);

        Button addUserButton = findViewById(R.id.add_user_button);
        addUserButton.setOnClickListener(v -> openAddUserDialog());

        loadServices();
        loadUsers();
    }

    private void loadServices() {
        Log.d("AdminActivity", "Loading services...");
        db.collection("Services")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        serviceList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Services service = document.toObject(Services.class);
                            if (service != null) {
                                serviceList.add(service);
                            }
                        }
                        servicesAdapter.notifyDataSetChanged();
                        Log.d("AdminActivity", "Services loaded: " + serviceList.size());
                    } else {
                        Log.e("AdminActivity", "Error loading services", task.getException());
                        Toast.makeText(this, "Failed to load services.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUsers() {
        Log.d("AdminActivity", "Loading users...");
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                userList.add(user);
                            }
                        }
                        usersAdapter.notifyDataSetChanged();
                        Log.d("AdminActivity", "Users loaded: " + userList.size());
                    } else {
                        Log.e("AdminActivity", "Error loading users", task.getException());
                        Toast.makeText(this, "Failed to load users.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openAddServiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Service");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_service, null);
        EditText nameInput = view.findViewById(R.id.service_name_input);
        EditText descInput = view.findViewById(R.id.service_desc_input);
        EditText costInput = view.findViewById(R.id.service_price_input);
        EditText categoryInput = view.findViewById(R.id.service_category_input);

        builder.setView(view);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();
            String costStr = costInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || costStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            int cost;
            try {
                cost = Integer.parseInt(costStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid cost format.", Toast.LENGTH_SHORT).show();
                return;
            }

            Services newService = new Services(name, description, cost, category);
            db.collection("Services")
                    .document(name)
                    .set(newService)
                    .addOnSuccessListener(aVoid -> {
                        servicesAdapter.addService(newService);
                        Toast.makeText(this, "Service added.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error adding service.", Toast.LENGTH_SHORT).show();
                        Log.w("Firebase", "Error adding service", e);
                    });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void openEditServicesDialog(Services service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Service");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_service, null);
        EditText nameInput = view.findViewById(R.id.service_name_input);
        EditText descInput = view.findViewById(R.id.service_desc_input);
        EditText costInput = view.findViewById(R.id.service_price_input);
        EditText categoryInput = view.findViewById(R.id.service_category_input);

        nameInput.setText(service.getName());
        nameInput.setEnabled(false);
        descInput.setText(service.getDescription());
        costInput.setText(String.valueOf(service.getCost()));
        categoryInput.setText(service.getCategory());

        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String description = descInput.getText().toString().trim();
            String costStr = costInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();

            if (description.isEmpty() || costStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            int cost;
            try {
                cost = Integer.parseInt(costStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid cost format.", Toast.LENGTH_SHORT).show();
                return;
            }

            Services updatedService = new Services(service.getName(), description, cost, category);
            db.collection("Services")
                    .document(service.getName())
                    .set(updatedService)
                    .addOnSuccessListener(aVoid -> {
                        servicesAdapter.updateService(updatedService);
                        Toast.makeText(this, "Service updated.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating service.", Toast.LENGTH_SHORT).show();
                        Log.w("Firebase", "Error updating service", e);
                    });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    private void deleteServices(Services service) {
        db.collection("Services")
                .document(service.getName())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    servicesAdapter.removeService(service);
                    Toast.makeText(this, "Service deleted.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting service.", Toast.LENGTH_SHORT).show();
                    Log.w("Firebase", "Error deleting service", e);
                });
    }

    private void openAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New User");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        EditText emailInput = view.findViewById(R.id.user_email_input);
        EditText passwordInput = view.findViewById(R.id.user_password_input);
        EditText nameInput = view.findViewById(R.id.user_name_input);

        builder.setView(view);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String displayName = nameInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || displayName.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();
                        User newUser = new User(uid, email, displayName);
                        db.collection("users")
                                .document(uid)
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    usersAdapter.addUser(newUser);
                                    Toast.makeText(this, "User added.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error adding user to Firestore.", Toast.LENGTH_SHORT).show();
                                    Log.w("Firebase", "Error adding user", e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error creating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.w("Firebase", "Error creating user", e);
                    });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openEditUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit User");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);
        EditText emailInput = view.findViewById(R.id.user_email_input);
        EditText passwordInput = view.findViewById(R.id.user_password_input);
        EditText nameInput = view.findViewById(R.id.user_name_input);

        emailInput.setText(user.getEmail());
        emailInput.setEnabled(false); // Не даём менять email
        passwordInput.setHint("Leave blank to keep current password");
        nameInput.setText(user.getDisplayName());

        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String displayName = nameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (displayName.isEmpty()) {
                Toast.makeText(this, "Name is required.", Toast.LENGTH_SHORT).show();
                return;
            }

            User updatedUser = new User(user.getUid(), user.getEmail(), displayName);
            db.collection("users")
                    .document(user.getUid())
                    .set(updatedUser)
                    .addOnSuccessListener(aVoid -> {
                        if (!password.isEmpty()) {
                            mAuth.getCurrentUser().updatePassword(password)
                                    .addOnSuccessListener(aVoid2 -> {
                                        usersAdapter.updateUser(updatedUser);
                                        Toast.makeText(this, "User updated.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error updating password.", Toast.LENGTH_SHORT).show();
                                        Log.w("Firebase", "Error updating password", e);
                                    });
                        } else {
                            usersAdapter.updateUser(updatedUser);
                            Toast.makeText(this, "User updated.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating user.", Toast.LENGTH_SHORT).show();
                        Log.w("Firebase", "Error updating user", e);
                    });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteUser(User user) {
        // Здесь только удаляем из Firestore
        db.collection("users")
                .document(user.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    usersAdapter.removeUser(user);
                    Toast.makeText(this, "User deleted from Firestore.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting user.", Toast.LENGTH_SHORT).show();
                    Log.w("Firebase", "Error deleting user", e);
                });
    }
}