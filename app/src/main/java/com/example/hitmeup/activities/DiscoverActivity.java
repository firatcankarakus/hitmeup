package com.example.hitmeup.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.hitmeup.R;
import com.example.hitmeup.adapters.EventAdapter;
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

public class DiscoverActivity extends AppCompatActivity {
    private static final String TAG = "DiscoverActivity";

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;

    private ProgressBar progressBar;

    private List<Event> eventList;

    private DatabaseReference reference;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(DiscoverActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        buildRecyclerView();
        progressBar = findViewById(R.id.progress_loading_events);

        findViewById(R.id.btn_create_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DiscoverActivity.this, CreateEventActivity.class));
            }
        });

        readEvents();
    }

    private void readEvents() {
        reference = FirebaseDatabase.getInstance().getReference("Events");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event = dataSnapshot.getValue(Event.class);

                    eventList.add(event);
                }

                eventAdapter = new EventAdapter(eventList, DiscoverActivity.this);
                recyclerView.setAdapter(eventAdapter);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void buildRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView_events);
        recyclerView.setHasFixedSize(true);

        eventList = new ArrayList<>();

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    }
}