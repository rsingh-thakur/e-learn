package com.nrt.e_learning;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nrt.e_learning.adapters.CourseAdapter;
import com.nrt.e_learning.model.CourseModel;
import com.nrt.e_learning.model.Purchase;
import com.nrt.e_learning.services.GMailSender;
import com.nrt.e_learning.util.AndroidUtil;
import com.nrt.e_learning.util.FirebaseUtil;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CourseActivity extends AppCompatActivity  implements  CourseAdapter.OnBuyClickListener, PaymentResultListener {

    private RecyclerView recyclerViewCategories;
    private List<CourseModel> courseModels;
    private CourseAdapter courseAdapter;
    private Checkout checkout;
    private FirebaseFirestore firestore;
    private CollectionReference coursesCollection, purchasesCollection;


    String price, pdfName, Transaction_ID ;
    private int position;
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        Checkout.preload( getApplicationContext());
        firestore = FirebaseFirestore.getInstance();

        coursesCollection = firestore.collection("courses");
        purchasesCollection = firestore.collection("purchases");

        recyclerViewCategories = findViewById(R.id.cateNotesRecyclerView);


        courseModels = new ArrayList<>();
        courseAdapter = new CourseAdapter(courseModels,this,this);

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewCategories.setAdapter(courseAdapter);

        loadCourses();



        filterSpinner = findViewById(R.id.filter_spinner);

        // Define filter options
        List<String> filterOptions = new ArrayList<>();
        filterOptions.add("All Course ▼");
        filterOptions.add("Free Course ▼");
        filterOptions.add("Paid Course ▼");

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        filterSpinner.setAdapter(adapter);

        // Set item click listener for the spinner
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Handle filter option selection
                String selectedOption = filterOptions.get(position);
                switch (selectedOption) {
                    case "All Course ▼":
                        // Load all courses
                        loadCourses();
                        break;
                    case "Free Course ▼":
                        // Load free courses
                        loadFreeCourses();
                        break;
                    case "Paid Course ▼":
                        // Load paid courses
                        loadPaidCourses();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }



    public void loadCourses() {
        // Fetch categories from Firestore
        coursesCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                courseModels.clear();
                for (CourseModel courseModel : task.getResult().toObjects(CourseModel.class)) {
                    courseModels.add(courseModel);
                    Log.i("Datasss",String.valueOf(courseModel.getPeriod()));
                }
                courseAdapter.notifyDataSetChanged();
            } else {

            }
        });
    }



    public void loadFreeCourses() {
        // Fetch categories from Firestore
        coursesCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                courseModels.clear();
                for (CourseModel courseModel : task.getResult().toObjects(CourseModel.class)) {
                    // Check if the price is null for the current CourseModel
                    if (courseModel.getPrice() == "") {
                        // Add the CourseModel to the list only if the price is null
                        courseModels.add(courseModel);
                    }
                }
                courseAdapter.notifyDataSetChanged();
            } else {
                // Handle error
            }
        });
    }


    public void loadPaidCourses() {
        // Fetch categories from Firestore
        coursesCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                courseModels.clear();
                for (CourseModel courseModel : task.getResult().toObjects(CourseModel.class)) {
                    // Check if the price is null for the current CourseModel
                    if (courseModel.getPrice() != "") {
                        // Add the CourseModel to the list only if the price is null
                        courseModels.add(courseModel);
                    }
                }
                courseAdapter.notifyDataSetChanged();
            } else {
                // Handle error
            }
        });
    }

    @Override
    public void buy(int position) {
        CourseModel courseModel = courseModels.get(position);
        this.position  =  position;
        String coursePrice = courseModel.getPrice();

        if(coursePrice!="" || coursePrice != "0" ) {
            int amount = Math.round(Integer.parseInt(coursePrice) * 100);
            startPayment(amount);
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        Transaction_ID =s;
        CourseModel courseModel  = courseModels.get(position);
        price = courseModel.getPrice();
        pdfName = courseModel.getCategoryName();
        Log.d("PaymentSuccess", "Payment successful. Transaction ID: " + s);

        addPurchase(FirebaseUtil.getCurrentUser().getEmail(),courseModel.getCategoryName());

        Toast.makeText(this, "Payment successful", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), VideoListActivity.class);
        intent.putExtra("categoryName", courseModel.getCategoryName()) ;
        showSendInvoiceDialog();

        startActivity(intent);
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.e("PaymentError", "Payment failed. Code: " + i + ", Message: " + s);
        Toast.makeText(this, "Payment failed", Toast.LENGTH_LONG).show();
        // You can handle payment failure here
    }


    public void addPurchase(String userEmail, String courseName) {
        purchasesCollection.add(new Purchase(userEmail, courseName))
                .addOnSuccessListener(documentReference -> {
                    // Purchase added successfully
                })
                .addOnFailureListener(e -> {
                    // Error adding purchase
                });
    }

    private void showSendInvoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send OR Download Invoice ?");
        builder.setMessage("Choose option to send the invoice via email or Download ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Yes," generate invoice and send
                generateInvoiceAndSend();
            }
        });

        builder.setNegativeButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "No," handle accordingly (e.g., close the dialog)
                AndroidUtil.convertTextToPdfAndDownload(getApplicationContext(),Transaction_ID, price, pdfName);

                dialog.dismiss();
            }
        });

        builder.setIcon(R.drawable.emails_icon);
        builder.show();
    }



    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Log.i("doInBackground", "invoice gmail" + FirebaseUtil.getCurrentUser().getEmail());

                String body = AndroidUtil.getEmailHtmlTemplate(Transaction_ID,price,pdfName);

                GMailSender gMailSender = new GMailSender("basic.erps@gmail.com", "xnymokcfghnhhmzv");
                gMailSender.sendMail("Payment course invoice", body, "basic.erps@gmail.com", FirebaseUtil.getCurrentUser().getEmail());

                return true; // Email sent successfully
            } catch (Exception e) {
                Log.e("EmailError", e.getMessage());
                return false; // Failed to send email
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            // Perform UI-related operations here (show toast, etc.)
            if (success) {
                Toast.makeText(getApplicationContext(), "Email sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to send email", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void generateInvoiceAndSend() {
        new CourseActivity.SendEmailTask().execute();
    }




    public void startPayment(int price ) {

//        int amount = Math.round(Integer.parseInt(price)*100);
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
        final Activity activity = this ;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            options.put("name", "E-Learn");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg");
            // options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", price);//pass amount in currency subunits
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

    public void onBackButtonClicked(View view) {
        finish();
    }
}
