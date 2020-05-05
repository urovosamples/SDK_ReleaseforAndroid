
package com.example.piccmanager;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.os.Vibrator;

public class SoundTool {
    private SoundPool soundPool = null;

    private int errorSoundId, scanSoundId;

    public final static int SOUND_TYPE_SUCCESS = 0;

    public final static int SOUND_TYPE_ERR = 1;

    private static SoundTool mySound = null;

    private Context context;

    public static SoundTool getMySound(Context context) {
        if (mySound == null) {
            mySound = new SoundTool(context);
        }
        return mySound;
    }

    public SoundTool(Context context) {
        this.context = context;
        if (soundPool == null) {
            soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100);
            errorSoundId = soundPool.load(context, R.raw.error, 1);
            scanSoundId = soundPool.load(context, R.raw.success, 1);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void playSound(int soundType) {

        float streamVolume = 0.8f;
        int soundResId = scanSoundId;
        switch (soundType) {
            case SOUND_TYPE_SUCCESS:
                soundPool.play(soundResId, streamVolume, streamVolume, 1, 0, 1f);
                break;
            case SOUND_TYPE_ERR:
                soundPool.play(errorSoundId, streamVolume, streamVolume, 1, 0, 1f);
                break;
            default:
                break;
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
        }
    }

    public void playMusic(String name) {
        // MediaPlayer mediaPlayer=new MediaPlayer();
        MediaPlayer mediaPlayer = MediaPlayer.create(context,
                context.getResources().getIdentifier(name, "raw", context.getPackageName()));
        try {
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }

    public void Vibrate(long milliSeconds) {
        try {
            Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            vib.vibrate(milliSeconds);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void scanSound() {
        if (soundPool == null) {
        }
        if (scanSoundId == 0) {
        }
        soundPool.play(scanSoundId, 1, 1, 0, 0, 1);
    }

    public void scansoundTrueOrFalse(boolean isTrue) {
        if (isTrue)
            SoundTool.getMySound(context).playSound(SOUND_TYPE_SUCCESS);
        else {
            SoundTool.getMySound(context).playSound(SOUND_TYPE_ERR);
            SoundTool.getMySound(context).Vibrate(500);
        }
    }

}
