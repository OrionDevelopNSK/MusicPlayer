package com.orion.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.orion.musicplayer.adapters.MusicStateAdapter;
import com.orion.musicplayer.fragments.SoundRecyclerViewFragment;
import com.orion.musicplayer.fragments.SoundTrackListDialogFragment;
import com.orion.musicplayer.fragments.SoundtrackPlayerControllerFragment;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.utils.MediaScannerObserver;
import com.orion.musicplayer.utils.StateMode;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private MediaSessionService mediaSessionService;
    private ServiceConnection serviceConnection;
    private SoundtrackPlayerModel soundtrackPlayerModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        checkPermissions();
        addFragmentControlPanel();
        Button buttonDialog = findViewById(R.id.open_dialog);
        setDialogClickListener(buttonDialog);
        soundtrackPlayerModel = new ViewModelProvider(this).get(SoundtrackPlayerModel.class);
        createServiceConnection();
        Intent intent = new Intent(new Intent(getApplicationContext(), MediaSessionService.class));
        ContextCompat.startForegroundService(getApplicationContext(), intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void createServiceConnection() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                Log.d(TAG, "Подключение сервиса");
                mediaSessionService = ((MediaSessionService.BinderService) binder).getService();
                setDatabaseLoadListeners();
                mediaSessionService.getDataLoader().execute();
                setSoundsControllerListeners();
                createMediaScannerObserver();
                createActions();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "Отключение сервиса");
            }
        };
    }

    private void createMediaScannerObserver() {
        @SuppressWarnings("unused")
        MediaScannerObserver mediaScannerObserver = new MediaScannerObserver(
                new Handler(Looper.getMainLooper()),
                this, mediaSessionService);;
    }

    private void setSoundsControllerListeners() {
        SoundsController soundsController = mediaSessionService.getSoundsController();
        soundsController.setOnChangeStateModeListener(new SoundsController.OnChangeStateModeListener() {
            @Override
            public void onChangeStateMode(StateMode stateMode) {
                soundtrackPlayerModel.getStateModeLiveData().setValue(stateMode);
            }
        });

        soundsController.setOnCurrentDurationListener(new SoundsController.OnCurrentDurationListener() {
            @Override
            public void onCurrentDuration(int duration) {
                soundtrackPlayerModel.getCurrentDurationLiveData().setValue(duration);
            }
        });

        soundsController.setOnCurrentPositionListener(new SoundsController.OnCurrentPositionListener() {
            @Override
            public void onCurrentPosition(int position) {
                soundtrackPlayerModel.getCurrentPositionLiveData().setValue(position);
            }
        });

        soundsController.setOnPlayingStatusListener(new SoundsController.OnPlayingStatusListener() {
            @Override
            public void onPlayingStatus(boolean isPlay) {
                soundtrackPlayerModel.getIsPlayingLiveData().setValue(isPlay);
            }
        });
    }

    private void setDatabaseLoadListeners() {
        DataLoader dataLoader = mediaSessionService.getDataLoader();
        dataLoader.setOnDatabaseLoadCompleteListener(new DataLoader.OnDatabaseLoadCompleteListener() {
            @Override
            public void onDatabaseLoadComplete() {
                soundtrackPlayerModel.getIsLoaded().postValue(true);
            }
        });

        dataLoader.setOnDatabaseLoadListener(new DataLoader.OnDatabaseLoadListener() {
            @Override
            public void onDatabaseLoad(List<Soundtrack> soundtracks) {
                soundtrackPlayerModel.getSoundtracksLiveData().postValue(soundtracks);
            }
        });
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

    private void createTabs() {
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
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
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

    private void createActions(){
        Log.d(TAG, "Создание обсервера нажатия кнопок плеера");
        soundtrackPlayerModel.getPlayerAction().observe(this, new Observer<Action>() {
            @Override
            public void onChanged(Action action) {
                switch (action) {
                    case PLAY_OR_PAUSE:
                        mediaSessionService.getSoundsController().playOrPause(
                                soundtrackPlayerModel.getCurrentPositionLiveData().getValue(),
                                soundtrackPlayerModel.getSoundtracksLiveData().getValue());
                        break;
                    case PREVIOUS:
                        mediaSessionService.getSoundsController().previous(
                                soundtrackPlayerModel.getCurrentPositionLiveData().getValue(),
                                soundtrackPlayerModel.getSoundtracksLiveData().getValue());
                        break;
                    case NEXT:
                        mediaSessionService.getSoundsController().next(
                                soundtrackPlayerModel.getCurrentPositionLiveData().getValue(),
                                soundtrackPlayerModel.getSoundtracksLiveData().getValue());
                        break;
                    case SWITCH_MODE:
                        mediaSessionService.getSoundsController().switchMode();
                        break;
                    case TO_START:
                        mediaSessionService.getSoundsController().playOrPause(
                                0,
                                soundtrackPlayerModel.getSoundtracksLiveData().getValue());
                        break;
                    case SLIDER_MANIPULATE:
                        mediaSessionService.getSoundsController().setCurrentDuration(
                                soundtrackPlayerModel.getCurrentDurationLiveData().getValue());
                        break;
                }
                Log.d(TAG, "Выбрано действие: " + action);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Отсоединение сервиса");
        unbindService(serviceConnection);
//        stopService(intent);
        Log.d(TAG, "Уничтожение активити");
        super.onDestroy();
    }


}
