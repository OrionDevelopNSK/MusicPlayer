package com.orion.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.orion.musicplayer.fragments.SoundtrackPlayerControllerFragment;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        addFragmentControlPanel();
    }

    private void addFragmentControlPanel() {
        Log.d(TAG, "Добавление фрагмента к Активити");
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container_control_panel, SoundtrackPlayerControllerFragment.newInstance())
                .commit();
    }

    private void checkPermissions() {
        Log.d(TAG, "Проверка разрешений чтения и записи внутренней памяти");
        int requestCode = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (requestCode != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Установка разрешений");
            Toast.makeText(this, "Требуется установить разрешения", Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_MEDIA_LOCATION},
                        1);
            }
        } else {
            Log.d(TAG, "Доступ на чтение внутреннего хранилища разрешен");
            createTabs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                Log.d(TAG, "Проверка разрешений на чтение внутреннего хранилища");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Доступ на чтение внутреннего хранилища разрешен");
                    createTabs();
                } else {
                    Log.d(TAG, "Доступ на чтение внутреннего хранилища запрещен");
                    Toast.makeText(MainActivity.this, "В доступе отказано", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    void createTabs() {
        MusicStateAdapter musicStateAdapter = new MusicStateAdapter(this);
        TabLayout tabLayout = findViewById(R.id.tab_layout_media);
        ViewPager2 viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(musicStateAdapter);
        addFragmentsToAdapter(musicStateAdapter);
        createTabLayoutMediator(tabLayout, viewPager);
        addTabLayoutsListener(tabLayout, viewPager);
        addPageChangeCallback(tabLayout, viewPager);
    }

    private void addPageChangeCallback(TabLayout tabLayout, ViewPager2 viewPager) {
        Log.d(TAG, "Добавление обратного вызова изменения отображения страницы");
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private void addTabLayoutsListener(TabLayout tabLayout, ViewPager2 viewPager) {
        Log.d(TAG, "Добавление слушателей к вкладкам");
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void addFragmentsToAdapter(MusicStateAdapter musicStateAdapter) {
        Log.d(TAG, "Добавление фрагментов к адаптеру");
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);


        ///TODO
        Fragment fragment2 = getSupportFragmentManager().findFragmentById(R.id.fragment_container_control_panel);
        fragment2 = SoundRecyclerViewFragment.newInstance();

        if (fragment == null) {
            fragment = SoundRecyclerViewFragment.newInstance();
            musicStateAdapter.addFragment(fragment);
            ///TODO
            musicStateAdapter.addFragment(fragment2);
        }
    }

    private void createTabLayoutMediator(TabLayout tabLayout, ViewPager2 viewPager) {
        Log.d(TAG, "Создание медиатора для связывания TabLayout с ViewPager2");
        new TabLayoutMediator(tabLayout, viewPager,
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
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "Уничтожение активити");
        super.onDestroy();

    }
}