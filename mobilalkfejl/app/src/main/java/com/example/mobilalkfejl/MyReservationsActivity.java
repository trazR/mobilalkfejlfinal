package com.example.mobilalkfejl;

import android.content.Intent;
import android.os.Bundle;
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

public class MyReservationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private List<Reservation> reservationList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private TextView tvReservationCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);
        getSupportActionBar().hide();

        tvReservationCount = findViewById(R.id.tvReservationCount);

        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Aktuális foglalások");
        TextView tvPreviousReservations = findViewById(R.id.tvPreviousReservations);
        tvPreviousReservations.setText("Korábbi foglalások");

        recyclerView = findViewById(R.id.recyclerView);
        reservationList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        adapter = new ReservationAdapter(reservationList, new ReservationAdapter.OnReservationClickListener() {
            @Override
            public void onEdit(Reservation reservation) {
                Intent intent = new Intent(MyReservationsActivity.this, EditReservationActivity.class);
                intent.putExtra("reservationId", reservation.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Reservation reservation) {
                db.collection("reservations").document(reservation.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            reservationList.remove(reservation);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(MyReservationsActivity.this, "Foglalás törölve", Toast.LENGTH_SHORT).show();
                            loadReservationCount(); // Frissítjük a számot törlés után
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MyReservationsActivity.this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }, true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.tvPreviousReservations).setOnClickListener(v -> {
            Intent intent = new Intent(MyReservationsActivity.this, PreviousReservationActivity.class);
            startActivity(intent);
        });

        loadReservations();
        loadReservationCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReservations();
        loadReservationCount();
    }

    private void loadReservations() {
        if (currentUser == null) {
            Toast.makeText(this, "Nincs bejelentkezett felhasználó.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("reservations")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("startTimestamp")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reservationList.clear();
                    long now = System.currentTimeMillis();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Reservation reservation = doc.toObject(Reservation.class);
                        reservation.setId(doc.getId());

                        long reservationStartTime = parseTimestampToMillis(reservation.getStartTimestamp());

                        if (reservationStartTime > now) {
                            reservationList.add(reservation);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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


    private long parseTimestampToMillis(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(timestamp);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
