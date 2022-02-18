package com.example.hitmeup.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hitmeup.R;
import com.example.hitmeup.model.Event;
import com.example.hitmeup.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SingleEventActivity extends AppCompatActivity {
    private static final String TAG = "SingleEventActivity";

    private TextView tv_eventName, tv_author, tv_eventLocation, tv_eventDate, tv_minAge;
    private ImageView img_eventPic;
    private Button btn_participants, btn_participate;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_event);
        init();

        setEvent();

        btn_participants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Event event = (Event) intent.getSerializableExtra("Event");
                Log.d(TAG, "onClick: " + event.getEventName());

                Intent intent = new Intent(SingleEventActivity.this, ParticipantsActivity.class);
                intent.putExtra("SingleEvent", event);
                startActivity(intent);
            }
        });

        btn_participate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                participate();
            }
        });
    }

    private void participate() {
        final Event currentEvent = (Event) intent.getSerializableExtra("Event");
        Log.d(TAG, "participate: " + currentEvent);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Events");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String id = "";
                HashMap<String, Object> hashMap = new HashMap<>();

                List<String> participants;

                for (final DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    final Event event = dataSnapshot.getValue(Event.class);

                    if (currentEvent.getAuthor().equals(event.getAuthor()) &&
                            currentEvent.getEventName().equals(event.getEventName())) {

                        id = dataSnapshot.getKey();
                        participants = event.getParticipants();

                        if (!participants.contains(user.getUid())) {
                            participants.add(user.getUid());
                            Toast.makeText(SingleEventActivity.this, "You are now a participant of this event", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(SingleEventActivity.this, "You are already participated to this event.", Toast.LENGTH_SHORT).show();

                        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User currentUser = snapshot.getValue(User.class);

                                if(currentUser.getParticipatedEvents() != null) {
                                    List<String> participatedEvents = currentUser.getParticipatedEvents();

                                    if(!participatedEvents.contains(dataSnapshot.getKey())) {
                                        participatedEvents.add(dataSnapshot.getKey());

                                        HashMap<String, Object> participatedEventMap = new HashMap<>();
                                        participatedEventMap.put("participatedEvents", participatedEvents);

                                        userReference.updateChildren(participatedEventMap);
                                    }
                                }
                                else {
                                    List<String> participatedEvents = new ArrayList<>();

                                    participatedEvents.add(dataSnapshot.getKey());

                                    HashMap<String, Object> participatedEventMap = new HashMap<>();
                                    participatedEventMap.put("participatedEvents", participatedEvents);

                                    userReference.updateChildren(participatedEventMap);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        hashMap.put("participants", participants);
                    }
                }

                reference.child(id).updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SingleEventActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setEvent() {
        final Event currentEvent = (Event) intent.getSerializableExtra("Event");

        if (currentEvent == null) return;

        String eventName = currentEvent.getEventName();
        String eventLocation = currentEvent.getEventLocation();
        String eventDate = currentEvent.getEventDate();
        String eventImage = currentEvent.getImageURL();
        int minAge = currentEvent.getMinAge();

        tv_eventName.setText("Event Name: " + eventName);
        tv_eventLocation.setText("Event Location: " + eventLocation);
        tv_eventDate.setText("Event Date: " + eventDate);
        tv_minAge.setText("Age Limit: " + minAge);

        if (currentEvent.getImageURL().equals("default"))
            Glide.with(this).load(R.drawable.sinema).into(img_eventPic);
        else
            Glide.with(this).load(eventImage).into(img_eventPic);

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Events");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event = dataSnapshot.getValue(Event.class);

                    if (currentEvent.getAuthor().equals(event.getAuthor()) &&
                            currentEvent.getEventName().equals(event.getEventName())) {

                        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("Users").child(event.getAuthor());
                        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                tv_author.setText("Event Author: " + user.getName() + " " + user.getSurname());

                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                                if (user.getId().equals(currentUser.getUid())) {
                                    //btn_participate.setVisibility(View.GONE);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init() {
        tv_eventName = findViewById(R.id.tv_event_name);
        tv_author = findViewById(R.id.tv_event_author);
        tv_eventLocation = findViewById(R.id.tv_event_location);
        tv_eventDate = findViewById(R.id.tv_event_date);
        tv_minAge = findViewById(R.id.tv_min_age);

        img_eventPic = findViewById(R.id.img_event_pic);

        btn_participants = findViewById(R.id.btn_participants);
        btn_participate = findViewById(R.id.btn_participate);

        intent = getIntent();
    }
}