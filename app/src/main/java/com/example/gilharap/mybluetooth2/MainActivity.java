package com.example.gilharap.mybluetooth2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final String EXTRA_DEVICE_ADDRESS = "device_address";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private BluetoothAdapter mBtAdapter;

    @BindView(R.id.showPairedDevices)
    View showPairedDevices;
    @BindView(R.id.sendMessageToPairedDevice)
    View sendMessageToPairedDevice;
    @BindView(R.id.closeSocket)
    View closeSocket;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.answer)
    TextView answer;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.indicatorsLayout1)
    LinearLayout indicatorsLayout1;
    @BindView(R.id.indicatorsLayout2)
    LinearLayout indicatorsLayout2;


    //    private Set<BluetoothDevice> pairedDevices;
    private AlreadyConnectedThread alreadyConnectedThread;
    private ArrayList<BluetoothDevice> pairedDevicesList;
    private PairedAdapter adapter;

    private BluetoothSocket socket;
    private OutputStream outStream;
    private BluetoothDevice selectedDevice;
    private ArrayList<String> binaries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // check ig BT enabled on device
        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }


        showPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
// Get a set of currently paired devices
                Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
                pairedDevicesList = new ArrayList<>(pairedDevices);
            }
        });

        sendMessageToPairedDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);

                byte[] send = new byte[]{(byte) 0xDA,
                        (byte) 0xDE,
                        (byte) 0x00,
                        (byte) 0x00,
                        (byte) 0x00,
                        (byte) 0x00,
                        (byte) 0x00,
                };

                switch (idx) {
                    case 0:
                        send[3] =  (byte) 0x01;
                        break;
                    case 1:
                        send[3] =  (byte) 0x21;
                        break;
                    case 2:
                        send[3] =  (byte) 0x22;
                        break;
                }

                sendMessage(send);

//                BluetoothDevice.createRfcommSocketToServiceRecord().connect();
            }
        });

        closeSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the connect socket", e);
                }
            }
        });


        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        pairedDevicesList = new ArrayList<>(pairedDevices);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(linearLayoutManager);
        adapter = new PairedAdapter(pairedDevicesList, new SelectListen());
        recycler.setAdapter(adapter);

        binaries = generateTableOfAllBinaries();

    }

    private ArrayList<String> generateTableOfAllBinaries() {
        ArrayList<String> binaries = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            String binary = intToBinary(i);
            binaries.add(binary);
        }
        return binaries;
    }

    private String intToBinary(int num) {
        String ans =  Integer.toBinaryString(num);
        for(int i = ans.length(); i < 8; i++){
            ans = "0" + ans ;
        }
        return ans;
    }

    private void sendMessage(byte[] send) {

       /* // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

        }*/

//        byte[] send = message.getBytes();

      /*  // config lead off detection
        byte[] send = new byte[]{(byte) 0xDA,
                (byte) 0xDE,
                (byte) 0x00,
                (byte) 0x21,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
        };*/

//        Get FW Version

        write(send);

        alreadyConnectedThread = new AlreadyConnectedThread(socket, new Receiver());
        alreadyConnectedThread.start();

/*      // TODO remove
        byte[] testBytes = new byte[]{(byte)0xDA,
                (byte)0xDE,
                (byte)0xB1,
                (byte)0x21,
                (byte)0x00,
                (byte)0x00,
                (byte)0x00,
        };
        updateUI(testBytes);*/
    }

    private class Receiver implements AlreadyConnectedThread.MessageReceiver {
        @Override
        public void onReceive(byte[] buffer) {
            Log.d(" BT ", "getbytes: " +  buffer.toString());
            updateUI(buffer);
        }
    }

    private void updateUI(byte[] buffer) {

        String payload = "";
        final String hex = bytesToHex(buffer);

        // payload to binary
        for(int i=5; i<buffer.length-2; i++){
            int binaryindex = buffer[i] & 0xFF;
            String binaryStr = binaries.get(binaryindex);
            payload = payload + binaryStr;
        }




        final String finalPayload = payload;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                answer.setText(hex);
                Log.d(TAG, "hex string: " + hex);

                for(int i=0; i < finalPayload.length()/2; i++){
                    if(finalPayload.charAt(i) == '0'){ // attached
                        indicatorsLayout1.getChildAt(i).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_green));
                        Log.d(TAG, "0, green");
                    } else {
                        indicatorsLayout1.getChildAt(i).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_red));
                        Log.d(TAG, "1, red");
                    }
                }

                int layoutIndex = 0;
                for(int i = finalPayload.length()/2; i < finalPayload.length(); i++){
                    if(finalPayload.charAt(i) == '0'){ // attached
                        indicatorsLayout2.getChildAt(layoutIndex).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_green));
                        Log.d(TAG, "0, green");
                    } else {
                        indicatorsLayout2.getChildAt(layoutIndex).setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_red));
                        Log.d(TAG, "1, red");
                    }

                    layoutIndex++;
                }
            }
        });

    }

    private class SelectListen implements PairedAdapter.SelectListener {

        @Override
        public void onSelect(BluetoothDevice device) {
            selectedDevice = device;
            socket = null;
            try {
                // TODO make in new thread
                socket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ConnectingThread connectingThread = new ConnectingThread(socket, new ConnectingThread.SocketConnectedListener() {
                @Override
                public void onConnectionSucess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "socket connected", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onConnectionError() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "socket connection error", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            connectingThread.start();
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {


        try {
           /* if (!socket.isConnected()) {
                socket.close();
                socket = selectedDevice.createRfcommSocketToServiceRecord(selectedDevice.getUuids()[0].getUuid());
            }*/
            outStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e("tag", "Error occurred when creating output stream", e);
        }

        try {
            outStream.write(bytes);

        } catch (IOException e) {
            Log.e("tag", "Error occurred when sending data", e);
        }
    }


    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}