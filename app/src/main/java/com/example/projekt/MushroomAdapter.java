package com.example.projekt;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MushroomAdapter extends RecyclerView.Adapter<MushroomAdapter.MushroomViewHolder> {

    private List<Mushroom> mushroomList;
    private final Context context;

    public MushroomAdapter(List<Mushroom> mushroomList, Context context) {
        this.mushroomList = mushroomList;
        this.context = context;
    }

    public void setMushrooms(List<Mushroom> mushrooms) {
        this.mushroomList = mushrooms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MushroomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mushroom, parent, false);
        return new MushroomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MushroomViewHolder holder, int position) {
        Mushroom mushroom = mushroomList.get(position);
        holder.tvName.setText(mushroom.nazwa);

        // Dynamiczne pobieranie ID zasobu z drawable na podstawie nazwy z bazy danych
        int imageId = context.getResources().getIdentifier(mushroom.nazwaObrazka, "drawable", context.getPackageName());
        if (imageId != 0) {
            holder.ivImage.setImageResource(imageId);
        } else {
            holder.ivImage.setImageResource(R.drawable.grzyb_glowna); // fallback
        }

        // Obsługa kliknięcia
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MushroomDetailActivity.class);
            // Możemy przekazać ID grzyba, aby wyświetlić detale
            intent.putExtra("MUSHROOM_ID", mushroom.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mushroomList != null ? mushroomList.size() : 0;
    }

    static class MushroomViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivImage;

        public MushroomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMushroomName);
            ivImage = itemView.findViewById(R.id.ivMushroomItem);
        }
    }
}