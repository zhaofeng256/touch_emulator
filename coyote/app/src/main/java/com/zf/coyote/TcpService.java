package com.zf.coyote;

import static com.zf.coyote.definition.V_CHECKSUM;
import static com.zf.coyote.definition.V_ID;
import static com.zf.coyote.definition.V_PARAM1;
import static com.zf.coyote.definition.V_PARAM2;
import static com.zf.coyote.definition.V_TYPE;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

public class TcpService extends Service {

    public static final String TAG = TcpService.class.getSimpleName();
    public static ArrayList<byte[]> data_list_tcp = new ArrayList<>();

    public static final Object sync_key_tcp = new Object();

    static class TcpData extends cStruct {
        public TcpData() {
            super(new field[]{
                    new field(V_ID, 4),
                    new field(V_TYPE, 1),
                    new field(V_PARAM1, 4),
                    new field(V_PARAM2, 4),
                    new field(V_CHECKSUM, 2)});
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
        for (int i = 0; i < len / data.size; i++) {

/*            for (String name : data.names.keySet()) {
                byte[] tmp = Arrays.copyOfRange(buf, i * data.size + data.offset(name),
                        i * data.size + data.offset(name) + data.size(name));
                data.set(name, tmp);
            }

            Log.d(TAG, "id=" + data.get(V_ID) + " type=" + data.get(V_TYPE) +
                    " param1=" + data.get(V_PARAM1) + " param2=" + data.get(V_PARAM2));*/

            synchronized (sync_key_tcp) {
                byte[] m = Arrays.copyOfRange(buf, i * data.size, (i + 1) * data.size);
                TcpService.data_list_tcp.add(m);
                TcpService.sync_key_tcp.notify();
            }

/*            int chk = calc_Checksum(Arrays.copyOfRange(buf, i * data.size,
                    i * data.size + data.offset(V_CHECKSUM)), data.offset(V_CHECKSUM));

            if (chk == data.get(V_CHECKSUM)) {
                byte[] echo = {0};
                client.send(echo, 1);

                synchronized (sync_key) {
                    TcpService.data_list.add(data);
                    TcpService.sync_key.notify();
                }

            } else {
                byte[] echo = {1};
                client.send(echo, 1);
            }*/
        }
    };

    public static int calc_Checksum(byte[] data, int len) {

        int s = 0;
        int i = 0;

        while (i + 1 < len) {
            s += (int) data[i] & 0xff;
            s += ((int) data[i + 1] & 0xff) << 8;
            i += 2;
        }

        if (i + 1 == len) {
            s += (int) data[i] & 0xff;
        }

        s = s & 0xffff + (s >> 16);
        s = (~s) & 0xffff;

//        byte [] sum = new byte[2];
//        sum[0] = (byte)(s & 0xff);
//        sum[1] = (byte)((s >> 8) & 0xff);
//        Log.d(TAG, "checksum is " + charToHex(sum, 2));

        return s;
    }

    static public String charToHex(byte[] buf, int len) {

        char[] hexDigit = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(hexDigit[(buf[i] >> 4) & 0x0f]);
            sb.append(hexDigit[buf[i] & 0x0f]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public static void int_to_buf(int v, byte[] buf, int offset) {
        byte[] bs = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(v).array();
        System.arraycopy(bs, 0, buf, offset, 4);
    }

    public static void short_to_buf(short v, byte[] buf, int offset) {
        byte[] bs = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(v).array();
        System.arraycopy(bs, 0, buf, offset, 2);
    }

    public static void sendTcpData(int v_id, int v_type, int v_param1, int v_param2) {
        TcpData data = new TcpData();
        byte[] buf = new byte[data.size];
        int_to_buf(v_id, buf, data.offset(V_ID));
        buf[data.offset(V_TYPE)] = (byte) (v_type & 0xFF);
        int_to_buf(v_param1, buf, data.offset(V_PARAM1));
        int_to_buf(v_param2, buf, data.offset(V_PARAM2));
        int checksum = calc_Checksum(buf, data.offset(V_CHECKSUM));
        short_to_buf((short)checksum, buf,  data.offset(V_CHECKSUM));
        TcpClient.send(buf, data.size);
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
