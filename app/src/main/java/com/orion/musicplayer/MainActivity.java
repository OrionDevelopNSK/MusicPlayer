package com.orion.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.orion.musicplayer.adapters.MusicStateAdapter;
import com.orion.musicplayer.fragments.SoundRecyclerViewFragment;
import com.orion.musicplayer.fragments.SoundTrackListDialogFragment;
import com.orion.musicplayer.fragments.SoundtrackPlayerControllerFragment;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = SoundtrackPlayerModel.class.getSimpleName();


    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback callback;
    private ServiceConnection serviceConnection;
    private MediaPlaybackService.MediaPlaybackServiceBinder playerServiceBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        checkPermissions();
        addFragmentControlPanel();
        Button buttonDialog = findViewById(R.id.open_dialog);
        setDialogClickListener(buttonDialog);


        callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if (state == null)
                    return;
                boolean playing = state.getState() == PlaybackStateCompat.STATE_PLAYING;
//                playButton.setEnabled(!playing);
//                pauseButton.setEnabled(playing);
//                stopButton.setEnabled(playing);
            }
        };

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerServiceBinder = (MediaPlaybackService.MediaPlaybackServiceBinder) service;
                try {
                    mediaController = new MediaControllerCompat(MainActivity.this, playerServiceBinder.getMediaSessionToken());
                    mediaController.registerCallback(callback);
                    callback.onPlaybackStateChanged(mediaController.getPlaybackState());
                }
                catch (RemoteException e) {
                    mediaController = null;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                playerServiceBinder = null;
                if (mediaController != null) {
                    mediaController.unregisterCallback(callback);
                    mediaController = null;
                }
            }
        };

        bindService(new Intent(this, MediaPlaybackService.class), serviceConnection, BIND_AUTO_CREATE);

    }

    private void setDialogClickListener(Button buttonDialog) {
        buttonDialog.setOnClickListener(view -> {
            SoundTrackListDialogFragment fragment = SoundTrackListDialogFragment.newInstance();
            fragment.show(getSupportFragmentManager(), "Выберите песни");
        });
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
        playerServiceBinder = null;
        if (mediaController != null) {
            mediaController.unregisterCallback(callback);
            mediaController = null;
        }
        unbindService(serviceConnection);

    }



}
