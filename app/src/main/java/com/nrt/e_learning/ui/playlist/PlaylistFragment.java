package com.nrt.e_learning.ui.playlist;

import android.content.Intent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.nrt.e_learning.R;
import com.nrt.e_learning.Youtube_player_activity;
import com.nrt.e_learning.databinding.FragmentPlaylistBinding;



public class PlaylistFragment extends Fragment {

    private FragmentPlaylistBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PlaylistViewModel playlistViewModel =
                new ViewModelProvider(this).get(PlaylistViewModel.class);

        binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
//        TabLayout tabLayout = root.findViewById(R.id.tab_layout);

        AppCompatButton PaylistBtn = binding.PlaylistButton;

        PaylistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent  intent = new Intent(getActivity().getApplicationContext(), Youtube_player_activity.class);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}