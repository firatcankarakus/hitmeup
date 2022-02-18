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
import com.example.hitmeup.activities.SingleEventActivity;
import com.example.hitmeup.model.Event;
import com.example.hitmeup.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;

    public EventAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.single_event_item, parent, false);

        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventViewHolder holder, int position) {
        final Event currentEvent = eventList.get(position);

        if(currentEvent.getImageURL().equals("default"))
            Glide.with(context).load(R.drawable.sinema).into(holder.img_eventImage);
        else
            Glide.with(context).load(currentEvent.getImageURL()).into(holder.img_eventImage);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(currentEvent.getAuthor());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                holder.tv_eventAuthor.setText(user.getName() + " " + user.getSurname());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.tv_eventName.setText(currentEvent.getEventName());
        holder.tv_eventLocation.setText(currentEvent.getEventLocation());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SingleEventActivity.class);
                intent.putExtra("Event", currentEvent);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        private ImageView img_eventImage;
        private TextView tv_eventName, tv_eventAuthor, tv_eventLocation;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            img_eventImage = itemView.findViewById(R.id.img_event_pic);
            tv_eventName = itemView.findViewById(R.id.tv_event_name);
            tv_eventAuthor = itemView.findViewById(R.id.tv_event_author);
            tv_eventLocation = itemView.findViewById(R.id.tv_event_location);
        }
    }
}
