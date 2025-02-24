package com.example.androidfirebase;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private RecyclerView serviceRecyclerView;
    private Button bookButton, selectedDateButton;
    private TextView selectedDateTimeTextView;

    private String selectedServiceId;
    private String selectedServiceName;
    private String selectedDate;
    private String selectedTime;

    private ServicesAdapter servicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        serviceRecyclerView = findViewById(R.id.serviceListView);
        bookButton = findViewById(R.id.saveServicesButton);
        selectedDateTimeTextView = findViewById(R.id.selectedDateTimeTextView);
        selectedDateButton = findViewById(R.id.selectDate);

        serviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        populateServiceList(""); // Передаем пустую строку для категории

        selectedDateButton.setOnClickListener(v -> showDatePicker());
        bookButton.setOnClickListener(v -> createAppointment());
    }

    private void populateServiceList(String category) {
        db.collection("Services")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<Services> services = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String serviceName = document.getString("name");
                            String description = document.getString("description");
                            Object costObject = document.get("cost");
                            String serviceCategory = document.getString("category");
                            int cost = 0;
                            if (costObject != null) {
                                try {
                                    cost = Integer.parseInt(costObject.toString());
                                } catch (NumberFormatException e) {
                                    Log.e("UserActivity", "Invalid cost format", e);
                                }
                            }

                            if (serviceName != null && description != null) {
                                // Фильтруем по категории, если она задана
                                if (category.isEmpty() || (serviceCategory != null && serviceCategory.equals(category))) {
                                    services.add(new Services(serviceName, description, cost, serviceCategory));
                                }
                            }
                        }

                        servicesAdapter = new ServicesAdapter(services, null);
                        servicesAdapter.setOnItemClickListener(service -> {
                            selectedServiceId = "serviceId" + services.indexOf(service);
                            selectedServiceName = service.getName();
                        });
                        serviceRecyclerView.setAdapter(servicesAdapter);
                    } else {
                        Toast.makeText(this, "No services found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading services.", Toast.LENGTH_SHORT).show();
                    Log.e("UserActivity", "Error loading services", e);
                });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                    showTimePicker();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime = hourOfDay + ":" + (minute < 10 ? "0" + minute : minute);
                    selectedDateTimeTextView.setText("Выбрано: " + selectedDate + " " + selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void createAppointment() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedServiceId == null || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Please select all fields (service, date, time)", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String userEmail = auth.getCurrentUser().getEmail();

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("clientId", userId);
        appointment.put("clientName", userEmail);
        appointment.put("serviceId", selectedServiceId);
        appointment.put("serviceName", selectedServiceName);
        appointment.put("date", selectedDate);
        appointment.put("time", selectedTime);

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Appointment successfully created!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating appointment", Toast.LENGTH_SHORT).show();
                    Log.e("UserActivity", "Error creating appointment", e);
                });
    }
}
