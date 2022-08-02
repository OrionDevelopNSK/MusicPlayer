package com.orion.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class SoundListViewFragment extends Fragment {
    private final SoundtrackPlayer soundtrackPlayer = new SoundtrackPlayer();
    private final AudioReader audioReader = new AudioReader(getContext());


    public static SoundListViewFragment newInstance() {
        return new SoundListViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_list_view, container, false);

        int requestCode = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

//        ListView soundtracksList = view.findViewById(R.id.listSongs);
//        List<String> mediaData = audioReader.getMediaData(getContext());
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mediaData);


        RecyclerView recyclerView = view.findViewById(R.id.listSongs);
        List<Soundtrack> soundtracks = audioReader.readMediaData();
        SoundtrackAdapter soundtrackAdapter = new SoundtrackAdapter(getContext(), soundtracks);
        recyclerView.setAdapter(soundtrackAdapter);


//        soundtracksList.setAdapter(adapter);
//        soundtracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                soundtrackPlayer.playSoundtrack(mediaData, i);
//            }
//        });

        if (requestCode != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Требуется установить разрешения", Toast.LENGTH_LONG).show();
        }
        return view;
    }


}