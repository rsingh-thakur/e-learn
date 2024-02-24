package com.nrt.e_learning;

import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nrt.e_learning.model.UserModel;
import com.nrt.e_learning.util.AndroidUtil;
import com.nrt.e_learning.util.FirebaseUtil;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;



public class UserSignUpActivity extends AppCompatActivity {


    private EditText usernameEditText, emailEditText, phoneEditText, passwordEditText;
    private Button signUpButton;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;


    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
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
        progressBar.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(phoneNumber)) {
            // Show a message on the UI
            Toast.makeText(this, "All fields are mandatory to fill", Toast.LENGTH_LONG).show();
            return;
        }

        signUpUsertoAUTH(email,username,phoneNumber,password);

    }



    private void signUpUsertoAUTH(String email, String username, String phoneNumber, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // Email is already in use
                                Toast.makeText(UserSignUpActivity.this, "Email is already in use. Please use a different email.", Toast.LENGTH_LONG).show();
                            }
                            else {
                                    createUserInFireStoreCollection(email,username,phoneNumber,password);
                                }
                            // Sign up success, update UI or navigate to the next screen
                               }
                        else {
                            progressBar.setVisibility(View.GONE);
                                // Other errors during signup
                                Toast.makeText(UserSignUpActivity.this, "Sign up failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                }});
           }


    private void createUserInFireStoreCollection(String email, String username, String phoneNumber, String password) {

        String creationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        // Create a User object
        UserModel user = new UserModel(username, email, "", phoneNumber, password, creationDate, FirebaseUtil.currentUserId());

        // Add the user object to Firestore
        firestore.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // User signed up successfully
                        AndroidUtil.showToast(getApplicationContext(), "User signed up successfully!");
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error occurred while saving data to Firestore
                        AndroidUtil.showToast(getApplicationContext(), "Failed to sign up. Please try again.");
                    }
                });
    }
}


