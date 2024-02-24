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
import com.nrt.e_learning.NotesListByCateActivity;
import com.nrt.e_learning.R;
import com.nrt.e_learning.databinding.CategoryItemBinding;
import com.nrt.e_learning.databinding.LayoutAdBinding;
import com.nrt.e_learning.model.CourseModel;
import com.nrt.e_learning.model.NotesCategory;


import java.util.List;

public class MyCourseAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_VIEW = 0;
    private static final int AD_VIEW = 1;
    private static final int ITEM_FEED_COUNT = 6;
    private final Activity activity;

    private List<CourseModel> courseModels;


    private OnDeleteClickListener onDeleteClickListener;
    private OnClickListener onClickListener;

    private RewardedAd rewardedAd;


    public static final String TAG = "CourseAdapter";

    public MyCourseAdapter(List<CourseModel> courseModels, Activity activity, OnClickListener onClickListener) {
        this.courseModels = courseModels;
        this.activity = activity;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        if (viewType == ITEM_VIEW) {
            View view = layoutInflater.inflate(R.layout.category_item, parent, false);
            return new CategoryViewHolder(view);
        } else if (viewType == AD_VIEW) {
            View view = layoutInflater.inflate(R.layout.layout_ad, parent, false);
            return new AdViewHolder(view);
        } else {
            return null;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == ITEM_VIEW) {
            int pos = position - Math.round(position / ITEM_FEED_COUNT);
            ((CategoryViewHolder) holder).bind(courseModels.get(pos));
        } else if (holder.getItemViewType() == AD_VIEW) {
//            ((AdViewHolder) holder).bindAdData();
        }
    }




    public interface OnDeleteClickListener {
        void onDeleteClick(int position);

    }

    public interface OnClickListener {
        public void OpenVideos(int position);
    }


    @Override
    public int getItemCount() {
        return courseModels.size();
    }


    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        private TextView txtCategoryName;
        private TextView NotesPrice;
        private TextView NotesPeriod;
        private Button btnNotesPurchase_Dnld;
        private ImageView btnDelete ,notesImage;
        @NonNull
        CategoryItemBinding binding;

        public void onClick(View view) {
            // Handle click events, e.g., call onDeleteClick method
            int position = getAdapterPosition();
            if (onDeleteClickListener != null && position != RecyclerView.NO_POSITION) {
                onDeleteClickListener.onDeleteClick(position);
            }
        }


        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CategoryItemBinding.bind(itemView);
            txtCategoryName = itemView.findViewById(R.id.txtPdfNotesTitle);
            NotesPrice = itemView.findViewById(R.id.NotesPrice);
            NotesPeriod = itemView.findViewById(R.id.NotesPeriod);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotes);
            btnNotesPurchase_Dnld = itemView.findViewById(R.id.btnNotesPurchase_Dnld);
            notesImage = itemView.findViewById(R.id.notesImage);
            // Replace R.drawable.your_drawable with the actual resource ID of your drawable
            Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.courses_img);

            notesImage.setImageDrawable(drawable);
            btnDelete.setVisibility(View.GONE);
            btnNotesPurchase_Dnld.setText("Open Now");


            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onDeleteClickListener != null && position != RecyclerView.NO_POSITION) {
                    onDeleteClickListener.onDeleteClick(position);
                }
            });


            btnNotesPurchase_Dnld.setOnClickListener(v -> {
                int position = getAdapterPosition();
                showAds();
                onClickListener.OpenVideos(position);

            });

            notesImage.setOnClickListener(v -> {
                int position = getAdapterPosition();
                showAds();
                onClickListener.OpenVideos(position);

            });

        }


        public void bind(CourseModel courseModel) {
            txtCategoryName.setText(courseModel.getCategoryName());


            if (NotesPrice.getText().toString().trim().equalsIgnoreCase("0")) {
                NotesPrice.setText("FREE");
            } else {
                NotesPrice.setText(courseModel.getPrice() + "/-");
            }


            if (NotesPeriod.getText().toString().trim().equalsIgnoreCase("0"))
                NotesPeriod.setText("Unlimited");
            else
                NotesPeriod.setText(courseModel.getPeriod() + " Days");
        }


    }

    void showAds() {
        if (rewardedAd != null) {
            Activity activityContext = activity;
            rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                }
            });
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
        }
    }


    public class AdViewHolder extends RecyclerView.ViewHolder {

        private final LayoutAdBinding binding;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutAdBinding.bind(itemView);
        }
    }

}