package com.nrt.e_learning.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.nrt.e_learning.MyCoursesActivity;
import com.nrt.e_learning.NotesActivity;
import com.nrt.e_learning.PDFListActivity;

import com.nrt.e_learning.PaymentActivity;
import com.nrt.e_learning.R;
import com.nrt.e_learning.TestActivity;
import com.nrt.e_learning.VideoListActivity;
import com.nrt.e_learning.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ViewPager viewPager;
    private ImagePagerAdapter adapter;
    private Handler handler;

    AppCompatButton free_videos_button ;
    AppCompatButton myCourses_button ;
    AppCompatButton AllCourses_button ;
    AppCompatButton Notes_button ;
    AppCompatButton Test_button ;
    AppCompatButton LiveClass_button,pay_button ;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup the ViewPager and ImagePagerAdapter
        viewPager = root.findViewById(R.id.viewPager);
        adapter = new ImagePagerAdapter(getImageResources());
        viewPager.setAdapter(adapter);

        // Setup handler for automatic scrolling
        handler = new Handler(Looper.getMainLooper());
        startAutoScroll();

        free_videos_button = root.findViewById(R.id.free_videos) ;
        AllCourses_button = root.findViewById(R.id.AllCourses_button) ;
        Notes_button = root.findViewById(R.id.notes_button) ;
        LiveClass_button = root.findViewById(R.id.liveClass_button) ;
        Test_button = root.findViewById(R.id.test_button) ;
        myCourses_button = root.findViewById(R.id.myCourses_button) ;
        pay_button = root.findViewById(R.id.pay_button);



        myCourses_button .setOnClickListener(v-> {
            onMyCoursesButtonClick(v);
        });



        AllCourses_button .setOnClickListener(v-> {
            onAllCoursesButtonClick(v);
        });


        free_videos_button .setOnClickListener(v-> {
            onFreeVideosButtonClick(v);
        });


        Notes_button .setOnClickListener(v-> {
            onNotesButtonClick(v);
        });


        LiveClass_button .setOnClickListener(v-> {
            onLiveClassButtonClick(v);
        });


        Test_button .setOnClickListener(v-> {
            onTestButtonClick(v);
        });

        pay_button .setOnClickListener(v-> {
            onPayButtonClick(v);
        });



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the handler when the fragment is destroyed
        stopAutoScroll();
        binding = null;
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
        Intent intent = new Intent(getActivity(), NotesActivity.class);
        startActivity(intent);
    }
    public void onLiveClassButtonClick(View view) {
        Intent intent = new Intent(getActivity(), VideoListActivity.class);
        startActivity(intent);
    }
    public void onTestButtonClick(View view) {
        Intent intent = new Intent(getActivity(), TestActivity.class);
        startActivity(intent);
    }
    public void onMyCoursesButtonClick(View view) {
        Intent intent = new Intent(getActivity(), MyCoursesActivity.class);
        startActivity(intent);
    }

    public void onPayButtonClick(View view) {
        Intent intent = new Intent(getActivity(), PaymentActivity.class);
        startActivity(intent);
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
