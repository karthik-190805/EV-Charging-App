package com.example.evapp.server;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evapp.R;
import com.example.evapp.util.SessionManager;

public class OtpActivity_server extends AppCompatActivity {
    private SessionManager session;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_otp);
        session = new SessionManager(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        EditText etOtp = findViewById(R.id.etOtp);

        findViewById(R.id.btnVerify).setOnClickListener(v -> {
            String expected = session.getOtp(); // "123456" in demo
            if (etOtp.getText().toString().trim().equals(expected)) {
                session.clearOtp();
                startActivity(new Intent(this, NewPasswordActivity_server.class));
            } else {
                Toast.makeText(this, "Wrong OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
