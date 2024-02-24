package com.nrt.e_learning.adapters;



import android.app.Activity;

import android.content.Intent;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;
import com.nrt.e_learning.Course_Video_List_Activity;
import com.nrt.e_learning.NotesListByCateActivity;
import com.nrt.e_learning.R;
import com.nrt.e_learning.databinding.CategoryItemBinding;
import com.nrt.e_learning.databinding.LayoutAdBinding;
import com.nrt.e_learning.model.CourseModel;
import com.nrt.e_learning.model.NotesCategory;
import com.nrt.e_learning.util.AndroidUtil;


import java.util.List;

public class CourseAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Activity activity;

    private List<CourseModel> courseModels;

    private OnBuyClickListener onBuyClickListener;


    public CourseAdapter(List<CourseModel> courseModels, Activity activity, OnBuyClickListener onBuyClickListener) {
        this.courseModels = courseModels;
        this.activity = activity;
        this.onBuyClickListener = onBuyClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         LayoutInflater layoutInflater = LayoutInflater.from(activity);
            View view = layoutInflater.inflate(R.layout.category_item, parent, false);
            return new CategoryViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((CategoryViewHolder) holder).bind(courseModels.get(position));

    }



    public interface OnBuyClickListener {
        public void buy(int position);
    }


    @Override
    public int getItemCount() {
        return courseModels.size();
    }


    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        private TextView txtCategoryName;
        private TextView coursePrice;
        private TextView coursePeriod;
        private Button btnCoursePurchase_Dnld;
        private ImageView btnDelete ,courseImage;
        @NonNull
        CategoryItemBinding binding;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CategoryItemBinding.bind(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtPdfNotesTitle);
            coursePrice = itemView.findViewById(R.id.NotesPrice);
            coursePeriod = itemView.findViewById(R.id.NotesPeriod);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotes);
            btnCoursePurchase_Dnld = itemView.findViewById(R.id.btnNotesPurchase_Dnld);
            courseImage = itemView.findViewById(R.id.notesImage);
            Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.courses_img);

            courseImage.setImageDrawable(drawable);
            btnDelete.setVisibility(View.GONE);


            btnCoursePurchase_Dnld.setOnClickListener(v -> {
                int position = getAdapterPosition();
               CourseModel  courseModel=   courseModels.get(position);
               String coursePrice = courseModel.getPrice().toString().trim();
               if( coursePrice == "" || coursePrice == "0"  ){
                   Intent intent = new Intent(activity.getApplicationContext(), Course_Video_List_Activity.class);
                   intent.putExtra("categoryName",courseModel.getCategoryName());
                   activity.startActivity(intent);
               }
               else
                onBuyClickListener.buy(position);
            });
        }


        public void bind(CourseModel courseModel) {
            txtCategoryName.setText(courseModel.getCategoryName());

            if(courseModel.getPrice() == ""){
                coursePrice.setText("Free");
                btnCoursePurchase_Dnld.setText("Open Now");
                courseImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity.getApplicationContext(), Course_Video_List_Activity.class);
                        intent.putExtra("categoryName",courseModel.getCategoryName());
                        activity.startActivity(intent);
                    }
                });
            } else {
                coursePrice.setText(courseModel.getPrice() + "/-");
            }


            if(courseModel.getPrice()=="")
                coursePeriod.setText("Unlimited");
            else
                coursePeriod.setText(courseModel.getPeriod()+ " Days");
        }
    }
}