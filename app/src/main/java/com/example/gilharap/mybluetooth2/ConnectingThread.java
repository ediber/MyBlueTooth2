package com.example.gilharap.mybluetooth2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

/**
 * Created by Gil Harap on 04/04/2017.
 */

public class ConnectingThread extends Thread {

    public interface SocketConnectedListener{
        void onConnectionSucess();
        void onConnectionError();
    }

    private static final String TAG = "MY_APP_DEBUG_TAG";

    private SocketConnectedListener listener;
    private BluetoothSocket socket;

    public ConnectingThread(BluetoothSocket socket, SocketConnectedListener listener) {
        this.socket = socket;
        this.listener = listener;
    }

    @Override
    public void run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        try {
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onConnectionError();
        }
        listener.onConnectionSucess();
    }
}
