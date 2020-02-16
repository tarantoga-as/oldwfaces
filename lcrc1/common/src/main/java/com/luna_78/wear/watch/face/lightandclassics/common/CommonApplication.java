package com.luna_78.wear.watch.face.lightandclassics.common;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;

/**
 * Created by buba on 03/11/15.
 */
public class CommonApplication
    extends Application
    implements OnWearListenerCommand, OnWatchFaceCommand
{
    //public static final boolean L = ACommon.L;
    private static final String TAG = ACommon.TAG_PREFIX + "COMMON_APP";


    private String          mLocalPeerId;
    private final Object    mLocalPeerIdLock = new Object();

    private int             mWearListenerMode = ACommon.WEAR_LISTENER_UNDEF;
    private final Object    mWearListenerModeLock = new Object();

    //private String          mProductId;

    private String          mServiceCapability;
    private final Object    mServiceCapabilityLock = new Object();

    private String          mServicePeerId;
    private final Object    mServicePeerIdLock = new Object();

    //private SynBoolean      mIsServiceReachable = new SynBoolean(false);
    SynBoolean mReceiverForListenerRegistered = new SynBoolean(false);


    @Override
    public void onCreate() {
        super.onCreate();
        //mProductId = getResources().getString(R.string.product_id);
        mServiceCapability = getResources().getString(R.string.product_id) + ACommon.SERVICE_LOCAL_CAP_SUFFIX;
        //productIdWatchFaceCap = productId + ACommon.WATCHFACE_LOCAL_CAP_SUFFIX;

        if (ACommon.L) Log.i(TAG, "onCreate(), Local=" + getLocalPeerId() + ", Mode=" + getWearListenerMode()
                + ", Service=" + getServicePeerId() + ", Capability=" + getServiceCapability()
                + ", Receiver=" + isReceiverForListenerRegistered()
        );

        shareReceiverForListenerRegisteredTrigger();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (ACommon.L) Log.i(TAG, "onTerminate(), Local=" + getLocalPeerId() + ", Mode=" + getWearListenerMode()
                + ", Service=" + getServicePeerId() + ", Capability=" + getServiceCapability()
                + ", Receiver=" + isReceiverForListenerRegistered()
        );
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (ACommon.L) Log.i(TAG, "onConfigurationChanged(), Local=" + getLocalPeerId() + ", Mode=" + getWearListenerMode()
                + ", Service=" + getServicePeerId() + ", Capability=" + getServiceCapability()
                + ", Receiver=" + isReceiverForListenerRegistered()
        );
    }



//    public boolean isServiceReachable() { return mIsServiceReachable.getValue(); }
//    public void setServiceReachable(boolean reachable) { mIsServiceReachable.setValue(reachable); }



    public void setServicePeerId(String peerId) {
        if (ACommon.L) Log.i(TAG, "setServicePeerId() = " + peerId);
        synchronized (mServicePeerIdLock) {
            if (null == peerId) {
                mServicePeerId = null;
            } else {
                mServicePeerId = String.format("%s", peerId);
            }
        }
        //setStringValue(mServicePeerIdLock, mServicePeerId, peerId);
    }

    public String getServicePeerId() {
        synchronized (mServicePeerIdLock) {
            if (null == mServicePeerId) return null;
            return String.format("%s", mServicePeerId);
        }
        //return getStringValue(mServicePeerIdLock, mServicePeerId);
    }



    public String getLocalPeerId() {
        //if (L) Log.i(TAG, "getLocalPeerId = " + mLocalPeerId);
        synchronized (mLocalPeerIdLock) {
            //return mLocalPeerId;
            if (null == mLocalPeerId) return null;
            return String.format("%s", mLocalPeerId);
        }
        //return getStringValue(mLocalPeerIdLock, mLocalPeerId);
    }

    public void setLocalPeerId(String peerId) {
        if (ACommon.L) Log.i(TAG, "setLocalPeerId() = " + peerId);
        synchronized (mLocalPeerIdLock) {
            if (null == peerId) {
                mLocalPeerId = null;
            } else {
                mLocalPeerId = String.format("%s", peerId);
            }
            //mLocalPeerId = peerId;
        }
        //setStringValue(mLocalPeerIdLock, mLocalPeerId, peerId);
    }




    public String getServiceCapability() {
        return mServiceCapability;
//        synchronized (mServiceCapabilityLock) {
//            if (null == mServiceCapability) return null;
//            return String.format("%s", mServiceCapability);
//        }
    }

    public int getWearListenerMode() { synchronized (mWearListenerModeLock) { return mWearListenerMode; } }

    public void setWearListenerMode(int mode) { synchronized (mWearListenerModeLock) { mWearListenerMode = mode; } }


//    public void runWearWatchFaceChooser() {
//        Intent intent = new Intent(
//                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
//        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
//                new ComponentName(this, AWearFaceService.class));
//        startActivity(intent);
//    }





    public boolean isReceiverForListenerRegistered() { return mReceiverForListenerRegistered.getValue(); }
    public void setReceiverForListenerRegistered(boolean value) {
        mReceiverForListenerRegistered.setValue(value);
        shareReceiverForListenerRegisteredTrigger();
    }
    public void shareReceiverForListenerRegisteredTrigger() {
        boolean registered = mReceiverForListenerRegistered.getValue();

        if (ACommon.L) Log.i(TAG, "shareReceiverForListenerRegisteredTrigger(), Receiver = " + registered);
        new Exception().printStackTrace();

        DataMap dataMap = new DataMap();
        dataMap.putBoolean(ACommon.KEY_TRIGGER, registered);
        Utils.shareNodeData(this, ACommon.RECEIVER_TRIGGER_PATH, dataMap);
    }









    @Override //OnWearListenerCommand
    public void runWearWatchFaceChooser() {

    }

    @Override //OnWearListenerCommand
    public boolean isWatchFaceInstalled() {
        return false;
    }

    @Override //OnWatchFaceCommand
    public void setHandheldBatteryTrigger(boolean value) {

    }

    @Override //OnWatchFaceCommand
    public boolean getHandheldBatteryTrigger() {
        return false;
    }

    @Override //OnWatchFaceCommand
    public void setConsumeDataTrigger(boolean value) {

    }

    @Override //OnWatchFaceCommand
    public boolean getConsumeDataTrigger() {
        return true;
    }



    public void publishSharedValues() {
        if (ACommon.L) Log.i(TAG, "publishSharedValues(), Local=" + getLocalPeerId() + ", Mode=" + getWearListenerMode()
                        + ", Service=" + getServicePeerId() + ", Capability=" + getServiceCapability()
                        + ", Receiver=" + isReceiverForListenerRegistered()
        );
        if (ACommon.WEAR_LISTENER_CLIENT == getWearListenerMode()) shareReceiverForListenerRegisteredTrigger();
    }


}
