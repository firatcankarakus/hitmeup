package com.example.hitmeup.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.hitmeup.R;
import com.example.hitmeup.model.Gallery;
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

import java.util.HashMap;
import java.util.Objects;

public class CreateImageActivity extends AppCompatActivity {
    private static final String TAG = "CreateImageActivity";

    private ImageView image;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_image);
        init();

        findViewById(R.id.img_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver resolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(CreateImageActivity.this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("uploads").child("gallery");
        StorageTask<UploadTask.TaskSnapshot> uploadTask;

        if (imageUri != null) {
            final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = reference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Gallery");
                    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    Uri mUri = task.getResult();
                    final String imageUri = mUri.toString();

                    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("imageURL", imageUri);
                            map.put("author", user.getName() + " " + user.getSurname());

                            databaseReference.push().setValue(map);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    progressDialog.dismiss();

                    Intent intent = new Intent(CreateImageActivity.this, GalleryActivity.class);
                    startActivity(intent);
                    finish();
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private void init() {
        Intent intent = getIntent();
        image = findViewById(R.id.img_gallery);

        String uriString = intent.getStringExtra("Image_Uri");
        imageUri = Uri.parse(uriString);
        Glide.with(this)
                .load(imageUri)
                .into(image);
    }

}