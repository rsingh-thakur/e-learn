package com.nrt.e_learning.adapters;

import android.app.Activity;

import android.content.Intent;

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


import java.util.List;

public class NotesCategoryAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_VIEW = 0;
    private static final int AD_VIEW = 1;
    private static final int ITEM_FEED_COUNT = 6;
    private final Activity activity;

    private List<NotesCategory> categoryList;


    private OnDeleteClickListener onDeleteClickListener;
    private OnBuyClickListener onBuyClickListener;




    public static final String TAG = "CategoryAdapter";

    public NotesCategoryAdapter(List<NotesCategory> categoryList, Activity activity, OnDeleteClickListener onDeleteClickListener, OnBuyClickListener onBuyClickListener) {
        this.categoryList = categoryList;
        this.activity = activity;
        this.onDeleteClickListener = onDeleteClickListener;
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
            ((CategoryViewHolder) holder).bind(categoryList.get(position));

    }



    public interface OnDeleteClickListener {
        void onDeleteClick(int position);

    }

    public interface OnBuyClickListener {
        public void buy(int position);
    }


    @Override
    public int getItemCount() {
        return categoryList.size();
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
            notesImage = itemView.findViewById(R.id.notesImage);
            btnNotesPurchase_Dnld = itemView.findViewById(R.id.btnNotesPurchase_Dnld);



            btnNotesPurchase_Dnld.setOnClickListener(v -> {
                int position = getAdapterPosition();
                NotesCategory  notesCategory=   categoryList.get(position);
                String coursePrice = notesCategory.getPrice().toString().trim();
                if( coursePrice == "" || coursePrice == "0"  ){
                    Intent intent = new Intent(activity.getApplicationContext(), NotesListByCateActivity.class);
                    intent.putExtra("categoryName",notesCategory.getCategoryName());
                    activity.startActivity(intent);
                }
                else
                    onBuyClickListener.buy(position);

            });
        }


        public void bind(NotesCategory category) {
            txtCategoryName.setText(category.getCategoryName());

            if(category.getPrice() == ""){
                NotesPrice.setText("Free");
                btnNotesPurchase_Dnld.setText("Open Now");
                notesImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity.getApplicationContext(), NotesListByCateActivity.class);
                        intent.putExtra("categoryName",category.getCategoryName());
                        activity.startActivity(intent);
                    }
                });
            } else {
                NotesPrice.setText(category.getPrice() + "/-");
            }


            if(category.getPrice()=="")
                NotesPeriod.setText("Unlimited");
            else
                NotesPeriod.setText(category.getPeriod() + " Days");
        }


    }


}