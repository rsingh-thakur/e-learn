package com.nrt.e_learning.ui.mycourses;

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
import com.nrt.e_learning.Course_Video_List_Activity;
import com.nrt.e_learning.R;
import com.nrt.e_learning.adapters.MyCourseAdapter;
import com.nrt.e_learning.databinding.FragmentMyCoursesBinding;
import com.nrt.e_learning.model.CourseModel;
import com.nrt.e_learning.model.Purchase;
import com.nrt.e_learning.util.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;

public class MyCoursesFragment extends Fragment implements MyCourseAdapter.OnClickListener {
    private CollectionReference coursesCollection , purchasesCollection;
    private FirebaseFirestore firestore;
    private MyCourseAdapter courseAdapter;

    private List<CourseModel> courseModels;
    private FragmentMyCoursesBinding binding;
    private RecyclerView myCourseRecyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMyCoursesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        myCourseRecyclerView = root.findViewById(R.id.myCourseRecyclerView);

        firestore = FirebaseFirestore.getInstance();



        coursesCollection = firestore.collection("courses");
        purchasesCollection = firestore.collection("purchases");
        courseModels = new ArrayList<>();

        courseAdapter = new MyCourseAdapter(courseModels,getActivity(),this);
        myCourseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        myCourseRecyclerView.setAdapter(courseAdapter);
        String email =  FirebaseUtil.getCurrentUser().getEmail();


         findCoursesByEmail(email);


        return root;
    }



    public void findCoursesByEmail( String userEmail) {

            Query query = purchasesCollection
                    .whereEqualTo("userEmail", userEmail);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Handle found documents
                            Purchase purchase = document.toObject(Purchase.class);
                            Log.i("emailaeeee"  ,purchase.getCourseName());

                            loadCourses(purchase.getCourseName());
                        }
                    } else {
                        Log.i("Error"  ,"errors");
                    }
                }
            });
        }

    public void loadCourses(String courseName) {
        // Fetch couses from Firestore
        Log.i("getAllTheCoursesList3",courseName);
        Query query = coursesCollection
                .whereEqualTo("categoryName", courseName);
        Log.i("getAllTheCoursesList4",courseName);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (CourseModel courseModel : task.getResult().toObjects(CourseModel.class)) {
                        courseModels.add(courseModel);
                    }
                    courseAdapter.notifyDataSetChanged();
                } else {
                    Log.i("getAllTheCoursesList","error occurred");
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
    public void OpenVideos(int position) {
        CourseModel courseModel = courseModels.get(position);
        Intent intent = new Intent(getContext(), Course_Video_List_Activity.class);
        intent.putExtra("categoryName", courseModel.getCategoryName()) ;
        startActivity(intent);
    }
}