package com.example.oldagehome.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oldagehome.databinding.ItemPendingResidentBinding;
import com.example.oldagehome.models.ResidentModel;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class PendingResidentAdapter extends RecyclerView.Adapter<PendingResidentAdapter.ViewHolder> {

    private List<ResidentModel> residents;
    private FirebaseFirestore db;

    public PendingResidentAdapter(List<ResidentModel> residents) {
        this.residents = residents;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPendingResidentBinding binding = ItemPendingResidentBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResidentModel resident = residents.get(position);
        holder.binding.tvName.setText(resident.getName());
        holder.binding.tvAge.setText(String.valueOf(resident.getAge()));
        holder.binding.tvGender.setText(resident.getGender());

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
        String dateStr = sdf.format(new java.util.Date(resident.getAdmissionDate()));
        holder.binding.tvDate.setText("Applied: " + dateStr);

        if (resident.getProfileImageUrl() != null && !resident.getProfileImageUrl().isEmpty()) {
            holder.binding.ivAvatar.setPadding(0, 0, 0, 0);
            holder.binding.ivAvatar.setBackground(null);
            holder.binding.ivAvatar.setImageTintList(null);
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(resident.getProfileImageUrl())
                    .circleCrop()
                    .into(holder.binding.ivAvatar);
        } else {
            holder.binding.ivAvatar.setImageResource(com.example.oldagehome.R.drawable.app_logo);
            // 12dp to px
            int padding = (int) (12 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            holder.binding.ivAvatar.setPadding(padding, padding, padding, padding);
            holder.binding.ivAvatar.setBackgroundResource(com.example.oldagehome.R.drawable.bg_btn_gradient);
            holder.binding.ivAvatar
                    .setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        }

        holder.binding.btnApprove.setOnClickListener(v -> {
            String commId = resident.getCommunityId();
            if (commId != null) {
                // Update Global Registry (for login)
                db.collection("users").document(resident.getId()).update("status", "approved");
                // Update Community Vault (The "Separate Database")
                db.collection("communities").document(commId)
                        .collection("members").document(resident.getId()).update("status", "approved");
            }
        });

        holder.binding.btnDecline.setOnClickListener(v -> {
            String commId = resident.getCommunityId();
            if (commId != null) {
                // Remove from Global Registry
                db.collection("users").document(resident.getId()).delete();
                // Remove from Community Vault
                db.collection("communities").document(commId)
                        .collection("members").document(resident.getId()).delete();
            }
        });
    }

    @Override
    public int getItemCount() {
        return residents.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPendingResidentBinding binding;

        public ViewHolder(ItemPendingResidentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
