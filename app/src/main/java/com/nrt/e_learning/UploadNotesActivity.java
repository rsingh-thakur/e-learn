package com.nrt.e_learning;// PDFListActivity.java

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import com.nrt.e_learning.model.NotesCategory;
import com.nrt.e_learning.model.PdfItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.Map;


public class UploadNotesActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private StorageReference storageReference;
    private FirebaseFirestore firestore,firestorec;
    private CollectionReference pdfCollection;
    private EditText editTextName;
    private EditText editTextCategory ,priceInput;
    private Button btnUpload;
    private Uri selectedPdfUri;

    private Spinner spinnerCategory;
    private ArrayList<String> categoryListc = new ArrayList<>();


    ArrayAdapter<String> categoryAdapter;
    private AdView mAdView;
    private CollectionReference categoryCollectionc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);



        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adViews3);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        pdfCollection = firestore.collection("PDFCollection");

        editTextName = findViewById(R.id.editTextName);
        editTextCategory = findViewById(R.id.editTextCategory);
        priceInput = findViewById(R.id.priceInput);
        btnUpload = findViewById(R.id.btnUpload);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryListc);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Initially, disable the upload button
        btnUpload.setEnabled(false);

        // Set a listener to enable the upload button when both name and category are entered
        editTextName.addTextChangedListener(new SimpleTextWatcher());
        editTextCategory.addTextChangedListener(new SimpleTextWatcher());


        firestorec = FirebaseFirestore.getInstance();
        categoryCollectionc = firestorec.collection("PdfCategories");


         loadCategories() ;
    }

    // Helper class to simplify text change listeners
    private class SimpleTextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(android.text.Editable editable) {
            // Enable the upload button only if both name and category are entered
            btnUpload.setEnabled(!TextUtils.isEmpty(editTextName.getText().toString().trim()) &&
                    !TextUtils.isEmpty(editTextCategory.getText().toString().trim()));
        }
    }

    public void choosePDF(android.view.View view) {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedPdfUri = data.getData();

            // Enable the upload button after selecting a PDF
            btnUpload.setEnabled(true);
        }
    }

    public void uploadPDF(android.view.View view) {
        if (selectedPdfUri != null) {
            uploadFile(selectedPdfUri);
        } else {
            Toast.makeText(this, "Please select a PDF first", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(Uri pdfUri) {
        StorageReference fileReference = storageReference.child("PDFs/" + editTextName.getText().toString() + ".pdf");

        fileReference.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // File uploaded successfully
                    Toast.makeText(UploadNotesActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();

                    // Get the download URL for the uploaded file
                    fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Save file details to Firestore collection, including the download URL
                                saveFileToFirestore(fileReference.getPath(), uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                // Handle the failure to get the download URL
                                Toast.makeText(UploadNotesActivity.this, "Failed to retrieve download URL", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to upload the file
                    Toast.makeText(UploadNotesActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }


    private void saveFileToFirestore(String filePath, String downloadUrl) {
        String fileName = editTextName.getText().toString().trim();
        String selectedCategory = spinnerCategory.getSelectedItem().toString();
        String uploadedAt = getCurrentTimeStamp();
        String price = priceInput.getText().toString().trim();

        // Create PdfModel instance
        PdfItem pdfModel = new PdfItem(fileName, selectedCategory, filePath, price, downloadUrl, uploadedAt);

        // Convert PdfModel to Map
        Map<String, Object> pdfData = new HashMap<>();
        pdfData.put("fileName", pdfModel.getFileName());
        pdfData.put("category", pdfModel.getCategory());
        pdfData.put("filePath", pdfModel.getFilePath());
        pdfData.put("price", pdfModel.getPrice());
        pdfData.put("downloadUrl", pdfModel.getDownloadUrl());
        pdfData.put("uploadedAt", pdfModel.getUploadedAt());

        // Add Map to Firestore collection
        pdfCollection.add(pdfData)
                .addOnSuccessListener(documentReference -> {
                    // Reset input fields and disable the upload button after successful upload
                    editTextName.getText().clear();
                    editTextCategory.getText().clear();
                    priceInput.getText().clear();
                    btnUpload.setEnabled(false);
                    selectedPdfUri = null;
                    Log.i("uri", documentReference.getId());
                    String documentId = documentReference.getId();
                    Toast.makeText(UploadNotesActivity.this, "PDF details saved to Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(UploadNotesActivity.this, "Failed to save PDF details to Firestore", Toast.LENGTH_SHORT).show());
    }



    private String getCurrentTimeStamp () {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(new Date());
        }


        private void updateDownloadUrl (String documentId){
            StorageReference fileReference = storageReference.child("PDFs/" + editTextName.getText().toString() + ".pdf");

            // Add a listener to the upload task to wait for its completion
            fileReference.putFile(selectedPdfUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Return the download URL task
                        return fileReference.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        // Update the download URL in Firestore
                        pdfCollection.document(documentId).update("downloadUrl", uri.toString())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(UploadNotesActivity.this, "Download URL updated in Firestore", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(UploadNotesActivity.this, "Failed to update download URL in Firestore", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            // Retry or handle the case where the file is not immediately available
                            // You can add a retry mechanism or show a message to the user
                        } else {
                            Toast.makeText(UploadNotesActivity.this, "Failed to retrieve download URL", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    public void loadCategories() {
        // Fetch categories from Firestore
        categoryCollectionc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categoryListc.clear();
                for (NotesCategory pdfCategory : task.getResult().toObjects(NotesCategory.class)) {
                    categoryListc.add(pdfCategory.getCategoryName());
                }
                categoryAdapter.notifyDataSetChanged();
            } else {
                // Handle error
            }
        });
    }



}


