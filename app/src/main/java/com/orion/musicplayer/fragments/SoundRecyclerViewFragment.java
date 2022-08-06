package com.orion.musicplayer.fragments;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.orion.musicplayer.AudioReader;
import com.orion.musicplayer.MainActivity;
import com.orion.musicplayer.R;
import com.orion.musicplayer.SoundtrackPlayer;
import com.orion.musicplayer.adapters.SoundtrackAdapter;
import com.orion.musicplayer.dao.SoundtrackDao;
import com.orion.musicplayer.database.AppDatabase;
import com.orion.musicplayer.database.RoomSoundtrackRepository;
import com.orion.musicplayer.models.Soundtrack;

import java.util.List;
import java.util.Objects;


public class SoundRecyclerViewFragment extends Fragment {
    private final SoundtrackPlayer soundtrackPlayer = new SoundtrackPlayer();
    //private DatabaseManipulator databaseManipulator;


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
        SoundtrackAdapter.OnSoundtrackClickListener onSoundtrackClickListener = new SoundtrackAdapter.OnSoundtrackClickListener() {

            @Override
            public void onSoundtrackClick(Soundtrack soundtrack, int position) {
                Toast.makeText(requireActivity(), "Был выбран пункт " + soundtrack.getTitle(),
                        Toast.LENGTH_SHORT).show();
                soundtrackPlayer.play(soundtrack);
            }
        };

        AudioReader audioReader = new AudioReader(requireActivity());
        List<Soundtrack> soundtracks = audioReader.readMediaData();

        AppDatabase database = Room.databaseBuilder(requireActivity(),
                        AppDatabase.class,
                        "database").
                build();

        AsyncTask.execute(() -> {
            SoundtrackDao soundtrackDao = database.soundtrackDao();
            RoomSoundtrackRepository roomSoundtrackRepository = new RoomSoundtrackRepository(soundtrackDao);
            roomSoundtrackRepository.insertAllSoundtracks(soundtracks);
            List<Soundtrack> all = roomSoundtrackRepository.getAll();

            requireActivity().runOnUiThread(() ->{
                SoundtrackAdapter soundtrackAdapter = new SoundtrackAdapter(getContext(), all, onSoundtrackClickListener);
                recyclerView.setAdapter(soundtrackAdapter);
            });
        });



        return view;
    }


}