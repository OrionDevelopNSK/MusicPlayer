package com.orion.musicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class SoundRecyclerViewFragment extends Fragment {
    private final SoundtrackPlayer soundtrackPlayer = new SoundtrackPlayer();
    private DatabaseManipulator databaseManipulator;


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
        databaseManipulator = new DatabaseManipulator(MainActivity.getContext());

        SoundtrackAdapter.OnSoundtrackClickListener onSoundtrackClickListener = new SoundtrackAdapter.OnSoundtrackClickListener() {

            @Override
            public void onSoundtrackClick(Soundtrack soundtrack, int position) {
                Toast.makeText(MainActivity.getContext(), "Был выбран пункт " + soundtrack.getTitle(),
                        Toast.LENGTH_SHORT).show();
                soundtrackPlayer.play(soundtrack);
            }
        };

        List<Soundtrack> soundtracks = databaseManipulator.getSoundtracksCustomStorage();
        SoundtrackAdapter soundtrackAdapter = new SoundtrackAdapter(getContext(), soundtracks, onSoundtrackClickListener);
        recyclerView.setAdapter(soundtrackAdapter);


        return view;
    }










}