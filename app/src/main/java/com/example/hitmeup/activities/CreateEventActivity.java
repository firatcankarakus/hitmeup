package com.example.hitmeup.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hitmeup.R;
import com.example.hitmeup.model.Event;
import com.example.hitmeup.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CreateEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "CreateEventActivity";

    public static final int IMAGE_REQUEST_CODE = 1;

    private TextView tv_date;
    private EditText et_eventName, et_eventLocation, et_ageLimit;
    private ImageView img_eventImage;

    private String date;

    private Uri imageUri;

    private StorageReference storageReference;
    private StorageTask storageTask;
    private FirebaseUser currentUser;
    private DatabaseReference reference;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        init();

        Calendar calendar = Calendar.getInstance();
        date = DateFormat.getDateInstance().format(calendar.getTime());
        tv_date.setText(date);

        findViewById(R.id.layout_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePickerDialog = new com.example.hitmeup.fragments.DatePickerDialog();
                datePickerDialog.show(getSupportFragmentManager(), "Pick a Date");
            }
        });

        findViewById(R.id.btn_set_event_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        findViewById(R.id.img_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEvent();
            }
        });
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    private void createEvent() {
        String eventName = et_eventName.getText().toString();
        String eventLocation = et_eventLocation.getText().toString();
        int ageLimit;

        if (!et_ageLimit.getText().toString().isEmpty())
            ageLimit = Integer.parseInt(et_ageLimit.getText().toString());
        else
            ageLimit = 13;

        if (eventName.trim().isEmpty() || eventLocation.trim().isEmpty()) {
            Toast.makeText(this, "Event name or location cannot e empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadEvent(currentUser.getUid(), eventName, eventLocation, date, ageLimit);

        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final User user = snapshot.getValue(User.class);
                final HashMap<String, Object> participatedEventMap = new HashMap<>();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Events");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> participatedEvents;

                        if(user.getParticipatedEvents() != null) {
                            participatedEvents = user.getParticipatedEvents();
                        }
                        else {
                            participatedEvents = new ArrayList<>();
                        }

                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Event event = dataSnapshot.getValue(Event.class);

                            if(event.getParticipants().contains(currentUser.getUid())){
                                participatedEvents.add(dataSnapshot.getKey());
                            }
                        }

                        participatedEventMap.put("participatedEvents", participatedEvents);

                        userReference.updateChildren(participatedEventMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        finish();
    }

    private void uploadEvent(final String author, final String eventName, final String eventLocation, final String eventDate, final int minAge) {
        final List<String> participants = new ArrayList<>();
        participants.add(currentUser.getUid());

        if(imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            storageTask = fileReference.putFile(imageUri);
            storageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String imageURL = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("author", author);
                        hashMap.put("eventName", eventName);
                        hashMap.put("eventLocation", eventLocation);
                        hashMap.put("eventDate", eventDate);
                        hashMap.put("minAge", minAge);
                        hashMap.put("imageURL", imageURL);
                        hashMap.put("participants", participants);

                        reference.child("Events").push().setValue(hashMap);
                    }

                    else {
                        Toast.makeText(CreateEventActivity.this, "Upload failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateEventActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        else {
            reference = FirebaseDatabase.getInstance().getReference();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("author", author);
            hashMap.put("eventName", eventName);
            hashMap.put("eventLocation", eventLocation);
            hashMap.put("eventDate", eventDate);
            hashMap.put("minAge", minAge);
            hashMap.put("imageURL", "default");
            hashMap.put("participants", participants);

            reference.child("Events").push().setValue(hashMap);
        }

    }

    private String getFileExtension(Uri imageUri) {
        ContentResolver resolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(imageUri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                Log.d(TAG, "onActivityResult: " + imageUri);

                Glide.with(this)
                        .load(imageUri)
                        .into(img_eventImage);

                if (storageTask != null && storageTask.isInProgress()) {
                    Toast.makeText(this, "Upload is in progress.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        date = DateFormat.getDateInstance().format(calendar.getTime());
        tv_date.setText(date);
    }

    private void init() {
        tv_date = findViewById(R.id.tv_date);
        img_eventImage = findViewById(R.id.img_event_pic);

        et_eventName = findViewById(R.id.et_event_name);
        et_eventLocation = findViewById(R.id.et_event_location);
        et_ageLimit = findViewById(R.id.et_age_limit);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
    }

}