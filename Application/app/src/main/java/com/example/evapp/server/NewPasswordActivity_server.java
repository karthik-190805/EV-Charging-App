package com.example.evapp.server;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evapp.R;
import com.example.evapp.util.SessionManager;

public class NewPasswordActivity_server extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_new_password);

        session = new SessionManager(this);

        EditText etNew = findViewById(R.id.etNew);
        EditText etNew2 = findViewById(R.id.etNew2);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnBack = findViewById(R.id.btnBack);

        // Back button action
        btnBack.setOnClickListener(v -> finish());

        // Save button action
        btnSave.setOnClickListener(v -> {
            String p1 = etNew.getText().toString().trim();
            String p2 = etNew2.getText().toString().trim();

            if (TextUtils.isEmpty(p1) || TextUtils.isEmpty(p2)) {
                Toast.makeText(this, "Enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!p1.equals(p2)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            session.setPassword(p1); // save the new password locally
            Toast.makeText(this, "Password updated! Please login again.", Toast.LENGTH_SHORT).show();
            finish(); // back to LoginActivity
        });
    }
}
