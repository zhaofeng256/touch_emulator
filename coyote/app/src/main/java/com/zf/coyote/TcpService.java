package com.zf.coyote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.nio.ByteBuffer;

public class TcpService extends Service {

    public static final String TAG = TcpClient.class.getSimpleName();

    public TcpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public static class TcpData {
        int id;
        int type;
        int param1;
        int param2;
        public static int size() {return 16;}
    }


    @Override
    public int onStartCommand(Intent intent,int flags, int startId){


        //we create a TCPClient object
        TcpClient mTcpClient = new TcpClient(new TcpClient.OnMessageReceivedEx() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceivedEx(TcpClient client, char[] buf, int len) {
                //this method calls the onProgressUpdate
                Log.d(TAG, "receive len=" + len );

                TcpData data = new TcpData();
                byte [] bs = new String(buf).getBytes();
                ByteBuffer bb = ByteBuffer.wrap(bs);
                for (int i = 0; i < len / data.size(); i++) {
                    data.id = Integer.reverseBytes(bb.getInt());
                    data.type = Integer.reverseBytes(bb.getInt());
                    data.param1 = Integer.reverseBytes(bb.getInt());
                    data.param2 = Integer.reverseBytes(bb.getInt());
                    Log.d(TAG,"id="+data.id+" type="+data.type+" param1="+data.param1+" param2="+data.param2);
                }

                String msg = "echo";
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