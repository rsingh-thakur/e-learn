package com.nrt.e_learning;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.nrt.e_learning.adapters.VideoAdapter;
import com.nrt.e_learning.model.Videomodel;

public class shortVideo extends AppCompatActivity {

    ViewPager2 viewPager2;
    VideoAdapter adapter;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_video);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference shortsRef = firestore.collection("shorts");
        viewPager2 = findViewById(R.id.vpager2);


        Query query = shortsRef.orderBy("title"); // You can order the videos if needed

        FirestoreRecyclerOptions<Videomodel> options =
                new FirestoreRecyclerOptions.Builder<Videomodel>()
                        .setQuery(query, Videomodel.class)
                        .build();

        // Configure ViewPager2 Adapter
        adapter = new VideoAdapter(options);

        // Display Videos in ViewPager2
        viewPager2.setAdapter(adapter);



    }


    public void openYoutubeUrl(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.youtube.com/@FreeOnlinecoaching/featured"));
        startActivity(intent);
    }
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    public void onBackButtonClicked(View view){
        finish();
    }
}
