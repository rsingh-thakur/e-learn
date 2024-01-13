package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nrt.e_learning.util.OTPManager;

import java.util.Optional;

public class VerifyOTPActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyOTPButton;
    private ProgressBar verifyOTPProgressBar;

    private String userEmail; // Variable to store the user's email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otpactivity);

        otpEditText = findViewById(R.id.otpEditText);
        verifyOTPButton = findViewById(R.id.verifyOTPButton);
        verifyOTPProgressBar = findViewById(R.id.verifyOTPProgressBar);

        // Get the user's email from the intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        verifyOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOTP();
            }
        });
    }

    private void verifyOTP() {
        String enteredOTP = otpEditText.getText().toString().trim();

        // Retrieve the sent OTP from SharedPreferences or any other storage
        String sentOTP = OTPManager.getStoredOTP(getApplicationContext());

        // Compare the entered OTP with the sent OTP
        if (enteredOTP.equals(sentOTP) &&  OTPManager.isOTPValid(getApplicationContext())) {
            // OTP verification is successful
            OTPManager.clearOTP(getApplicationContext());
            allowPasswordReset();

        } else {
            // Incorrect OTP
            Toast.makeText(this, "Incorrect OTP. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }





    private void allowPasswordReset() {
        // TODO: Implement your logic to allow the user to reset the password
        // For example, navigate to the ResetPasswordActivity
        Intent intent = new Intent(VerifyOTPActivity.this, ResetPasswordActivity.class);
        intent.putExtra("USER_EMAIL_EXTRA", userEmail); // Pass the user's email to the ResetPasswordActivity
        startActivity(intent);
        finish(); // Close the VerifyOTPActivity
    }

}
