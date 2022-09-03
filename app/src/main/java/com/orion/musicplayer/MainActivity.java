package com.orion.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputLayout;
import com.orion.musicplayer.adapters.MusicStateAdapter;
import com.orion.musicplayer.database.DataLoader;
import com.orion.musicplayer.fragments.SoundRecyclerViewFragment;
import com.orion.musicplayer.fragments.SoundTrackListDialogFragment;
import com.orion.musicplayer.fragments.SoundtrackPlayerControllerFragment;
import com.orion.musicplayer.models.Soundtrack;
import com.orion.musicplayer.services.MediaSessionService;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.utils.MediaScannerObserver;
import com.orion.musicplayer.utils.StateMode;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static String KEY_DATA = "currentSoundtrackTitle";
    private final static String KEY_DURATION = "currentSoundtrackDuration";
    private final static String KEY_STATE_MODE = "currentStateModePlaying";

    private MediaSessionService mediaSessionService;
    private ServiceConnection serviceConnection;
    private SoundtrackPlayerModel soundtrackPlayerModel;
    private SharedPreferences defaultsSharedPreferences;
    private Intent intent;

    private Button buttonDialog;
    private Button buttonSortedSoundtrack;
    private TextInputLayout textInputLayout;
    private TabLayout tabLayout;

    private String soundTitle;
    private long currentDuration;
    private StateMode currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        soundtrackPlayerModel = new ViewModelProvider(this).get(SoundtrackPlayerModel.class);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        checkPermissions();
        addFragmentControlPanel();
        buttonDialog = findViewById(R.id.open_dialog);
        buttonSortedSoundtrack = findViewById(R.id.sorted_soundtrack);
        textInputLayout = findViewById(R.id.textInputLayout);
        subscribeDialogClickListener(buttonDialog);
        load();
        createServiceConnection();
        intent = new Intent(new Intent(getApplicationContext(), MediaSessionService.class));
        startService(intent);
        //ContextCompat.startForegroundService(getApplicationContext(), intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void createServiceConnection() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                Log.d(TAG, "Подключение сервиса");
                mediaSessionService = ((MediaSessionService.BinderService) binder).getService();
                subscribeDatabaseLoadListeners();
                mediaSessionService.getDataLoader().execute();
                subscribeSoundsControllerListeners();
                createMediaScannerObserver();
                bindActions();
                defaultDescription();
                createDataValidateObserver();
                createStateModeObserver();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "Отключение сервиса");
            }
        };
    }

    private void createStateModeObserver() {
        soundtrackPlayerModel.getStateModeLiveData().observe(this, stateMode ->
                mediaSessionService.getSoundsController().setStateMode(stateMode));
    }

    private void createMediaScannerObserver() {
        @SuppressWarnings("unused")
        MediaScannerObserver mediaScannerObserver = new MediaScannerObserver(
                new Handler(Looper.getMainLooper()),
                this, mediaSessionService);
    }

    private void createDataValidateObserver() {
        soundtrackPlayerModel.getSoundtracksLiveData().observe(this, soundtracks -> {
            if (soundtracks.isEmpty()) {
                Log.d(TAG, "Список пуст");
                buttonDialog.setEnabled(false);
                buttonSortedSoundtrack.setEnabled(false);
                textInputLayout.setEnabled(false);
                //TODO ((View)tabLayout).setEnabled(false);
            } else {
                Log.d(TAG, "Список не пуст");
                if (buttonDialog.isEnabled()) return;
                Log.d(TAG, "Список пуст");
                buttonDialog.setEnabled(true);
                buttonSortedSoundtrack.setEnabled(true);
                textInputLayout.setEnabled(true);
                tabLayout.setEnabled(true);
            }
        });
    }

    private void subscribeSoundsControllerListeners() {
        SoundsController soundsController = mediaSessionService.getSoundsController();
        soundsController.setOnChangeStateModeListener(stateMode -> soundtrackPlayerModel.getStateModeLiveData().setValue(stateMode));
        soundsController.setOnCurrentDurationListener(duration -> soundtrackPlayerModel.getCurrentDurationLiveData().setValue(duration));
        soundsController.setOnCurrentPositionListener(position -> soundtrackPlayerModel.getCurrentPositionLiveData().setValue(position));
        soundsController.setOnPlayingStatusListener(isPlay -> soundtrackPlayerModel.getIsPlayingLiveData().setValue(isPlay));
    }

    private void subscribeDatabaseLoadListeners() {
        DataLoader dataLoader = mediaSessionService.getDataLoader();
        dataLoader.setOnDatabaseLoadCompleteListener(() -> soundtrackPlayerModel.getIsLoadedLiveData().postValue(true));
        dataLoader.setOnDatabaseLoadListener(soundtracks -> soundtrackPlayerModel.getSoundtracksLiveData().postValue(soundtracks));
    }

    private void subscribeDialogClickListener(Button buttonDialog) {
        buttonDialog.setOnClickListener(view -> {
            SoundTrackListDialogFragment fragment = SoundTrackListDialogFragment.newInstance();
            fragment.show(getSupportFragmentManager(), "Выберите песни");
        });
    }

    private void addFragmentControlPanel() {
        Log.d(TAG, "Добавление фрагмента к Активити если отсутствует");
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container_control_panel) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container_control_panel, SoundtrackPlayerControllerFragment.newInstance())
                    .commit();
        }
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
        if (requestCode == 1) {
            Log.d(TAG, "Проверка разрешений на чтение внутреннего хранилища");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Доступ на чтение внутреннего хранилища разрешен");
                createTabs();
            } else {
                Log.d(TAG, "Доступ на чтение внутреннего хранилища запрещен");
                Toast.makeText(MainActivity.this, "В доступе отказано", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createTabs() {
        MusicStateAdapter musicStateAdapter = new MusicStateAdapter(this);
        tabLayout = findViewById(R.id.tab_layout_media);
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

    private void bindActions() {
        Log.d(TAG, "Создание обсервера нажатия кнопок плеера");
        soundtrackPlayerModel.getPlayerActionLiveData().observe(this, action -> {
            if (action == Action.UNKNOWN) return;
            switch (action) {
                case PLAY:
                    if (!soundtrackPlayerModel.getIsPlayingLiveData().getValue())
                        mediaSessionService.getSoundsController().playOrPause(
                                soundtrackPlayerModel.getCurrentPositionLiveData().getValue(),
                                soundtrackPlayerModel.getSoundtracksLiveData().getValue());
                    break;
                case PAUSE:
                    if (soundtrackPlayerModel.getIsPlayingLiveData().getValue())
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
                    soundtrackPlayerModel.getCurrentPositionLiveData().setValue(0);
                    break;
                case SLIDER_MANIPULATE:
                    mediaSessionService.getSoundsController().setCurrentDuration(
                            soundtrackPlayerModel.getCurrentDurationLiveData().getValue());
                    break;
            }
            Log.d(TAG, "Выбрано действие: " + action);

            mediaSessionService.createNotification(
                    soundtrackPlayerModel.getCurrentPositionLiveData().getValue(),
                    soundtrackPlayerModel.getStateModeLiveData().getValue() );
            //Чтобы не было повторный отправки действий при повороте экрана
            soundtrackPlayerModel.getPlayerActionLiveData().setValue(Action.UNKNOWN);
        });
    }

    @Override
    public void onBackPressed() {
        showDialogExit();
    }

    private void showDialogExit() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_close_app)
                .setPositiveButton(R.string.yes_dialog, (dialogInterface, i) -> {
                    stopService(intent);
                    finish();
                })
                .setNegativeButton(R.string.no_dialog, (dialogInterface, i) -> {
                })
                .show();
    }

    @SuppressLint("ApplySharedPref")
    private void save() {
        Log.d(TAG, "Сохранить состояние " + soundTitle);
        defaultsSharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = defaultsSharedPreferences.edit();
        currentDuration = soundtrackPlayerModel.getCurrentDurationLiveData().getValue();
        soundTitle = soundtrackPlayerModel.getSoundtracksLiveData().getValue()
                .get(soundtrackPlayerModel.getCurrentPositionLiveData().getValue()).getData();
        currentState = soundtrackPlayerModel.getStateModeLiveData().getValue();
        editor.putString(KEY_DATA, soundTitle);
        editor.putLong(KEY_DURATION, currentDuration);
        editor.putString(KEY_STATE_MODE, soundtrackPlayerModel.getStateModeLiveData().getValue().toString());
        editor.commit();
    }

    private void load() {
        defaultsSharedPreferences = getPreferences(Context.MODE_PRIVATE);
        currentDuration = defaultsSharedPreferences.getLong(KEY_DURATION, 0);
        soundTitle = defaultsSharedPreferences.getString(KEY_DATA, "");
        currentState = StateMode.valueOf(defaultsSharedPreferences.getString(KEY_STATE_MODE, "LOOP"));
        soundtrackPlayerModel.getStateModeLiveData().setValue(currentState);
        Log.d(TAG, String.format("Получить состояния soundTitle: %s currentDuration: %d currentState: %s", soundTitle, currentDuration, currentState));
    }

    public void defaultDescription() {
        soundtrackPlayerModel.getIsLoadedLiveData().observe(this, aBoolean -> {
            List<Soundtrack> soundtracks = soundtrackPlayerModel.getSoundtracksLiveData().getValue();
            if (soundtracks.size() == 0) return;
            int position = 0;
            for (int i = 0; i < soundtracks.size(); i++) {
                if (soundtracks.get(i).getData().equals(soundTitle)) {
                    position = i;
                    Log.d(TAG, "Найдена последняя воиспроизводимая песня, номер: " + position);
                    break;
                }
            }
            soundtrackPlayerModel.getCurrentPositionLiveData().setValue(position);
            mediaSessionService.getSoundsController().setCurrentPosition(position);
            //mediaSessionService.createNotification(position, currentState);
        });
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "Отсоединение сервиса");
        unbindService(serviceConnection);
        Log.d(TAG, "Сохранение настроек");
        save();
//        stopService(intent);
        Log.d(TAG, "Уничтожение активити");
        super.onDestroy();
    }


}
