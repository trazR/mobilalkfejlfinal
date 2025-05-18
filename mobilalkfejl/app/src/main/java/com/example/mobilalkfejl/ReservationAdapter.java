package com.example.mobilalkfejl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
    private List<Reservation> reservations;
    private OnReservationClickListener listener;
    private boolean showEditButton;

    public interface OnReservationClickListener {
        void onEdit(Reservation reservation);
        void onDelete(Reservation reservation);
    }

    public ReservationAdapter(List<Reservation> reservations, OnReservationClickListener listener, boolean showEditButton) {
        this.reservations = reservations;
        this.listener = listener;
        this.showEditButton = showEditButton;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvStartTimestamp, tvDuration, tvTableCount, tvWholePlace, tvPhone;
        public Button btnEdit, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvStartTimestamp = itemView.findViewById(R.id.tvStartTimestamp);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvTableCount = itemView.findViewById(R.id.tvTableCount);
            tvWholePlace = itemView.findViewById(R.id.tvWholePlace);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public ReservationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReservationAdapter.ViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);

        String formattedTimestamp = formatTimestamp(reservation.getStartTimestamp());

        holder.tvStartTimestamp.setText("Kezdés: " + formattedTimestamp);
        holder.tvDuration.setText("Időtartam: " + reservation.getDuration() + " óra");
        holder.tvTableCount.setText("Asztalok száma: " + reservation.getTableCount());
        holder.tvWholePlace.setText("Teljes hely: " + (reservation.isWholePlace() ? "Igen" : "Nem"));
        holder.tvPhone.setText("Telefonszám: " + reservation.getPhone());

        if (showEditButton) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(reservation));
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnEdit.setOnClickListener(null);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(reservation));
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
        notifyDataSetChanged();
    }

    private String formatTimestamp(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isEmpty()) return "";

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = isoFormat.parse(isoTimestamp);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return isoTimestamp;
        }
    }
}
