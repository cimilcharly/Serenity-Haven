package com.example.oldagehome.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oldagehome.R;
import com.example.oldagehome.models.StoreMedicineModel;
import java.util.List;

public class StoreMedicineAdapter extends RecyclerView.Adapter<StoreMedicineAdapter.ViewHolder> {

    private final List<StoreMedicineModel> medicineList;
    private final Context context;

    public StoreMedicineAdapter(Context context, List<StoreMedicineModel> medicineList) {
        this.context = context;
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoreMedicineModel item = medicineList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvDesc.setText(item.getDescription());
        holder.tvPrice.setText(item.getPrice());
        holder.tvPlatform.setText(item.getPlatform());

        // Dynamic branding color for tag badge
        String platform = item.getPlatform().toLowerCase();
        if (platform.contains("amazon")) {
            holder.tvPlatform.setBackgroundColor(android.graphics.Color.parseColor("#FF9900"));
        } else if (platform.contains("flipkart")) {
            holder.tvPlatform.setBackgroundColor(android.graphics.Color.parseColor("#2874F0"));
        } else if (platform.contains("1mg") || platform.contains("tata")) {
            holder.tvPlatform.setBackgroundColor(android.graphics.Color.parseColor("#1AAB9F"));
        } else {
            holder.tvPlatform.setBackgroundColor(android.graphics.Color.parseColor("#00BCD4"));
        }

        if (item.getImageResId() != 0) {
            holder.ivIcon.setImageResource(item.getImageResId());
        } else {
            holder.ivIcon.setImageResource(R.drawable.med_tablet_strip);
        }

        holder.btnBuy.setOnClickListener(v -> launchBuyUrl(item.getPurchaseUrl()));
        holder.itemView.setOnClickListener(v -> showMedicineDetailsDialog(item));
    }

    private void showMedicineDetailsDialog(StoreMedicineModel item) {
        try {
            com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = 
                    new com.google.android.material.bottomsheet.BottomSheetDialog(context);
            
            View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_medicine_details, null);
            bottomSheetDialog.setContentView(sheetView);

            ImageView ivDetailImage = sheetView.findViewById(R.id.ivDetailImage);
            TextView tvDetailName = sheetView.findViewById(R.id.tvDetailName);
            TextView tvDetailCategory = sheetView.findViewById(R.id.tvDetailCategory);
            TextView tvDetailPrice = sheetView.findViewById(R.id.tvDetailPrice);
            TextView tvDetailDesc = sheetView.findViewById(R.id.tvDetailDesc);
            TextView tvDetailSafetyTip = sheetView.findViewById(R.id.tvDetailSafetyTip);
            Button btnDetailBuy = sheetView.findViewById(R.id.btnDetailBuy);

            tvDetailName.setText(item.getName());
            tvDetailCategory.setText(item.getCategory() != null ? item.getCategory() : "General");
            tvDetailPrice.setText(item.getPrice());
            tvDetailDesc.setText(item.getDescription());
            tvDetailSafetyTip.setText(item.getSafetyTip() != null ? item.getSafetyTip() : "Use as directed by your physician or pharmacist.");

            if (item.getImageResId() != 0) {
                ivDetailImage.setImageResource(item.getImageResId());
            } else {
                ivDetailImage.setImageResource(R.drawable.med_tablet_strip);
            }

            btnDetailBuy.setText("Buy on " + item.getPlatform() + " - " + item.getPrice());
            btnDetailBuy.setOnClickListener(v -> {
                launchBuyUrl(item.getPurchaseUrl());
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchBuyUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(context, "Cannot open purchase link: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice, tvPlatform;
        ImageView ivIcon;
        Button btnBuy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvDesc = itemView.findViewById(R.id.tvMedicineDesc);
            tvPrice = itemView.findViewById(R.id.tvMedicinePrice);
            tvPlatform = itemView.findViewById(R.id.tvPlatformBadge);
            ivIcon = itemView.findViewById(R.id.ivMedicineIcon);
            btnBuy = itemView.findViewById(R.id.btnBuyNow);
        }
    }
}
