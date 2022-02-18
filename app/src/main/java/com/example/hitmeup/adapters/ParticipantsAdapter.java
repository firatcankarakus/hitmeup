package com.example.hitmeup.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hitmeup.R;
import com.example.hitmeup.activities.MessageActivity;
import com.example.hitmeup.activities.ProfileActivity;
import com.example.hitmeup.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {

    private Context context;
    private List<User> users;

    public ParticipantsAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ParticipantsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.single_user_item, parent, false);

        return new ParticipantsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantsAdapter.ViewHolder holder, int position) {
        final User currentUser = users.get(position);

        holder.name.setText(currentUser.getName() + " " + currentUser.getSurname());

        if (currentUser.getImageURL().equals("default")) {
            holder.img_profilePic.setImageResource(R.drawable.ic_profile);
        } else {
            Glide.with(context)
                    .load(currentUser.getImageURL())
                    .into(holder.img_profilePic);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("ParticipantProfile", currentUser.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView img_profilePic;
        public TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            img_profilePic = itemView.findViewById(R.id.img_otheruser_profile_image);
            name = itemView.findViewById(R.id.tv_otheruser_name);
        }
    }
}
