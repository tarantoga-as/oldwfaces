package com.luna_78.wear.watch.face.lightandclassics;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;

/**
 * Created by buba on 01/11/15.
 */
public class PipeToService implements ServiceConnection {

    private APhoneService mService;
    private Context mContext;

    private String mPeerId = null;
    private boolean mServiceBound = false;



    public PipeToService(Context context, String peerId) {
        mContext = context;
        mPeerId = peerId;
    }


    public void attachToService() {
        mContext.bindService((new Intent(mContext, APhoneService.class)), this, Context.BIND_AUTO_CREATE);
    }


    public void detachFromService() {
        if (mServiceBound) {
            mContext.unbindService(this);
        }
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        APhoneService.LocalBinder binder = (APhoneService.LocalBinder) service;
        mService = binder.getService();
        mServiceBound = true;
        //Log.i(TAG, "((( onServiceConnected: mService=" + mService);
        //setSlidingTabLayout();
        //mService.setPeerId(mPeerId); // !!!



        //mService.activityConnected(mPeerId);



        //setPagerSlidingTabStrip();
        //registerIntentFromServiceReceiver();
    }



    private boolean isIntentFromServiceReceiverRegistered = false;
    //
    public void registerIntentFromServiceReceiver(BroadcastReceiver receiver) {
        if (!isIntentFromServiceReceiverRegistered) {
            LocalBroadcastManager.getInstance(mContext).
                    registerReceiver(receiver, new IntentFilter(ACommon.BCAST_EVENT_ACTION));
            isIntentFromServiceReceiverRegistered = true;
        }
    } // registerIntentFromServiceReceiver
    public void unregisterIntentFromServiceReceiver(BroadcastReceiver receiver) {
        if (isIntentFromServiceReceiverRegistered) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(receiver);
        }
        isIntentFromServiceReceiverRegistered = false;
    } // unregisterIntentFromServiceReceiver




    @Override
    public void onServiceDisconnected(ComponentName name) {
        //Log.i(TAG, "((( onServiceDisconnected");
        mServiceBound = false;
        mService = null;
        //unregisterIntentFromServiceReceiver();
    }


    public boolean isBound() { return mServiceBound; }


    public APhoneService getService() { return mService; }
}
