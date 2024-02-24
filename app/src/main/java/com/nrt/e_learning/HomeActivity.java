package com.nrt.e_learning;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.compose.foundation.pager.PageInfo;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.nrt.e_learning.databinding.ActivityHomeBinding;


import com.nrt.e_learning.model.CourseModel;
import com.nrt.e_learning.model.Purchase;
import com.nrt.e_learning.services.GMailSender;
import com.nrt.e_learning.ui.home.HomeFragment;
import com.nrt.e_learning.ui.playlist.PlaylistFragment;
import com.nrt.e_learning.util.AndroidUtil;
import com.nrt.e_learning.util.FirebaseUtil;
import com.nrt.e_learning.util.SharedPreferencesManager;
import com.razorpay.PaymentResultListener;


//import com.tom_roush.pdfbox.pdmodel.PDDocument;
//import com.tom_roush.pdfbox.pdmodel.PDPage;
//import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
//import com.tom_roush.pdfbox.pdmodel.font.PDFont;
//import com.tom_roush.pdfbox.pdmodel.font.PDSimpleFont;
//import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
//import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HomeActivity extends AppCompatActivity  implements HomeFragment.OnDataPass , PaymentResultListener  {

    private static final long SESSION_EXPIRATION_TIME = 6000000L ;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    private CourseModel courseModel;
    private AdView mAdView;
    private CollectionReference  purchasesCollection;
    private FirebaseFirestore firestore;
    String Transaction_ID , price, pdfName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isUserLoggedIn() || isSessionExpired()) {
            redirectToLogin();
            return;
        }
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);

        firestore = FirebaseFirestore.getInstance();
        purchasesCollection = firestore.collection("purchases");


        binding.appBarHome.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), shortVideo.class);
                startActivity(intent);
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_my_courses,  R.id.nav_profile , R.id.nav_social_media,R.id.nav_my_notes)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void logoutUser() {
        SharedPreferencesManager.clearUserData(this);
        redirectToLogin();
    }



    private boolean isUserLoggedIn() {
        // Retrieve user login state from SharedPreferences

        return SharedPreferencesManager.isLoggedInUser(this);
    }

    private boolean isSessionExpired() {
        long loginTime = SharedPreferencesManager.getLoginTime(this);
        long currentTime = System.currentTimeMillis();
        long sessionDuration = currentTime - loginTime;
        return sessionDuration > SESSION_EXPIRATION_TIME;

    }

    private void redirectToLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }


    public void openWhatsAppchat(MenuItem item) {
        try {
            String url = "https://api.whatsapp.com/send?phone=" + "7723008951";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            AndroidUtil.showToast(HomeActivity.this,"can't open Whatsapp");
        }
    }


    public void logout(MenuItem item) {
        SharedPreferencesManager.logOut(HomeActivity.this);
        Intent intent = new Intent (HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    public void shareApp(MenuItem item) {

        ImageView imageView = new ImageView(getApplicationContext());
        // Replace the URL with your actual image URL
        String imageUrl = "https://yt3.googleusercontent.com/hRP2rrzEwHf7W61uhKJwhAkG4EpPA8ZjgF0-GpDRMyuX0598va1yQuWEEE02WKLudOqluZmwXw=s176-c-k-c0x00ffffff-no-rj";
        new DownloadImageTask(imageView, imageUrl).execute();

        // Rest of the code remains the same
    }


    public void gotoPlaylist(MenuItem  item) {
        Fragment fragment =new PlaylistFragment();
//        loadFragment(fragment);
    }

    @Override
    public void onPaymentSuccess(String s) {

        Log.d("PaymentSuccess", "Payment successful. Transaction ID: " + s);
        Transaction_ID = s ;
        price = courseModel.getPrice();
        pdfName = courseModel.getCategoryName();
        showSendInvoiceDialog();
        String email = FirebaseUtil.getCurrentUser().getEmail();
        addPurchase(email,pdfName);
        // You can perform any further actions here after successful payment
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.e("PaymentError", "Payment failed. Code: " + i + ", Message: " + s);
        Toast.makeText(getApplicationContext(), "Payment failed", Toast.LENGTH_LONG).show();
        // You can handle payment failure here
    }


    public void addPurchase(String userEmail, String courseName) {
        purchasesCollection.add(new Purchase(userEmail, courseName))
                .addOnSuccessListener(documentReference -> {
                    // Purchase added successfully
                })
                .addOnFailureListener(e -> {
                    // Error adding purchase
                });
    }

    private void shareApplicationWithImage(ImageView imageView) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        String appPackageName = "com.freeonlinecoaching.app";
        String playStoreLink = "https://play.google.com/store/apps/details?id=" + appPackageName;

        String shareMessage = "Check out this awesome app:\n" +
                "App: *Free Online Coaching*\n" +
                "Description: Makes you motivated for good preparation\n" +
                playStoreLink;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Free Online Coaching");
        sendIntent.setType("image/*");
        sendIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(), bitmap));

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image Description", null);
        return Uri.parse(path);
    }

    public void onRateUsClick(MenuItem item) {
        // Replace "your_package_name" with the actual package name of your app
        String packageName = "com.freeonlinecoaching.app";

        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            // If Play Store app is not available, open the Play Store website
            Uri playStoreUri = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
            Intent goToPlayStore = new Intent(Intent.ACTION_VIEW, playStoreUri);

            try {
                startActivity(goToPlayStore);
            } catch (ActivityNotFoundException ex) {
                // Handle the case where neither Play Store app nor website is available
                Toast.makeText(this, "Unable to open Play Store", Toast.LENGTH_SHORT).show();
            }
        }
    }



    public void AskChatGPT(MenuItem item){
        Intent intent = new Intent( getApplicationContext(), ChatGPTSupport.class);
        startActivity(intent);
    }

    public void DarkModeLightMode(MenuItem item) {
        // Check the current mode
        int nightModeFlags =
                getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                // Currently in dark mode, switch to light mode
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                // Currently in light mode, switch to dark mode
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }

        // Recreate the activity to apply the new mode
        recreate();
    }

    @Override
    public void onDataPassed(CourseModel courseModel) {
        this.courseModel = courseModel;
    }

    private class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {
        ImageView imageView;
        String imageUrl;

        public DownloadImageTask(ImageView imageView, String imageUrl) {
            this.imageView = imageView;
            this.imageUrl = imageUrl;
        }

        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();

            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                // Set the downloaded bitmap to the ImageView
                imageView.setImageBitmap(result);

                // Share the application with the image
                shareApplicationWithImage(imageView);
            } else {
                // Handle the case where the bitmap is null
                // You may want to log an error or show a message to the user.
            }
        }
    }


    private void showSendInvoiceDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            Log.i("showSendInvoiceDialog", "showSendInvoiceDialog");
            builder.setTitle("Send OR Download Invoice ?");
            builder.setMessage("Choose option to send the invoice via email or Download ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // User clicked "Yes," generate invoice and send
                    Intent intent = new Intent(getApplicationContext(), Course_Video_List_Activity.class);
                    intent.putExtra("categoryName", courseModel.getCategoryName()) ;
                    startActivity(intent);
                    generateInvoiceAndSend();
                }
            });

            builder.setNegativeButton("Download", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getApplicationContext(), Course_Video_List_Activity.class);
                    intent.putExtra("categoryName", courseModel.getCategoryName()) ;
                    startActivity(intent);
                    // User clicked "No," handle accordingly (e.g., close the dialog)
                   AndroidUtil.convertTextToPdfAndDownload(getApplicationContext(),Transaction_ID, price, pdfName);

                    dialog.dismiss();
                }
            });
            Log.i("showSendInvoiceDialog", "showSendInvoiceDialog2");
            builder.setIcon(R.drawable.emails_icon);
            builder.show();
        }catch (Exception e){
            Log.e("showSendInvoiceDialog",e.getMessage());
        }
    }

    public void generateInvoiceAndSend() {
        new HomeActivity.SendEmailTask().execute();
    }





    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Log.i("doInBackground", "invoice gmail" + FirebaseUtil.getCurrentUser().getEmail());

                String body = AndroidUtil.getEmailHtmlTemplate(Transaction_ID,price,pdfName);

                GMailSender gMailSender = new GMailSender("basic.erps@gmail.com", "xnymokcfghnhhmzv");
                gMailSender.sendMail("Payment course invoice", body, "basic.erps@gmail.com", FirebaseUtil.getCurrentUser().getEmail());

                return true; // Email sent successfully
            } catch (Exception e) {
                Log.e("EmailError", e.getMessage());
                return false; // Failed to send email
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            // Perform UI-related operations here (show toast, etc.)
            if (success) {
                Toast.makeText(getApplicationContext(), "Email sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to send email", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void convertHtmlToPdfAndDownload(String htmlContent, String pdfFilePath) {
        try {
            // Create a PdfWriter instance
            PdfWriter writer = new PdfWriter(pdfFilePath);

            // Create a PdfDocument instance
            com.itextpdf.kernel.pdf.PdfDocument pdfDocument = new com.itextpdf.kernel.pdf.PdfDocument(writer);

            // Create a Document instance with PdfDocument
            Document document = new Document(pdfDocument);

            // Parse HTML content and add it to the PDF document
            ConverterProperties properties = new ConverterProperties();
            HtmlConverter.convertToPdf(htmlContent, document.getPdfDocument(), properties);

            // Close the document
            document.close();

            // Show a toast indicating the PDF is saved
            Toast.makeText(getApplicationContext(), "PDF saved to " + pdfFilePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Failed to generate PDF", Toast.LENGTH_SHORT).show();
        }
    }


}