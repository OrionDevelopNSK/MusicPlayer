package com.orion.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.adapters.SongDetailListAdapter;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.models.Song;
import com.orion.musicplayer.utils.Action;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.List;
import java.util.Map;


public class SongDetailListFragment extends Fragment {
    private static final String TAG = SongDetailListFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private DataModel dataModel;
    private final Playlist playlist;

    public SongDetailListFragment(Playlist playlist) {
        this.playlist = playlist;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sound_list_view2, container, false);
        recyclerView = view.findViewById(R.id.list_soundtrack_detail);
        dataModel = new ViewModelProvider(requireActivity()).get(DataModel.class);
        Map<Playlist, List<Song>> playlistListMap = dataModel.getPlaylistLiveData().getValue();
        List<Song> songs = playlistListMap.get(playlist);
        SongDetailListAdapter songDetailListAdapter = new SongDetailListAdapter(
                getContext(),
                songs,
                null
        );
        recyclerView.setAdapter(songDetailListAdapter);
//TODO не корректно работает
        createSoundtracksObserver(playlist, new SongDetailListAdapter.OnSoundtrackClickListener() {
            @Override
            public void onSoundtrackClick(Song song, int position) {
                dataModel.getCurrentPositionLiveData().setValue(position);
                if (!dataModel.getIsPlayingLiveData().getValue()) {
                    dataModel.getPlayerActionLiveData().setValue(Action.PLAY);
                    dataModel.getIsPlayingLiveData().setValue(true);
                } else {
                    dataModel.getPlayerActionLiveData().setValue(Action.PAUSE);
                    dataModel.getIsPlayingLiveData().setValue(false);
                }
            }
        });
        return view;
    }

    public void createSoundtracksObserver(Playlist playlist, SongDetailListAdapter.OnSoundtrackClickListener onSoundtrackClickListener){

        Map<Playlist, List<Song>> playlistListMap = dataModel.getPlaylistLiveData().getValue();
        List<Song> songs = playlistListMap.get(playlist);
        SongDetailListAdapter songDetailListAdapter = new SongDetailListAdapter(
                getContext(),
                songs,
                onSoundtrackClickListener
        );
        recyclerView.setAdapter(songDetailListAdapter);
    }


}