package com.orion.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.adapters.PlaylistRecycleViewAdapter;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.viewmodels.SoundtrackPlayerModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PlaylistListFragment extends Fragment {
    private static final String TAG = PlaylistListFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private SoundtrackPlayerModel soundtrackPlayerModel;


    public static PlaylistListFragment newInstance() {
        return new PlaylistListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_list_view, container, false);
        recyclerView = view.findViewById(R.id.list_playlist);
        soundtrackPlayerModel = new ViewModelProvider(requireActivity()).get(SoundtrackPlayerModel.class);
        createPlaylistsObserver((soundtrack, position) -> {
            //TODO
        });
        return view;
    }



    @SuppressWarnings("rawtypes")
    private void createPlaylistsObserver(PlaylistRecycleViewAdapter.OnPlaylistClickListener onPlaylistClickListener) {
        Log.d(TAG, "Создание обсервера изменения списка плейлистов");
        soundtrackPlayerModel.getPlaylistLiveData().observe(requireActivity(), playlistListMap -> {
            List<Playlist> targetList = new ArrayList<>(playlistListMap.keySet());
            List<String> capacityList = new ArrayList<>();
            for (Collection c :playlistListMap.values()){
                capacityList.add(String.valueOf(c.size()));
            }
            PlaylistRecycleViewAdapter playlistRecycleViewAdapter = new PlaylistRecycleViewAdapter(
                    getContext(),
                    targetList,
                    onPlaylistClickListener,
                    capacityList
            );
            recyclerView.setAdapter(playlistRecycleViewAdapter);
        });

    }


}