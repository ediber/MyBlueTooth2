package com.example.gilharap.mybluetooth2;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Gil Harap on 03/04/2017.
 */


public class AlreadyConnectedThread extends Thread {


    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_TOAST = 2;

    public interface MessageReceiver {
        void onReceive(byte[] buffer);
    }

    // ... (Add other message types here as needed.)
    private static final String TAG = "MY_APP_DEBUG_TAG";
//    private Handler mHandler; // handler that gets info from Bluetooth service


    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private final MessageReceiver mMessageReceiver;

    public AlreadyConnectedThread(BluetoothSocket socket, MessageReceiver messageReceiver) {
        mmSocket = socket;
        mMessageReceiver = messageReceiver;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        mmBuffer = new byte[9];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                mMessageReceiver.onReceive(mmBuffer);

                Log.d(TAG, "mmBuffer: " + mmBuffer.toString());
                Log.d(TAG, "numBytes: " + numBytes);

            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

/*    // Call this from the main activity to send data to the remote device.
    public void writeToSocket(byte[] bytes) {
        try {
            mmOutStream.writeToSocket(bytes);

        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

        }
    }*/

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}

