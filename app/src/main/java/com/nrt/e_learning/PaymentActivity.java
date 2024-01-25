package com.nrt.e_learning;



import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;



import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nrt.e_learning.util.AndroidUtil;
import com.razorpay.Checkout;

import com.razorpay.PaymentResultListener;


import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    Button btnPay ;
    int amount  ;
    EditText amountFiled ;


   private Checkout checkout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
          Checkout.preload(PaymentActivity.this);
        btnPay = findViewById(R.id.btnPayment);
        amountFiled = findViewById(R.id.amountId);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String samount = amountFiled.getText().toString().trim();
                amount = Math.round(Integer.parseInt(samount)*100);

                startPayment();
            }
        });
    }




    public void startPayment() {


        /**
         * Instantiate Checkout
         */
        checkout = new Checkout();
        checkout.setKeyID("rzp_test_GYytSvAZkcc8c2");
        /**
         * Set your logo here
         */
        checkout.setImage(R.drawable.e_learn);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            options.put("name", "Ramu singh");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg");
           // options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", amount);//pass amount in currency subunits
            options.put("prefill.email", "elearn@gmail.com");
            options.put("prefill.contact", "125785498");
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 3);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch (Exception e) {
            Log.e("payment", "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        AndroidUtil.showToast(getApplicationContext(),"Payment Successful");
    }

    @Override
    public void onPaymentError(int i, String s) {
        AndroidUtil.showToast(getApplicationContext(),"Payment Faileds");
    }



}