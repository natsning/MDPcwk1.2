package com.example.mp3player_hfyst1;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MP3Service extends Service {

    private final static String TAG = "MP3Service";
    private MP3Player mp3;
    private IBinder binder = new MP3Binder();

    /**
     * Binder class for MP3Service to bind with outer components.
     */
    public class MP3Binder extends Binder {

        MP3Binder(){}

        /**
         * Return service
         * @return service
         */
        public MP3Service getService(){
            return MP3Service.this;
        }

        public void play(){
            mp3.play();
        }

        public void pause(){
            mp3.pause();
        }

        public void load(String s){
            mp3.stop();
            mp3.load(s);
        }

        public boolean isSongPlaying(){
                return (mp3.getState() == MP3Player.MP3PlayerState.PLAYING);
        }

        public boolean isSongPaused(){
            return mp3.getState()== MP3Player.MP3PlayerState.PAUSED;
        }

        public int getCurrentDuration(){
            return mp3.getProgress();
        }

        public int getSongLength(){
            return  mp3.getDuration();
        }

    }

    @Override
    public void onCreate() {
        Log.d(TAG,"Service onCreate");
        super.onCreate();
        mp3 = new MP3Player();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"Service Binded");
        return binder;
    }

    public boolean onUnbind(Intent intent){
        Log.d(TAG,"Service Unbinded");
        super.onUnbind(intent);
        return true;
    }

    public void onRebind(Intent intent){
        Log.d(TAG,"Service Rebinded");
        super.onUnbind(intent);
    }


}
