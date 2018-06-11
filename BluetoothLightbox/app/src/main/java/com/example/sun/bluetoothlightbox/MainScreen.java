package com.example.sun.bluetoothlightbox;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;

public class MainScreen extends AppCompatActivity implements BluetoothPairedFragment.onFragmentResultListener {

    final String TAG = "clock_lightbox";
    final int BLUETOOTH_ENABLE_REQUEST_CODE = 33;
    SharedPreferences sharedPreferences;
    BluetoothPart bt;
    boolean isBtPairedFlag = false;
    boolean isBtConnectedFlag = false;
    TextView tvStatus;
    int color_result = 0;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        handler = new MyHandler(this);
        bt = new BluetoothPart(handler);
        tvStatus = (TextView) findViewById(R.id.main_screen_status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBtPairedFlag) {
            connectBtDevice();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isBtPairedFlag", isBtPairedFlag);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isBtPairedFlag = savedInstanceState.getBoolean("isBtPairedFlag");
    }

    @Override
    protected void onPause() {
        super.onPause();
        bt.disconnectDevice();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.menu_group_bluetooth_on, !isBtPairedFlag);
        menu.setGroupVisible(R.id.menu_group_bluetooth_off, isBtPairedFlag);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_exit:
                this.finish();
                break;
            case R.id.menu_settings:
                //toast("the toast "+String.valueOf(isBtConnectedFlag));                                //dark test magick todo make good code
                chooseColor(33);

                break;

            case R.id.menu_connect:
                connectBtDevice();
                break;

            case R.id.menu_disconnect:
                sharedPreferences = getPreferences(MODE_PRIVATE);
                sharedPreferences.edit().putString("last_device", "NULL").commit();
                bt.disconnectDevice();
                isBtPairedFlag = false;

                break;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("activityResult", "requestCode = " + requestCode + ", resultCode = " + resultCode);
        switch (requestCode) {
            case BLUETOOTH_ENABLE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {                                                     // Bluetooth has been enabled
                    Log.d(TAG, "bluetooth has been successfully enabled");
                    //connectBtDevice();  //bug on sony xperia. locking the uuid
                } else {
                    Log.e(TAG, "Bluetooth hasn't been enabled");
                    toast("Bluetooth hasn't been enabled");
                } // Bluetooth hasn't been enabled :(


                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();


    }

    public void connectBtDevice() {

        String deviceMAC = "";
        boolean isKnownBtExistsFlag = false;
        sharedPreferences = getPreferences(MODE_PRIVATE);
        String lastConnectedDevice = sharedPreferences.getString("last_device", "NULL");


        if (bt.isBluetoothSupported()) if (bt.isBluetoothOn()) {
            //get device list
            Set<BluetoothDevice> deviceList = bt.showSetBondedDevices();


            for (BluetoothDevice device : deviceList) {

                if (device.getAddress().equals(lastConnectedDevice)) {       //If the last connected device is in paired device list
                    isKnownBtExistsFlag = true;
                    deviceMAC = device.getAddress();
                    break;
                }


            }

            if (isKnownBtExistsFlag) {                                        //If the last connected device is in paired device list

                toast("Connecting to: " + deviceMAC);
                bt.connectDevice(deviceMAC);
                isBtPairedFlag = true;
            } else {                                                       //If the last connected device isn't exists or last attempt was unsuccesfull
                showPairedDevices(deviceList);
            }

        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_REQUEST_CODE);
        }

    }

    public void showPairedDevices(Set<BluetoothDevice> btDevices) {

        BluetoothPairedFragment bluetoothPairedFragment = new BluetoothPairedFragment();

        ArrayList<String> deviceNameList = new ArrayList<>();
        for (BluetoothDevice device : btDevices) {
            deviceNameList.add(device.getName() + " " + device.getAddress());
        }

        Bundle btFragBundle = new Bundle();
        btFragBundle.putStringArrayList("devices", deviceNameList);
        bluetoothPairedFragment.setArguments(btFragBundle);

        bluetoothPairedFragment.show(getFragmentManager(), "btPairedList");

    }

    @Override
    public void getFragmentResult(String tag, String result) {
        if (tag.equals("paired")) {
            //toast("!"+result);
            sharedPreferences = getPreferences(MODE_PRIVATE);
            sharedPreferences.edit().putString("last_device", result).commit();
            connectBtDevice();

        }
    }


    public boolean chooseColor(final int position) {

        final int ledPosition = position;
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .lightnessSliderOnly()
                .initialColor(Color.MAGENTA)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(11)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        bt.btFillLedColor(selectedColor);
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        //Toast.makeText(getApplicationContext(), Integer.toHexString(selectedColor), Toast.LENGTH_SHORT).show();
                        color_result = selectedColor;
                        toast(Integer.toHexString(color_result));
                        if (isBtConnectedFlag) {
                            //bt.btSendString(Integer.toHexString(color_result));
                            bt.btFillLedColor(color_result);
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();

        return true;

    }                                                                   //Dirty hack with global color

    static class MyHandler extends Handler {

        WeakReference<MainScreen> wrActivity;

        public MyHandler(MainScreen activity) {
            wrActivity = new WeakReference<MainScreen>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainScreen activity = wrActivity.get();
            if (activity != null){

                activity.tvStatus.setText(String.valueOf(msg.what));
                switch (msg.what){
                    case R.integer.BT_DEVICE_CONNECTED:
                        activity.tvStatus.setText(activity.getResources().getString(R.string.btConnected));
                        activity.isBtConnectedFlag=true;
                        break;
                    case R.integer.BT_DEVICE_DISCONNECTED:
                        activity.tvStatus.setText(activity.getResources().getString(R.string.btDisconnected));
                        activity.isBtConnectedFlag=false;
                        break;
                    case R.integer.BT_CONNECTION_ERROR:
                        activity.tvStatus.setText("Connection Failed at "+String.valueOf(msg.arg1)+" attempt from" +String.valueOf(msg.arg2));
                        activity.isBtConnectedFlag=false;
                        break;
                    case R.integer.BT_SEND_ERROR:
                        activity.tvStatus.setText("Failed to send data");
                        break;

                    case R.integer.BT_SEND_OK:
                        activity.tvStatus.setText("Data successfully sended");
                        break;
                }
            }

        }
    }

}

