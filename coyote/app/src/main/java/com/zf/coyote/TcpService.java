package com.zf.coyote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;


public class TcpService extends Service {

    public static final String TAG = TcpClient.class.getSimpleName();
    public static ArrayList<TcpData> data_list = new ArrayList<>();

    public static final Object sync_key = new Object();


    static class TcpData extends cStruct {
        public TcpData() {
            super(new field[]{
                new field("id", 4),
                new field("type", 1),
                new field("param1", 4),
                new field("param2", 4),
                new field("checksum", 2)});
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        TcpClient mTcpClient = new TcpClient(received);

        new Thread(() -> {
            while (true) {
                mTcpClient.run();
            }
        }).start();

        return START_STICKY;
    }

    TcpClient.OnReceived received = (client, buf, len) -> {
        if (len < 15) {
            Log.e(TAG, "tcp receive len=" + len);
            return;
        }

        TcpData data = new TcpData();
        byte[] bs = new byte[len];
        for (int i = 0; i < len; i++)
            bs[i] = (byte) buf[i];
        for (int i = 0; i < len / data.size; i++) {

            for (String name : data.names.keySet()) {
                byte[] tmp = Arrays.copyOfRange(bs, i * data.size + data.offset(name),
                        i * data.size + data.size(name));
                data.set(name, tmp);
            }
            Log.d(TAG, " id=" + data.get("id") + " type=" + data.get("type") +
                    " param1=" + data.get("param1") + " param2=" + data.get("param2"));
//            for (int j = 0; j < data.fields.size();j++){
//                Arrays.copyOfRange(bs, i * data.size + data.fields.get(j).offset, i * data.size + data.fields.get(j).size);}
//            byte  type = Arrays.copyOfRange(bs, i * data.size, i * data.size + data.type.length);
//            data.param1 = bytes2Int(Arrays.copyOfRange(bs, i * 15 + 5, i * 15 + 9));
//            data.param2 = bytes2Int(Arrays.copyOfRange(bs, i * 15 + 9, i * 15 + 13));
//            data.checksum = bytes2Short(Arrays.copyOfRange(bs, i * 15 + 13, i * 15 + 15));
//                    Log.d(TAG,bs.length + " id="+data.id+" type="+(int)(data.type)+" param1="+
//                            data.param1+" param2="+data.param2+ " checksum="+
//                            String.format("0x%04X", data.checksum));

            short chk = calc_Checksum(bs, data.offset("checksum"));

            if (chk == data.get("checksum")) {
                byte[] echo = {0};
                client.send(echo, 1);

                synchronized (sync_key) {
                    TcpService.data_list.add(data);
                    TcpService.sync_key.notify();
                }

            } else {
                byte[] echo = {1};
                client.send(echo, 1);
            }
        }
    };

    public short calc_Checksum(byte[] data, int len) {

        int sum = 0;
        int i = 0;

        while (i + 1 < len) {
            sum += data[i];
            sum += data[i + 1] << 8;
            i += 2;
        }

        if (i + 1 == len) {
            sum += data[i];
        }

        sum = sum & 0xffff + sum >> 16;

        return (short) (~sum & 0xffff);
    }

    static public String charToHex(byte[] buf, int len) {

        char[] hexDigit = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((hexDigit[(buf[i] >> 4) & 0x0f]));
            sb.append((char)(hexDigit[buf[i] & 0x0f]));
            sb.append(' ');
        }
        return sb.toString();
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
