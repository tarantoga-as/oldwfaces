package com.luna_78.wear.watch.face.lightandclassics.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;

import com.google.android.gms.wearable.DataMap;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by buba on 29/10/15.
 */
public class ACommon {

    public static boolean L = true;
    public static final String TAG_PREFIX = "LACF-";

    private static final String TAG = TAG_PREFIX + "COMMON";


//    public static final int WEAR_CONN_UNKNOWN = 0;
//    public static final int WEAR_CONN_FILED = 1;
//    public static final int WEAR_CONN_SUSPENDED = 2;
//    public static final int WEAR_CONN_CONNECTED = 3;
//
//    public static final String KEY_TOPEER = "recv2peer";
//    public static final String KEY_SERIAL_SEQUENCE = "serialSeq";


    public static final int WEAR_LISTENER_UNDEF = 0;
    public static final int WEAR_LISTENER_SERVER = 1;
    public static final int WEAR_LISTENER_CLIENT = 2;


    public static final String BCAST_EVENT_ACTION = "com.luna_78.lacf1144.NEW_EVENT";
    public static final String BCAST_EXTRA_EVENT = "lbc.event";
    public static final String BCAST_EXTRA_TIME = "lbc.time";




    // pseudo path, used only in LocalBroadcasts to conform the callback function onWearData(....., String uriPath, .....)
    public static final String WEAR_EVENT_PATH = "/lacf/event";
    public static final String MESSAGE_PATH = "/lacf/message";
    //
    public static final String PATH_CONSUMED = "/lacf/consumed";
    public static final String PATH_SHARED = "/lacf/shared";
    public static final String WEAR_CRASH_PATH = PATH_CONSUMED + "/crash";
    public static final String FROM_HANDHELD_PATH = PATH_CONSUMED + "/from_phone";
    public static final String TO_LISTENER_PATH = PATH_CONSUMED + "/to.listener";
    public static final String FROM_LISTENER_PATH = PATH_CONSUMED + "/from.listener";
    //
    public static final String HHBATT_TRIGGER_PATH = PATH_SHARED + "/hhbattt";
    public static final String RECEIVER_TRIGGER_PATH = PATH_SHARED + "/receiver";





    public static final String KEY_EVENT = "key.event";
            public static final int EVT_WEAR_CRASHREPORT = 1; //92;
            public static final int EVT_PHONE_BATTERY_SAMPLE = 2; //1;
            public static final int EVT_LISTENER_CONTROL = 3;
            public static final int EVT_CONNECTED_NODES = 4;
            public static final int EVT_NODE_CONNECTED = 5;
            public static final int EVT_NODE_DISCONNECTED = 6;
            public static final int EVT_SERVICE_HERE = 7;
            public static final int EVT_WATCHFACE_HERE = 8;
            //public static final int EVT_HHBATT_TRIGGER = 9;
    public static final String KEY_TIME = "key.time";
    public static final String KEY_LEVEL = "key.level";
    public static final String KEY_CRASHREPORT_TIME = "crash.time";
    public static final String KEY_CRASHREPORT_CONTENT = "crash.content";
    public static final String KEY_DATA_MAP = "data.map";
    public static final String KEY_URI_HOST = "uri.host";
    public static final String KEY_URI_PATH = "uri.path";
    public static final String KEY_NODE_ID_LIST = "node.id.list";
    public static final String KEY_NODE_ID = "node.id";
    public static final String KEY_NUM_ITEMS = "num.items";
    public static final String KEY_TRIGGER = "trigger";
    public static final String KEY_HANDSHAKE_ACTION = "here.action";
            public static final int HANDSHAKE_REQUEST = 0;
            public static final int HANDSHAKE_REPLY = 1;
    public static final String KEY_LISTENER_COMMAND = "lst.level";
            public static final int LC_RUN_WATCHFACE = 1;
            public static final int LC_PING_LISTENER = 2;
            public static final int LC_PING_LISTENER_REPLY = 3;
            public static final int LC_IS_WATCHFACE_INSTALLED = 4;



    public static final String WEAR_CRASHREPORT_FILE_SUFFIX = "-acra";
    public static final String PENDING_CRASH_REPORT = "PENDING REPORT(s) FOLLOWS";
    public static final String APPROVED_STACKTRACE = "-approved.stacktrace";
    public static final float BATTERY_LEVEL_UNSPECIFIED = -1f;
    public static final String SERVICE_LOCAL_CAP_SUFFIX = "_SERVICE";
    public static final String WATCHFACE_LOCAL_CAP_SUFFIX = "_WATCHFACE";







//    public static void copyFile(InputStream sourceInputStream, File destination) throws IOException {
//        FileOutputStream destinationOutputStream = new FileOutputStream(destination);
//        byte[] buf = new byte[1024];
//        int len;
//
//        while ((len = sourceInputStream.read(buf)) > 0) {
//            destinationOutputStream.write(buf, 0, len);
//        }
//
//        sourceInputStream.close();
//        destinationOutputStream.close();
//    }












//    public static void broadcastToConsumers(Context context, DataMap dataMap) {
//        Intent intent = new Intent();
//        intent.setAction(ACommon.BCAST_EVENT_ACTION);
//        //intent.putExtra(ACommon.BCAST_EXTRA_EVENT, dataMap.getInt(ACommon.KEY_EVENT));
//        intent.putExtra(ACommon.BCAST_EXTRA_TIME, System.currentTimeMillis());
//        intent.putExtra(ACommon.KEY_DATA_MAP, dataMap.toBundle());
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//    }








//    volatile static long            mCounter = 0;
//    static final Object             mLockCounter = new Object();
//
//
//    public static class WearNetSend extends Thread {
//        String              path, wearPeer;
//        DataMap             dataMap;
//        GoogleApiClient     mGoogleApiClient;
//        long                counter;
//
//        // Constructor for sending data objects to the data layer
//        public WearNetSend(String p, DataMap data, GoogleApiClient googleApiClient, String wearPeer) {
//            mGoogleApiClient = googleApiClient;
//            if (null != mGoogleApiClient) {
//                path = p;
//                dataMap = data;
//                this.wearPeer = wearPeer;
//                synchronized (mLockCounter) {
//                    mCounter++;
//                    counter = mCounter;
//                }
//            }
//        }
//
//        public void run() {
//            if (null == mGoogleApiClient) {
//                if (L) Log.i(TAG, "!!! WearNetSend: mGoogleApiClient is null; abort.");
//                return;
//            }
//            if (!mGoogleApiClient.isConnected()) {
//                if (L) Log.i(TAG, "!!! WearNetSend: mGoogleApiClient NOT CONNECTED; proceed.");
//            }
//            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//            //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//            List<Node> nodeList;
//            int numNodes = 0;
//            if (nodes != null) {
//                nodeList = nodes.getNodes();
//                if (nodeList != null) numNodes = nodeList.size();
//            }
//            if (numNodes > 0) {
//                dataMap.putLong(ACommon.KEY_SERIAL_SEQUENCE, counter); //"serialSeq"
//
//                if (null != wearPeer) {
//                    dataMap.putString(ACommon.KEY_TOPEER, wearPeer);
//                }
//                //Log.i(TAG, "((( WearNetSend dataMap=" + dataMap + ", path=" + path);
//                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
//                putDMR.getDataMap().putAll(dataMap);
//                PutDataRequest request = putDMR.asPutDataRequest();
//                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
//
//                //dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WEARCFG_TOGGLE_LAYOUT);
////                int event = dataMap.getInt(ACommon.KEY_EVENT);
////                if (ACommon.EVT_WEARCFG_TOGGLE_LAYOUT == event) {
////                    boolean success = result.getStatus().isSuccess();
////                    //Log.i(TAG, "#TOGGLE_LAYOUT success=" + success + ", serialSeq=" + counter); //dataMap.getLong(ACommon.KEY_SERIAL_SEQUENCE)
////                }
//            }
//        }
//    } // class WearNetSend
//
//
//    public static class TaskWearNetSend
//        implements
//            Runnable,
//            OnWearEvent
//    {
//        String              path, wearPeer;
//        DataMap             dataMap;
//        long                counter;
//        WearDataSender mWearSender;
//        volatile Integer    mConnResult; // = new Integer(ACommon.WEAR_CONN_UNKNOWN);
//
//        public TaskWearNetSend(Context context, String p, DataMap data, String wearPeer) {
//            mWearSender = new WearDataSender(context, this);
//            //mWearSender.requestConnect();
//            path = p;
//            dataMap = new DataMap();
//            dataMap.putAll(data);
//            this.wearPeer = wearPeer;
//            synchronized (mLockCounter) {
//                mCounter++;
//                counter = mCounter;
//            }
//            mConnResult = new Integer(ACommon.WEAR_CONN_UNKNOWN);
//            if (L) Log.i(TAG, "TaskWearNetSend constructor, mConnResult=" + mConnResult);
//        }
//
//        @Override
//        public void run() {
//            if (L) Log.i(TAG, "TaskWearNetSend enter run(), mConnResult=" + mConnResult);
//            mWearSender.requestConnect();
//            synchronized (mWearSender) {
//                try {
//                    for (int i=0; i<3; i++) {
//                        if (ACommon.WEAR_CONN_UNKNOWN == mConnResult) {
//                            mWearSender.wait(10000);
//                        } else break;
//                    }
////                    while (ACommon.WEAR_CONN_UNKNOWN == mConnResult) {
////                        mWearSender.wait(10000);
////                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            switch (mConnResult) {
//                case ACommon.WEAR_CONN_UNKNOWN:
//                    if (L) Log.i(TAG, "TaskWearNetSend timeout; abort.");
//                    mWearSender.requestDisconnect();
//                    return;
//                case ACommon.WEAR_CONN_FILED:
//                    if (L) Log.i(TAG, "TaskWearNetSend connection filed; abort.");
//                    mWearSender.requestDisconnect();
//                    return;
//                case ACommon.WEAR_CONN_SUSPENDED:
//                    if (L) Log.i(TAG, "TaskWearNetSend connection suspended; abort.");
//                    mWearSender.requestDisconnect();
//                    return;
//            }
//            if (L) Log.i(TAG, "TaskWearNetSend connected, proceed.");
//
//            int numNodes;
//            numNodes = getNumNodesConnected();
////            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mWearSender.getGoogleApiClient()).await();
////            //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
////            List<Node> nodeList;
////            if (nodes != null) {
////                nodeList = nodes.getNodes();
////                if (nodeList != null) numNodes = nodeList.size();
////            }
//            if (numNodes > 0) {
//                dataMap.putLong(ACommon.KEY_SERIAL_SEQUENCE, counter); //"serialSeq"
//
//                if (null != wearPeer) {
//                    dataMap.putString(ACommon.KEY_TOPEER, wearPeer);
//                }
//                //Log.i(TAG, "((( WearNetSend dataMap=" + dataMap + ", path=" + path);
//                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
//                putDMR.getDataMap().putAll(dataMap);
//                PutDataRequest request = putDMR.asPutDataRequest();
//                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mWearSender.getGoogleApiClient(), request).await();
//                if (L) Log.i(TAG, "TaskWearNetSend result=" + result.getStatus().isSuccess());
//
//                //dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WEARCFG_TOGGLE_LAYOUT);
////                int event = dataMap.getInt(ACommon.KEY_EVENT);
////                if (ACommon.EVT_WEARCFG_TOGGLE_LAYOUT == event) {
////                    boolean success = result.getStatus().isSuccess();
////                    //Log.i(TAG, "#TOGGLE_LAYOUT success=" + success + ", serialSeq=" + counter); //dataMap.getLong(ACommon.KEY_SERIAL_SEQUENCE)
////                }
//            }
//            mWearSender.requestDisconnect();
//        }
//
//
//        @Override
//        public void onWearConnResult(int i) {
//            if (L) Log.i(TAG, "TaskWearNetSend enter onWearConnResult(), i=" + i + ", mConnResult=" + mConnResult);
//            synchronized (mWearSender) {
//                mConnResult = i;
//                //if (ACommon.WEAR_CONN_CONNECTED != mConnResult) mWearSender.requestDisconnect();
//                mWearSender.notify();
//            }
//        }
//
//
//        private int getNumNodesConnected() {
//            int result = 0;
//            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mWearSender.getGoogleApiClient()).await();
//            List<Node> nodeList;
//            if (nodes != null) {
//                nodeList = nodes.getNodes();
//                if (nodeList != null) result = nodeList.size();
//            }
//            return result;
//        }
//
//
//        @Override
//        public boolean isPeerIdKnown() {
//            return false;
//        }
//
//
//        @Override
//        public void rememberPeerId(String peerId) {
//
//        }
//
//
//        @Override
//        public boolean isLocalPeerIdKnown() {
//            return false;
//        }
//
//
//        @Override
//        public void rememberLocalPeerId(String peerId) {
//
//        }
//
//    }

}

