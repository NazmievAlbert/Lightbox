package com.example.sun.bluetoothlightbox;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;



public class BtThread extends Thread implements Interfaces.Observable
 {
    private List<Interfaces.Observer> observers;

    final String TAG ="clock_bt_thread" ;
    private int connectAttempts=0;
    private String status = "created";
    private BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private boolean isConnectedFlag=false;
    private OutputStream outStream = null;
    private  UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");





    public BtThread(BluetoothSocket socket, BluetoothDevice device) {
        mmSocket = socket;
        mmDevice= device;
        observers = new LinkedList<>();
    }


    @Override
    public void registerObserver(Interfaces.Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Interfaces.Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Interfaces.Observer observer : observers)
            observer.update(status);
    }

    public void sendStatus(String status){
        this.status=status;
        notifyObservers();
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
            mmSocket.connect();                                                                             //Device is connected!!!!
            Log.d(TAG, "Device is connected");
            sendStatus("btThreadConnected");
            isConnectedFlag=true;
            outStream=mmSocket.getOutputStream();
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
            sendStatus("btThreadDisonnected");

        } catch (IOException e) {  Log.e(TAG, "unable to close socket" + e.getMessage() + ".");}

    }


    public void cancel() {
        disconnectBt();
    }


     public void writeString(String message){

        if(outStream!=null){
         byte[] msgBuffer = message.getBytes();
         Log.d(TAG, "...Посылаем данные: " + message + "...");
         try {
             outStream.write(msgBuffer);
         } catch (IOException e) {
             Log.e(TAG, "writeString: an exception occurred during write:" + e.getMessage() );
         }

     }else {Log.e(TAG, "Out stream is NULL!");}

     }

     public void writeColor(int position, int color){

         byte[] ret = new byte[5];
         ret[4] = (byte) (color & 0xFF);
         ret[3] = (byte) ((color >> 8) & 0xFF);
         ret[2] = (byte) ((color >> 16) & 0xFF);
         ret[1] = (byte)(position  & 0xFF);
         ret[0] = (byte)(13);


         if(outStream!=null){

             Log.d(TAG, "...Sending color.. "+Integer.toHexString(color)+" at "+ String.valueOf(position)+" led");
             try {
                 outStream.write(ret);
             } catch (IOException e) {
                 Log.e(TAG, "writeString: an exception occurred during write:" + e.getMessage() );
             }

         }else {Log.e(TAG, "Out stream is NULL!");}

     }


}