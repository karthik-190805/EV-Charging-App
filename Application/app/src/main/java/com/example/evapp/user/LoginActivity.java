package com.example.evapp.user;

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

public class LoginActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_login);

        session = new SessionManager(this);

        // --- 🧩 DUMMY USER SEED ---
        // If no user exists, create a demo account (mobile: 1234567890, password: abc12345)
        if (session.getUserId() == null || session.getPassword() == null) {
            Car demoCar = new Car();
            demoCar.name = "Tata Nexon EV";
            demoCar.batteryCapacity = 40.0;
            demoCar.chargeSpeed = 30.0;

            String demoEmail = "evapp@gmail.com";
            String demoName = "EV App User";
            String carJson = ApiClient.GSON.toJson(demoCar);

            // Save base session data
            session.saveUser(demoName, demoEmail, carJson);
            session.setPassword("abc12345");
            session.saveProfile(
                    "EV", "User",
                    demoEmail,
                    "1234567890",
                    "TN-00-0000"
            );
        }

        // --- UI Elements ---
        EditText etId = findViewById(R.id.etId);
        EditText etPwd = findViewById(R.id.etPwd);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Button btnBack = findViewById(R.id.btnBack);
        TextView linkForgot = findViewById(R.id.linkForgot);
        TextView linkSignUp = findViewById(R.id.linkSignUp);

        // --- Login Button Logic ---
        btnSubmit.setOnClickListener(v -> {
            String id = etId.getText().toString().trim();
            String pw = etPwd.getText().toString();

            String savedUserId = safe(session.getUserId());
            String savedEmail = safe(session.getEmail());
            String savedMobile = safe(session.getMobile());
            String savedPwd = safe(session.getPassword());

            boolean idMatches =
                    id.equals(savedMobile) ||
                            id.equalsIgnoreCase(savedEmail) ||
                            id.equalsIgnoreCase(savedUserId);

            if (idMatches && pw.equals(savedPwd)) {
                Toast.makeText(this, "✅ Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "❌ Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        linkForgot.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        linkSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
