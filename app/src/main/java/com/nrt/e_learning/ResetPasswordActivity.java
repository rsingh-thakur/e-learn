package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize UI components
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please enter both new and confirmed passwords.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement logic to update the user's password in Firebase or your authentication system
        // You can use the userEmail received as an extra from the intent to identify the user
        updatePassword(newPassword);
        // Once password reset is successful, you can navigate the user to the desired screen
        Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show();
        finish(); // Close the ResetPasswordActivity
    }

    private void updatePassword(String newPassword) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Get the user email received as an extra from the intent
        String userEmail = getIntent().getStringExtra("USER_EMAIL_EXTRA");

        // Find the user by email
        mAuth.fetchSignInMethodsForEmail(userEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check if the email is registered
                        if (task.getResult().getSignInMethods().size() > 0) {
                            // The email is registered, proceed to update password
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.updatePassword(newPassword)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                // Password update successful
                                                Toast.makeText(ResetPasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                                finish(); // Close the ResetPasswordActivity
                                            } else {
                                                // Password update failed
                                                Toast.makeText(ResetPasswordActivity.this, "Failed to update password. " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // User is null
                                Toast.makeText(ResetPasswordActivity.this, "User not found. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Email is not registered
                            Toast.makeText(ResetPasswordActivity.this, "Email not registered. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Task failed
                        Toast.makeText(ResetPasswordActivity.this, "Failed to fetch sign-in methods. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }





}
