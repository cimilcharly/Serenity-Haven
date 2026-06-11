package com.example.oldagehome.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oldagehome.databinding.ItemMedicineBinding;
import com.example.oldagehome.models.MedicineModel;

import java.util.List;
import java.util.Map;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private List<MedicineModel> medicines;

    public MedicineAdapter(List<MedicineModel> medicines) {
        this.medicines = medicines;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMedicineBinding binding = ItemMedicineBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false);
        return new ViewHolder(binding);
    }

    private OnMedicineActionListener actionListener;

    public interface OnMedicineActionListener {
        void onEdit(MedicineModel medicine);

        void onDelete(MedicineModel medicine);
    }

    public void setOnMedicineActionListener(OnMedicineActionListener listener) {
        this.actionListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineModel medicine = medicines.get(position);
        holder.binding.tvMedicineName.setText(medicine.getMedicineName());
        holder.binding.tvDosage.setText(medicine.getDosage());
        holder.binding.tvQuantity.setText("Available: " + medicine.getTotalQuantity());

        StringBuilder time = new StringBuilder();
        if (medicine.getExactTimes() != null) {
            for (String t : medicine.getExactTimes()) {
                time.append(t).append(", ");
            }
        } else {
            time.append("No time set");
        }

        String timeStr = time.toString();
        if (timeStr.endsWith(", ")) {
            timeStr = timeStr.substring(0, timeStr.length() - 2);
        }
        holder.binding.tvTime.setText(timeStr);

        holder.binding.btnEdit.setOnClickListener(v -> {
            if (actionListener != null)
                actionListener.onEdit(medicine);
        });

        holder.binding.btnDelete.setOnClickListener(v -> {
            if (actionListener != null)
                actionListener.onDelete(medicine);
        });
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemMedicineBinding binding;

        public ViewHolder(ItemMedicineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
