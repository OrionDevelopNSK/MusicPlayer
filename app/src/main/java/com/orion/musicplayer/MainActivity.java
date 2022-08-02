package com.orion.musicplayer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    private static boolean READ_AUDIO = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container_view);

        if (fragment == null){
            fragment = SoundListViewFragment.newInstance();
            fragmentManager.beginTransaction().
                    add(R.id.fragment_container_view, fragment).commit();
        }
    }
}