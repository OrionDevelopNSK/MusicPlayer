package com.orion.musicplayer.ui;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputLayout;
import com.orion.musicplayer.R;
import com.orion.musicplayer.data.database.DataLoader;
import com.orion.musicplayer.data.database.PlaylistDatabaseHelper;
import com.orion.musicplayer.models.ActionBinder;
import com.orion.musicplayer.models.DefaultDescriptionControllerFragmentCreator;
import com.orion.musicplayer.models.NotificationController;
import com.orion.musicplayer.models.PlayerController;
import com.orion.musicplayer.models.SharedPreferencesController;
import com.orion.musicplayer.services.MediaSessionService;
import com.orion.musicplayer.ui.adapters.MusicStateAdapter;
import com.orion.musicplayer.ui.fragments.CreatorPlaylistDialogFragment;
import com.orion.musicplayer.ui.fragments.PlayingControllerFragment;
import com.orion.musicplayer.ui.fragments.PlaylistDetailListFragment;
import com.orion.musicplayer.ui.fragments.SongDetailListFragment;
import com.orion.musicplayer.ui.fragments.SongListFragment;
import com.orion.musicplayer.utils.MediaScannerObserver;
import com.orion.musicplayer.utils.SortingType;
import com.orion.musicplayer.utils.TabLayoutWrapper;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private MediaSessionService mediaSessionService;
    private ServiceConnection serviceConnection;
    private DataModel dataModel;
    private PlaylistDatabaseHelper playlistDatabaseHelper;
    private MediaScannerObserver mediaScannerObserver;
    private SharedPreferencesController sharedPreferencesController;
    private ActionBinder actionBinder;
    private NotificationController notificationController;
    private DefaultDescriptionControllerFragmentCreator defaultDescriptionControllerFragmentCreator;
    private String soundTitle;
    private Intent intent;

    private Button buttonSortingMode;
    private MusicStateAdapter musicStateAdapter;
    private ViewPager2 viewPager;
    private TextInputLayout textInputLayoutFindSong;
    private Animation buttonAnimationClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        checkPermissions();
    }

    private void initialize() {
        dataModel = new ViewModelProvider(this).get(DataModel.class);
        setContentView(R.layout.activity_main);
        addFragmentControlPanel();
        buttonAnimationClick = AnimationUtils.loadAnimation(this, R.anim.button_click);
        Button buttonDialog = findViewById(R.id.open_dialog);
        buttonSortingMode = findViewById(R.id.sorted_soundtrack);
        textInputLayoutFindSong = findViewById(R.id.textInputLayout);
        subscribeButtonDialogClickListener(buttonDialog);
        subscribeButtonSortingClickListener();
        sharedPreferencesController = new SharedPreferencesController(dataModel, this);
        sharedPreferencesController.loadDefaultsSharedPreferences();
        soundTitle = sharedPreferencesController.getSoundTitle();
        subscribeCurrentDataPositionChanged();
        createServiceConnection();
        intent = new Intent(new Intent(getApplicationContext(), MediaSessionService.class));
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        playlistDatabaseHelper = new PlaylistDatabaseHelper(this);
        createTabs();
    }

    private void subscribeCurrentDataPositionChanged() {
        dataModel.getCurrentPositionLiveData().observe(this, integer ->
                soundTitle = Objects.requireNonNull(dataModel.getSongsLiveData().getValue())
                        .get(Objects.requireNonNull(dataModel.getCurrentPositionLiveData().getValue()))
                        .getData());
    }

    @SuppressLint("NonConstantResourceId")
    public void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.BOTTOM);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.sort_by_date:
                    dataModel.getSortingTypeLiveData().setValue(SortingType.DATE);
                    return true;
                case R.id.sort_by_rating:
                    dataModel.getSortingTypeLiveData().setValue(SortingType.RATING);
                    return true;
                case R.id.sort_by_repeatability:
                    dataModel.getSortingTypeLiveData().setValue(SortingType.REPEATABILITY);
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.inflate(R.menu.menu_sorting);
        popupMenu.show();
    }

    private void createServiceConnection() {
        MainActivity activity = this;
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                Log.d(TAG, "Подключение сервиса");
                mediaSessionService = ((MediaSessionService.BinderService) binder).getService();
                subscribeDatabaseLoadListeners();
                mediaSessionService.getDataLoader().execute(dataModel.getSortingTypeLiveData().getValue());
                playlistDatabaseHelper.loadPlaylistWithSoundtrack();
                subscribeSoundsControllerListeners();
                createMediaScannerObserver();
                notificationController = new NotificationController(dataModel, mediaSessionService);
                actionBinder = new ActionBinder(dataModel, mediaSessionService, notificationController, activity);
                actionBinder.bindActions();
                defaultDescriptionControllerFragmentCreator =
                        new DefaultDescriptionControllerFragmentCreator(
                                dataModel,
                                mediaSessionService,
                                soundTitle,
                                activity);
                defaultDescriptionControllerFragmentCreator.defaultDescription();
                createDataValidateObserver();
                createStateModeObserver();
                createSortingTypeObserver();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "Отключение сервиса");
            }
        };
    }

    private void createStateModeObserver() {
        dataModel.getStateModeLiveData().observe(this, stateMode -> {
            mediaSessionService.getSoundsController().setStateMode(stateMode);
            mediaSessionService.getSoundsController().clearDequeSoundtrack();
        });
    }

    private void createSortingTypeObserver() {
        dataModel.getSortingTypeLiveData().observe(this, thisSortingType -> {
            mediaScannerObserver.setSortingType(thisSortingType);
            mediaSessionService.getDataLoader().refresh(thisSortingType);
            mediaSessionService.getSoundsController().clearDequeSoundtrack();
        });
    }

    private void createMediaScannerObserver() {
        mediaScannerObserver = new MediaScannerObserver(
                new Handler(Looper.getMainLooper()),
                this, mediaSessionService, sharedPreferencesController.getSortingType());
    }

    private void createDataValidateObserver() {
        dataModel.getSongsLiveData().observe(this, soundtracks -> {
            if (!Objects.requireNonNull(dataModel.getSongsLiveData().getValue()).isEmpty()) return;
            TextView textView = new TextView(getApplicationContext());
            textView.setText("Песни отсутствуют");
            textView.setGravity(Gravity.CENTER);
            setContentView(textView);
        });
    }

    private void subscribeSoundsControllerListeners() {
        PlayerController soundsController = mediaSessionService.getSoundsController();
        soundsController.setOnChangeStateModeListener(stateMode -> dataModel.getStateModeLiveData().setValue(stateMode));
        soundsController.setOnCurrentDurationListener(duration -> dataModel.getDurationLiveData().setValue(duration));
        soundsController.setOnCurrentPositionListener(position -> {
            dataModel.getCurrentPositionLiveData().setValue(position);
            notificationController.createOrRefreshNotification();
        });
        soundsController.setOnPlayingStatusListener(isPlay -> dataModel.getIsPlayingLiveData().setValue(isPlay));
    }

    private void subscribeDatabaseLoadListeners() {
        DataLoader dataLoader = mediaSessionService.getDataLoader();
        dataLoader.setOnDatabaseLoadListener(soundtracks -> {
            dataModel.getSongsLiveData().postValue(soundtracks);
            dataModel.getIsLoadedLiveData().postValue(true);
        });
    }

    private void subscribeButtonDialogClickListener(Button buttonDialog) {
        buttonDialog.setOnClickListener(view -> {
            buttonDialog.startAnimation(buttonAnimationClick);
            CreatorPlaylistDialogFragment fragment = new CreatorPlaylistDialogFragment(playlistDatabaseHelper);
//            fragment.setStyle(ChooserDialogFragment.STYLE_NO_TITLE, R.style.Dialog);
            fragment.show(getSupportFragmentManager(), "Выберите песни");
        });
    }

    private void subscribeButtonSortingClickListener() {
        buttonSortingMode.setOnClickListener(view -> {
            buttonSortingMode.startAnimation(buttonAnimationClick);
            showPopup(view);
        });
    }

    private void addFragmentControlPanel() {
        Log.d(TAG, "Добавление фрагмента к Активити если отсутствует");
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container_control_panel) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container_control_panel, PlayingControllerFragment.newInstance())
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
            initialize();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            Log.d(TAG, "Проверка разрешений на чтение внутреннего хранилища");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Доступ на чтение внутреннего хранилища разрешен");
                initialize();
            } else {
                Log.d(TAG, "Доступ на чтение внутреннего хранилища запрещен");
                Toast.makeText(MainActivity.this, "В доступе отказано", Toast.LENGTH_SHORT).show();
            }
        }
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
        tabLayout.addOnTabSelectedListener(new TabLayoutWrapper() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });
    }



    private void createTabs() {
        musicStateAdapter = new MusicStateAdapter(this);
        TabLayout tabLayout = findViewById(R.id.tab_layout_media);
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(musicStateAdapter);
        addSongListFragment();
        addPlaylistDetailListFragment();
        createTabLayoutMediator(tabLayout, viewPager);
        addTabLayoutsListener(tabLayout, viewPager);
        addPageChangeCallback(tabLayout, viewPager);
    }

    private void addSongListFragment() {
        Log.d(TAG, "Добавление фрагмента списка песней к адаптеру");
        musicStateAdapter.addFragment(findOrCreateSongListFragment());
    }

    @NonNull
    private SongListFragment findOrCreateSongListFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof SongListFragment) {
                return (SongListFragment) fragment;
            }
        }
        return SongListFragment.newInstance();
    }

    private void findOrCreateSongDetailListFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof SongDetailListFragment) {
                songDetailListFragment = (SongDetailListFragment) fragment;
                subscribeClickBackToSongsListener();
                break;
            }
        }
    }

    @NonNull
    private PlaylistDetailListFragment findOrCreatePlaylistDetailListFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        PlaylistDetailListFragment playlistDetailListFragment;
        for (Fragment fragment : fragments) {
            if (fragment instanceof PlaylistDetailListFragment) {
                playlistDetailListFragment = (PlaylistDetailListFragment) fragment;
                playlistDetailListFragment.setPlaylistDatabaseHelper(playlistDatabaseHelper);
                return playlistDetailListFragment;
            }
        }
        playlistDetailListFragment = PlaylistDetailListFragment.newInstance();
        playlistDetailListFragment.setPlaylistDatabaseHelper(playlistDatabaseHelper);
        return playlistDetailListFragment;
    }

    private SongDetailListFragment songDetailListFragment;
    private PlaylistDetailListFragment playlistListFragment;

    private void addPlaylistDetailListFragment() {
        Log.d(TAG, "Добавление фрагмента списка плейлистоа к адаптеру");
        playlistListFragment = findOrCreatePlaylistDetailListFragment();
        musicStateAdapter.addFragment(playlistListFragment);
        findOrCreateSongDetailListFragment();
        subscribeOnClickPlaylistListener();
    }

    private void subscribeOnClickPlaylistListener() {
        playlistListFragment.setOnClickPlaylistListener(playlist -> {
            if (songDetailListFragment == null) {
                songDetailListFragment = new SongDetailListFragment();
            }
            musicStateAdapter.addFragment(songDetailListFragment);
            musicStateAdapter.removeFragment(playlistListFragment);
            viewPager.setAdapter(musicStateAdapter);
            viewPager.setCurrentItem(1);
            subscribeClickBackToSongsListener();
        });
    }

    private void subscribeClickBackToSongsListener() {
        songDetailListFragment.setOnClickBackToSongsListener(() -> {
            addPlaylistDetailListFragment();
            musicStateAdapter.removeFragment(songDetailListFragment);
            viewPager.setAdapter(musicStateAdapter);
            viewPager.setCurrentItem(1);
        });
    }

    private void createTabLayoutMediator(TabLayout tabLayout, ViewPager2 viewPager) {
        Log.d(TAG, "Создание медиатора для связывания TabLayout с ViewPager2");
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.music);
                    break;
                case 1:
                    tab.setText(R.string.playlist);
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

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Отсоединение сервиса");
        unbindService(serviceConnection);
        Log.d(TAG, "Сохранение настроек");
        sharedPreferencesController.saveDefaultsSharedPreferences();
        Log.d(TAG, "Уничтожение активити");
        super.onDestroy();
    }


}
