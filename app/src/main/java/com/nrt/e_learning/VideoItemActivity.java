package com.nrt.e_learning;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class VideoItemActivity extends AppCompatActivity {

   ImageButton btnEdit ;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_item);


        btnEdit = findViewById(R.id.btnEdit);

        btnEdit.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), EditVideoActivity.class);
            startActivity(intent);

        });

    }

}
