package com.example.hitmeup.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.hitmeup.R;
import com.example.hitmeup.adapters.UsersAdapter;
import com.example.hitmeup.model.Chat;
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


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private List<User> users;

    private FirebaseUser fUser;
    private DatabaseReference reference;

    private List<String> userList;

    private ProgressBar progressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_chats, container, false);

        progressBar = v.findViewById(R.id.progress_loading_chats);

        recyclerView = v.findViewById(R.id.recyclerView_chats);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        userList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);


                    if(chat.getSender().equals(fUser.getUid())) {
                        if(!userList.contains(chat.getReceiver())) {
                            userList.add(chat.getReceiver());
                        }
                    }
                    if(chat.getReceiver().equals(fUser.getUid())) {
                        if(!userList.contains(chat.getSender())) {
                            userList.add(chat.getSender());
                        }
                    }
                }

                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return v;
    }

    private void readChats() {
        users = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();

                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    for(String id: userList) {
                        if(user.getId().equals(id)){
                            if(users.size() != 0) {
                                for(User user1: users) {
                                    if(!user.getId().equals(user1.getId())) {
                                        users.add(user);
                                        break;
                                    }
                                }
                            }
                            else {
                                users.add(user);
                                break;
                            }
                        }
                    }

                    usersAdapter = new UsersAdapter(getContext(), users);
                    recyclerView.setAdapter(usersAdapter);

                    progressBar.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}