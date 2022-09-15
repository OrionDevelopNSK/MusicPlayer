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
import com.orion.musicplayer.adapters.PlaylistDetailListAdapter;
import com.orion.musicplayer.models.Playlist;
import com.orion.musicplayer.viewmodels.DataModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PlaylistDetailListFragment extends Fragment {
    public interface OnClickPlaylistListener {
        void onClickPlaylist(Playlist playlist);
    }


    private static final String TAG = PlaylistDetailListFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private DataModel dataModel;
    private OnClickPlaylistListener onClickPlaylistListener;

    public void setOnClickPlaylistListener(OnClickPlaylistListener onClickPlaylistListener) {
        this.onClickPlaylistListener = onClickPlaylistListener;
    }

    public static PlaylistDetailListFragment newInstance() {
        return new PlaylistDetailListFragment();
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
        dataModel = new ViewModelProvider(requireActivity()).get(DataModel.class);
        createPlaylistsObserver((playlist, position) -> {
            onClickPlaylistListener.onClickPlaylist(playlist);
            dataModel.getCurrentPlaylist().setValue(playlist);
            //TODO
        });
        return view;
    }

    @SuppressWarnings("rawtypes")
    private void createPlaylistsObserver(PlaylistDetailListAdapter.OnPlaylistClickListener onPlaylistClickListener) {
        Log.d(TAG, "Создание обсервера изменения списка плейлистов");
        dataModel.getPlaylistLiveData().observe(requireActivity(), playlistListMap -> {
            if (getContext() == null) return;
            List<Playlist> targetList = new ArrayList<>(playlistListMap.keySet());
            List<String> capacityList = new ArrayList<>();
            for (Collection c : playlistListMap.values()) {
                capacityList.add(String.valueOf(c.size()));
            }
            PlaylistDetailListAdapter playlistDetailListAdapter = new PlaylistDetailListAdapter(
                    getContext(),
                    targetList,
                    onPlaylistClickListener,
                    capacityList
            );
            recyclerView.setAdapter(playlistDetailListAdapter);
        });

    }


}