package com.example.samplefirebase.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.samplefirebase.R;
import com.example.samplefirebase.modals.FaceLandmarkData;

import java.util.List;

public class FaceBitmapAdapter extends RecyclerView.Adapter<FaceBitmapAdapter.ViewHolder> {

    private Context context;
    private List<FaceLandmarkData> faceLandmarkData;
    LayoutInflater inflater;


    public FaceBitmapAdapter(Context context, List<FaceLandmarkData> faceLandmarkData) {
        this.context = context;
        this.faceLandmarkData = faceLandmarkData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.face_bitmap_card, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // loading bitmap from The list obtained
        holder.imageView.setImageBitmap(faceLandmarkData.get(position).getImageBitmap());
    }

    @Override
    public int getItemCount() {
        return faceLandmarkData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
