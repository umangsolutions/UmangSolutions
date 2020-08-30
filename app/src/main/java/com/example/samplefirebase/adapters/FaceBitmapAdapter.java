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

import java.util.List;

public class FaceBitmapAdapter extends RecyclerView.Adapter<FaceBitmapAdapter.ViewHolder> {

    Context context;
    List<Bitmap> bitmaps;
    LayoutInflater inflater;


    public FaceBitmapAdapter(Context context, List<Bitmap> bitmaps) {
        this.context = context;
        this.bitmaps = bitmaps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View view = inflater.inflate(R.layout.face_bitmap_card, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // loading bitmap from The list obtained
        holder.imageView.setImageBitmap(bitmaps.get(position));
    }

    @Override
    public int getItemCount() {
        return bitmaps.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.helloView);
        }
    }
}
