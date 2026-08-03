package com.example.evapp.server;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SignUpActivity_server extends AppCompatActivity {

    private static final String FILE_NAME = "server_signup_data.json";

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword,
            etStationName, etStationId, etCompany, etStationType, etNumChargers,
            etAddress, etCity, etState, etPincode, etLatitude, etLongitude,
            etOperatingHours, etPrice, etPaymentMethods, etMaintenanceContact, etAuthCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_activity_sign_up); // reuse same layout or make a server-specific one

        // Initialize views
        etFullName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etId);
        etPhone = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPwd);
        etConfirmPassword = findViewById(R.id.etPwd2);
        etStationName = findViewById(R.id.etStationName);
        etStationId = findViewById(R.id.etStationId);
        etCompany = findViewById(R.id.etCompany);
        etStationType = findViewById(R.id.etStationType);
        etNumChargers = findViewById(R.id.etNumChargers);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        etPincode = findViewById(R.id.etPincode);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etOperatingHours = findViewById(R.id.etOperatingHours);
        etPrice = findViewById(R.id.etPrice);
        etPaymentMethods = findViewById(R.id.etPaymentMethods);
        etMaintenanceContact = findViewById(R.id.etMaintenanceContact);
        etAuthCode = findViewById(R.id.etAuthCode);

        Button btnSubmit = findViewById(R.id.btnSubmit);
        Button btnBack = findViewById(R.id.btnBack);

        btnSubmit.setOnClickListener(v -> saveServerSignup());
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveServerSignup() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String confirmPass = etConfirmPassword.getText().toString();

        // Validate mandatory fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create JSON object for new server
            JSONObject newServer = new JSONObject();
            newServer.put("id", etStationId.getText().toString().trim());

            JSONObject basic = new JSONObject();
            basic.put("full_name", name);
            basic.put("email_address", email);
            basic.put("phone_number", phone);
            basic.put("password", pass);
            basic.put("confirm_password", confirmPass);
            newServer.put("basic_account_details", basic);

            JSONObject org = new JSONObject();
            org.put("station_name", etStationName.getText().toString().trim());
            org.put("station_id_code", etStationId.getText().toString().trim());
            org.put("organization_company_name", etCompany.getText().toString().trim());
            org.put("station_type", etStationType.getText().toString().trim());
            org.put("number_of_chargers_installed", etNumChargers.getText().toString().trim());
            newServer.put("organization_station_details", org);

            JSONObject location = new JSONObject();
            location.put("address", etAddress.getText().toString().trim());
            location.put("city", etCity.getText().toString().trim());
            location.put("state", etState.getText().toString().trim());
            location.put("pincode", etPincode.getText().toString().trim());
            location.put("latitude", etLatitude.getText().toString().trim());
            location.put("longitude", etLongitude.getText().toString().trim());
            newServer.put("location_details", location);

            JSONObject ops = new JSONObject();
            ops.put("operating_hours", etOperatingHours.getText().toString().trim());
            ops.put("pricing_per_kwh_inr", etPrice.getText().toString().trim());
            ops.put("payment_methods_accepted", etPaymentMethods.getText().toString().trim());
            ops.put("maintenance_contact", etMaintenanceContact.getText().toString().trim());
            newServer.put("station_operation_details", ops);

            JSONObject verify = new JSONObject();
            verify.put("authorization_code", etAuthCode.getText().toString().trim());
            verify.put("id_proof_upload", "");
            verify.put("station_photo_upload", "");
            newServer.put("verification_access", verify);

            JSONObject consent = new JSONObject();
            consent.put("authorized_confirmation", true);
            consent.put("agreed_to_terms", true);
            newServer.put("consent_terms", consent);

            // Load existing JSON
            JSONObject root = loadJSON();
            JSONArray list = root.optJSONArray("server_signups");
            if (list == null) list = new JSONArray();

            list.put(newServer);
            root.put("server_signups", list);

            // Save updated file
            saveJSON(root);

            Toast.makeText(this, "Server signed up successfully", Toast.LENGTH_SHORT).show();
            finish();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON data", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONObject loadJSON() {
        try {
            // Copy from assets to internal storage if not already
            File file = new File(getFilesDir(), FILE_NAME);
            if (!file.exists()) {
                InputStream is = getAssets().open("assets_server/" + FILE_NAME);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                is.close();
                fos.close();
            }

            // Read file
            InputStream is = openFileInput(FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            return new JSONObject(json);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    private void saveJSON(JSONObject jsonObject) {
        try {
            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(jsonObject.toString(4).getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
