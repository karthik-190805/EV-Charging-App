package com.example.evapp.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evapp.R;
import com.example.evapp.model.Car;
import com.example.evapp.util.ApiClient;
import com.example.evapp.util.SessionManager;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    private SessionManager session;
    private Car selectedCar;
    private TextView tvCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_sign_up);

        session = new SessionManager(this);

        // Views
        EditText etFirst = findViewById(R.id.etName);
        EditText etLast = findViewById(R.id.etLast);
        EditText etEmail = findViewById(R.id.etId);
        EditText etMobile = findViewById(R.id.etMobile);
        EditText etPwd = findViewById(R.id.etPwd);
        EditText etPwd2 = findViewById(R.id.etPwd2);
        tvCar = findViewById(R.id.tvCar);

        Button btnPickCar = findViewById(R.id.btnPickCar);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        Button btnBack = findViewById(R.id.btnBack);

        // Pick car
        btnPickCar.setOnClickListener(v -> loadCarsAndPick());

        // Submit
        btnSubmit.setOnClickListener(v -> {
            String first = etFirst.getText().toString().trim();
            String last = etLast.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String pw1 = etPwd.getText().toString();
            String pw2 = etPwd2.getText().toString();

            if (TextUtils.isEmpty(first) || TextUtils.isEmpty(last) ||
                    TextUtils.isEmpty(email) || TextUtils.isEmpty(mobile) ||
                    TextUtils.isEmpty(pw1) || TextUtils.isEmpty(pw2)) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pw1.equals(pw2)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCar == null) {
                Toast.makeText(this, "Pick your car", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user + profile
            String carJson = ApiClient.GSON.toJson(selectedCar);
            session.saveUser(first + " " + last, email, carJson);
            session.setPassword(pw1);
            session.saveProfile(first, last, email, mobile, ""); // vehicle no. empty for now

            Toast.makeText(this, "Signed up. Please login.", Toast.LENGTH_SHORT).show();
            finish(); // back to LoginActivity
        });

        // Back
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadCarsAndPick() {
        Request req = new Request.Builder().url(ApiClient.BASE + "/cars").build();
        ApiClient.HTTP.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(SignUpActivity.this, "Failed to load cars", Toast.LENGTH_SHORT).show()
                );
            }

            @Override public void onResponse(Call call, Response resp) throws IOException {
                if (!resp.isSuccessful()) return;
                String body = resp.body().string();
                Type t = new TypeToken<List<Car>>(){}.getType();
                List<Car> cars = ApiClient.GSON.fromJson(body, t);
                runOnUiThread(() -> showCarPicker(cars));
            }
        });
    }

    private void showCarPicker(List<Car> carList) {
        final List<Car> cars = (carList != null) ? carList : new ArrayList<>();

        String[] names = new String[cars.size()];
        for (int i = 0; i < cars.size(); i++) names[i] = cars.get(i).name;

        new android.app.AlertDialog.Builder(this)
                .setTitle("Select your car")
                .setItems(names, (d, which) -> {
                    selectedCar = cars.get(which);
                    tvCar.setText(selectedCar.name + " — " +
                            selectedCar.batteryCapacity + "kWh, " +
                            selectedCar.chargeSpeed + "kW");
                })
                .show();
    }

}
