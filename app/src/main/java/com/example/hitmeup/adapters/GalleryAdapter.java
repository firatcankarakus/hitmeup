package com.example.hitmeup.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hitmeup.R;
import com.example.hitmeup.activities.MessageActivity;
import com.example.hitmeup.model.Gallery;
import com.example.hitmeup.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private Context context;
    private List<Gallery> images;

    public GalleryAdapter(Context context, List<Gallery> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.single_gallery_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Gallery currentImage = images.get(position);

        Glide.with(context)
                .load(currentImage.getImageURL())
                .into(holder.img_gallery);

        holder.tv_author.setText(currentImage.getAuthor());
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView img_gallery;
        public TextView tv_author;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            img_gallery = itemView.findViewById(R.id.img_gallery);
            tv_author = itemView.findViewById(R.id.tv_gallery_author);
        }
    }
}
