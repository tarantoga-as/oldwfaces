package com.luna_78.wear.watch.face.lightandclassics;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;
import com.luna_78.wear.watch.face.lightandclassics.common.CommonApplication;
import com.luna_78.wear.watch.face.lightandclassics.common.OnWearDataArrived;
import com.luna_78.wear.watch.face.lightandclassics.common.OnWearEvent;
//import com.luna_78.wear.watch.face.lightandclassics.common.PeerId;
import com.luna_78.wear.watch.face.lightandclassics.common.Utils;

import java.util.ArrayList;

/**
 * Created by buba on 31/10/15.
 */
public class WearDataForService
    implements
        OnWearDataArrived,
        OnWearEvent
{

    private static final String TAG = ACommon.TAG_PREFIX + "DATA_FOR_SERVICE";
    private static final boolean L = ACommon.L;

    Context                 mContext;
    Handler                 mHandler;
    //private PeerId mPeerId = new PeerId();


    public WearDataForService(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
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




    @Override //OnWearEvent
    public boolean isPeerIdKnown() {
        //return !mPeerId.isNull();
        return false;
    }


    @Override //OnWearEvent
    public void rememberPeerId(String peerId) {
//        mPeerId.setPeerId(peerId);
//        mHandler.post(taskSavePeerId);
//        if (L) Log.i(TAG, "rememberPeerId() = " + peerId);
    }

    @Override //OnWearEvent
    public boolean isLocalPeerIdKnown() {
        return false;
    }

    @Override //OnWearEvent
    public void rememberLocalPeerId(String peerId) {
        ((CommonApplication) mContext.getApplicationContext()).setLocalPeerId(peerId);
    }

    @Override //OnWearEvent
    public void onWearConnResult(int i) {

    }


    @Override //OnWearDataArrived
    public boolean onWearData(String uriHost, String uriPath, DataMap dataMap) {

        // for uriPath==WEAR_EVENT_PATH uriHost is null!

        boolean result = false;

        int evtType = dataMap.getInt(ACommon.KEY_EVENT);
        long time = dataMap.getLong(ACommon.KEY_TIME);

        Bundle bundle;
        int intPrm;
        ArrayList<String> strings;
        boolean boolPrm;

        if (L) Log.i(TAG, "onWearData(): host=" + uriHost + ", path=" + uriPath + ", event=" + evtType);

        // we need to accept ANY crash report, from any watch
        // and any wear event
        if (uriPath.equals(ACommon.WEAR_CRASH_PATH)) {
//            switch (evtType) {
//                // теперь это делает listener сервера
//                case ACommon.EVT_WEAR_CRASHREPORT:
//                    if (L) Log.i(TAG, "onWearData(): event=" + evtType + ": CRASH REPORT");
//                    byte[] crashContent = dataMap.getByteArray(ACommon.KEY_CRASHREPORT_CONTENT);
//                    long crashTime = dataMap.getLong(ACommon.KEY_CRASHREPORT_TIME, 0);
//                    //Log.i(TAG, "((( EVT_WEAR_CRASHREPORT, time=" + crashTime + ", len=" + crashContent.length);
//                    if (0 == crashTime) crashTime = System.currentTimeMillis();
//                    if (null != crashContent && crashContent.length > 0) {
//                        new Utils.SaveWearCrashReport(mContext.getApplicationContext(), crashTime, crashContent, uriHost).start();
//                    }
//                    break;
//
//                default:
//                    //Log.i(TAG, "### UNCATCHED EVENT by WEAR_CRASH_PATH !!!");
//                    break;
//            }
//            result = true;

        } else if (uriPath.equals(ACommon.WEAR_EVENT_PATH)) {
            switch (evtType) {
                case ACommon.EVT_WEAR_CRASHREPORT:
                    if (L) Log.i(TAG, "onWearData(): event=" + evtType + ": CRASH REPORT (from node="+uriHost+") SAVED BY LISTENER");
                    //todo: дёрнуть ACRA?
                    //ACRA.getErrorReporter().reportBuilder().forceSilent().message(ACommon.PENDING_CRASH_REPORT).send();
                    HandheldCrashReport.askToSendPendingReports();
                    break;

//                dataMap.putString(ACommon.KEY_URI_PATH, ACommon.WEAR_EVENT_PATH);
//                dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_CONNECTED_NODES);
//                dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//                dataMap.putInt(ACommon.KEY_NUM_ITEMS, count);
//                if (count > 0) dataMap.putStringArrayList(ACommon.KEY_NODE_ID_LIST, nodeIds);
                case ACommon.EVT_CONNECTED_NODES:
                    intPrm = dataMap.getInt(ACommon.KEY_NUM_ITEMS);
                    if (L) Log.i(TAG, "onWearData(): event=" + evtType + ": CONNECTED NODES LIST, " + intPrm + " items");
                    if (intPrm > 0) {
                        strings = dataMap.getStringArrayList(ACommon.KEY_NODE_ID_LIST);
                        for (String nodeId : strings) {
                            if (L) Log.i(TAG, "CONNECTED NODES LIST: " + nodeId);
                            ((APhoneService) mContext).sendServiceHereMsg(nodeId,
                                    ACommon.EVT_SERVICE_HERE, ACommon.HANDSHAKE_REQUEST);
                        }

                        ((CommonApplication) mContext.getApplicationContext()).publishSharedValues();
                    }
                    break;

//                dataMap.putString(ACommon.KEY_URI_PATH, ACommon.WEAR_EVENT_PATH);
//                dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_NODE_DISCONNECTED);
//                dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//                dataMap.putString(ACommon.KEY_NODE_ID, peer.getId());
                case ACommon.EVT_NODE_DISCONNECTED:
                    if (L) Log.i(TAG, "onWearData(): event=" + evtType + ": NODE " + dataMap.getString(ACommon.KEY_NODE_ID) + " DISCONNECTED");
                    break;

//                dataMap.putString(ACommon.KEY_URI_PATH, ACommon.WEAR_EVENT_PATH);
//                dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_NODE_CONNECTED);
//                dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//                dataMap.putString(ACommon.KEY_NODE_ID, peer.getId());
                case ACommon.EVT_NODE_CONNECTED:
                    if (L) Log.i(TAG, "onWearData(): event=" + evtType + ": NODE " + dataMap.getString(ACommon.KEY_NODE_ID) + " CONNECTED");
                    ((APhoneService) mContext).sendServiceHereMsg(dataMap.getString(ACommon.KEY_NODE_ID),
                            ACommon.EVT_SERVICE_HERE, ACommon.HANDSHAKE_REQUEST);
                    break;

                default:
                    break;
            }
            result = true;

        } else if (uriPath.equals(ACommon.MESSAGE_PATH)) {
            switch (evtType) {
                case ACommon.EVT_WATCHFACE_HERE:
                    intPrm = dataMap.getInt(ACommon.KEY_HANDSHAKE_ACTION);
                    //((CommonApplication) mContext.getApplicationContext()).setServicePeerId(uriHost);
                    if (ACommon.HANDSHAKE_REQUEST == intPrm) {
                        if (L) Log.i(TAG, "Watch face handshake REQUEST from watch face node = " + uriHost);
                        //mEngine.sendWatchFaceHereMsg(uriHost, ACommon.HANDSHAKE_REPLY);
                        ((APhoneService) mContext).sendServiceHereMsg(uriHost, evtType, ACommon.HANDSHAKE_REPLY);
                    } else {
                        if (L) Log.i(TAG, "!!! HANDSHAKE ERROR");
                    }
                    break;
                case ACommon.EVT_SERVICE_HERE:
                    intPrm = dataMap.getInt(ACommon.KEY_HANDSHAKE_ACTION);
                    if (ACommon.HANDSHAKE_REPLY == intPrm) {
                        if (L) Log.i(TAG, "REPLY to service handshake from watch face node = " + uriHost);
                    } else {
                        if (L) Log.i(TAG, "!!! HANDSHAKE ERROR");
                    }
                    break;

                default:
                    break;
            }
            result = true;

        } else if (uriPath.equals(ACommon.HHBATT_TRIGGER_PATH)) {
            boolPrm = dataMap.getBoolean(ACommon.KEY_TRIGGER);
            if (L) Log.i(TAG, "Handheld battery trigger = " + boolPrm + ", from node = " + uriHost);
            if (null != ((APhoneService) mContext).mPhoneBattery) ((APhoneService) mContext).mPhoneBattery.subscribe(uriHost, boolPrm);
            result = true;

        } else if (uriPath.equals(ACommon.RECEIVER_TRIGGER_PATH)) {
            boolPrm = dataMap.getBoolean(ACommon.KEY_TRIGGER);
            if (L) Log.i(TAG, "Receiver trigger = " + boolPrm + ", from node = " + uriHost);
            if (!boolPrm) {
                if (null != ((APhoneService) mContext).mPhoneBattery) ((APhoneService) mContext).mPhoneBattery.subscribe(uriHost, boolPrm);
                //todo: и от остальных подписок тоже отписать этот узел

            } else {
                ((CommonApplication) mContext.getApplicationContext()).publishSharedValues();
            }
            result = true;

        }

        return result;
    }

}
