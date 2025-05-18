package com.example.mobilalkfejl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReservationActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1;

    RadioGroup rgWholePlace, rgTableCount;
    EditText etDate, etTime, etDuration, etPhone;
    Button btnSubmit;
    FirebaseFirestore db;
    FirebaseUser currentUser;

    Calendar selectedDateTime = Calendar.getInstance();

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_reservation);

        rgWholePlace = findViewById(R.id.rgWholePlace);
        rgTableCount = findViewById(R.id.rgTableCount);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etDuration = findViewById(R.id.etDuration);
        etPhone = findViewById(R.id.etPhone);
        btnSubmit = findViewById(R.id.btnSubmitReservation);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }

        etDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDateTime.getTime()));
            }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH)).show();
        });

        etTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), true).show();
        });

        btnSubmit.setOnClickListener(v -> {
            int selectedPlaceOption = rgWholePlace.getCheckedRadioButtonId();
            int selectedTableOption = rgTableCount.getCheckedRadioButtonId();

            String durationStr = etDuration.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (selectedPlaceOption == -1 || selectedTableOption == -1 || TextUtils.isEmpty(durationStr) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Kérlek, tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean wholePlace = selectedPlaceOption == R.id.rbYes;
            int tableCount = Integer.parseInt(((RadioButton) findViewById(selectedTableOption)).getText().toString().split(" ")[0]);
            int duration = Integer.parseInt(durationStr);

            Map<String, Object> reservation = new HashMap<>();
            reservation.put("userId", currentUser.getUid());
            reservation.put("startTimestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(selectedDateTime.getTime()));
            reservation.put("duration", duration);
            reservation.put("tableCount", tableCount);
            reservation.put("wholePlace", wholePlace);
            reservation.put("phone", phone);
            reservation.put("createdAt", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date()));

            db.collection("reservations")
                    .add(reservation)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Foglalás sikeres!", Toast.LENGTH_SHORT).show();

                        Calendar notificationTime = (Calendar) selectedDateTime.clone();
                        notificationTime.add(Calendar.HOUR, -1);

                        Intent intent = new Intent(getApplicationContext(), ReminderBroadcast.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                getApplicationContext(),
                                0,
                                intent,
                                PendingIntent.FLAG_IMMUTABLE
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        if (alarmManager != null) {
                            alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    notificationTime.getTimeInMillis(),
                                    pendingIntent
                            );
                        }

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Értesítési engedély megadva", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Értesítési engedély megtagadva, az értesítések nem fognak megjelenni.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
