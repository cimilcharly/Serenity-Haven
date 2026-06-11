package com.example.oldagehome.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oldagehome.databinding.ItemAppointmentBinding;
import com.example.oldagehome.models.AppointmentModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<AppointmentModel> appointmentList;

    public AppointmentAdapter(List<AppointmentModel> appointmentList) {
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppointmentBinding binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new AppointmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        AppointmentModel appointment = appointmentList.get(position);
        holder.binding.statusTv.setText("Status: " + appointment.getStatus());
        holder.binding.notesTv.setText("Notes: " + appointment.getNotes());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.binding.dateTv.setText("Date: " + sdf.format(new Date(appointment.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        ItemAppointmentBinding binding;

        public AppointmentViewHolder(@NonNull ItemAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
