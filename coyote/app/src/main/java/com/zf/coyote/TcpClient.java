package com.zf.coyote;

import static com.zf.coyote.TcpService.charToHex;
import static com.zf.coyote.TcpService.sendTcpData;
import static com.zf.coyote.TouchService.display_size;
import static com.zf.coyote.definition.EventType.TYPE_SETTING;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class TcpClient {

    public static final String TAG = TcpClient.class.getSimpleName();
    public static final String SERVER_IP = "192.168.40.1"; //server IP address
    public static final int SERVER_PORT = 65432;
    private final OnReceived received;
    private boolean running = false;
    InputStream input_stream;
    static OutputStream output_stream;
    static byte[] read_buf = new byte[1024];
    int read_len = 0;

    public TcpClient(OnReceived listener) {
        received = listener;
    }

    public void run() {

        running = true;

        try {
            Log.d(TAG, "connecting to server...");

            Socket socket = new Socket();
            socket.setReceiveBufferSize(1024);
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
            socket.connect(new InetSocketAddress(serverAddress, SERVER_PORT), 3000);
            input_stream = socket.getInputStream();
            output_stream = socket.getOutputStream();

            sendTcpData(TYPE_SETTING, display_size[0], display_size[1]);

            while (running) {
                read_len = input_stream.read(read_buf);
                //Log.d(TAG, "rec len=" + read_len + " " + charToHex(read_buf, read_len));
                if (read_len > 0 && received != null) {
                    received.dataHandler(this, read_buf, read_len);
                }
                if (-1 == read_len) {
                    Log.d(TAG, "server lost connect");
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "socket connect timeout");
        } catch (SocketException e) {
            Log.e(TAG, "SocketException" + e);
            //throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            Log.e(TAG, "UnknownHostException" + e);
        } catch (IOException e) {
            Log.d(TAG, "socket IOException");
            //throw new RuntimeException(e);
        } finally {
            stop();
        }
    }

    public static void send(byte[] buf, int len) {
        if (output_stream != null) {
            //Log.d(TAG, "Sending...");
            try {
                output_stream.write(buf, 0, len);
                output_stream.flush();
            } catch (IOException e) {
                Log.e(TAG, "send exception");
            }
        }
    }

    public void stop() {

        Log.d(TAG, "stop Client");
        running = false;

        if (output_stream != null) {

            try {
                output_stream.flush();
                output_stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        input_stream = null;
        output_stream = null;
    }

    public interface OnReceived {
        void dataHandler(TcpClient client, byte[] buf, int len);
    }
}


//                SocketOption opt;
//                opt.opt
//                socket.setOption(SocketOption);
//                socket.setKeepAlive();
//                socket.setSoLinger();
//                socket.setTcpNoDelay(true);
//                socket.setSoTimeout();
//                socket.setSendBufferSize(1);
//    private PrintWriter mBufferOut;

//    private BufferedReader mBufferIn;

//mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
//mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

//                        while (!socket.isConnected()) {
//                            try {
//                                socket.connect(new InetSocketAddress(serverAddress, SERVER_PORT));
//                            } catch (Exception ex) {
//                                Log.e(TAG, "reconnect failed", ex);
//                            }
//
//                        }