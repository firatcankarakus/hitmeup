package com.example.hitmeup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.hitmeup.R;
import com.google.firebase.auth.FirebaseAuth;

public class MoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        LinearLayout layout_logout = findViewById(R.id.layout_logout);
        layout_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MoreActivity.this, StartingActivity.class));
                finish();
            }
        });

        LinearLayout layout_profile = findViewById(R.id.layout_profile);
        layout_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MoreActivity.this, ProfileActivity.class));
            }
        });

    }
}