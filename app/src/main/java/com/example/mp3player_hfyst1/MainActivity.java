package com.example.mp3player_hfyst1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private MainState state = MainState.SHOW_PLAY;
    private MP3Service.MP3Binder binder = null;
    private ProgressBar progressBar;
    private TextView durationTimer;
    private boolean isBound = false;
    private String songTitle = "No Song Playing..";
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

        // permission checker
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }


        durationTimer = findViewById(R.id.durationTimer);
        progressBar = findViewById(R.id.progressBar);

        // restore previous state
        if(savedInstanceState!=null){
            TextView tv = findViewById(R.id.name_music);
            tv.setText(savedInstanceState.getString("songTitle"));
            songTitle = savedInstanceState.getString("songTitle");
            if(Objects.equals(savedInstanceState.getString("state"), MainState.SHOW_PAUSE.toString())){
                state = MainState.SHOW_PAUSE;
                updateUI();
            }
        }

        // binds the service to this activity and runs it indefinitely
        Intent intent = new Intent(this, MP3Service.class);
        startService(intent);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);

    }

    /**
     * Creates a service connection to MP3Service and access it through the its binder
     * @see MP3Service
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.d(TAG,"Service connected to main");
            binder =(MP3Service.MP3Binder)iBinder;
            isBound = true;

            // restore previously saved instance
            if(binder.isSongPlaying()||binder.isSongPaused()){
                progressBar.setMax(binder.getSongLength());
                progressBar.post(progressBarRunner);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
            isBound = false;
        }
    };

    /**
     * onClick method for the play/pause button
     * @param v play/pause button
     */
    public void onControlClicked(View v){
        if(state==MainState.SHOW_PLAY){
            onPlayClicked();
        }else{
            onPauseClicked();
        }
    }

    /**
     * if play button is clicked
     */
    private void onPlayClicked(){
        state = MainState.SHOW_PAUSE;
        updateUI();
        binder.play();
        progressBar.post(progressBarRunner);
    }

    /**
     * if pause button is clicked
     */
    private void onPauseClicked(){
        state = MainState.SHOW_PLAY;
        updateUI();
        binder.pause();
        progressBar.removeCallbacks(progressBarRunner);

    }

    /**
     * Updates the image shown for play/pause button
     */
    private void updateUI( ){
        ImageButton controlButton = findViewById(R.id.buttonControl);

        if (state == MainState.SHOW_PLAY){
            controlButton.setImageResource(R.drawable.icon_play);
        }else if(state == MainState.SHOW_PAUSE){
            controlButton.setImageResource(R.drawable.icon_pause);
        }
    }

    /**
     * onClick method for search button
     * Searches and lists out the device's sdcard folder for .mp3 files in a listView
     * Handles onClick files to load the song
     * @param v search button
     */
    public void onSearchClicked(View v){
        final ListView lv = findViewById(R.id.listSong);
        lv.setVisibility(View.VISIBLE);

        //Look for files
        String path = Environment.getExternalStorageDirectory().getPath() + "/Music/";
        File[] songFiles = new File(path).listFiles(new FileFilter() {
            //get only files with .mp3 extension
            @Override
            public boolean accept(File file) {
                return file.getPath().endsWith(".mp3");
            }
        });

        // Display files if there are files
        if(songFiles==null || songFiles.length < 1){
            TextView tv = findViewById(R.id.name_music);
            tv.setText(R.string.no_music_found);
            return ;
        }else{
            lv.setAdapter(new ArrayAdapter<>(this,R.layout.listview_layout,songFiles));
            Log.d(TAG,"Song List loaded");
        }

        // if file clicked
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                File f = (File) lv.getItemAtPosition(myItemInt);
                if(isBound){
                    selectSong(f.getAbsolutePath());
                    lv.setVisibility(View.INVISIBLE);
                    progressBar.setMax(binder.getSongLength());
                }
            }
        });

    }

    /**
     * A thread to handle the updating for progressBar and timer.
     */
    private Runnable progressBarRunner = new Runnable() {
        @Override
        public void run() {
            if(binder.isSongPlaying()||binder.isSongPaused()){
                progressBar.setProgress(binder.getCurrentDuration());
                durationTimer.setText(convertMilliSectoMinSec(binder.getCurrentDuration()));
                if(binder.isSongPlaying()) {
                    progressBar.post(progressBarRunner);
                }
            }
        }
    };

    /**
     * Clean the string to leave only the title of the song
     * @param filePath song file path
     * @return cleaned string
     */
    public String getSongTitle(String filePath) {
        int indexOfLastSlash = filePath.lastIndexOf("/");
        int indexOfExtension = filePath.lastIndexOf(".");

        return filePath.substring(indexOfLastSlash+1,indexOfExtension);
    }

    /**
     * Handles the UI, communication to service after a song is selected
     * @param uri song file path
     */
    private void selectSong(String uri){

        TextView tv = findViewById(R.id.name_music);
        songTitle = getSongTitle(uri);
        tv.setText(songTitle);

        binder.load(uri);
        if(binder.isSongPlaying()){
            state = MainState.SHOW_PAUSE;
            updateUI();
        }

        progressBar.post(progressBarRunner);

        //scrolls back to top after song is selected
        NestedScrollView n = findViewById(R.id.nestedScrollView);
        n.smoothScrollTo(0,0);
    }

    /**
     * Convert time into a readable format
     * @param milliseconds duration returned from MP3Player.getProgress
     * @return String in MM:SS to be displayed
     */
    private String convertMilliSectoMinSec(int milliseconds) {

        int minute = (milliseconds / 1000) / 60;
        int second = (milliseconds / 1000) % 60;

        return minute + ":" + String.format("%02d", second);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("songTitle",songTitle);
        savedInstanceState.putString("state",state.toString());
        super.onSaveInstanceState(savedInstanceState);

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

    @Override
    public void onDestroy() {
        unbindService(connection);
        isBound = false;
        super.onDestroy();
    }
}