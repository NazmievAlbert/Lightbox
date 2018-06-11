package com.example.sun.bluetoothlightbox;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


public class BtThread extends Thread  {


    final String TAG = "clock_bt_thread";
    private int connectAttempts = 0;
    private BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private boolean isConnectedFlag = false;
    private OutputStream outStream = null;
    private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Handler handler;
    private Message msg;
    private static final int max_attempts = 5;


    public BtThread(BluetoothSocket socket, BluetoothDevice device, Handler h) {
        mmSocket = socket;
        mmDevice = device;
        handler = h;
    }

    public void run() {
        do {
            if (!Thread.interrupted() && connectAttempts < max_attempts)    //Проверка прерывания
            {
                if (!isConnectedFlag) {
                    if (!connectBt()) connectAttempts++;
                }
            } else {
                disconnectBt();
                return;        //Завершение потока
            }

            try {
                Thread.sleep(1000);        //Приостановка потока на 1 сек.
            } catch (InterruptedException e) {
                disconnectBt();
                return;    //Завершение потока после прерывания
            }
        }
        while (true);

    }

    private boolean connectBt() {
        boolean result = true;

        if(createSocket()){
            result=connectToSocket();
        }

        return result;
    }

    private boolean createSocket(){
        boolean result =true;
        Log.d(TAG, "Trying to connect attempt #" + String.valueOf(connectAttempts + 1));

        if (connectAttempts > 2) {
            //ParcelUuid[] UUIDs = mmDevice.getUuids();
            //MY_UUID = UUIDs[0].getUuid();
        }

        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d(TAG, "socket for " + mmDevice.getAddress() + " is created");
        } catch (IOException e) {
            Log.e(TAG, "socket create failed: " + e.getMessage() + ".");

            msg = handler.obtainMessage(R.integer.BT_CONNECTION_ERROR, (connectAttempts+1), (max_attempts+1));          //Notify main activity
            handler.sendMessage(msg);
            result = false;
        }
        return result;
    }

    private boolean connectToSocket(){
        boolean result =true;

        Log.d(TAG, "...Connecting to device: " + mmSocket.getRemoteDevice().getAddress() + "...");
        try {
            mmSocket.connect();                                                                             //Device is connected!!!!
            Log.d(TAG, "Device is connected");
            handler.sendEmptyMessage(R.integer.BT_DEVICE_CONNECTED);
            isConnectedFlag = true;
            outStream = mmSocket.getOutputStream();

        } catch (IOException e) {
            result = false;
            Log.e(TAG, "unable to connect socket trying to close the socket " + e.getMessage() + ".");
            msg = handler.obtainMessage(R.integer.BT_CONNECTION_ERROR, connectAttempts, max_attempts);
            handler.sendMessage(msg);
            try {
                mmSocket.close();
                Log.e(TAG, "the bluetooth socket is closed");
            } catch (IOException e2) {
                Log.e(TAG, "unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }
        return result;
    }

    private void disconnectBt() {
        Log.d(TAG, "trying to close the socket ");
        try {
            mmSocket.close();
            Log.d(TAG, "socket succesfully closed");
            handler.sendEmptyMessage(R.integer.BT_DEVICE_DISCONNECTED);

        } catch (IOException e) {
            Log.e(TAG, "unable to close socket" + e.getMessage() + ".");
        }

    }

    public void cancel() {
        disconnectBt();
    }

    public void writeString(String message) {

        if (outStream != null) {
            byte[] msgBuffer = message.getBytes();
            Log.d(TAG, "...Посылаем данные: " + message + "...");
            try {
                outStream.write(msgBuffer);
            } catch (IOException e) {
                Log.e(TAG, "writeString: an exception occurred during write:" + e.getMessage());
            }

        } else {
            Log.e(TAG, "Out stream is NULL!");
        }

    }

    public void sendBytes(byte[] string) {

        if (outStream != null) {
            Log.d(TAG, "...Посылаем данные: ");
            try {
                outStream.write(string);
                handler.sendEmptyMessage(R.integer.BT_SEND_OK);
            } catch (IOException e) {
                Log.e(TAG, "writeString: an exception occurred during write:" + e.getMessage());
                handler.sendEmptyMessage(R.integer.BT_SEND_ERROR);
            }

        } else {
            Log.e(TAG, "Out stream is NULL!");
        }

    }


}