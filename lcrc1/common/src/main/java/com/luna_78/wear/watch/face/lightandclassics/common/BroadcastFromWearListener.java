package com.luna_78.wear.watch.face.lightandclassics.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.wearable.DataMap;

/**
 * Created by buba on 03/11/15.
 *
 * communication with listener service
 * LocalBroadcastManager; must be registered in (Engine.)onCreate, unregistered in (Engine.)onDestroy
 */
public class BroadcastFromWearListener extends BroadcastReceiver {

    OnWearDataArrived mOnWearDataCallback;

    public BroadcastFromWearListener(OnWearDataArrived onWearDataCallback) {
        mOnWearDataCallback = onWearDataCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!action.equals(ACommon.BCAST_EVENT_ACTION)) return;
        Bundle bundle = intent.getExtras();
        //long bcastTime = bundle.getLong(ACommon.BCAST_EXTRA_TIME);
        DataMap dataMap = DataMap.fromBundle(bundle.getBundle(ACommon.KEY_DATA_MAP));
        String uriHost = dataMap.getString(ACommon.KEY_URI_HOST, null);
        String uriPath = dataMap.getString(ACommon.KEY_URI_PATH);
        //int evtType = dataMap.getInt(ACommon.KEY_EVENT);
        //long evtTime = dataMap.getLong(ACommon.KEY_TIME);

        mOnWearDataCallback.onWearData(uriHost, uriPath, dataMap);
    }


    public static void broadcastToConsumers(Context context, DataMap dataMap) {
        Intent intent = new Intent();
        intent.setAction(ACommon.BCAST_EVENT_ACTION);
        //intent.putExtra(ACommon.BCAST_EXTRA_EVENT, dataMap.getInt(ACommon.KEY_EVENT));
        intent.putExtra(ACommon.BCAST_EXTRA_TIME, System.currentTimeMillis());
        intent.putExtra(ACommon.KEY_DATA_MAP, dataMap.toBundle());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    //LocalBroadcastManager.getInstance(this).unregisterReceiver(mWearDataFromListenerReceiver);
    public void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        ((CommonApplication) context.getApplicationContext()).setReceiverForListenerRegistered(false);
    }


    public void register(Context context, IntentFilter filter) {
        LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
        ((CommonApplication) context.getApplicationContext()).setReceiverForListenerRegistered(true);
    }

}

