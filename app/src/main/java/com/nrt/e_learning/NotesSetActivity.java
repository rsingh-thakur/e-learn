package com.nrt.e_learning;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nrt.e_learning.adapters.NotesCategoryAdapter;
import com.nrt.e_learning.model.MyNotesPurchaseModel;
import com.nrt.e_learning.model.NotesCategory;
import com.nrt.e_learning.services.GMailSender;
import com.nrt.e_learning.util.AndroidUtil;
import com.nrt.e_learning.util.FirebaseUtil;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NotesSetActivity extends AppCompatActivity  implements NotesCategoryAdapter.OnDeleteClickListener, NotesCategoryAdapter.OnBuyClickListener, PaymentResultListener {

    private RecyclerView recyclerViewCategories;
    private Button btnAddCategory;
    private List<NotesCategory> categoryList;
    private NotesCategoryAdapter categoryAdapter;
    private Checkout checkout;
    private FirebaseFirestore firestore;
    private CollectionReference categoryCollection , notesPurchaseCollection;
    NotesCategory categoryItem ;
    String price, pdfName, Transaction_ID ;
    private int position;
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);
        Checkout.preload( getApplicationContext());
        firestore = FirebaseFirestore.getInstance();

        categoryCollection = firestore.collection("PdfCategories");
        notesPurchaseCollection = firestore.collection("NotesPurchases");

        recyclerViewCategories = findViewById(R.id.cateNotesRecyclerView);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        categoryList = new ArrayList<>();
        categoryAdapter = new NotesCategoryAdapter(categoryList,this,this,this );

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategories.setAdapter(categoryAdapter);

        btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement logic to add a new category
                // For simplicity, let's assume you are adding a dummy category
                addCategoryWithDialog();
            }
        });

        loadAllNotesCategories();

        filterSpinner = findViewById(R.id.filter_spinner);

        // Define filter options
        List<String> filterOptions = new ArrayList<>();
        filterOptions.add("All Sets ▼");
        filterOptions.add("Free Sets ▼");
        filterOptions.add("Paid Sets ▼");

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
                    case "All Sets ▼":
                        // Load all courses
                        loadAllNotesCategories();
                        break;
                    case "Free Sets ▼":
                        // Load free courses
                        loadFreeNotesCategories();
                        break;
                    case "Paid Sets ▼":
                        // Load paid courses
                        loadPaidNotesCategories();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }



    public void loadAllNotesCategories() {
        categoryCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categoryList.clear();
                for (NotesCategory pdfCategory : task.getResult().toObjects(NotesCategory.class)) {
                    categoryList.add(pdfCategory);
                }
                categoryAdapter.notifyDataSetChanged();
            } else {
                // Handle error
            }
        });
    }



    public void loadFreeNotesCategories() {
        // Fetch categories from Firestore
        categoryCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categoryList.clear();
                for (NotesCategory pdfCategory : task.getResult().toObjects(NotesCategory.class)) {
                    if(pdfCategory.getPrice() == "")
                    categoryList.add(pdfCategory);
                }
                categoryAdapter.notifyDataSetChanged();
            } else {
                // Handle error
            }
        });
    }

    public void loadPaidNotesCategories() {
        // Fetch categories from Firestore
        categoryCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categoryList.clear();
                for (NotesCategory pdfCategory : task.getResult().toObjects(NotesCategory.class)) {
                    if(pdfCategory.getPrice() != "")
                    categoryList.add(pdfCategory);
                }
                categoryAdapter.notifyDataSetChanged();
            } else {
                // Handle error
            }
        });
    }

    private void addCategoryWithDialog() {
        // Create an AlertDialog.Builder instance
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Category");

        // Create an EditText view to get user input
        final EditText categoryNameInput = new EditText(this);
        final EditText priceInput = new EditText(this);
        final EditText periodInput = new EditText(this);

        categoryNameInput.setHint("Category Name");
        priceInput.setHint("Price");
        periodInput.setHint("Period");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(categoryNameInput);
        layout.addView(priceInput);
        layout.addView(periodInput);
        layout.setPadding(15,0,15,0);
        layout.setShowDividers(LinearLayout.SHOW_DIVIDER_BEGINNING);
        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the entered values
                String categoryName = categoryNameInput.getText().toString().trim();
                String priceStr = priceInput.getText().toString().trim();
                String periodStr = periodInput.getText().toString().trim();

                if (!categoryName.isEmpty() && !priceStr.isEmpty() && !periodStr.isEmpty()) {
                    try {
                        // Convert price and period to appropriate types
                        double price = Double.parseDouble(priceStr);
                        int period = Integer.parseInt(periodStr);

                        // Add the category to Firestore
                        NotesCategory newCategory = new NotesCategory(categoryName, priceStr, periodStr);
                        categoryCollection.add(newCategory).addOnSuccessListener(documentReference -> {
                            // Refresh the list after adding the new category
                            loadAllNotesCategories();
                        }).addOnFailureListener(e -> {
                            // Handle error
                        });
                    } catch (NumberFormatException e) {
                        // Handle the case where price or period is not a valid number
                        // You can show a message or handle it as needed
                    }
                } else {
                    // Handle empty fields
                    // You can show a message or handle it as needed
                    Toast.makeText(NotesSetActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                }
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the AlertDialog
        builder.show();
    }



    public void onDeleteClick(int position) {
        // Handle the delete action here
        NotesCategory deletedCategory = categoryList.get(position);

        // Assuming you have a unique identifier or some way to uniquely identify the category (e.g., category name)
        String categoryName = deletedCategory.getCategoryName();

        // Delete the category from Firestore based on the name
        categoryCollection.whereEqualTo("categoryName", categoryName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Assuming there's only one category with this name (or handle multiple matches as needed)
                        String categoryId = task.getResult().getDocuments().get(0).getId();

                        categoryCollection.document(categoryId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Refresh the list after deleting the category
                                    loadAllNotesCategories();
                                })
                                .addOnFailureListener(e -> {
                                    // Handle error
                                });
                    } else {
                        // Handle category not found or other conditions
                    }
                });
    }

    @Override
    public void buy(int position) {
       
        NotesCategory buyCategory = categoryList.get(position);
        this.position  =  position;
        // Assuming you have a unique identifier or some way to uniquely identify the category (e.g., category name)
        String categoryName = buyCategory.getPrice();
        int amount = Math.round(Integer.parseInt(categoryName)*100);
        if(amount==0)
            return;
        startPayment(amount);


    }

    @Override
    public void onPaymentSuccess(String s) {
        Transaction_ID =s;

        categoryItem  = categoryList.get(position);
        price = categoryItem.getPrice();
        pdfName = categoryItem.getCategoryName();
        Log.d("PaymentSuccess", "Payment successful. Transaction ID: " + s);
        Toast.makeText(this, "Payment successful", Toast.LENGTH_LONG).show();

       String email = FirebaseUtil.getCurrentUser().getEmail();
       String notesName = categoryItem.getCategoryName();
        addPurchase(  email,  notesName );
         showSendInvoiceDialog();

    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.e("PaymentError", "Payment failed. Code: " + i + ", Message: " + s);
        Toast.makeText(this, "Payment failed", Toast.LENGTH_LONG).show();
        // You can handle payment failure here
    }

    public void addPurchase(String userEmail, String courseName) {
        notesPurchaseCollection.add(new MyNotesPurchaseModel(userEmail, courseName))
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
                Intent intent = new Intent(getApplicationContext(), NotesListByCateActivity.class);
                intent.putExtra("categoryName",categoryItem.getCategoryName()) ;
                startActivity(intent);

                generateInvoiceAndSend();
            }
        });

        builder.setNegativeButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "No," handle accordingly (e.g., close the dialog)
                AndroidUtil.convertTextToPdfAndDownload(getApplicationContext(),Transaction_ID,price,pdfName);
                Intent intent = new Intent(getApplicationContext(), NotesListByCateActivity.class);
                intent.putExtra("categoryName",categoryItem.getCategoryName()) ;
                startActivity(intent);
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
        new NotesSetActivity.SendEmailTask().execute();
    }







    private void downloadPdf(String pdfPath, String fileName) {
        File file = new File(pdfPath);

        // Use FileProvider to get a content URI
        Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

        // Check if there's an application to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Handle the case where no PDF viewer is available
            Toast.makeText(this, "No PDF viewer app installed", Toast.LENGTH_SHORT).show();
        }
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
