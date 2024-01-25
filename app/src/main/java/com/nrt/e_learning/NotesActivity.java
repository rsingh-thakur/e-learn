package com.nrt.e_learning;// PDFListActivity.java

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.nrt.e_learning.model.PdfItem;

import java.text.SimpleDateFormat;
import java.util.Date;


public class NotesActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private CollectionReference pdfCollection;
    private EditText editTextName;
    private EditText editTextCategory;
    private Button btnUpload;
    private Uri selectedPdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        pdfCollection = firestore.collection("PDFCollection");

        editTextName = findViewById(R.id.editTextName);
        editTextCategory = findViewById(R.id.editTextCategory);
        btnUpload = findViewById(R.id.btnUpload);

        // Initially, disable the upload button
        btnUpload.setEnabled(false);

        // Set a listener to enable the upload button when both name and category are entered
        editTextName.addTextChangedListener(new SimpleTextWatcher());
        editTextCategory.addTextChangedListener(new SimpleTextWatcher());
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
                    Toast.makeText(NotesActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();

                    // Get the download URL for the uploaded file
                    fileReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Save file details to Firestore collection, including the download URL
                                saveFileToFirestore(fileReference.getPath(), uri.toString());
                            })
                            .addOnFailureListener(e -> {
                                // Handle the failure to get the download URL
                                Toast.makeText(NotesActivity.this, "Failed to retrieve download URL", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to upload the file
                    Toast.makeText(NotesActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }


    private void saveFileToFirestore (String filePath, String downloadUrl){
            String fileName = editTextName.getText().toString().trim();
            String category = editTextCategory.getText().toString().trim();
            String uploadedAt = getCurrentTimeStamp();

            // Create PdfModel instance
            PdfItem pdfModel = new PdfItem(fileName, category, filePath, downloadUrl, uploadedAt);

            // Add PdfModel to Firestore collection
            pdfCollection.add(pdfModel)
                    .addOnSuccessListener(documentReference -> {
                        // Reset input fields and disable the upload button after successful upload
                        editTextName.getText().clear();
                        editTextCategory.getText().clear();
                        btnUpload.setEnabled(false);
                        selectedPdfUri = null;
                        Log.i("uri", documentReference.getId());
                        String documentId = documentReference.getId();
                        Toast.makeText(NotesActivity.this, "PDF details saved to Firestore", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(e -> Toast.makeText(NotesActivity.this, "Failed to save PDF details to Firestore", Toast.LENGTH_SHORT).show());
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
                                    Toast.makeText(NotesActivity.this, "Download URL updated in Firestore", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(NotesActivity.this, "Failed to update download URL in Firestore", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            // Retry or handle the case where the file is not immediately available
                            // You can add a retry mechanism or show a message to the user
                        } else {
                            Toast.makeText(NotesActivity.this, "Failed to retrieve download URL", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

}


