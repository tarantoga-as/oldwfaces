package com.luna_78.wear.watch.face.lightandclassics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;
import com.luna_78.wear.watch.face.lightandclassics.common.Utils;
import com.luna_78.wear.watch.face.lightandclassics.common.WearDataSender;

import java.util.ArrayList;

/**
 * Created by buba on 30/10/15.
 */
public class HandheldBattery {

    private static final String TAG = ACommon.TAG_PREFIX + "PHONE_BATT";
    //private static final boolean L = ACommon.L;

    float               mBatteryLevel = ACommon.BATTERY_LEVEL_UNSPECIFIED;
    boolean             mRegisteredBatteryReceiver = false;
    boolean             mEnabled = false;
    WearDataSender      mWearSender;
    Context             mContext;
    ArrayList<String>   mSubscribers;


//    public HandheldBattery(GoogleApiClient googleApiClient) {
//        setGoogleApiClient(googleApiClient);
//    }


    public HandheldBattery(Context context, GoogleApiClient googleApiClient) {
        //setGoogleApiClient(googleApiClient);
        mContext = context;
        setEnabled(true);
    }

    public HandheldBattery(Context context, WearDataSender wearSender) {
        mWearSender = wearSender;
        mContext = context;
        //setEnabled(true);
        mSubscribers = new ArrayList<String>();
    }


    public void subscribe(String subscriberNodeId, boolean isRequested) {
        if (isRequested) {
            if (!mSubscribers.contains(subscriberNodeId)) {
                mSubscribers.add(subscriberNodeId);
            }
        } else {
            if (mSubscribers.contains(subscriberNodeId)) {
                mSubscribers.remove(subscriberNodeId);
            }
        }
        setEnabled(!(mSubscribers.isEmpty()));
    }


    public void quitWork() {
        setEnabled(false);
    }


//    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
//        mGoogleApiClient = googleApiClient;
//    }


    public void setEnabled(boolean enabled/*, Context context*/) {
        if (enabled == mEnabled) return;
        int nSubscribers = mSubscribers.size();
        if (ACommon.L) Log.i(TAG, "Set setEnabled( " + nSubscribers + " ) to " + enabled);
        mEnabled = enabled;
        registerBatteryReceiver(mEnabled/*, mContext*/);
    }


    final BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level<0 || scale<0) {
                mBatteryLevel = ACommon.BATTERY_LEVEL_UNSPECIFIED;
            } else {
                mBatteryLevel = ((float)level / (float)scale) * 100.0f;
            }
            //Log.i(TAG, "*** PhoneBattery: int=" + level + ", float=" + mBatteryLevel);
//            if (mWearSender.isConnected()) {
//            } else {
//                if (ACommon.L) Log.i(TAG, "!!! BatteryInfoReceiver: mWearSender is not connected; ignored.");
//            }
            mWearSender.requestConnect(0);
            int nSubscribers = mSubscribers.size();
            if (ACommon.L) Log.i(TAG, "BatteryInfoReceiver( " + nSubscribers + " )" + ", level = " + level);
            if (nSubscribers > 1) {
                DataMap dataMap = new DataMap();
                dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_PHONE_BATTERY_SAMPLE);
                dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis()); //new Date().getTime()
                dataMap.putFloat(ACommon.KEY_LEVEL, mBatteryLevel);
                (new WearDataSender.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap,
                        mWearSender.getGoogleApiClient(), null, Utils.getSerialSeq(), false)).start();
            } else if (1 == nSubscribers) {
                Bundle bundle = new Bundle();
                bundle.putInt(ACommon.KEY_EVENT, ACommon.EVT_PHONE_BATTERY_SAMPLE);
                bundle.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
                bundle.putFloat(ACommon.KEY_LEVEL, mBatteryLevel);
                String toNodeId;
                toNodeId = mSubscribers.get(0);
                (new Thread(new WearDataSender.TaskWearMessageSend(mWearSender.getGoogleApiClient(),
                                bundle, toNodeId, ACommon.MESSAGE_PATH))).start();
            }
        }
    };


    private void registerBatteryReceiver(boolean register/*, Context context*/) {
        int nSubscribers = mSubscribers.size();
        if (register) {
            mBatteryLevel = shotBatteryLevel(/*mContext*/);
            if (!mRegisteredBatteryReceiver) {
                if (ACommon.L) Log.i(TAG, "Register BatteryInfoReceiver( " + nSubscribers + " )");
                mRegisteredBatteryReceiver = true;
                mContext.registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                //mWearSender.requestConnect();
            }
        } else {
            if (mRegisteredBatteryReceiver) {
                if (ACommon.L) Log.i(TAG, "Unregister BatteryInfoReceiver( " + nSubscribers + " )");
                mRegisteredBatteryReceiver = false;
                mContext.unregisterReceiver(mBatteryInfoReceiver);
                //mWearSender.requestDisconnect();
            }
        }
    }


    private float shotBatteryLevel(/*Context context*/) {
        Intent batteryIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level<0 || scale<0) return ACommon.BATTERY_LEVEL_UNSPECIFIED;
        return ((float)level / (float)scale) * 100.0f;
    }

}
