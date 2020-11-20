package com.example.mp3player_hfyst1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;

public class MainActivity extends AppCompatActivity {

    private MP3Player mp3;
    private MainState state = MainState.SHOW_PLAY;
    private ImageButton controlButton;
    private final String TAG = "MainActivity";
    private final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;

    private enum MainState {
        SHOW_PLAY,
        SHOW_PAUSE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
        controlButton = findViewById(R.id.buttonControl);

        mp3 = new MP3Player();
    }

    public void onControlClicked(View v){
        if(state==MainState.SHOW_PLAY){
            onPlayClicked();
        }else{
            onPauseClicked();
        }

    }

    private void onPlayClicked(){
        state = MainState.SHOW_PAUSE;
        updateUI();
        mp3.play();
    }

    private void onPauseClicked(){
        state = MainState.SHOW_PLAY;
        updateUI();
        mp3.pause();
    }

    private void updateUI(){
        if (state == MainState.SHOW_PLAY){
            controlButton.setImageResource(R.drawable.icon_play);
        }else if(state == MainState.SHOW_PAUSE){
            controlButton.setImageResource(R.drawable.icon_pause);
        }
    }

    public void onSearchClicked(View v){
        final ListView lv = findViewById(R.id.listSong);
        lv.setVisibility(View.VISIBLE);

        String path = "/sdcard/Music";
        File[] songFiles = new File(path).listFiles(new FileFilter() {
            //get only files with .mp3 extension
            @Override
            public boolean accept(File file) {
                return file.getPath().endsWith(".mp3");
            }
        });

        if(songFiles==null || songFiles.length < 1){
            TextView display = findViewById(R.id.name_music);
            display.setText(R.string.no_music_found);
            return ;
        }else{
            lv.setAdapter(new ArrayAdapter<>(this,R.layout.listview_layout,songFiles));
            Log.d(TAG,"Song List loaded");
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                File f = (File) lv.getItemAtPosition(myItemInt);
                selectSong(f.getAbsolutePath());
                lv.setVisibility(View.INVISIBLE);
            }
        });

    }

    public String getSongTitle(String filePath) {
        int indexOfLastSlash = filePath.lastIndexOf("/");
        int indexOfExtension = filePath.lastIndexOf(".");

        return filePath.substring(indexOfLastSlash+1,indexOfExtension);
    }

    private void selectSong(String uri){
        TextView tv = findViewById(R.id.name_music);
        tv.setText(getSongTitle(uri));
        mp3.load(uri);
        if(mp3.isSongPlaying()){
            state = MainState.SHOW_PAUSE;
            updateUI();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==REQUEST_PERMISSION_READ_EXTERNAL_STORAGE && grantResults.length>0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG,"Permission Granted");
        }else{
            Toast.makeText(this,"Permission is needed.",Toast.LENGTH_LONG).show();
            ((ActivityManager)(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
        }
    }
}