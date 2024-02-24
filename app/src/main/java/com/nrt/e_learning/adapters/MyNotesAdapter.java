package com.nrt.e_learning.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.nrt.e_learning.R;
import com.nrt.e_learning.databinding.CategoryItemBinding;
import com.nrt.e_learning.model.NotesCategory;
import java.util.List;

public class MyNotesAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity activity;
    private List<NotesCategory> notesSetList;
    private OnClickListener onClickListener;

    public MyNotesAdapter(List<NotesCategory> notesSetList, Activity activity, OnClickListener onClickListener) {
        this.notesSetList = notesSetList;
        this.activity = activity;
        this.onClickListener = onClickListener;
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

        ( ( MyNotesAdapter.CategoryViewHolder) holder).bind(notesSetList.get(position));
    }

    public interface OnClickListener {
        public void OpenNotes(int position);
    }


    @Override
    public int getItemCount() {
        return notesSetList.size();
    }
    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        private TextView txtCategoryName;
        private TextView NotesPrice;
        private TextView NotesPeriod;
        private Button btnNotesPurchase_Dnld;
        private ImageView btnDelete ,notesImage;
        @NonNull
        CategoryItemBinding binding;

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
            Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.notebook);

            notesImage.setImageDrawable(drawable);
            btnDelete.setVisibility(View.GONE);
            btnNotesPurchase_Dnld.setText("Open Now");


            btnNotesPurchase_Dnld.setOnClickListener(v -> {
                int position = getAdapterPosition();
                onClickListener.OpenNotes(position);

            });

            notesImage.setOnClickListener(v -> {
                int position = getAdapterPosition();
                onClickListener.OpenNotes(position);

            });
        }

        public void bind(NotesCategory category) {
            txtCategoryName.setText(category.getCategoryName());

            if (NotesPrice.getText().toString().trim().equalsIgnoreCase("0")) {
                NotesPrice.setText("FREE");
            } else {
                NotesPrice.setText(category.getPrice() + "/-");
            }

            if (NotesPeriod.getText().toString().trim().equalsIgnoreCase("0"))
                NotesPeriod.setText("Unlimited");
            else
                NotesPeriod.setText(category.getPeriod() + " Days");
        }
    }
}