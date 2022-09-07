package com.orion.musicplayer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.orion.musicplayer.R;
import com.orion.musicplayer.models.Soundtrack;

import java.util.List;

public class SoundtrackDialogAdapter extends RecyclerView.Adapter<SoundtrackDialogAdapter.ViewHolder> {
    public interface OnSoundtrackClickListener {
        void onSoundtrackClick(Soundtrack soundtrack, int position);
    }

    public interface OnSoundTrackChoseListener {
        void OnSoundTrackChose(int position, boolean isSelected);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        final Button musicButton;
        final CheckBox checkBox;

        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.soundtrack_title_list);
            musicButton = view.findViewById(R.id.play_music_dialog);
            checkBox = view.findViewById(R.id.checkbox_soundtrack);
        }
    }

    private static final String TAG = SoundtrackDialogAdapter.class.getSimpleName();

    private final LayoutInflater layoutInflater;
    private final List<Soundtrack> soundtrackList;
    private final OnSoundtrackClickListener onClickListener;
    private final Animation buttonAnimationClick;
    private OnSoundTrackChoseListener onSoundTrackChoseListener;
    private final boolean[] checked;

    public SoundtrackDialogAdapter(
            Context context,
            List<Soundtrack> soundtrackList,
            OnSoundtrackClickListener onClickListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.soundtrackList = soundtrackList;
        this.onClickListener = onClickListener;
        buttonAnimationClick = AnimationUtils.loadAnimation(context, R.anim.button_click);
        checked = new boolean[soundtrackList.size()];
    }

    public void setOnSoundTrackChoseListener(OnSoundTrackChoseListener onSoundTrackChoseListener) {
        this.onSoundTrackChoseListener = onSoundTrackChoseListener;
    }


    @NonNull
    @Override
    public SoundtrackDialogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.list_item_dialog, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SoundtrackDialogAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Soundtrack soundtrack = soundtrackList.get(position);
        holder.textView.setText(soundtrack.getTitle() + "\n"
                + soundtrack.getArtist());
        //TODO

        holder.musicButton.setOnClickListener(view -> {
            holder.musicButton.startAnimation(buttonAnimationClick);
            onClickListener.onSoundtrackClick(soundtrack, position);
        });

        holder.checkBox.setChecked(checked[position]);
        holder.checkBox.setOnClickListener(view -> {
            if (holder.checkBox.isChecked()) {
                onSoundTrackChoseListener.OnSoundTrackChose(position, true);
                Log.d(TAG, String.format("Позиция %d добавлена в исписок", position));

            } else {
                onSoundTrackChoseListener.OnSoundTrackChose(position, false);
                Log.d(TAG, String.format("Позиция %d удалена из исписка", position));
            }
            checked[position] = !checked[position];
            holder.checkBox.startAnimation(buttonAnimationClick);
        });

    }

    @Override
    public int getItemCount() {
        return soundtrackList.size();
    }
}
