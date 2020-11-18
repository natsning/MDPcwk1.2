package com.example.mp3player_hfyst1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private MP3Player mp3;
    private MainState state = MainState.SHOW_PLAY;
    private ImageButton controlButton;

    private enum MainState {
        SHOW_PLAY,
        SHOW_PAUSE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }
}