package com.nrt.e_learning;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nrt.e_learning.adapters.PdfListAdapter;
import com.nrt.e_learning.model.PdfItem;
import java.util.ArrayList;
import java.util.List;

public class PDFListActivity extends AppCompatActivity {

    private RecyclerView pdfRecyclerView;
    private List<PdfItem> pdfList;
    private PdfListAdapter pdfListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdflist);

        pdfRecyclerView = findViewById(R.id.pdfRecyclerView);
        pdfList = new ArrayList<>();
        pdfListAdapter = new PdfListAdapter(pdfList, this);

        // Set up GridLayoutManager with a span count of 2
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        pdfRecyclerView.setLayoutManager(layoutManager);
        pdfRecyclerView.setAdapter(pdfListAdapter);

        // Load PDFs from Firestore
        loadPdfsFromFirestore();
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
}
