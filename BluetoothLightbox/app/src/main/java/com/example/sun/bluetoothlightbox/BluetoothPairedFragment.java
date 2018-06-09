package com.example.sun.bluetoothlightbox;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class BluetoothPairedFragment extends DialogFragment implements View.OnClickListener {

    final String TAG ="clock_PairBtFragment";
    ListView devicelist;
    String[] default_list = {"There aren't any paired devices"};
    ArrayList<String> btDevicesList;

    public interface onFragmentResultListener{
        public void getFragmentResult(String tag, String result);
    }
    onFragmentResultListener btFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AppCompatActivity appCompatActivityactivity;
        Activity activity;
        if (context instanceof AppCompatActivity) {

            appCompatActivityactivity = (AppCompatActivity) context;
            try {
                btFragmentListener = (onFragmentResultListener) appCompatActivityactivity;
                Log.d(TAG, "Fragment Context AppCompatActivityAttached");
            } catch (ClassCastException e) {
                throw new ClassCastException(appCompatActivityactivity.toString() + " must implement onFragmentResultListener");
            }
        }else
        if (context instanceof Activity) {

            activity = (Activity) context;

            try {
                btFragmentListener = (onFragmentResultListener) activity;
                Log.d(TAG, "Fragment Context ActivityAttached");
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement onFragmentResultListener");
            }
        }else Log.e(TAG, "Fragment NOT ATTACHED!");

    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

            try {
                btFragmentListener = (onFragmentResultListener) activity;
                Log.d(TAG, "Fragment ActivityAttached (API23 compat)");
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement onFragmentResultListener");
            }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View body = inflater.inflate(R.layout.bluetooth_device_fragment,null);
        getDialog().setTitle(R.string.fragment_Bt_paired_title);
        body.findViewById(R.id.btnFragOK).setOnClickListener(this);
        body.findViewById(R.id.btnFragCancel).setOnClickListener(this);

        devicelist = (ListView)body.findViewById(R.id.listBtDevices);
        devicelist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        Bundle btFragBundle = getArguments();

        if (btFragBundle!=null){
            btDevicesList=btFragBundle.getStringArrayList("devices");
            Log.d(TAG, "Devices are got from the bundle ");
        }
        else{
            btDevicesList=new ArrayList<>(Arrays.asList(default_list));
            Log.e(TAG, "Haven't got any devices from the bundle ");
        }


        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, btDevicesList);
        devicelist.setAdapter(itemsAdapter);

        return body;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnFragOK:

               String selected = devicelist.getItemAtPosition(devicelist.getCheckedItemPosition()).toString();
               String result = selected.substring(selected.length()-17);

               btFragmentListener.getFragmentResult("paired",result);
               dismiss();
               //Return MAC address
               // Toast.makeText(getActivity(), String.valueOf(devicelist.getCheckedItemPosition())  , Toast.LENGTH_SHORT).show();
               // Toast.makeText(getActivity(), result  , Toast.LENGTH_SHORT).show();

            break;
            case R.id.btnFragCancel:
                dismiss();
                break;

            default:

                break;
        }
    }
}
