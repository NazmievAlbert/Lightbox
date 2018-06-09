package com.example.sun.bluetoothlightbox;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BtThread extends Thread {
    final String TAG ="Bluetooth_thread_class" ;
    int connectAttempts=0;
    private BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private boolean isConnectedFlag=false;
    private  UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public BtThread(BluetoothSocket socket, BluetoothDevice device) {
        mmSocket = socket;
        mmDevice= device;
    }


    public void run(){
        do
        {
            if(!Thread.interrupted()&&connectAttempts<5)	//Проверка прерывания
            {if(!isConnectedFlag){
                    if(!connectBt())connectAttempts++;
                }
            }
            else {
                disconnectBt();
                return;        //Завершение потока
            }

            try{
                Thread.sleep(1000);		//Приостановка потока на 1 сек.
            }catch(InterruptedException e){
                disconnectBt();
                return;	//Завершение потока после прерывания
            }
        }
        while(true);

    }

    private boolean connectBt(){
        boolean result = true;

        Log.d(TAG, "Trying to connect attempt #" + String.valueOf(connectAttempts+1));

        if (connectAttempts>2){
            ParcelUuid[] UUIDs = mmDevice.getUuids();
            MY_UUID= UUIDs[connectAttempts%UUIDs.length].getUuid();}

        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d(TAG, "socket for "+ mmDevice.getAddress() +" is created");
        } catch (IOException e) {
            Log.e(TAG, "socket create failed: " + e.getMessage() + "." );
            result=false;
            }

        Log.d(TAG, "...Connecting to device: "+ mmSocket.getRemoteDevice().getAddress() +"..." );
        try {
            mmSocket.connect();
            Log.d(TAG, "Device is connected");
            isConnectedFlag=true;
        } catch (IOException e) {
            result=false;
            Log.e(TAG, "unable to connect socket trying to close the socket " + e.getMessage() + ".");
            try {
                mmSocket.close();
                Log.e(TAG, "the bluetooth socket is closed");
            } catch (IOException e2) {
                Log.e(TAG, "unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }
        return result;
    }

    private void disconnectBt(){
        Log.d(TAG, "trying to close the socket ");
        try {
            mmSocket.close();
            Log.d(TAG, "socket succesfully closed");
        } catch (IOException e) {  Log.e(TAG, "unable to close socket" + e.getMessage() + ".");}
    }


    public void cancel() {
        disconnectBt();
    }
}