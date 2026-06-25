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
        holder.itemView.setOnClickListener(v -> launchBuyUrl(item.getPurchaseUrl()));
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
