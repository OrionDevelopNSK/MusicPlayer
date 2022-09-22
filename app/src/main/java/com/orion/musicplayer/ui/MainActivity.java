package com.orion.musicplayer.ui;

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
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
import com.orion.musicplayer.models.PlayerController;
import com.orion.musicplayer.ui.adapters.MusicStateAdapter;
import com.orion.musicplayer.data.database.DataLoader;
import com.orion.musicplayer.data.database.PlaylistDatabaseHelper;
import com.orion.musicplayer.ui.fragments.CreatorPlaylistDialogFragment;
import com.orion.musicplayer.ui.fragments.PlayingControllerFragment;
import com.orion.musicplayer.ui.fragments.PlaylistDetailListFragment;
import com.orion.musicplayer.ui.fragments.SongDetailListFragment;
import com.orion.musicplayer.ui.fragments.SongListFragment;
import com.orion.musicplayer.data.models.Song;
import com.orion.musicplayer.services.MediaSessionService;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.utils.MediaScannerObserver;
import com.orion.musicplayer.utils.SortingType;
import com.orion.musicplayer.utils.StateMode;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_DATA = "currentSoundtrackTitle";
    private static final String KEY_DURATION = "currentSoundtrackDuration";
    private static final String KEY_STATE_MODE = "currentStateModePlaying";
    private static final String KEY_SORTING_TYPE = "currentSortingType";

    private MediaSessionService mediaSessionService;
    private ServiceConnection serviceConnection;
    private DataModel dataModel;
    private SharedPreferences defaultsSharedPreferences;
    private PlaylistDatabaseHelper playlistDatabaseHelper;
    private MediaScannerObserver mediaScannerObserver;
    private Intent intent;

    private Button buttonDialog;
    private Button buttonSortingMode;
    private TextInputLayout textInputLayout;
    private TabLayout tabLayout;
    private Animation buttonAnimationClick;

    private String soundTitle;
    private long currentDuration;
    private StateMode currentState;
    private SortingType sortingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        checkPermissions();
    }

    private void initialize() {
        dataModel = new ViewModelProvider(this).get(DataModel.class);

//        TextView textView = new TextView(getApplicationContext());
//        textView.setText("Песни отсутствуют");
//        textView.setGravity(Gravity.CENTER);

        setContentView(R.layout.activity_main);
        addFragmentControlPanel();
        buttonAnimationClick = AnimationUtils.loadAnimation(this, R.anim.button_click);
        buttonDialog = findViewById(R.id.open_dialog);
        buttonSortingMode = findViewById(R.id.sorted_soundtrack);
        textInputLayout = findViewById(R.id.textInputLayout);
        subscribeButtonDialogClickListener(buttonDialog);
        subscribeButtonSortingClickListener();
        loadDefaultsSharedPreferences();
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
                soundTitle = dataModel.getSongsLiveData().getValue()
                        .get(dataModel.getCurrentPositionLiveData().getValue())
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
                bindActions();
                defaultDescription();
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
        dataModel.getSortingTypeLiveData().observe(this, sortingType -> {
            mediaScannerObserver.setSortingType(sortingType);
            mediaSessionService.getDataLoader().refresh(sortingType);
            mediaSessionService.getSoundsController().clearDequeSoundtrack();
        });
    }

    private void createMediaScannerObserver() {
        mediaScannerObserver = new MediaScannerObserver(
                new Handler(Looper.getMainLooper()),
                this, mediaSessionService, sortingType);
    }

    private void createDataValidateObserver() {
        dataModel.getSongsLiveData().observe(this, soundtracks -> {
            if (soundtracks.isEmpty()) {
                Log.d(TAG, "Список пуст");
                buttonDialog.setEnabled(false);
                buttonSortingMode.setEnabled(false);
                textInputLayout.setEnabled(false);
                //TODO ((View)tabLayout).setEnabled(false);
            } else {
                Log.d(TAG, "Список не пуст");
                if (buttonDialog.isEnabled()) return;
                Log.d(TAG, "Список пуст");
                buttonDialog.setEnabled(true);
                buttonSortingMode.setEnabled(true);
                textInputLayout.setEnabled(true);
                tabLayout.setEnabled(true);
            }
        });
    }

    private void subscribeSoundsControllerListeners() {
        PlayerController soundsController = mediaSessionService.getSoundsController();
        soundsController.setOnChangeStateModeListener(stateMode -> dataModel.getStateModeLiveData().setValue(stateMode));
        soundsController.setOnCurrentDurationListener(duration -> dataModel.getDurationLiveData().setValue(duration));
        soundsController.setOnCurrentPositionListener(position -> {
            dataModel.getCurrentPositionLiveData().setValue(position);
            MainActivity.this.createOrRefreshNotification();
        });
        soundsController.setOnPlayingStatusListener(isPlay -> dataModel.getIsPlayingLiveData().setValue(isPlay));
    }

    private void subscribeDatabaseLoadListeners() {
        DataLoader dataLoader = mediaSessionService.getDataLoader();
        dataLoader.setOnDatabaseLoadListener(soundtracks -> {
            //setContentView(R.layout.activity_main);
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

    private MusicStateAdapter musicStateAdapter;
    private ViewPager2 viewPager;

    private void createTabs() {
        musicStateAdapter = new MusicStateAdapter(this);
        tabLayout = findViewById(R.id.tab_layout_media);
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
        PlaylistDetailListFragment playlistDetailListFragment = null;
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
            System.out.println(musicStateAdapter);
            musicStateAdapter.addFragment(songDetailListFragment);
            System.out.println(musicStateAdapter);
            musicStateAdapter.removeFragment(playlistListFragment);
            System.out.println(musicStateAdapter);
            viewPager.setAdapter(musicStateAdapter);
            viewPager.setCurrentItem(1);

            List<Fragment> fragments = getSupportFragmentManager().getFragments();
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

    private void bindActions() {
        Log.d(TAG, "Создание обсервера нажатия кнопок плеера");
        dataModel.getPlayerActionLiveData().observe(this, action -> {
            if (action == Action.UNKNOWN) return;
            List<Song> songs;
            if (!dataModel.getIsFromPlaylist().getValue()) {
                songs = dataModel.getSongsLiveData().getValue();
            } else {
                songs = dataModel.getPlaylistLiveData().getValue().get(dataModel.getCurrentPlaylist().getValue());
            }

            int position = dataModel.getCurrentPositionLiveData().getValue();

            switch (action) {
                case PLAY:
                    if (!dataModel.getIsPlayingLiveData().getValue()) {
                        mediaSessionService.getSoundsController().playOrPause(position, songs);
                        createOrRefreshNotification();
                    }
                    break;
                case PAUSE:
                    if (dataModel.getIsPlayingLiveData().getValue())
                        mediaSessionService.getSoundsController().playOrPause(position, songs);
                    break;
                case PREVIOUS:
                    mediaSessionService.getSoundsController().previous(position, songs);
                    break;
                case NEXT:
                    mediaSessionService.getSoundsController().next(position, songs);
                    break;
                case SWITCH_MODE:
                    mediaSessionService.getSoundsController().switchMode();
                    break;
                case TO_START:
                    mediaSessionService.getSoundsController().playOrPause(0, songs);
                    dataModel.getCurrentPositionLiveData().setValue(0);
                    break;
                case SLIDER_MANIPULATE:
                    mediaSessionService.getSoundsController().setCurrentDuration(
                            dataModel.getDurationLiveData().getValue());
                    break;
            }
            Log.d(TAG, "Выбрано действие: " + action);
            //чтобы команды не проходили повторно при смене ориентации экрана
            dataModel.getPlayerActionLiveData().setValue(Action.UNKNOWN);
        });
    }

    public void createOrRefreshNotification() {
        mediaSessionService.createNotification(
                dataModel.getCurrentPositionLiveData().getValue(),
                dataModel.getStateModeLiveData().getValue(),
                dataModel.getSongsLiveData().getValue()
                        .get(dataModel.getCurrentPositionLiveData().getValue()).getRating());
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
    private void saveDefaultsSharedPreferences() {
        Log.d(TAG, "Сохранить состояние " + soundTitle);
        defaultsSharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = defaultsSharedPreferences.edit();
        //Защита от NPE при уничтожении активити
        if (dataModel.getDurationLiveData().getValue() == null) return;
        currentDuration = dataModel.getDurationLiveData().getValue();
        soundTitle = dataModel.getSongsLiveData().getValue()
                .get(dataModel.getCurrentPositionLiveData().getValue()).getData();
        currentState = dataModel.getStateModeLiveData().getValue();
        editor.putString(KEY_DATA, soundTitle);
        editor.putString(KEY_SORTING_TYPE, dataModel.getSortingTypeLiveData().getValue().toString());
        editor.putLong(KEY_DURATION, currentDuration);
        editor.putString(KEY_STATE_MODE, dataModel.getStateModeLiveData().getValue().toString());
        editor.commit();
    }

    private void loadDefaultsSharedPreferences() {
        defaultsSharedPreferences = getPreferences(Context.MODE_PRIVATE);
        currentDuration = defaultsSharedPreferences.getLong(KEY_DURATION, 0);
        soundTitle = defaultsSharedPreferences.getString(KEY_DATA, "");
        currentState = StateMode.valueOf(defaultsSharedPreferences.getString(KEY_STATE_MODE, "LOOP"));
        sortingType = SortingType.valueOf(defaultsSharedPreferences.getString(KEY_SORTING_TYPE, "DATE"));
        dataModel.getSortingTypeLiveData().setValue(sortingType);
        dataModel.getStateModeLiveData().setValue(currentState);
        Log.d(TAG, String.format("Получить состояния soundTitle: %s currentDuration: %d currentState: %s",
                soundTitle, currentDuration, currentState));
    }

    public void defaultDescription() {
        dataModel.getIsLoadedLiveData().observe(this, aBoolean -> {
            List<Song> songs = dataModel.getSongsLiveData().getValue();
            if (songs.size() == 0) return;
            int position = 0;
            for (int i = 0; i < songs.size(); i++) {
                if (songs.get(i).getData().equals(soundTitle)) {
                    position = i;
                    Log.d(TAG, "Найдена последняя воиспроизводимая песня, номер: " + position);
                    break;
                }
            }
            dataModel.getCurrentPositionLiveData().setValue(position);
            mediaSessionService.getSoundsController().setCurrentPosition(position);
            mediaSessionService.initMediaPlayer();
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Отсоединение сервиса");
        unbindService(serviceConnection);
        Log.d(TAG, "Сохранение настроек");
        saveDefaultsSharedPreferences();
        Log.d(TAG, "Уничтожение активити");
        super.onDestroy();
    }


}
