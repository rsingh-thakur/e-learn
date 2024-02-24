package com.nrt.e_learning.ui.profile;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nrt.e_learning.R;

public class SocialMediaFragment extends Fragment {

    private SocialMediaViewModel mViewModel;

    public static SocialMediaFragment newInstance() {
        return new SocialMediaFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social_media, container, false);



        LinearLayout youtubeLayout = view.findViewById(R.id.youtubeLayout);
        LinearLayout facebookLayout = view.findViewById(R.id.facebookLayout);
        LinearLayout instagramLayout = view.findViewById(R.id.instagramLayout);
        LinearLayout whatsappLayout = view.findViewById(R.id.whatsappLayout);
        LinearLayout telegramLayout = view.findViewById(R.id.telegramLayout);
        LinearLayout twitterLayout = view.findViewById(R.id.twitterLayout);

        setClickListener(youtubeLayout, "https://www.youtube.com/@FreeOnlinecoaching/featured");
        setClickListener(facebookLayout, "https://www.facebook.com/profile.php?id=100038417075513");
        setClickListener(instagramLayout, "https://www.instagram.com/invites/contact/?i=1186o9dctait9&utm_content=k7kq9bo");
        setClickListener(whatsappLayout, "https://chat.whatsapp.com/IInLHww3pZWK32Di4uQ9Ku");
        setClickListener(telegramLayout, "https://t.me/testingchannelforAndroid");
        setClickListener(twitterLayout, "https://twitter.com/YOUR_USERNAME");

        return view;
    }


    private void setClickListener(final LinearLayout layout, final String url) {
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the corresponding link or perform your desired action
                openUrl(url);
            }
        });
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }


}