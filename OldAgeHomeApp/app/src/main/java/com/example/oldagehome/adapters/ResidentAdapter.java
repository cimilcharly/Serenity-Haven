package com.example.oldagehome.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oldagehome.databinding.ItemResidentBinding;
import com.example.oldagehome.models.ResidentModel;
import java.util.List;

public class ResidentAdapter extends RecyclerView.Adapter<ResidentAdapter.ResidentViewHolder> {

    private List<ResidentModel> residentList;

    public ResidentAdapter(List<ResidentModel> residentList) {
        this.residentList = residentList;
    }

    @NonNull
    @Override
    public ResidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemResidentBinding binding = ItemResidentBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false);
        return new ResidentViewHolder(binding);
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ResidentModel resident);

        void onDeleteClick(ResidentModel resident);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ResidentViewHolder holder, int position) {
        ResidentModel resident = residentList.get(position);
        holder.binding.nameTv.setText(resident.getName());
        holder.binding.roomTv.setText("Room: " + resident.getRoomNumber());
        holder.binding.ageTv.setText(resident.getAge() + " Yrs");

        holder.binding.conditionTv
                .setText(resident.getMedicalHistory() != null && !resident.getMedicalHistory().isEmpty()
                        ? resident.getMedicalHistory()
                        : "No conditions listed");

        String visitInfo = "Next Visit: Not Scheduled";
        if (resident.getDoctorVisitDate() != null && !resident.getDoctorVisitDate().isEmpty()) {
            visitInfo = "Next Visit: " + resident.getDoctorVisitDate();
            if (resident.getDoctorVisitTime() != null && !resident.getDoctorVisitTime().isEmpty()) {
                visitInfo += " at " + resident.getDoctorVisitTime();
            }
        }
        holder.binding.doctorVisitTv.setText(visitInfo);

        // Bind Medicines
        String medString = "None";
        if (resident.getMedicines() != null && !resident.getMedicines().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (com.example.oldagehome.models.MedicineModel med : resident.getMedicines()) {
                if (med.getMedicineName() != null && !med.getMedicineName().isEmpty()) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(med.getMedicineName());
                }
            }
            if (sb.length() > 0)
                medString = sb.toString();
        }
        holder.binding.medicinesTv.setText(medString);

        holder.binding.btnEditMedicines.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(),
                    com.example.oldagehome.activities.MedicineActivity.class);
            intent.putExtra("residentId", resident.getId());
            v.getContext().startActivity(intent);
        });

        // Bind Profile Image - Placeholder Only
        holder.binding.ivResidentProfile.setImageResource(android.R.drawable.ic_menu_myplaces);
        holder.binding.ivResidentProfile.setPadding(8, 8, 8, 8);

        holder.binding.btnEditResident.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(),
                    com.example.oldagehome.activities.AddResidentActivity.class);
            intent.putExtra("residentId", resident.getId());
            v.getContext().startActivity(intent);
        });

        holder.binding.btnDeleteResident.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(resident);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            // Updated to use the passed listener for item click instead of hardcoded
            if (listener != null) {
                listener.onItemClick(resident);
            }
        });
    }

    @Override
    public int getItemCount() {
        return residentList.size();
    }

    static class ResidentViewHolder extends RecyclerView.ViewHolder {
        ItemResidentBinding binding;

        public ResidentViewHolder(@NonNull ItemResidentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
