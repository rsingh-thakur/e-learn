package com.nrt.e_learning;

import static com.nrt.e_learning.util.OTPManager.OTP_KEY;
import static com.nrt.e_learning.util.OTPManager.OTP_PREFS_NAME;
import static com.nrt.e_learning.util.OTPManager.TIMESTAMP_KEY;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nrt.e_learning.util.OTPManager;


public class VerifyOTPActivity extends AppCompatActivity {

    EditText otpEditText;
    Button verifyOTPButton;
    ProgressBar verifyOTPProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otpactivity);

        otpEditText = findViewById(R.id.otpEditText);
        verifyOTPButton = findViewById(R.id.verifyOTPButton);
        verifyOTPProgressBar = findViewById(R.id.verifyOTPProgressBar);

        verifyOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOTP = otpEditText.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences(OTP_PREFS_NAME, Context.MODE_PRIVATE);
                String storedOTP = sharedPreferences.getString(OTP_KEY, "");
                long storedTimestamp = sharedPreferences.getLong(TIMESTAMP_KEY, 0);

                if (enteredOTP.equals(storedOTP)) {

                    Toast.makeText(VerifyOTPActivity.this, "OTP Verified", Toast.LENGTH_SHORT).show();
                    OTPManager.clearOTP(getApplicationContext());
                    String userEmail = getIntent().getStringExtra("USER_EMAIL_EXTRA");
                    Intent intent = new Intent(getApplicationContext(), SendOTPActivity.class);
                    intent.putExtra("USER_EMAIL_EXTRA",userEmail);
                    startActivity(intent);

                } else {

                    Toast.makeText(VerifyOTPActivity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
