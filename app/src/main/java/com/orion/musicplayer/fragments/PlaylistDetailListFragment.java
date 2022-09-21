package com.orion.musicplayer.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.adapters.PlaylistDetailListAdapter;
import com.orion.musicplayer.database.PlaylistDatabaseHelper;
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
    private PlaylistDatabaseHelper playlistDatabaseHelper;

    public void setOnClickPlaylistListener(OnClickPlaylistListener onClickPlaylistListener) {
        this.onClickPlaylistListener = onClickPlaylistListener;
    }

    public static PlaylistDetailListFragment newInstance() {
        return new PlaylistDetailListFragment();
    }

    public void setPlaylistDatabaseHelper(PlaylistDatabaseHelper playlistDatabaseHelper) {
        this.playlistDatabaseHelper = playlistDatabaseHelper;
    }


    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_list_view, container, false);
        recyclerView = view.findViewById(R.id.list_playlist);
        dataModel = new ViewModelProvider(requireActivity()).get(DataModel.class);
        createPlaylistsObserver(
                (playlist, position) -> {
                    onClickPlaylistListener.onClickPlaylist(playlist);
                    dataModel.getCurrentPlaylist().setValue(playlist);
                },
                (view1, playlist) -> {
                    dataModel.getCurrentPlaylist().setValue(playlist);
                    showPopup(view1, playlist);
                });
        return view;
    }

    @SuppressWarnings("rawtypes")
    private void createPlaylistsObserver(
            PlaylistDetailListAdapter.OnPlaylistClickListener onPlaylistClickListener,
            PlaylistDetailListAdapter.OnSettingsPlaylistClickListener onSettingsPlaylistClickListener
            ) {
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
                    capacityList,
                    onPlaylistClickListener,
                    onSettingsPlaylistClickListener
            );
            recyclerView.setAdapter(playlistDetailListAdapter);
        });

    }

    @SuppressLint("NonConstantResourceId")
    public void showPopup(View view, Playlist playlist) {
        @SuppressLint("RtlHardcoded") PopupMenu popupMenu = new PopupMenu(requireContext(), view, Gravity.RIGHT);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.delete_playlist:
                    playlistDatabaseHelper.deletePlaylist(playlist);
                    return true;
                case R.id.edit_playlist:
                    openChooserDialogFragment();
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.inflate(R.menu.menu_playlist_settings);
        popupMenu.show();
    }

    private void openChooserDialogFragment() {
        List<Fragment> fragments = requireActivity().getSupportFragmentManager().getFragments();
        ChooserDialogFragment chooserDialogFragment = null;
        for (Fragment fragment : fragments) {
            if (fragment instanceof ChooserDialogFragment) {
                chooserDialogFragment = (ChooserDialogFragment) fragment;
                chooserDialogFragment.setPlaylistDatabaseHelper(playlistDatabaseHelper);
            }
        }
        if (chooserDialogFragment == null) {
            chooserDialogFragment = new ChooserDialogFragment(playlistDatabaseHelper);
        }

        dataModel.getIsReadPlaylist().setValue(true);
        chooserDialogFragment.setPlaylistName(dataModel.getCurrentPlaylist().getValue().getPlaylistName());
        chooserDialogFragment.setStyle(ChooserDialogFragment.STYLE_NO_TITLE, R.style.Dialog);
        chooserDialogFragment.show(requireActivity().getSupportFragmentManager(), "Выберите песни");
    }

}