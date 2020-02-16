package com.luna_78.wear.watch.face.lightandclassics;

import android.content.Context;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;
import com.luna_78.wear.watch.face.lightandclassics.common.CommonApplication;
import com.luna_78.wear.watch.face.lightandclassics.common.OnWearDataArrived;

import java.util.ArrayList;

/**
 * Created by buba on 31/10/15.
 */
public class WearDataForWatchFace
    implements
        OnWearDataArrived
{

    private static final boolean L = ACommon.L;
    private static final String TAG = ACommon.TAG_PREFIX + "WEAR_DATA";


    Context                     mContext;
    AWearFaceService.Engine     mEngine;




    public WearDataForWatchFace(Context context, AWearFaceService.Engine engine) {
        mContext = context;
        mEngine = engine;
    }


    @Override
    public boolean onWearData(String uriHost, String uriPath, DataMap dataMap) {
        boolean result = false;

        int evtType = dataMap.getInt(ACommon.KEY_EVENT);
        long time = dataMap.getLong(ACommon.KEY_TIME);

        int intPrm;
        ArrayList<String> strings;
        boolean boolPrm;

        if (uriPath.equals(ACommon.FROM_HANDHELD_PATH) || uriPath.equals(ACommon.MESSAGE_PATH)) {
            switch (evtType) {
                case ACommon.EVT_PHONE_BATTERY_SAMPLE:
                    float level = dataMap.getFloat(ACommon.KEY_LEVEL);
                    if (L) Log.i(TAG, "Battery sample = " + level);
                    //broadcastFloatToWearFaceService(evtType, time, level);
                    break;

                default:
                    break;
            }
            result = true;

        } else if (uriPath.equals(ACommon.MESSAGE_PATH)) {
            switch (evtType) {
                case ACommon.EVT_SERVICE_HERE:
                    intPrm = dataMap.getInt(ACommon.KEY_HANDSHAKE_ACTION);
                    if (ACommon.HANDSHAKE_REQUEST == intPrm) {
                        //((CommonApplication) mContext.getApplicationContext()).setServicePeerId(uriHost);
                        if (L) Log.i(TAG, "Service handshake REQUEST from service node = " + uriHost);
                        mEngine.sendWatchFaceHereMsg(uriHost, evtType, ACommon.HANDSHAKE_REPLY);
                    } else {
                        if (L) Log.i(TAG, "!!! HANDSHAKE ERROR");
                    }
                    break;
                case ACommon.EVT_WATCHFACE_HERE:
                    intPrm = dataMap.getInt(ACommon.KEY_HANDSHAKE_ACTION);
                    if (ACommon.HANDSHAKE_REPLY == intPrm) {
                        if (L) Log.i(TAG, "Watch face handshake REPLY from service node = " + uriHost);
                        ((CommonApplication) mContext.getApplicationContext()).setServicePeerId(uriHost);
                    } else {
                        if (L) Log.i(TAG, "!!! HANDSHAKE ERROR");
                    }
                    break;

                default:
                    break;
            }
            result = true;

        } else if (uriPath.equals(ACommon.WEAR_EVENT_PATH)) {
            switch (evtType) {
//                dataMap.putString(ACommon.KEY_URI_PATH, ACommon.WEAR_EVENT_PATH);
//                dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_CONNECTED_NODES);
//                dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//                dataMap.putInt(ACommon.KEY_NUM_ITEMS, count);
//                if (count > 0) dataMap.putStringArrayList(ACommon.KEY_NODE_ID_LIST, nodeIds);
                case ACommon.EVT_CONNECTED_NODES:
                    intPrm = dataMap.getInt(ACommon.KEY_NUM_ITEMS);
                    if (L) Log.i(TAG, "Event=" + evtType + ": CONNECTED NODES LIST, " + intPrm + " items");
                    if (intPrm > 0) {
                        strings = dataMap.getStringArrayList(ACommon.KEY_NODE_ID_LIST);
                        for (String nodeId : strings) {
                            if (L) Log.i(TAG, "CONNECTED NODES LIST: " + nodeId);
                        }

                        ((CommonApplication) mContext.getApplicationContext()).publishSharedValues();
                    }
                    break;

//                dataMap.putString(ACommon.KEY_URI_PATH, ACommon.WEAR_EVENT_PATH);
//                dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_NODE_DISCONNECTED);
//                dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//                dataMap.putString(ACommon.KEY_NODE_ID, peer.getId());
                case ACommon.EVT_NODE_DISCONNECTED:
                    if (L) Log.i(TAG, "Event=" + evtType + ": NODE " + dataMap.getString(ACommon.KEY_NODE_ID) + " DISCONNECTED");
                    break;

//                dataMap.putString(ACommon.KEY_URI_PATH, ACommon.WEAR_EVENT_PATH);
//                dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_NODE_CONNECTED);
//                dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//                dataMap.putString(ACommon.KEY_NODE_ID, peer.getId());
                case ACommon.EVT_NODE_CONNECTED:
                    if (L) Log.i(TAG, "Event=" + evtType + ": NODE " + dataMap.getString(ACommon.KEY_NODE_ID) + " CONNECTED");
                    break;

                default:
                    break;
            }
            result = true;

        } else if (uriPath.equals(ACommon.RECEIVER_TRIGGER_PATH)) {
            boolPrm = dataMap.getBoolean(ACommon.KEY_TRIGGER);
            if (L) Log.i(TAG, "Receiver trigger = " + boolPrm + ", from node = " + uriHost);
            result = true;

        } else if (uriPath.equals(ACommon.HHBATT_TRIGGER_PATH)) {
            boolPrm = dataMap.getBoolean(ACommon.KEY_TRIGGER);
            if (L) Log.i(TAG, "Handheld battery trigger = " + boolPrm + ", from node = " + uriHost);
            result = true;

        }


        return result;
    }

}
