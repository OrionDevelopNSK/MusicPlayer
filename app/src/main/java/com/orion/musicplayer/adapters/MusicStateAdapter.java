package com.orion.musicplayer.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class MusicStateAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragments = new ArrayList<>();

    public MusicStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {

        return fragments.size();
    }

    public void addFragment(Fragment fragment){

        fragments.add(fragment);
    }


}
