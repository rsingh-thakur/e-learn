package com.nrt.e_learning;

import static com.nrt.e_learning.util.OTPManager.generateOTP;
import static com.nrt.e_learning.util.OTPManager.saveOTP;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nrt.e_learning.util.AndroidUtil;
import com.nrt.e_learning.services.GMailSender;

public class  SendOTPActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    String generatedOTP ;
    EditText receiverEmail;
    Button BtnSendEmail;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_otp);
        receiverEmail=(EditText)findViewById(R.id.emailEditText);

        BtnSendEmail=(Button) findViewById(R.id.sendOTPButton);

        BtnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth = FirebaseAuth.getInstance();

                String receiver=receiverEmail.getText().toString();
                isEmailRegistered(receiver);
            }
        });

    }


    private void sendResetPasswordEmail(String email) {


        // Send a password reset email to the user
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
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




    private void sendEmail(final String Sender,final String Password,final String Receiver,final String Title,final String Message)
    {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender(Sender,Password);
                    sender.sendMail(Title, "<b>"+Message+"</b>", Sender, Receiver);
                    makeAlert();
                    saveOTP(getApplicationContext(),generatedOTP);
                    Intent intent = new Intent(getApplicationContext(),VerifyOTPActivity.class);
                    intent.putExtra("USER_EMAIL_EXTRA",Receiver);
                    startActivity(intent);

                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
            }

        }).start();
    }
    private void makeAlert(){
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(SendOTPActivity.this, "Mail Sent", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void isEmailRegistered(String email) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User with this email exists
                    // Continue with sending OTP or show an error message
                    String sender = "basic.erps@gmail.com";
                    String senderPass = "xnymokcfghnhhmzv";
                    String title = "Forgot Password OTP";

                    generatedOTP = generateOTP();
                    String message = "Your forgot password Otp is: " + generatedOTP;
                    sendResetPasswordEmail(email);
                } else {
                    // No such user, show an error message
                    Toast.makeText(SendOTPActivity.this, "User is not registered", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SendOTPActivity.this, "some error occurred", Toast.LENGTH_SHORT).show();

            }
        });
    }


}