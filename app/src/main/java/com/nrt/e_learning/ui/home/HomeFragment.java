package com.nrt.e_learning.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nrt.e_learning.NotesSetActivity;

import com.nrt.e_learning.CourseActivity;
import com.nrt.e_learning.UploadNotesActivity;
import com.nrt.e_learning.PDFListActivity;

import com.nrt.e_learning.R;

import com.nrt.e_learning.VideoListActivity;
import com.nrt.e_learning.adapters.CourseAdapter;
import com.nrt.e_learning.databinding.FragmentHomeBinding;
import com.nrt.e_learning.model.CourseModel;
import com.nrt.e_learning.quiz.QuizHomeActivity;
import com.nrt.e_learning.UploadVideoActivity;
import com.nrt.e_learning.util.FirebaseUtil;
import com.razorpay.Checkout;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment  implements  CourseAdapter.OnBuyClickListener  {

    private InterstitialAd mInterstitialAd;
    private FragmentHomeBinding binding;
    private ViewPager viewPager;
    private ImagePagerAdapter adapter;
    private Handler handler;
    private AdView mAdView;
//    AppCompatButton free_videos_button ;
    AppCompatButton myCourses_button ;
    AppCompatButton AllCourses_button ;
    AppCompatButton Notes_button ;
    AppCompatButton Course_btn;
    AppCompatButton LiveClass_button, AllNotescategory ;
    private String token ;
    private AdRequest adRequest;
    private  FirebaseFirestore firestore;
    private   int clikCount =0 ;

    RecyclerView CourseRecyclerView;

    private List<CourseModel> courseModels;
    private CourseAdapter courseAdapter;
    private Checkout checkout;
    private int position;
    private CollectionReference coursesCollection;

    private static final int CLICK_LIMIT =10;
    public static final String TAG = "Homefragment";

    public HomeFragment() {
        // Required empty public constructor
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
//        View root = inflater.inflate(R.layout.fragment_home, container, false);
        firestore = FirebaseFirestore.getInstance();
        // Setup the ViewPager and ImagePagerAdapter
        viewPager = root.findViewById(R.id.viewPager);
        adapter = new ImagePagerAdapter(getImageResources());
        viewPager.setAdapter(adapter);

        coursesCollection = firestore.collection("courses");

        CourseRecyclerView = root.findViewById(R.id.CourseRecyclerView);


        courseModels = new ArrayList<>();
        courseAdapter = new CourseAdapter(courseModels,getActivity(),this);

        CourseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        CourseRecyclerView.setLayoutManager(layoutManager);
        CourseRecyclerView.setAdapter(courseAdapter);

        CourseRecyclerView.setHorizontalFadingEdgeEnabled(true);
        // Setup handler for automatic scrolling
        handler = new Handler(Looper.getMainLooper());
        startAutoScroll();


        MobileAds.initialize(getContext().getApplicationContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // Ads SDK initialized successfully
            }
        });

        mAdView = root.findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//
        if(mInterstitialAd == null) {
                loadAds();
        }
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if(mInterstitialAd!=null) {
//                    mInterstitialAd.show(getActivity());
//                }
//            }
//        },10000);


//        free_videos_button = root.findViewById(R.id.free_videos) ;
        AllCourses_button = root.findViewById(R.id.AllCourses_button) ;
        Notes_button = root.findViewById(R.id.notes_button) ;
        LiveClass_button = root.findViewById(R.id.liveClass_button) ;
        Course_btn = root.findViewById(R.id.Course_btn) ;
        myCourses_button = root.findViewById(R.id.myCourses_button) ;
        AllNotescategory = root.findViewById(R.id.AllNotescategory);



        myCourses_button .setOnClickListener(v-> {
            if (mInterstitialAd != null&& clikCount==5) {
                clikCount = 0;
                mInterstitialAd.show(requireActivity());
            }
            else {
                clikCount++;
                onMyCoursesButtonClick(v);
                loadAds();
            }
        });



        AllCourses_button .setOnClickListener(v-> {
            if (mInterstitialAd != null && clikCount==CLICK_LIMIT) {
                clikCount = 0;
                mInterstitialAd.show(requireActivity());
            }
             else{
                loadAds();
                clikCount++;
              onAllCoursesButtonClick(v);

           }
        });


//        free_videos_button .setOnClickListener(v-> {
//            if (mInterstitialAd != null && clikCount==CLICK_LIMIT) {
//                clikCount=0;
//                mInterstitialAd.show(requireActivity());
//            }
//            else {
//                clikCount++;
//                loadAds();
//                onFreeVideosButtonClick(v);
//            }
//        });


        Notes_button .setOnClickListener(v-> {
            if (mInterstitialAd != null&& clikCount==CLICK_LIMIT) {
                clikCount = 0;
                mInterstitialAd.show(requireActivity());
            }
            else {
                loadAds();
                clikCount++;
                onNotesButtonClick(v);
            }
        });


        LiveClass_button .setOnClickListener(v-> {
            if (mInterstitialAd != null&& clikCount==CLICK_LIMIT) {
                clikCount = 0;
                mInterstitialAd.show(requireActivity());
            }
            else {
                loadAds();
                clikCount++;
                onLiveClassButtonClick(v);
            }
        });


        Course_btn.setOnClickListener(v-> {
            if (mInterstitialAd != null&& clikCount==CLICK_LIMIT) {
                clikCount = 0;
                mInterstitialAd.show(requireActivity());
            }
            else {
                loadAds();
                clikCount++;
                onCourse_btnClick(v);
            }
        });

        AllNotescategory.setOnClickListener(v-> {
            if (mInterstitialAd != null && clikCount==CLICK_LIMIT) {
                clikCount = 0;
                mInterstitialAd.show(requireActivity());
            }
            else{
                loadAds();
                clikCount++;
            onAllNotescategoryButtonClick(v);}
        });



        getFCMToken();
        loadCategories();
        return root;
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                  token = task.getResult();
                Log.i("token", token);
                updateFcmToken();
            }
        });
    }




    void loadAds(){
        InterstitialAd.load(getContext().getApplicationContext(), "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("HomeFragment", "onAdLoaded");
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.");
                                String urlToOpen = "https://www.youtube.com/channel/UCZNz9yK_EMhErlPRV-VBMew"; // Replace with your actual URL
                                openUrlInBrowser(urlToOpen);

                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.");
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.");
                            }
                        });

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d("HomeFragment", loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }


    private void openUrlInBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void updateFcmToken() {
        // Get the current user's email
        String userEmail = FirebaseUtil.getCurrentUser().getEmail();

        if (userEmail != null) {
            // Query Firestore to find the user document with the matching email
            firestore.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Update the fcmToken for the matched user
                            String userId = document.getId();
                            updateFcmToken(userId, token);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UserSignUpActivity", "Error querying user document by email", e);
                    });
        } else {
            Log.e("UserSignUpActivity", "User email is null");
        }
    }

    // Method to update the fcmToken for a user in Firestore
    private void updateFcmToken(String userId, String newFcmToken) {
        // Get the document reference for the user
        DocumentReference userRef = firestore.collection("users").document(userId);

        // Update the fcmToken field
        userRef.update("fcmToken", newFcmToken)
                .addOnSuccessListener(aVoid -> {
                    // FcmToken updated successfully
                    Log.d("UserSignUpActivity", "FcmToken updated successfully for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    // Error occurred while updating fcmToken
                    Log.e("UserSignUpActivity", "Failed to update FcmToken for user: " + userId, e);
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the handler when the fragment is destroyed
        stopAutoScroll();
        binding = null;
    }

    // Interface definition
    public interface OnDataPass {
        void onDataPassed(CourseModel courseModel);
    }

    private OnDataPass dataPasser;

    // Override onAttach to initialize the interface reference
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            dataPasser = (OnDataPass) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDataPass interface");
        }
    }

    // Method to pass data to the hosting Activity
    private void passDataToActivity(CourseModel courseModel) {
        dataPasser.onDataPassed(courseModel);
    }

    private List<Integer> getImageResources() {
        // Replace these dummy image resources with your actual image resources
        List<Integer> imageResources = new ArrayList<>();
        imageResources.add(R.drawable.image1);
        imageResources.add(R.drawable.image2);
        imageResources.add(R.drawable.image3);
        imageResources.add(R.drawable.image4);
        imageResources.add(R.drawable.image5);
        return imageResources;
    }

    private void startAutoScroll() {
        // Start a runnable to change the current item of the ViewPager every 5 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % adapter.getCount();
                viewPager.setCurrentItem(nextItem, true); // Set smooth scroll to true
                handler.postDelayed(this, 5000); // Delay for 5 seconds
            }
        }, 5000); // Initial delay for 5 seconds
    }

    private void stopAutoScroll() {
        // Remove any callbacks from the handler
        handler.removeCallbacksAndMessages(null);
    }

    public void onFreeVideosButtonClick(View view) {
        Intent intent = new Intent(getActivity(), UploadVideoActivity.class);
        startActivity(intent);
    }

    public void onAllCoursesButtonClick(View view) {
        Intent intent = new Intent(getActivity(), PDFListActivity.class);
        startActivity(intent);
    }
    public void onNotesButtonClick(View view) {
        Intent intent = new Intent(getActivity(), UploadNotesActivity.class);
        startActivity(intent);
    }
    public void onLiveClassButtonClick(View view) {
        Intent intent = new Intent(getActivity(), VideoListActivity.class);
        startActivity(intent);
    }
    public void onCourse_btnClick(View view) {
        Intent intent = new Intent(getActivity(), CourseActivity.class);
        startActivity(intent);
    }
    public void onMyCoursesButtonClick(View view) {
        Intent intent = new Intent(getActivity(), QuizHomeActivity.class);
        startActivity(intent);
    }

    public void onAllNotescategoryButtonClick(View view) {
        Intent intent = new Intent(getActivity(), NotesSetActivity.class);
        startActivity(intent);
    }

    public void loadCategories() {
        // Fetch categories from Firestore
        coursesCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                courseModels.clear();
                for (CourseModel courseModel : task.getResult().toObjects(CourseModel.class)) {
                    courseModels.add(courseModel);
                }
                courseAdapter.notifyDataSetChanged();
            } else {
                // Handle error
            }
        });
    }


    @Override
    public void buy(int position) {
        CourseModel courseModel = courseModels.get(position);
        this.position  =  position;
        // Assuming you have a unique identifier or some way to uniquely identify the category (e.g., category name)
        String categoryName = courseModel.getPrice();
        int amount = Math.round(Integer.parseInt(categoryName)*100);
        if(amount==0)
            return;
        startPayment(amount);
    }



    public void startPayment(int price ) {

//        int amount = Math.round(Integer.parseInt(price)*100);
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
        final Activity activity = getActivity() ;

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
            options.put("amount", price);//pass amount in currency subunits
            options.put("prefill.email", "elearn@gmail.com");
            options.put("prefill.contact", "125785498");
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 3);
            options.put("retry", retryObj);
            checkout.open(activity, options);
            CourseModel courseModel = courseModels.get(position);
            passDataToActivity(courseModel);
        } catch (Exception e) {
            Log.e("payment", "Error in starting Razorpay Checkout", e);
        }
    }



    private static class ImagePagerAdapter extends PagerAdapter {

        private final List<Integer> imageResources;

        ImagePagerAdapter(List<Integer> imageResources) {
            this.imageResources = imageResources;
        }

        @Override
        public int getCount() {
            return imageResources.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = new ImageView(container.getContext());
            imageView.setImageResource(imageResources.get(position));
            container.addView(imageView, 0);
            return imageView;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }



}
