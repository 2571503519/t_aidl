package com.jacky.t_jlplayer_client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;

import com.jacky.t_jlplayer_service.IJLPlayerListener;
import com.jacky.t_jlplayer_service.IJLPlayerService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private IJLPlayerService jlservice;

    private boolean isBind = false;

    private IJLPlayerListener listener = new IJLPlayerListener.Stub() {
        @Override
        public void onPlaySuccess() throws RemoteException {
            Log.d(TAG, "onPlaySuccess");
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "JLPlayerService connected");
            jlservice = IJLPlayerService.Stub.asInterface(service);
            if (jlservice != null) {
                try {
                    jlservice.registerListener(listener);
                    service.linkToDeath(new JLServiceDeathRecipient(), 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "JLPayerService disconnected");
            isBind = false;
        }
    };

    private void bindService() {
        Intent intent = new Intent();
        intent.setPackage("com.jacky.t_jlplayer_service");
        intent.setAction("com.jacky.t_jlplayer_service.JLPlayerService");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindService();

        Button btn = findViewById(R.id.button);

        btn.setOnClickListener((view) -> {
            if (jlservice != null) {
                try {
                    jlservice.play(10);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "jlservice is null");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBind)
            unbindService(conn);
    }

    class JLServiceDeathRecipient implements IBinder.DeathRecipient {
        @Override
        public void binderDied() {
            Log.d(TAG, "jlservice binder died");
        }
    }
}
