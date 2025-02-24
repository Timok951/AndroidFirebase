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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class WorkerActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private ServicesAdapter servicesAdapter;
    private List<Services> serviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in as worker.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button addButton = findViewById(R.id.add_service_button);
        addButton.setOnClickListener(v -> openAddServiceDialog());

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
        recyclerView.setAdapter(servicesAdapter);

        loadServices();
    }

    private void loadServices() {
        Log.d("WorkerActivity", "Loading services...");
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
                        Log.d("WorkerActivity", "Services loaded: " + serviceList.size());
                        if (serviceList.isEmpty()) {
                            Toast.makeText(this, "No services found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("WorkerActivity", "Error loading services", task.getException());
                        Toast.makeText(this, "Failed to load services.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openAddServiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Service");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_service, null);
        EditText nameInput = view.findViewById(R.id.service_name_input);
        EditText descInput = view.findViewById(R.id.service_desc_input);
        EditText priceInput = view.findViewById(R.id.service_price_input);
        EditText categoryInput = view.findViewById(R.id.service_category_input);

        builder.setView(view);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            int price;
            try {
                price = Integer.parseInt(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format.", Toast.LENGTH_SHORT).show();
                return;
            }


            Services newService = new Services(name, description, price, category);
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
        EditText priceInput = view.findViewById(R.id.service_price_input);
        EditText categoryInput = view.findViewById(R.id.service_category_input);

        nameInput.setText(service.getName());
        nameInput.setEnabled(false);
        descInput.setText(service.getDescription());
        priceInput.setText(String.valueOf(service.getCost()));
        categoryInput.setText(service.getCategory());

        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String description = descInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();

            if (description.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            int price;
            try {
                price = Integer.parseInt(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format.", Toast.LENGTH_SHORT).show();
                return;
            }

            Services updatedService = new Services(service.getName(), description, price, category);
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
}