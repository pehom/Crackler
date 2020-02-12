package com.android.pehom.cracklechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private final static String TAG = "detective";
    private static final int RC_IMAGE_PICKER = 669;
    private ListView messageListView;
    private MessageAdapter adapter;
    private ProgressBar progressBar;
    private  ImageButton sendMessageImageButton;
    private ImageButton sendPhotoImageButton;
    private EditText messageEditText;
    private List<MessageModel> messageList;
    private FirebaseDatabase database;
    private DatabaseReference messagesDatabaseReference;
    private ChildEventListener messagesChildEventListener;
    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;
    private FirebaseStorage storage;
    private StorageReference chatImagesStorageReference;
    private FirebaseAuth auth;


    private String userName= "default user";
    private String recipientUserId;
    private String recipientUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("userName");
            recipientUserId = intent.getStringExtra("recipientUserId");
            recipientUserName = intent.getStringExtra("recipientUserName");
        }
        setTitle("Crackling with " + recipientUserName);

        database = FirebaseDatabase.getInstance();
        messagesDatabaseReference = database.getReference().child("messages");
        usersDatabaseReference = database.getReference().child("users");
        storage = FirebaseStorage.getInstance();
        chatImagesStorageReference = storage.getReference().child("chat_images");

        messageListView = findViewById(R.id.messageListView);
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, R.layout.message_item, messageList);
        messageListView.setAdapter(adapter);

        sendMessageImageButton = findViewById(R.id.sendImageButton);
        setEnabledImageButton(sendMessageImageButton, false);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        sendPhotoImageButton = findViewById(R.id.sendPhotoImageButton);
        messageEditText = findViewById(R.id.messageEditText);




        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) setEnabledImageButton(sendMessageImageButton, true);
                else setEnabledImageButton(sendMessageImageButton, false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        messageEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(500) });


        sendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageModel message = new MessageModel();
                message.setText(messageEditText.getText().toString());
                message.setName(userName);
                message.setSender(auth.getCurrentUser().getUid());
                message.setRecipient(recipientUserId);
                message.setImageUrl(null);
                messagesDatabaseReference.push().setValue(message);

                messageEditText.setText("");
            }
        });

        sendPhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Select image"), RC_IMAGE_PICKER);



            }
        });
        usersChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                userName = user.getName();
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

        messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);

                if (messageModel.getSender().equals(auth.getCurrentUser().getUid()) &&
                messageModel.getRecipient().equals(recipientUserId)) {
                    messageModel.setIsMine(true);
                    // sender speaks

                    adapter.add(messageModel);


                } else if ( messageModel.getSender().equals(recipientUserId) &&
                                messageModel.getRecipient().equals(auth.getCurrentUser().getUid())) {
                    messageModel.setIsMine(false);
                    //recipient speaks

                    adapter.add(messageModel);
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

        messagesDatabaseReference.addChildEventListener(messagesChildEventListener);


    }

    private  void setEnabledImageButton (ImageButton button, boolean isEnabled) {
        if (!isEnabled) {
           // button.setAlpha(75);
            button.setClickable(false);
            button.setBackgroundColor(getResources().getColor(R.color.buttonDisabled));
        }
        else {
           // button.setAlpha(100);
            button.setClickable(true);
            button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        }
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
                startActivity(new Intent(ChatActivity.this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            final StorageReference selectedImageReference = chatImagesStorageReference.
                    child(selectedImageUri.getLastPathSegment());
            UploadTask uploadTask = selectedImageReference.putFile(selectedImageUri);


            uploadTask = selectedImageReference.putFile(selectedImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return selectedImageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        MessageModel message = new MessageModel();
                        Log.d(TAG, "downloadUri = " + downloadUri.toString() );
                        message.setImageUrl(downloadUri.toString());
                        message.setName(userName);
                        message.setSender(auth.getCurrentUser().getUid());
                        message.setRecipient(recipientUserId);
                        messagesDatabaseReference.push().setValue(message);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });


        }
    }
}
