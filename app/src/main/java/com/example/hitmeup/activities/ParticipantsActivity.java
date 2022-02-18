package com.example.hitmeup.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.example.hitmeup.R;
import com.example.hitmeup.adapters.ParticipantsAdapter;
import com.example.hitmeup.adapters.UsersAdapter;
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
import java.util.List;

public class ParticipantsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ParticipantsAdapter usersAdapter;

    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);
        buildRecyclerView();

        intent = getIntent();

        readParticipants();
    }

    private void readParticipants() {
        final Event currentEvent = (Event) intent.getSerializableExtra("SingleEvent");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Events");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event = dataSnapshot.getValue(Event.class);

                    if (currentEvent.getAuthor().equals(event.getAuthor()) &&
                            currentEvent.getEventName().equals(event.getEventName())) {

                        final List<String> participantIds = event.getParticipants();

                        final List<User> participants = new ArrayList<>();

                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
                        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                                for(DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
                                    User user = dataSnapshot1.getValue(User.class);

                                    for(String id : participantIds) {
                                        if(user.getId().equals(id)) {
                                            if(!user.getId().equals(currentUser.getUid())) {
                                                participants.add(user);
                                            }
                                        }
                                    }
                                }

                                usersAdapter = new ParticipantsAdapter(ParticipantsActivity.this, participants);
                                recyclerView.setAdapter(usersAdapter);
                                findViewById(R.id.progress_participants).setVisibility(View.GONE);
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

    private void buildRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView_participants);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }
}