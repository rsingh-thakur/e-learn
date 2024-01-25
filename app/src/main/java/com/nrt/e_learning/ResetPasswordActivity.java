package com.nrt.e_learning;


import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.nrt.e_learning.util.AndroidUtil;


public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button resetPasswordButton;
    private ProgressBar resetPasswordProgressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        resetPasswordProgressBar = findViewById(R.id.resetPasswordProgressBar);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user's email from the intent
                String email = getIntent().getStringExtra("USER_EMAIL_EXTRA").trim();

                // Get the new password from the EditText
                String newPassword = newPasswordEditText.getText().toString().trim();

                // Call the method to reset the password
                resetPassword(email);
            }
        });
    }

    private void resetPassword(String email) {
        resetPasswordProgressBar.setVisibility(View.VISIBLE);

        // Send a password reset email to the user
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    resetPasswordProgressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Password reset email sent successfully
                        AndroidUtil.showToast(getApplicationContext(), "Password reset email sent.");
                        finish(); // Close the ResetPasswordActivity
                    } else {
                        // Password reset failed
                        AndroidUtil.showToast(getApplicationContext(), "Failed to send password reset email.");
                    }
                });
    }

















//        private void readDataAndUpdatePassword(String email, String newPassword) {
//            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
//
//            usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.exists()) {
//                        // User with this email exists in the database
//
//                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
//                            // Assuming there is only one user with this email, but you can adapt accordingly
//                            String userId = userSnapshot.getKey();
//
//                            // Update the password
//                            usersRef.child(userId).child("password").setValue(newPassword)
//                                    .addOnCompleteListener(updateTask -> {
//                                        if (updateTask.isSuccessful()) {
//                                            // Password update successful
//                                            Toast.makeText(ResetPasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
//                                        //    readUserData(email); // Optionally, read and display other user data
//                                        } else {
//                                            // Password update failed
//                                            Toast.makeText(ResetPasswordActivity.this, "Failed to update password. " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                        }
//                    } else {
//                        // User with this email doesn't exist in the database
//                        Toast.makeText(ResetPasswordActivity.this, "User with this email not found in the database.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    // Handle potential errors
//                    Toast.makeText(ResetPasswordActivity.this, "Failed to fetch data. Please try again.", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }

}

