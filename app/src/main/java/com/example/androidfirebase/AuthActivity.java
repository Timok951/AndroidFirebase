package com.example.androidfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {


    private EditText emailField, passwordField;
    private Button loginButton, registerButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.log);
        registerButton = findViewById(R.id.reg);


        loginButton.setOnClickListener(v -> {
            loginUser();
        });

        registerButton.setOnClickListener(v -> {
            registerUser();
        });


    }

    private void loginUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRole();
                    } else {
                        Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 5) {
            Toast.makeText(this, "Пароль должен быть не менее 5 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                        saveUserToFirestore();

                        loginUser();
                    } else {
                        if (task.getException() != null) {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(this, "Ошибка регистрации: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void saveUserToFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.e("Firestore", "Ошибка: пользователь не найден");
            return;
        }

        String userId = user.getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("role", "user");

        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Пользователь сохранен в Firestore"))
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка сохранения пользователя", e));
    }


    private void checkUserRole() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;  // Прерываем выполнение, если пользователя нет
        }


        String userId = user.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("admin".equals(role)) {
                            startActivity(new Intent(this, AdminActivity.class));
                        } else {
                            startActivity(new Intent(this, UserActivity.class));
                        }
                        finish();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при получении роли", e));
    }
}
