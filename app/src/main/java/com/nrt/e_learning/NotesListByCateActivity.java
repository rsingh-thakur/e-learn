package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nrt.e_learning.adapters.PdfListAdapter;
import com.nrt.e_learning.model.PdfItem;

import com.nrt.e_learning.services.GMailSender;
import com.nrt.e_learning.util.AndroidUtil;
import com.nrt.e_learning.util.FirebaseUtil;
import com.razorpay.PaymentResultListener;

import java.util.ArrayList;
import java.util.List;

public class NotesListByCateActivity extends AppCompatActivity  implements PaymentResultListener {

    private RecyclerView pdfRecyclerView;
    private List<PdfItem> pdfList;
    private PdfListAdapter pdfListAdapter;
    String cateName;
    private boolean isPaymentDone;
    private EditText searchNotesTitleText ;
    private ImageButton notes_search_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdflist);

        searchNotesTitleText = findViewById(R.id.search_bar);
        notes_search_btn = findViewById(R.id.notes_search_btn);

        pdfRecyclerView = findViewById(R.id.pdfRecyclerView);
        pdfList = new ArrayList<>();
        pdfListAdapter = new PdfListAdapter(pdfList, this);

        // Set up GridLayoutManager with a span count of 2
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        pdfRecyclerView.setLayoutManager(layoutManager);
        pdfRecyclerView.setAdapter(pdfListAdapter);

         cateName=  getIntent().getStringExtra("categoryName");
         Log.i("cateName",cateName);

        // Load PDFs from Firestore
        loadPdfsFromFirestore(cateName);

        notes_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchedText = searchNotesTitleText.getText().toString().trim();
                loadPdfsFromFireStoreBySearchText(cateName,searchedText);
                if(pdfList.size()==0)
                    AndroidUtil.showToast(getApplicationContext(),"No Result Found");
            }

        });
    }



    private void loadPdfsFromFirestore(String catNAME) {


        FirebaseFirestore.getInstance().collection("PDFCollection")
                .whereEqualTo("category", catNAME.toString().trim()) // Replace "fieldName" with the actual field name in your documents
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        pdfList.clear(); // Clear the list before adding new items

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            PdfItem pdfModel = document.toObject(PdfItem.class);
                            pdfModel.setDocumentId(document.getId());

                            // Add the PdfModel to the list
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                pdfList.add(pdfModel);
                            } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                                pdfList.remove(pdfModel);
                            }
                        }

                        // Update the RecyclerView
                        pdfListAdapter.notifyDataSetChanged();
                    }
                });
    }


    private void loadPdfsFromFireStoreBySearchText(String catNAME,String searchText) {


        FirebaseFirestore.getInstance().collection("PDFCollection")
                .whereEqualTo("category", catNAME.toString().trim()) // Replace "fieldName" with the actual field name in your documents
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        pdfList.clear(); // Clear the list before adding new items

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            PdfItem pdfModel = document.toObject(PdfItem.class);
                            pdfModel.setDocumentId(document.getId());
                            // Add the PdfModel to the list
                            if (pdfModel.getFileName().toLowerCase().contains(searchText.toLowerCase())) {
                                // Add the PdfModel to the list
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    pdfList.add(pdfModel);
                                } else if (dc.getType() == DocumentChange.Type.REMOVED) {
                                    pdfList.remove(pdfModel);
                                }
                            }
                        }

                        // Update the RecyclerView
                        pdfListAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onPaymentSuccess(String s) {
        Log.d("PaymentSuccess", "Payment successful. Transaction ID: " + s);
        Toast.makeText(this, "Payment successful", Toast.LENGTH_LONG).show();
//        if (pdfListAdapter != null && pdfListAdapter.pdfToDownload != null) {
//            pdfListAdapter.downloadPdf(pdfListAdapter.pdfToDownload.getDownloadUrl(), pdfListAdapter.pdfToDownload.getFileName());
//        }
        // You can perform any further actions here after successful payment
//        isPaymentDone=true;
        showSendInvoiceDialog();


    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.e("PaymentError", "Payment failed. Code: " + i + ", Message: " + s);
        Toast.makeText(this, "Payment failed", Toast.LENGTH_LONG).show();
        // You can handle payment failure here
        isPaymentDone=false;
    }

    private void showSendInvoiceDialog() {
        Log.e("showSendInvoiceDialog", "showSendInvoiceDialog " );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send Invoice via Email?");
        builder.setMessage("Do you want to send the invoice via email?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "Yes," generate invoice and send
                generateInvoiceAndSend();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked "No," handle accordingly (e.g., close the dialog)
                dialog.dismiss();
            }
        });
        builder.show();
    }



    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String body = AndroidUtil.getEmailHtmlTemplate("Transaction_ID","price","pdfName");


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
                Toast.makeText(NotesListByCateActivity.this, "Email sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NotesListByCateActivity.this, "Failed to send email", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void generateInvoiceAndSend() {
        new SendEmailTask().execute();
    }


    public void onBackButtonClicked(View view) {
        finish();
    }
}
