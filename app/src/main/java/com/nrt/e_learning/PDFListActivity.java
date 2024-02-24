package com.nrt.e_learning;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nrt.e_learning.adapters.PdfListAdapter;
import com.nrt.e_learning.model.PdfItem;
import com.nrt.e_learning.util.AndroidUtil;

import java.util.ArrayList;
import java.util.List;

public class PDFListActivity extends AppCompatActivity  {
    private RecyclerView pdfRecyclerView;
    private List<PdfItem> pdfList;
    private PdfListAdapter pdfListAdapter;
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

        // Load PDFs from Firestore
        loadPdfsFromFirestore();

        notes_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchedText = searchNotesTitleText.getText().toString().trim();
                loadPdfsFromFirestoreBySearchText(searchedText);
                if(pdfList.size()==0)
                    AndroidUtil.showToast(getApplicationContext(),"No Result Found");
            }

        });
    }



    private void loadPdfsFromFirestore() {
        FirebaseFirestore.getInstance().collection("PDFCollection")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
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


    private void loadPdfsFromFirestoreBySearchText(String searchText) {
        FirebaseFirestore.getInstance().collection("PDFCollection")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        // Clear the existing pdfList
                        pdfList.clear();

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            PdfItem pdfModel = document.toObject(PdfItem.class);
                            pdfModel.setDocumentId(document.getId());

                            // Check if the PDF title contains the search text
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


    public void onBackButtonClicked(View view) {
        finish();
    }


}
