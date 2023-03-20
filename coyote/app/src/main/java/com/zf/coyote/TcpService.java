package com.zf.coyote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TcpService extends Service {

    public static final String TAG = TcpClient.class.getSimpleName();
    public static ArrayList data_list = new ArrayList();

    public static Object sync_key = new Object();
    public TcpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public static class TcpData {
        int id;
        char type;
        int param1;
        int param2;
        short checksum;

    }


    @Override
    public int onStartCommand(Intent intent,int flags, int startId){

        TcpClient mTcpClient = new TcpClient(new TcpClient.OnMessageReceivedEx() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceivedEx(TcpClient client, char[] buf, int len) {
                //this method calls the onProgressUpdate
                Log.d(TAG, "receive len=" + len );

                TcpData data = new TcpData();
                byte [] bs = new byte[len];
                for (int i = 0; i < len; i++)
                    bs[i] = (byte) buf[i];
                for (int i = 0; i < len / 15; i++) {
                    data.id = Bytes2Int(Arrays.copyOfRange(bs, i*15, i*15+4));
                    data.type = (char)(bs[4]&0xFF);
                    data.param1 = Bytes2Int(Arrays.copyOfRange(bs, i*15+5, i*15+9));
                    data.param2 = Bytes2Int(Arrays.copyOfRange(bs, i*15+9, i*15+13));
                    data.checksum = Bytes2Short(Arrays.copyOfRange(bs, i*15+13, i*15+15));
                    Log.d(TAG,bs.length + " id="+data.id+" type="+(int)(data.type)+" param1="+
                            data.param1+" param2="+data.param2+ " checksum="+
                            String.format("0x%04X", data.checksum));

                    short chk = calc_chksum(bs, 13);
                    if (chk == data.checksum) {
                        String msg = "echo";
                        client.send(msg.toCharArray(), msg.length());

                        synchronized(sync_key) {
                            TcpService.data_list.add(data);
                            TcpService.sync_key.notify();
                        }

                    } else {
                        String msg = "bad";
                        client.send(msg.toCharArray(), msg.length());

                    }
                }



//                ByteBuffer bb = ByteBuffer.wrap(bs);
//                for (int i = 0; i < len / data.size(); i++) {
//                    data.id = Integer.reverseBytes(bb.getInt());
//                    data.type = Integer.reverseBytes(bb.getInt());
//                    data.param1 = Integer.reverseBytes(bb.getInt());
//                    data.param2 = Integer.reverseBytes(bb.getInt());
//                    Log.d(TAG,bs.length + " id="+data.id+" type="+data.type+" param1="+data.param1+" param2="+data.param2);
//                    Log.d(TAG,bs.length + " id="+data.id+" type="+data.type+" param1="+data.param1+" param2="+data.param2);
//                }


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


    public static int Bytes2Int(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 24) |
                ((bytes[2] & 0xFF) << 16) |
                ((bytes[1] & 0xFF) << 8) |
                (bytes[0] & 0xFF);
    }

    public static short Bytes2Short(byte[] bytes) {
        return (short)(((bytes[1] & 0xFF) << 8) |
                (bytes[0] & 0xFF) & 0xFFFF);
    }

    public short calc_chksum(byte[] data, int len) {

        int s = 0;
        int i = 0;

        while(i + 1 < len) {
            s += data[i];
            s += data[i+1] << 8;
            i += 2;
        }

        if (i + 1 == len) {
            s += data[i];
        }

        s = s & 0xffff + s >> 16;

        return (short)(~s & 0xffff);
    }
}