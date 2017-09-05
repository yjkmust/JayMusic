package com.yjkmust.jaymusic.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

import com.yjkmust.jaymusic.Bean.MusicBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    private MusicControlBinder binder = new MusicControlBinder();
    private MediaPlayer mediaPlayer;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MusicControlBinder extends Binder {
        public MusicControlBinder() {
            mediaPlayer = new MediaPlayer();
        }

        public void prepareMusic(String path) {
            initMediaPlayer(path);
        }

        public void startMusic() {
            playMusic();

        }

        public void StopMusic() {
            mediaPlayer.pause();
        }

        public void NextMusic(String path) {
            initMediaPlayer(path);
            playMusic();

        }

        public void PreMusic(String path) {
            initMediaPlayer(path);
            playMusic();
        }

        public int getDuration() {
            return mediaPlayer.getDuration();
        }

        public int getCurrentDuaration() {
            return mediaPlayer.getCurrentPosition();
        }

        public void musicSeekTo(int progress) {
            mediaPlayer.seekTo(progress);
        }
    }

    private void initMediaPlayer(String path) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(path);
            Log.i("TAG", "initMediaPlayer: "+path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playMusic() {
        mediaPlayer.start();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                int currentPosition = mediaPlayer.getCurrentPosition();
                Intent intent = new Intent("YJKMUST.RECEIVER");
                intent.putExtra("time", currentPosition);
                sendBroadcast(intent);
            }
        };
        timer.schedule(timerTask, 0, 10);
    }


    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}
