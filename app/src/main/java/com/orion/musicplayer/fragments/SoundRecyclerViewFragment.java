package com.orion.musicplayer.fragments;

import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.orion.musicplayer.AudioReader;
import com.orion.musicplayer.R;
import com.orion.musicplayer.SoundtrackPlayer;
import com.orion.musicplayer.adapters.SoundtrackAdapter;
import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.utils.MediaScannerObserver;
import com.orion.musicplayer.viewmodels.SoundtracksModel;


public class SoundRecyclerViewFragment extends Fragment {



    private final SoundtrackPlayer soundtrackPlayer = new SoundtrackPlayer();

    public static SoundRecyclerViewFragment newInstance() {
        return new SoundRecyclerViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_list_view, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.listSongs);
        SoundtrackAdapter.OnSoundtrackClickListener onSoundtrackClickListener = (soundtrack, position) -> {
            Toast.makeText(requireActivity(), "Был выбран пункт " + soundtrack.getTitle(),
                    Toast.LENGTH_SHORT).show();
            soundtrackPlayer.play(soundtrack);
        };

        AudioReader audioReader = new AudioReader(requireActivity());
        AppDatabase database = Room.databaseBuilder(requireActivity(),
                        AppDatabase.class,
                        "database").
                build();

        Looper looper = Looper.getMainLooper();
        Handler handler = new Handler(looper);


        SoundtracksModel soundtracksModel = new ViewModelProvider(requireActivity()).get(SoundtracksModel.class);
        MediaScannerObserver mediaScannerObserver = new MediaScannerObserver(
                handler,
                soundtracksModel,
                database,
                audioReader,
                requireActivity());

        AsyncTask.execute(()-> {
            soundtracksModel.execute(database, audioReader);
        });

        soundtracksModel.getSoundtracks().observe(requireActivity(), soundtracks -> {
            SoundtrackAdapter soundtrackAdapter = new SoundtrackAdapter(getContext(), soundtracks, onSoundtrackClickListener);
            recyclerView.setAdapter(soundtrackAdapter);
        });

        return view;
    }




}