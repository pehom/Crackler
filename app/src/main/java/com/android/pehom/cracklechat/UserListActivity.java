package com.android.pehom.cracklechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {
    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;

    private ArrayList<User> userArrayList;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager userLayoutManager;

    private FirebaseAuth auth;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {

            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").
                    child(auth.getCurrentUser().getUid());

            currentUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User currentUser = dataSnapshot.getValue(User.class);
                    userName = currentUser.getName();
                    setTitle("Crackler - " + userName);
                    Log.d("userName", "userName = " + userName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }


        userArrayList = new ArrayList<>();


        attachUserDatabaseReferenceListener();
        buildRecyclerView();

    }

    private void attachUserDatabaseReferenceListener() {
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        if (usersChildEventListener == null) {
            usersChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    User user = dataSnapshot.getValue(User.class);


                    if (!user.getId().equals(auth.getCurrentUser().getUid())) {
                        user.setAvatarMockUpResource(R.drawable.ic_person_gray_24dp);
                        userArrayList.add(user);
                        userAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            usersDatabaseReference.addChildEventListener(usersChildEventListener);
        }
    }

    private void buildRecyclerView() {
        userRecyclerView = findViewById(R.id.userListRecyclerView);
        userRecyclerView.setHasFixedSize(true);

        userLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(userRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        userRecyclerView.addItemDecoration(dividerItemDecoration);
        userAdapter = new UserAdapter(userArrayList);
        userAdapter.setOnUserClickListener(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(int position) {
                goToChat(position);
            }
        });

        userRecyclerView.setLayoutManager(userLayoutManager);
        userRecyclerView.setAdapter(userAdapter);
    }

    private void goToChat(int position) {
        Intent intent = new Intent(UserListActivity.this, ChatActivity.class);
        intent.putExtra("recipientUserId", userArrayList.get(position).getId());
        intent.putExtra("userName", userName);
        intent.putExtra("recipientUserName",userArrayList.get(position).getName() );
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserListActivity.this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
