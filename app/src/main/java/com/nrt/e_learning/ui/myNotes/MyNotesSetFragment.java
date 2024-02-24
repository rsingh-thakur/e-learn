package com.nrt.e_learning.ui.myNotes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.nrt.e_learning.NotesListByCateActivity;
import com.nrt.e_learning.PDFListActivity;
import com.nrt.e_learning.R;
import com.nrt.e_learning.adapters.MyNotesAdapter;
import com.nrt.e_learning.databinding.FragmentMyNotesSetBinding;
import com.nrt.e_learning.model.CourseModel;
import com.nrt.e_learning.model.MyNotesPurchaseModel;
import com.nrt.e_learning.model.NotesCategory;
import com.nrt.e_learning.model.Purchase;
import com.nrt.e_learning.util.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class MyNotesSetFragment extends Fragment implements MyNotesAdapter.OnClickListener {
    private CollectionReference pdfCategoryCollection , notesPurchasesCollection;
    private FirebaseFirestore firestore;
    private MyNotesAdapter myNotesAdapter;

    private List<NotesCategory> categories;
    private FragmentMyNotesSetBinding binding;
    private RecyclerView myCourseRecyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMyNotesSetBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        myCourseRecyclerView = root.findViewById(R.id.myNotesRecyclerView);

        firestore = FirebaseFirestore.getInstance();



        pdfCategoryCollection = firestore.collection("PdfCategories");
        notesPurchasesCollection = firestore.collection("NotesPurchases");
        categories = new ArrayList<>();

        myNotesAdapter = new MyNotesAdapter(categories,getActivity(),this);
        myCourseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        myCourseRecyclerView.setAdapter(myNotesAdapter);
        String email =  FirebaseUtil.getCurrentUser().getEmail();

        findNotesByEmail(email);

        return root;
    }



    public void findNotesByEmail( String userEmail) {

        Query query = notesPurchasesCollection
                .whereEqualTo("userEmail", userEmail);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        // Handle found documents
                        MyNotesPurchaseModel myNotesPurchaseModel = document.toObject(MyNotesPurchaseModel.class);

                        loadNotesSet(myNotesPurchaseModel.getNotesSetName());
                    }
                } else {
                    Log.i("Error"  ,"errors");
                }
            }
        });
    }

    public void loadNotesSet(String courseName) {
        // Fetch couses from Firestore

        Query query = pdfCategoryCollection
                .whereEqualTo("categoryName", courseName);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (NotesCategory notesCategory : task.getResult().toObjects(NotesCategory.class)) {
                        categories.add(notesCategory);
                    }
                    myNotesAdapter.notifyDataSetChanged();
                } else {
                    Log.i("loadNotesSet","error occurred");
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void OpenNotes(int position) {
        NotesCategory notesCategory = categories.get(position);
        Intent intent = new Intent(getContext(), NotesListByCateActivity.class);
        intent.putExtra("categoryName", notesCategory.getCategoryName()) ;
        startActivity(intent);
    }


}
