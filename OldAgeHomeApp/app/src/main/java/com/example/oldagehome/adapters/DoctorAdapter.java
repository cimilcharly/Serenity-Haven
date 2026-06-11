package com.example.oldagehome.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oldagehome.databinding.ItemDoctorBinding;
import com.example.oldagehome.models.DoctorModel;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<DoctorModel> doctorList;

    public DoctorAdapter(List<DoctorModel> doctorList) {
        this.doctorList = doctorList;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDoctorBinding binding = ItemDoctorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DoctorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        DoctorModel doctor = doctorList.get(position);
        holder.binding.nameTv.setText(doctor.getName());
        holder.binding.specializationTv.setText(doctor.getSpecialization());
        holder.binding.availabilityTv.setText("Avail: " + doctor.getAvailableDays());
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        ItemDoctorBinding binding;

        public DoctorViewHolder(@NonNull ItemDoctorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
