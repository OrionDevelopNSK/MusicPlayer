package com.orion.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.orion.musicplayer.adapters.MusicStateAdapter;
import com.orion.musicplayer.fragments.SoundRecyclerViewFragment;

public class MainActivity extends AppCompatActivity {

    class MediaScannerObserver extends ContentObserver {
        public MediaScannerObserver(Handler handler) {
            super(handler);
            getContentResolver().registerContentObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    false,
                    new MediaScannerObserver(new Handler())
            );
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            refreshTabs();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        request();
    }

    private void request() {
        int requestCode = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (requestCode != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Требуется установить разрешения", Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_MEDIA_LOCATION},
                        1);
            }
        } else {
            refreshTabs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        refreshTabs();
    }

    void refreshTabs() {
        MusicStateAdapter musicStateAdapter = new MusicStateAdapter(this);
        TabLayout tabLayout = findViewById(R.id.tab_layout_media);
        ViewPager2 viewPager2 = findViewById(R.id.pager);
        viewPager2.setAdapter(musicStateAdapter);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);

        ///TODO
        Fragment fragment2 = getSupportFragmentManager().findFragmentById(R.id.fragment_container_view2);
        fragment2 = SoundRecyclerViewFragment.newInstance();
        ///END TODO

        if (fragment == null) {
            fragment = SoundRecyclerViewFragment.newInstance();

            musicStateAdapter.addFragment(fragment);

            ///TODO
            musicStateAdapter.addFragment(fragment2);
            ///END TODO
        }

        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Music");
                            break;
                        case 1:
                            tab.setText("Playlist");
                            break;
                        case 2:
                            tab.setText("VK");
                            break;
                        default:
                            tab.setText("NULL");
                            break;
                    }
                }
        ).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

    }

}