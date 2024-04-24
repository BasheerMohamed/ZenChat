package com.basheer.whatsappclone.manage;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

public class AudioService {
    Context context;
    MediaPlayer tmpMediaPlayer;
    onPlayCallBack onPlayCallBack;

    public AudioService(Context context) {
        this.context = context;
    }

    public void playAudioFromUrl(String url, onPlayCallBack onPlayCallBack) {

//        if (tmpMediaPlayer != null) {
//            tmpMediaPlayer.stop();
//        }

        if (tmpMediaPlayer != null) {
            try {
                if (tmpMediaPlayer.isPlaying()) {
                    tmpMediaPlayer.stop();
                }
                tmpMediaPlayer.release();
            } catch (Exception ignored) {
            } finally {
                tmpMediaPlayer = null;
            }
        }

        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();

            tmpMediaPlayer = mediaPlayer;

        } catch (IOException ignored) {
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                onPlayCallBack.onFinished();
            }
        });
    }

    public interface onPlayCallBack {
        void onFinished();
    }
}
