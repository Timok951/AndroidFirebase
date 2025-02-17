package com.example.androidfirebase;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    private ListView serviceListView;
    private Button bookButton, selectedDatebutton;
    private TextView selectedDateTimeTextView;

    private String selectedServiceId;
    private String selectedServiceName;
    private String selectedDate;
    private String selectedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        serviceListView = findViewById(R.id.serviceListView);
        bookButton = findViewById(R.id.saveServicesButton);
        selectedDateTimeTextView = findViewById(R.id.selectedDateTimeTextView);
        selectedDatebutton = findViewById(R.id.selectDate);



        populateServiceList();

        serviceListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedServiceId = "serviceId" + position;
            selectedServiceName = "Service " + (position + 1);
        });


        selectedDatebutton.setOnClickListener(v -> {
            showDatePicker();


        });



        bookButton.setOnClickListener(v -> {
            createAppointment();
        });
    }

    private void populateServiceList() {
        db.collection("Services")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        List<Services> services = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String serviceName = document.getString("name");
                            String description = document.getString("description");

                            // Проверка на null для цены
                            Object priceObject = document.get("cost");
                            int price = 0;
                            if (priceObject != null) {
                                try {
                                    price = Integer.parseInt(priceObject.toString());
                                } catch (NumberFormatException e) {
                                    Log.e("UserActivity", "Invalid price format", e);
                                }
                            }

                            if (serviceName != null && description != null) {
                                services.add(new Services(serviceName, description, price));
                            }
                        }

                        ServicesAdapter adapter = new ServicesAdapter(this, services);
                        serviceListView.setAdapter(adapter);
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
                    showTimePicker(); // После выбора даты сразу открываем выбор времени
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
