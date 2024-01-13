package com.nrt.e_learning;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nrt.e_learning.model.UserModel;
import com.nrt.e_learning.util.AndroidUtil;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;



public class UserSignUpActivity extends AppCompatActivity {


    private EditText usernameEditText, emailEditText, phoneEditText, passwordEditText;
    private Button signUpButton;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;



    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize Views
        usernameEditText = findViewById(R.id.usernameId);
        emailEditText = findViewById(R.id.emailId);
        phoneEditText = findViewById(R.id.phoneId);
        passwordEditText = findViewById(R.id.passwordId);
        signUpButton = findViewById(R.id.signUpButton);
        progressBar = findViewById(R.id.progressBarId);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    private void signUp() {
        // Get user input
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phoneNumber = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();


        // Check if the email is already in use
        checkIfEmailExists(email, new OnEmailCheckListener() {
            @Override
            public void onEmailCheck(boolean isExists) {
                if (isExists) {
                    // Email already exists, show an error message to the user
                    AndroidUtil.showToast(UserSignUpActivity.this, "Email is already registered. Please use a different email.");
                } else {

                    // Get current date and time
                    String creationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(Calendar.getInstance().getTime());

                    // Create a User object
                    UserModel user = new UserModel(username, email, phoneNumber, password, creationDate);

                    // Push the user object to Firebase database
                    String userId = databaseReference.push().getKey();
                    databaseReference.child(userId).setValue(user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                // Error occurred while saving data
                                AndroidUtil.showToast(UserSignUpActivity.this, "Failed to sign up. Please try again.");
                            } else {
                                // Data saved successfully
                                signUpUser(email, password);
                                AndroidUtil.showToast(UserSignUpActivity.this, "User signed up successfully!");
                                Intent intent = new Intent(UserSignUpActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }

    // Interface to handle the result of the email check
    interface OnEmailCheckListener {
        void onEmailCheck(boolean isExists);
    }

    // Method to check if the email already exists
    private void checkIfEmailExists(String email, OnEmailCheckListener listener) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    boolean isExists = !task.getResult().getSignInMethods().isEmpty();
                    listener.onEmailCheck(isExists);
                });
    }




    private void signUpUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, update UI or navigate to the next screen
                            Toast.makeText(UserSignUpActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // Email is already in use
                                Toast.makeText(UserSignUpActivity.this, "Email is already in use. Please use a different email.", Toast.LENGTH_LONG).show();
                            } else {
                                // Other errors during signup
                                Toast.makeText(UserSignUpActivity.this, "Sign up failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }});
    }



}


