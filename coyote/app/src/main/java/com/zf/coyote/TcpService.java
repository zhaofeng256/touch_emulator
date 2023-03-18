package com.zf.coyote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class TcpService extends Service {

    public static final String TAG = TcpClient.class.getSimpleName();

    public TcpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent,int flags, int startId){


        //we create a TCPClient object
        TcpClient mTcpClient = new TcpClient(new TcpClient.OnMessageReceivedEx() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceivedEx(TcpClient client, char[] buf, int len) {
                //this method calls the onProgressUpdate
                Log.d(TAG, "receive:" );
                String msg = "echo from client";
                client.send(msg.toCharArray(), msg.length());
            }
        });

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                 mTcpClient.run();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        return START_STICKY;
    }

}