package com.example.hitmeup.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hitmeup.R;
import com.example.hitmeup.adapters.ParticipantsAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private TextView tv_name, tv_surname, tv_email, tv_screenTitle, tv_titleFollows;
    private CircleImageView img_profileImage;
    private ProgressBar progressBar;

    private RecyclerView recyclerView;
    private ParticipantsAdapter followsAdapter;

    private Button btn_addFriends, btn_message;

    private FirebaseUser currentUser;
    private DatabaseReference reference;

    private StorageReference storageReference;
    public static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        img_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!intent.hasExtra("ParticipantProfile")) {
                    openImage();
                }
            }
        });

        if(!intent.hasExtra("ParticipantProfile")) {
            setCurrentUser(reference);
            btn_addFriends.setVisibility(View.GONE);
            btn_message.setVisibility(View.GONE);
        }
        else {
            setParticipantProfile();
            btn_addFriends.setVisibility(View.VISIBLE);
            tv_titleFollows.setVisibility(View.GONE);
        }

        btn_addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
            }
        });

        btn_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = intent.getStringExtra("ParticipantProfile");

                Intent intent = new Intent(ProfileActivity.this, MessageActivity.class);
                intent.putExtra("userId", id);
                startActivity(intent);
            }
        });

    }

    private void addFriend() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String id = intent.getStringExtra("ParticipantProfile");

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if(user.getFollows() != null) {
                    List<String> participantIds = user.getFollows();
                    if (!participantIds.contains(id)) {
                        participantIds.add(id);

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("follows", participantIds);

                        reference.updateChildren(hashMap);
                        Toast.makeText(ProfileActivity.this, "You are now following this user.", Toast.LENGTH_SHORT).show();
                    }
                    else Toast.makeText(ProfileActivity.this, "You are already following this user.", Toast.LENGTH_SHORT).show();
                }
                else {
                    List<String> participantIds = new ArrayList<>();
                    participantIds.add(id);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("follows", participantIds);

                    reference.updateChildren(hashMap);
                    Toast.makeText(ProfileActivity.this, "You are now following this user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void buildFollowsRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView_follows);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final User user = snapshot.getValue(User.class);

                if (user.getFollows() != null) {

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
                    reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<User> follows = new ArrayList<>();
                            
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                User user1 = dataSnapshot.getValue(User.class);
                                
                                if(user.getFollows().contains(user1.getId())) {
                                    follows.add(user1);
                                }
                            }
                            
                            followsAdapter = new ParticipantsAdapter(ProfileActivity.this, follows);
                            recyclerView.setAdapter(followsAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setParticipantProfile() {
        String id = intent.getStringExtra("ParticipantProfile");

        reference = FirebaseDatabase.getInstance().getReference("Users").child(id);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                String name = user.getName();
                String surname = user.getSurname();
                String imgURL = user.getImageURL();
                String email = currentUser.getEmail();

                tv_name.setText("Name: " + name);
                tv_surname.setText("Surname: " + surname);
                tv_email.setText("E-Mail: ********");
                tv_screenTitle.setText(name + " " + surname);

                if (imgURL.equals("default")) {
                    img_profileImage.setImageResource(R.drawable.ic_profile);
                } else {
                    Glide.with(getApplicationContext())
                            .load(imgURL)
                            .into(img_profileImage);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver resolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if(imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        reference.updateChildren(map);

                        progressDialog.dismiss();
                    }
                    else {
                        Toast.makeText(ProfileActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
        else {
            Toast.makeText(ProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            if(data != null && data.getData() != null) {
                imageUri = data.getData();

                if(uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(this, "Upload is in progress", Toast.LENGTH_SHORT).show();
                }
                else {
                    uploadImage();
                }
            }
        }
    }

    private void setCurrentUser(DatabaseReference reference) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                String name = user.getName();
                String surname = user.getSurname();
                String imgURL = user.getImageURL();
                String email = currentUser.getEmail();

                tv_name.setText("Name: " + name);
                tv_surname.setText("Surname: " + surname);
                tv_email.setText("E-Mail: " + email);

                if (imgURL.equals("default")) {
                    img_profileImage.setImageResource(R.drawable.ic_profile);
                } else {
                    Glide.with(getApplicationContext())
                            .load(imgURL)
                            .into(img_profileImage);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        buildFollowsRecyclerView();
    }

    private void init() {
        tv_name = findViewById(R.id.tv_name);
        tv_surname = findViewById(R.id.tv_surname);
        tv_email = findViewById(R.id.tv_email);
        progressBar = findViewById(R.id.progress_loading_image);
        tv_screenTitle = findViewById(R.id.tv_title_profile);
        tv_titleFollows = findViewById(R.id.tv_follows_title);

        btn_addFriends = findViewById(R.id.btn_add_friends);
        btn_message = findViewById(R.id.btn_message);

        img_profileImage = findViewById(R.id.img_profile_pic);

        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        intent = getIntent();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
    }
}