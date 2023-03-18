package com.zf.coyote;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TcpClient {

    public static final String TAG = TcpClient.class.getSimpleName();
    public static final String SERVER_IP = "192.168.40.1"; //server IP address
    public static final int SERVER_PORT = 65432;

    // sends message received notifications
    private OnMessageReceived mMessageListener;
    private OnMessageReceivedEx mMessageListenerEx;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    char [] read_buf = new char[1024];
    int read_len = 0;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }
    public TcpClient(OnMessageReceivedEx listener) {
        mMessageListenerEx = listener;
    }
    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }


    public void send( char [] buf, int len) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.d(TAG, "Sending...");
                    mBufferOut.write(buf, 0, len);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        Log.d(TAG,"stop Client");
        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;

    }

    public void run() {

        mRun = true;

        try {
            Log.d(TAG, "C: Connecting...");
            //here you must put your computer's IP address.
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);


            //create a socket to make the connection with the server

            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(serverAddress, SERVER_PORT), 3000);
                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    try {
                        read_len = mBufferIn.read(read_buf);

                        if (read_len != 0 && mMessageListenerEx != null) {
                            //call the method messageReceived from MyActivity class
                            mMessageListenerEx.messageReceivedEx(this, read_buf, read_len);
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "lost connect");
                        stopClient();
                    }
                }

            } catch (SocketTimeoutException e) {
                Log.e(TAG, "socket connect timeout");
            } catch (Exception e) {
                Log.e(TAG, "socket error", e);
            }
            //the socket must be closed. It is not possible to reconnect to this socket
            // after it is closed, which means a new socket instance has to be created.

        } catch (Exception e) {
            Log.e(TAG, "C: Error", e);
        } finally {
            stopClient();
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(TcpClient client, String message);

    }
    public interface OnMessageReceivedEx {
        void messageReceivedEx(TcpClient client, char [] buf, int len);
    }
}

//                        while (!socket.isConnected()) {
//                            try {
//                                socket.connect(new InetSocketAddress(serverAddress, SERVER_PORT));
//                            } catch (Exception ex) {
//                                Log.e(TAG, "reconnect failed", ex);
//                            }
//
//                        }