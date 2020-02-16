package com.luna_78.wear.watch.face.lightandclassics;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;
import com.luna_78.wear.watch.face.lightandclassics.common.BroadcastFromWearListener;
import com.luna_78.wear.watch.face.lightandclassics.common.CommonApplication;
import com.luna_78.wear.watch.face.lightandclassics.common.DemoPackData;
import com.luna_78.wear.watch.face.lightandclassics.common.Utils;
import com.luna_78.wear.watch.face.lightandclassics.common.WearDataSender;

public class APhoneService extends Service
//        implements
//        OnWearDataArrived
//        DataApi.DataListener,
//        NodeApi.NodeListener,
//        MessageApi.MessageListener,
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener
{

    private static final String TAG = ACommon.TAG_PREFIX + "PHONE_SERVICE";
    private static final boolean L = ACommon.L;


    public class LocalBinder extends Binder {
        APhoneService getService() {
            return APhoneService.this;
        }
    }

    private final IBinder mServiceBinder = new LocalBinder();

    @Override // Service
    public IBinder onBind(Intent intent) {
        return mServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) {
            //we are started by Android
            //todo: remove: ACRA.getErrorReporter().reportBuilder().forceSilent().message("Service started by Android").send();
        } else {
            //we are started by activity
            //todo: remove: ACRA.getErrorReporter().reportBuilder().forceSilent().message("Service started by intent").send();
        }
        //return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // Handler
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
        }
    }
    private Looper          mServiceLooper;
    private ServiceHandler  mServiceHandler;









//    private String          mLocalPeerId = null;
//    private PeerId          mPeerId = new PeerId();
    public String           productId;
    public String           productIdServiceCap;
    //public String           productIdWatchFaceCap;
//    GoogleApiClient         mGoogleApiClient;
    HandheldBattery         mPhoneBattery;
    public DemoPackData[]   demoPackData = new DemoPackData[DemoPackData.NUM_DEMOPACK_PARAMETERS];
    HandheldCrashReport     mCrashReporter;
    //WearNetworkForService   mWearPAN;
    WearDataSender          mWearPAN;
    WearDataForService      mWearWorker;
    BroadcastFromWearListener mWearDataFromListenerReceiver;











    @Override
    public void onCreate() {
        super.onCreate();
        if (ACommon.L) Log.i(TAG, "onCreate()");

        HandlerThread thread = new HandlerThread("LightClassicsService", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);


        ((CommonApplication) getApplicationContext()).setWearListenerMode(ACommon.WEAR_LISTENER_SERVER);

        productId = getResources().getString(R.string.product_id);
        productIdServiceCap = ((CommonApplication) getApplicationContext()).getServiceCapability(); //productId + ACommon.SERVICE_LOCAL_CAP_SUFFIX;
        //productIdWatchFaceCap = productId + ACommon.WATCHFACE_LOCAL_CAP_SUFFIX;

        mWearWorker = new WearDataForService(APhoneService.this, mServiceHandler);
        //mWearWorker.restorePeerId();
        mWearPAN = new WearDataSender(APhoneService.this, mWearWorker, /*productIdWatchFaceCap*/null);
        mWearPAN.addLocalCapability(productIdServiceCap);

        //mHandler.post(taskRestorePeerId);
            //mWearPAN.restorePeerId();
            //connectWearNetworkClient();
            //mWearPAN.requestConnect();


        mWearDataFromListenerReceiver = new BroadcastFromWearListener(mWearWorker);
//        LocalBroadcastManager.getInstance(this).registerReceiver(mWearDataFromListenerReceiver,
//                new IntentFilter(ACommon.BCAST_EVENT_ACTION));
        mWearDataFromListenerReceiver.register(this, new IntentFilter(ACommon.BCAST_EVENT_ACTION));


        //mPhoneBattery = new HandheldBattery(APhoneService.this, mWearPAN.getGoogleApiClient()); //mPhoneBattery.showHandheldBattery(true);
        mPhoneBattery = new HandheldBattery(APhoneService.this, mWearPAN); //mPhoneBattery.showHandheldBattery(true);
        mPhoneBattery.setEnabled(false); //mPhoneBattery.setEnabled(true);
        mCrashReporter = new HandheldCrashReport(APhoneService.this, mServiceHandler);

        for (int i=0; i<DemoPackData.NUM_DEMOPACK_PARAMETERS; i++) { demoPackData[i] = new DemoPackData(); }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ACommon.L) Log.i(TAG, "onDestroy()");

        mWearPAN.removeLocalCapability(productIdServiceCap);
        //mPhoneBattery.quitWork(); //mPhoneBattery.showHandheldBattery(false, APhoneService.this);
        mPhoneBattery.setEnabled(false);

        mCrashReporter.quitWork();

        mWearDataFromListenerReceiver.unregister(this);
        mWearPAN.disconnectDelayed(20000);

        mServiceLooper.quitSafely();
    }





    public void sendServiceHereMsg(String toNodeId, int handshakeEvent, int handshakeAction) {
        if (ACommon.L) Log.i(TAG, "sendServiceHereMsg(), toNode=" + toNodeId);
        mWearPAN.requestConnect(0);
        Utils.sendHandshakeMsg(toNodeId, handshakeEvent, handshakeAction, mWearPAN.getGoogleApiClient());
    }




//    public void restorePeerId() {
//        mHandler.post(taskRestorePeerId);
//    }
//
//    private Runnable taskSavePeerId = new Runnable() {
//        @Override
//        public void run() {
//            //String peerId; synchronized (mLockPeerId) { peerId = "" + smPeerId; }
//            //savePeerIdToFile(peerId);
//            mPeerId.savePeerIdToFile(mContext.getApplicationContext());
//        }
//    };
//
//    private Runnable taskRestorePeerId = new Runnable() {
//        @Override
//        public void run() {
//            //String peerId; synchronized (mLockPeerId) { peerId = "" + smPeerId; }
//            //savePeerIdToFile(peerId);
//            mPeerId.restorePeerIdFromFile(mContext.getApplicationContext());
//        }
//    };

}
