package com.example.sun.bluetoothlightbox;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothPart {

    final String TAG ="Bluetooth_class";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket =null;
    BluetoothDevice device = null;
    private BtThread btThread =null;


    public  BluetoothPart(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }


    public boolean isBluetoothSupported(){
        if (bluetoothAdapter == null) {
            Log.e(TAG,"BlueTooth is not supported on this device");
            return false;
        }
        else{
            Log.d(TAG,"bluetooth is supported");
            return true;
        }
    }

    public boolean isBluetoothOn(){

        if (this.isBluetoothSupported()){
        if (bluetoothAdapter.isEnabled()) {
            Log.d(TAG,"bluetooth is on");
            return true;
        }
        else{
            Log.d(TAG,"bluetooth is disabled");
            return false;
        }
    }else {return false;}

    }

    public Set<BluetoothDevice> showSetBondedDevices(){
        Set<BluetoothDevice> pairedDevices= bluetoothAdapter.getBondedDevices();
        return pairedDevices;
    }

    public boolean connectDevice(String MAC){
        Log.d(TAG, "trying to connect to device: " + MAC);
        try{device = bluetoothAdapter.getRemoteDevice(MAC);
            Log.d(TAG, "device is found "+ MAC);
        }catch (ClassCastException e){
            Log.e(TAG,  " device with MAC: "+MAC+" isn't found "+e.getClass()+e.getMessage() );
            return false;
        }


        btThread = new BtThread(bluetoothSocket, device);
        btThread.start();
        return true;
        }

        public void disconnectDevice(){

            if(btThread!=null){
                if(!btThread.isInterrupted())
                    btThread.interrupt();
            }

        }


}
