package com.nrt.e_learning.adapters;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nrt.e_learning.R;
import com.nrt.e_learning.model.PdfItem;
import com.razorpay.Checkout;

import org.json.JSONObject;

public class PdfListAdapter extends RecyclerView.Adapter<PdfListAdapter.PdfViewHolder>  {

    private List<PdfItem> pdfItemList;
    private Context context;
    private  Checkout checkout;

    public PdfItem pdfToDownload;

    public PdfListAdapter(List<PdfItem> pdfItemList, Context context) {
        this.pdfItemList = pdfItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf, parent, false);
        Checkout.preload(context);
        return new PdfViewHolder(view,parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        PdfItem pdfItem = pdfItemList.get(position);

        holder.fileNameTextView.setText(pdfItem.getFileName());

        String pdfPrice = pdfItem.getPrice();

        if(pdfPrice=="")
             holder.pdfPrice.setText("Free");
        else
            holder.pdfPrice.setText(pdfItem.getPrice()+"/-");

        // Load PDF image (you can customize this based on your requirements)
        Glide.with(context).load(R.drawable.pdf_image_icon).into(holder.bgImageView);

        // Handle item click
        holder.itemView.setOnClickListener(v -> openPdf(pdfItem.getDownloadUrl()));

        // Handle download button click
        holder.downloadImageView.setOnClickListener(v -> downloadPdf(pdfItem.getDownloadUrl(), pdfItem.getFileName()));

        holder.deleteImageView.setOnClickListener(v -> deletePdf(pdfItem));

        holder.btnDownload.setOnClickListener(v -> downloadPdf(pdfItem.getDownloadUrl(), pdfItem.getFileName()));

    }

    private void purchaseThenDownload(PdfItem pdfItem) {
        if (pdfItem.getPrice() != null) {
            pdfToDownload = pdfItem;
           startPayment(pdfItem.getPrice().trim());
        } else {
            // PDF is free, allow direct download
            downloadPdf(pdfItem.getDownloadUrl(), pdfItem.getFileName());
        }
    }


    @Override
    public int getItemCount() {
        return pdfItemList.size();
    }



    static class PdfViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;
        ImageView bgImageView;
        ImageView downloadImageView;
        TextView  pdfPrice;
        ImageView deleteImageView;

        Button btnDownload;


        PdfViewHolder(@NonNull View itemView,Context context) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.txtPdfTitle);
            bgImageView = itemView.findViewById(R.id.bgimage);
            downloadImageView = itemView.findViewById(R.id.imgDownload);
            deleteImageView = itemView.findViewById(R.id.btnDeletePDF);
            pdfPrice = itemView.findViewById(R.id.pdfPrice);
            btnDownload = itemView.findViewById(R.id.btnPurchase_Dnld);
        }
    }

    private void openPdf(String pdfUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(pdfUrl), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public void downloadPdf(String pdfUrl, String fileName) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(pdfUrl);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + ".pdf");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        downloadManager.enqueue(request);
    }


    private void deletePdf(PdfItem pdfItem) {
        FirebaseFirestore.getInstance().collection("PDFCollection")
                .whereEqualTo("downloadUrl", pdfItem.getDownloadUrl())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete the document found
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Document deleted successfully
                                        Toast.makeText(context, "PDF deleted", Toast.LENGTH_SHORT).show();
                                        deleteStorageItem(pdfItem.getFileName());
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle the failure to delete the document
                                        Toast.makeText(context, "Failed to delete PDF", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // Handle the failure to query Firestore
                        Toast.makeText(context, "Error querying Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void deleteStorageItem(String fileName) {
        // Get a reference to the Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        // Specify the path to the storage item
        StorageReference pdfReference = storageReference.child("PDFs/" + fileName + ".pdf");

        // Delete the storage item
        pdfReference.delete()
                .addOnSuccessListener(aVoid -> {
                    // File deleted successfully
                    Toast.makeText(context, "Storage item deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to delete the storage item
                    Toast.makeText(context, "Failed to delete storage item", Toast.LENGTH_SHORT).show();
                });
    }



    public void startPayment(String price ) {

       int amount = Math.round(Integer.parseInt(price)*100);
        /**
         * Instantiate Checkout
         */
        checkout = new Checkout();
        checkout.setKeyID("rzp_test_GYytSvAZkcc8c2");
        /**
         * Set your logo here
         */
        checkout.setImage(R.drawable.e_learn);

        /**
         * Reference to current activity
         */
        final Activity activity = (Activity) context;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            options.put("name", "E-Learn");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg");
            // options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", amount);//pass amount in currency subunits
            options.put("prefill.email", "elearn@gmail.com");
            options.put("prefill.contact", "125785498");
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 3);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch (Exception e) {
            Log.e("payment", "Error in starting Razorpay Checkout", e);
        }
    }





}
