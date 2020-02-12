package com.android.pehom.cracklechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {
    private final static String TAG = "freshUser";
    private FirebaseAuth auth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText nicknameEditText;
    private EditText repeatPasswordEditText;
    private TextView loginTextView;
    private Button signUpButton;
    private boolean loginModeActive;
    private FirebaseDatabase database;
    private DatabaseReference usersDatabaseReference;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        database = FirebaseDatabase.getInstance();
        usersDatabaseReference = database.getReference().child("users");
        auth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nicknameEditText= findViewById(R.id.nicknameEditText);
        loginTextView = findViewById(R.id.loginTextView);
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText);

        if (auth.getCurrentUser() != null) {

            Intent intent = new Intent(SignInActivity.this, UserListActivity.class);
                 //   intent.putExtra("userName", currentUserName);
            startActivity(intent);

        }

        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSignUpUser(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim());
            }
        });

    }

    private void loginSignUpUser(String email, String password) {
        if (loginModeActive) {
            if (passwordEditText.getText().toString().trim().length() < 6) {
                Toast.makeText(SignInActivity.this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
            } else if (emailEditText.getText().toString().trim().length() < 1) {
                Toast.makeText(SignInActivity.this, "Enter your email", Toast.LENGTH_LONG).show();
            } else {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("currentUserName", "signInWithEmail:success");



                                    Intent intent = new Intent(SignInActivity.this, UserListActivity.class);
                                    startActivity(intent);

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }

                                // ...
                            }
                        });
            }


        } else {
            if (!passwordEditText.getText().toString().trim().equals(
                    repeatPasswordEditText.getText().toString().trim())) {
                Toast.makeText(SignInActivity.this, "Passwords don't match", Toast.LENGTH_LONG).show();
            } else if (passwordEditText.getText().toString().trim().length() < 6) {
                Toast.makeText(SignInActivity.this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
            } else if (emailEditText.getText().toString().trim().length() < 1) {
                Toast.makeText(SignInActivity.this, "Enter your email", Toast.LENGTH_LONG).show();
            }

                else {

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = auth.getCurrentUser();
                                    createUser(user);
                                    Toast.makeText(SignInActivity.this, "Successful  reg", Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(SignInActivity.this, UserListActivity.class);

                                    intent.putExtra("userName", nicknameEditText.getText().toString().trim());
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_LONG).show();
                                    //   updateUI(null);
                                }
                            }
                        });
            }

        }


    }

    public void toggleLoginMode(View view) {
        if (loginModeActive) {
            loginModeActive = false;
            repeatPasswordEditText.setVisibility(View.VISIBLE);
            nicknameEditText.setVisibility(View.VISIBLE);
            signUpButton.setText("Sign up");
            loginTextView.setText("or log in");
        } else {
            loginModeActive = true;
            repeatPasswordEditText.setVisibility(View.GONE);
            nicknameEditText.setVisibility(View.GONE);
            signUpButton.setText("Log in");
            loginTextView.setText("or sign up");
        }

    }

    private void createUser(FirebaseUser firebaseUser) {
        User user = new User();
        user.setEmail(firebaseUser.getEmail());
        user.setId(firebaseUser.getUid());
        user.setName(nicknameEditText.getText().toString().trim());
        usersDatabaseReference.child(firebaseUser.getUid()).setValue(user);
    }
}
