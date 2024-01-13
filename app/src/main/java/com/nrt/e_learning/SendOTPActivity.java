package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import android.content.Intent;

import android.view.View;

import com.nrt.e_learning.util.EmailSender;
import com.nrt.e_learning.util.OTPManager;

public class SendOTPActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendOTPButton;
    private ProgressBar sendOTPProgressBar;

    private String userEmail; // Variable to store the entered email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);

        emailEditText = findViewById(R.id.emailEditText);
        sendOTPButton = findViewById(R.id.sendOTPButton);
        sendOTPProgressBar = findViewById(R.id.sendOTPProgressBar);

        sendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOTP();
            }
        });
    }

    private void sendOTP() {
        userEmail = emailEditText.getText().toString().trim();
        String otp = OTPManager.generateOTP();
        EmailSender.sendOTPEmail(userEmail,otp);
        Log.d("sendOtpActivity","emailAdress"+userEmail + "     otp "+otp);

        OTPManager.saveOTP(getApplicationContext(),otp);
        // Once OTP is sent, proceed to the OTP verification activity
        Intent intent = new Intent(SendOTPActivity.this, VerifyOTPActivity.class);
        intent.putExtra("USER_EMAIL", userEmail);
        startActivity(intent);
    }
}
