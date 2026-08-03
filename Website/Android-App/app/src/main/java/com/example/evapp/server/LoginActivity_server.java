package com.example.evapp.server;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evapp.R;
import com.example.evapp.model.Car;
import com.example.evapp.util.ApiClient;
import com.example.evapp.util.SessionManager;

public class LoginActivity_server extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_login);

        session = new SessionManager(this);

        // --- DEMO USER SEED ---
        if (session.getUserId() == null || session.getPassword() == null) {
            Car demoCar = new Car();
            demoCar.name = "Tata Nexon EV";
            demoCar.batteryCapacity = 40.0;
            demoCar.chargeSpeed = 30.0;

            String demoId = "evapp@gmail.com";
            String demoName = "EV App";
            String carJson = ApiClient.GSON.toJson(demoCar);

            session.saveUser(demoName, demoId, carJson);
            session.setPassword("abc12345");
            session.saveProfile("EV", "App", "evapp@gmail.com", "1234567890", "TN-00-0000");
        }

        EditText etId = findViewById(R.id.etId);
        EditText etPwd = findViewById(R.id.etPwd);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Button btnBack = findViewById(R.id.btnBack);
        TextView linkForgot = findViewById(R.id.linkForgot);
        TextView linkSignUp = findViewById(R.id.linkSignUp);

        btnSubmit.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String pw = etPwd.getText().toString();

            String savedUserId = safe(session.getUserId());
            String savedEmail  = safe(session.getEmail());
            String savedMobile = safe(session.getMobile());
            String savedPwd    = safe(session.getPassword());

            boolean idMatches =
                    id.equals(savedMobile) ||
                            id.equalsIgnoreCase(savedEmail) ||
                            id.equalsIgnoreCase(savedUserId);

            if (idMatches && pw.equals(savedPwd)) {
                startActivity(new Intent(LoginActivity_server.this, HomeActivity_server.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        linkForgot.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity_server.this, ForgotPasswordActivity_server.class)));

        linkSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity_server.this, SignUpActivity_server.class)));
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
