package com.example.evapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    Button btnUser, btnServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        btnUser = findViewById(R.id.btnUser);
        btnServer = findViewById(R.id.btnServer);

        // 👤 User button → go to User Login
        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, com.example.evapp.user.LoginActivity.class);
            startActivity(intent);
        });

        // 🖥️ Server button → go to Server Login
        btnServer.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, com.example.evapp.server.LoginActivity_server.class);
            startActivity(intent);
        });
    }
}
