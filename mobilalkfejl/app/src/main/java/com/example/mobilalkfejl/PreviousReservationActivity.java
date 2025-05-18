package com.example.mobilalkfejl;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PreviousReservationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private List<Reservation> reservationList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private TextView tvReservationCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_my_reservations);

        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Korábbi foglalások");

        tvReservationCount = findViewById(R.id.tvReservationCount);

        TextView tvPreviousReservations = findViewById(R.id.tvPreviousReservations);
        tvPreviousReservations.setText("Aktuális foglalások");
        tvPreviousReservations.setOnClickListener(v -> {
            Intent intent = new Intent(PreviousReservationActivity.this, MyReservationsActivity.class);
            startActivity(intent);
            finish();
        });

        recyclerView = findViewById(R.id.recyclerView);
        reservationList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        adapter = new ReservationAdapter(reservationList, new ReservationAdapter.OnReservationClickListener() {
            @Override
            public void onEdit(Reservation reservation) {
            }

            @Override
            public void onDelete(Reservation reservation) {
                db.collection("reservations").document(reservation.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            reservationList.remove(reservation);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(PreviousReservationActivity.this, "Foglalás törölve", Toast.LENGTH_SHORT).show();
                            loadReservationCount(); // újraszámolás törlés után
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PreviousReservationActivity.this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }, false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadPastReservations();
        loadReservationCount();
    }

    private void loadPastReservations() {
        if (currentUser == null) {
            Toast.makeText(this, "Nincs bejelentkezett felhasználó.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("reservations")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("startTimestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reservationList.clear();
                    long now = System.currentTimeMillis();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Reservation reservation = doc.toObject(Reservation.class);
                        reservation.setId(doc.getId());

                        long startTime = parseTimestampToMillis(reservation.getStartTimestamp());

                        if (startTime < now) {
                            reservationList.add(reservation);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PreviousReservation", "Hiba a foglalások lekérdezésekor", e);
                });
    }

    private void loadReservationCount() {
        if (currentUser == null) return;

        db.collection("reservations")
                .whereEqualTo("userId", currentUser.getUid())
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener(snapshot -> {
                    long count = snapshot.getCount();
                    tvReservationCount.setText("Összes foglalásod: " + count);
                })
                .addOnFailureListener(e -> {
                    tvReservationCount.setText("Nem sikerült betölteni a foglalások számát.");
                });
    }

    private long parseTimestampToMillis(String isoTimestamp) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoTimestamp);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
