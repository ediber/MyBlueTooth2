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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

/*    @BindView(R.id.showPairedDevices)
    View showPairedDevices;*/

    @BindView(R.id.start)
    View start;

    @BindView(R.id.stop)
    View stop;

    @BindView(R.id.closeSocket)
    View closeSocket;

    @BindView(R.id.recycler)
    RecyclerView recycler;

    @BindView(R.id.answer)
    TextView answer;

//    @BindView(R.id.radioGroup)
//    RadioGroup radioGroup;

    @BindView(R.id.indicatorsLayout1)
    LinearLayout indicatorsLayout1;

    @BindView(R.id.indicatorsLayout2)
    LinearLayout indicatorsLayout2;

    @BindView(R.id.level)
    Spinner spinnerLevel;

    @BindView(R.id.current)
    Spinner spinnerCurrent;


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


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setStart();
                message.setPayloadCount(2);
                message.addPayload(spinnerLevel.getSelectedItemPosition());
                message.addPayload(spinnerCurrent.getSelectedItemPosition());

                sendMessage(message);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setStop();
                sendMessage(message);
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
        showPairedDevices();

        binaries = generateTableOfAllBinaries();
        setSpinners();


    }

    private void setSpinners() {
        // set spinners
        List levelsLst = new ArrayList(Arrays.asList("P_95_N_5", "P_92_5_N_7_5", "P_90_N_10" ,"P_87_5_N_12_5", "P_85_N_15", "P_80_N_20", "P_75_N_25", "P_70_N_30"));
        ArrayAdapter<String> levelArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levelsLst); //selected item will look like a spinner set from XML
        levelArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelArrayAdapter);

        List currentLst = new ArrayList(Arrays.asList("CURRENT_6_NA", "CURRENT_24_NA", "CURRENT_6_UA", "CURRENT_24_UA"));
        ArrayAdapter<String> currentArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currentLst); //selected item will look like a spinner set from XML
        currentArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrent.setAdapter(currentArrayAdapter);
    }

    private void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        pairedDevicesList = new ArrayList<>(pairedDevices);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(linearLayoutManager);
        adapter = new PairedAdapter(pairedDevicesList, new SelectListen());
        recycler.setAdapter(adapter);
    }

    private ArrayList<String> generateTableOfAllBinaries() {
        ArrayList<String> binaries = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            String binary = ConvertUtil.intToBinaryString(i);
            binaries.add(binary);
        }
        return binaries;
    }



    private void sendMessage(Message message) {

        writeToSocket(message.toBytes());

        alreadyConnectedThread = new AlreadyConnectedThread(socket, new Receiver());
        alreadyConnectedThread.start();
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
        final String hex = ConvertUtil.bytesToHexString(buffer);

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
    public void writeToSocket(byte[] bytes) {

        try {
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



}
