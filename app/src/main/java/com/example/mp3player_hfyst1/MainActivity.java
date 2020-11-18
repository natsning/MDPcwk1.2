package com.example.mp3player_hfyst1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.AdapterView;
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
        controlButton.setImageResource(R.drawable.icon_pause);
//        mp3.play();
    }

    private void onPauseClicked(){
        state = MainState.SHOW_PLAY;
        controlButton.setImageResource(R.drawable.icon_play);
//        mp3.pause();
    }

    public void onSearchClicked(View v){
        final ListView lv = findViewById(R.id.listView);
        lv.setVisibility(View.VISIBLE);

        String path = "/sdcard/Music/";
        String[] files = new File(path).list();
        if(files.length>0)
            Log.d(TAG,files[0]);
//        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                null,
//                MediaStore.Audio.Media.IS_MUSIC + "!= 0",
//                null,
//                null);
//
//        lv.setAdapter(new SimpleCursorAdapter(this,
//                android.R.layout.simple_list_item_1,
//                cursor,
//                new String[] { MediaStore.Audio.Media.DATA},
//                new int[] { android.R.id.text1 }));
//
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> myAdapter,
//                                    View myView,
//                                    int myItemInt,
//                                    long mylng) {
//                Cursor c = (Cursor) lv.getItemAtPosition(myItemInt);
//                String uri = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
//                selectSong(uri);
//                lv.setVisibility(View.INVISIBLE);
//
//            }
//        });



    }

    public String getSongTitle(String filePath)
    {
        int indexOfLastSlash = filePath.lastIndexOf("/");
        int indexOfExtension = filePath.lastIndexOf(".");

        return filePath.substring(indexOfLastSlash+1,indexOfExtension);
    }

    private void selectSong(String uri){
        Log.d(TAG,String.format("%s----------------------------",uri));
        TextView tv = findViewById(R.id.name_music);
        tv.setText(getSongTitle(uri));
        mp3.load(uri);

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