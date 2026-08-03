package com.example.evapp.server;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evapp.R;
import com.example.evapp.util.SessionManager;

public class ForgotPasswordActivity_server extends AppCompatActivity {
    private SessionManager session;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_forgot);
        session = new SessionManager(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        EditText etContact = findViewById(R.id.etContact);
        findViewById(R.id.btnSendOtp).setOnClickListener(v -> {
            String contact = etContact.getText().toString().trim();
            if (TextUtils.isEmpty(contact)) {
                Toast.makeText(this, "Enter email or phone", Toast.LENGTH_SHORT).show();
                return;
            }
            // demo OTP
            session.setOtp("123456");
            Toast.makeText(this, "OTP sent: 123456 (demo)", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, OtpActivity_server.class));
        });
    }
}
