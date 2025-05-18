package com.example.mobilalkfejl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditReservationActivity extends AppCompatActivity {
    private EditText etStartTimestamp, etDuration, etTableCount, etPhone;
    private Switch switchWholePlace;
    private Button btnSave;
    private FirebaseFirestore db;
    private String reservationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reservation);
        getSupportActionBar().hide();

        etStartTimestamp = findViewById(R.id.etStartTimestamp);
        etDuration = findViewById(R.id.etDuration);
        etTableCount = findViewById(R.id.etTableCount);
        etPhone = findViewById(R.id.etPhone);
        switchWholePlace = findViewById(R.id.switchWholePlace);
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();
        reservationId = getIntent().getStringExtra("reservationId");

        loadReservationData();

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadReservationData() {
        db.collection("reservations").document(reservationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Reservation reservation = documentSnapshot.toObject(Reservation.class);
                        etStartTimestamp.setText(reservation.getStartTimestamp());
                        etDuration.setText(String.valueOf(reservation.getDuration()));
                        etTableCount.setText(String.valueOf(reservation.getTableCount()));
                        etPhone.setText(reservation.getPhone());
                        switchWholePlace.setChecked(reservation.isWholePlace());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveChanges() {
        String startTimestamp = etStartTimestamp.getText().toString();
        int duration = Integer.parseInt(etDuration.getText().toString());
        int tableCount = Integer.parseInt(etTableCount.getText().toString());
        String phone = etPhone.getText().toString();
        boolean wholePlace = switchWholePlace.isChecked();

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("startTimestamp", startTimestamp);
        updatedData.put("duration", duration);
        updatedData.put("tableCount", tableCount);
        updatedData.put("phone", phone);
        updatedData.put("wholePlace", wholePlace);

        db.collection("reservations").document(reservationId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Foglalás frissítve", Toast.LENGTH_SHORT).show();


                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
