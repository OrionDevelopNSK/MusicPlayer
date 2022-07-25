package com.orion.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static boolean READ_AUDIO = false;
    private final AudioReader audioReader = new AudioReader();
    private final SoundtrackPlayer soundtrackPlayer = new SoundtrackPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int hasReadMediaPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadMediaPermission == PackageManager.PERMISSION_GRANTED) {
            READ_AUDIO = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (READ_AUDIO) readAudio();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        if (requestCode == 1 && grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            READ_AUDIO = true;
        }

        if (READ_AUDIO) {
            readAudio();
        } else {
            Toast.makeText(this, "Требуется установить разрешения", Toast.LENGTH_LONG).show();
        }
    }


    private void readAudio() {
        ListView soundtracksList = findViewById(R.id.listSongs);
        List<String> mediaData = audioReader.getMediaData(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mediaData);
        soundtracksList.setAdapter(adapter);

        soundtracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                soundtrackPlayer.playSoundtrack(mediaData, i);
            }
        });
    }


}