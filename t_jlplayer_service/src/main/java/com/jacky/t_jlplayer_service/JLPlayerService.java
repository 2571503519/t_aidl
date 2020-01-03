package com.jacky.t_jlplayer_service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JLPlayerService extends Service {

    private static final String TAG = JLPlayerService.class.getSimpleName();

    private HandlerThread thread;

    private Handler handler;

    private MediaPlayer mediaPlayer;

    private List<IJLPlayerListener> listenerList = new ArrayList<>();

    private static final int ACTION_PLAY = 1;

    public JLPlayerService() {
        Log.d(TAG, "JLPlayerService");
        thread = new HandlerThread("playback");
        thread.start();

        handler = new Handler(thread.getLooper(), msg -> {
            Log.d(TAG, "handle msg: " + msg.what);
            switch (msg.what) {
                case ACTION_PLAY:
                    doPlay();
                    break;
                default: break;
            }
            return true;
        });

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getPath() + "/music1.mp3");
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doPlay() {
        mediaPlayer.seekTo(0);
        mediaPlayer.start();
        for (IJLPlayerListener l : listenerList) {
            try {
                l.onPlaySuccess();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private IJLPlayerService.Stub stub = new IJLPlayerService.Stub() {

        @Override
        public void play(int fileId) {
            Message msg = Message.obtain();
            msg.what = ACTION_PLAY;
            handler.sendMessage(msg);
        }

        @Override
        public void registerListener(IJLPlayerListener listener) throws RemoteException {
            Log.d(TAG, "register listener");
            IBinder listenerBinder = listener.asBinder();
            listenerBinder.linkToDeath(new JLClientDeathRecipient(), 0);
            listenerList.add(listener);
        }

        @Override
        public void unregisterListener(IJLPlayerListener listener) throws RemoteException {
            Log.d(TAG, "unregister listener");
            listenerList.remove(listener);
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return stub;
    }

    class JLClientDeathRecipient implements IBinder.DeathRecipient {

        @Override
        public void binderDied() {
            Log.d(TAG, "jlclient died");
        }
    }

}
