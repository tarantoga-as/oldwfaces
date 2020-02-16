package com.luna_78.wear.watch.face.lightandclassics.common;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by buba on 02/11/15.
 *
 * see here: https://developers.google.com/android/reference/com/google/android/gms/wearable/WearableListenerService
 */
public class WearDataListener
    extends
        WearableListenerService
{

    private static final boolean L = ACommon.L;
    private static final String TAG = ACommon.TAG_PREFIX + "WEAR_LISTENER";


    private String      mLocalPeerId;
    private int         mWearListenerMode;
    private String      mServicePeerId;
    private String      mServiceCapability;

    GoogleApiClient     mGoogleApiClient;

    boolean             mReceiver, mConsumeData;


    @Override
    public void onCreate() {
        super.onCreate();
        //if (L) Log.i(TAG, "onCreate()");

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .build();
//
//        ConnectionResult connectionResult =
//                mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
//
//        if (!connectionResult.isSuccess()) {
//            if (L) Log.i(TAG, "!!! Failed to connect to GoogleApiClient; proceed.");
//            //return;
//        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        }
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
//            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
//            if (!connectionResult.isSuccess()) return;
        }

        mLocalPeerId = ((CommonApplication) getApplication()).getLocalPeerId();
        //lookLocalPeerId(); // never in UI thread!!!

        mWearListenerMode = ((CommonApplication) getApplication()).getWearListenerMode(); //getApplicationContext

        if (ACommon.WEAR_LISTENER_CLIENT == mWearListenerMode) {
            mServiceCapability = ((CommonApplication) getApplication()).getServiceCapability();
            //lookServicePeerId(CapabilityApi.FILTER_ALL); // never in UI thread!!!
            mServicePeerId = ((CommonApplication) getApplication()).getServicePeerId();
        }

        mReceiver = ((CommonApplication) getApplication()).isReceiverForListenerRegistered();

        mConsumeData = ((CommonApplication) getApplication()).getConsumeDataTrigger();

        if (L) Log.i(TAG, "onCreate(), Local=" + mLocalPeerId + ", Mode=" + mWearListenerMode +
                ", Service=" + mServicePeerId + ", Capability=" + mServiceCapability +
                ", Receiver=" + mReceiver
        );
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (L) Log.i(TAG, "onDestroy(), connected = " + mGoogleApiClient.isConnected());
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    private String lookLocalPeerId() {
        if (null != mLocalPeerId) return mLocalPeerId;
        String foundNodeId = Utils.findLocalPeerId(mGoogleApiClient, 10);
        if (null != foundNodeId) {
            ((CommonApplication) getApplication()).setLocalPeerId(foundNodeId);
            return foundNodeId;
        }
        return mLocalPeerId;
    }


    private String lookServicePeerId(int capabilityFilter) {
        if (null != mServicePeerId || null == mServiceCapability) return mServicePeerId;
        String foundNodeId = Utils.findCapabilityNodeId(mServiceCapability, mGoogleApiClient, 10, capabilityFilter);
        if (null != foundNodeId) {
            ((CommonApplication) getApplication()).setServicePeerId(foundNodeId);
            return foundNodeId;
        }
        return mServicePeerId;
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        //super.onDataChanged(dataEvents);
        //if (L) Log.i(TAG, "onDataChanged()");

        mLocalPeerId = lookLocalPeerId();
        if (ACommon.WEAR_LISTENER_CLIENT == mWearListenerMode) mServicePeerId = lookServicePeerId(CapabilityApi.FILTER_ALL);

        if (ACommon.WEAR_LISTENER_SERVER != mWearListenerMode) {
            if (null == mLocalPeerId) { // || null == mServicePeerId
                dataEvents.release();
                if (L) Log.i(TAG, "onDataChanged() ABORTED, reason: mLocalPeerId="+mLocalPeerId); //+ " or mServicePeerId="+mServicePeerId
                return;
            }
        }

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        DataMap dataMap;
        boolean connected = false;

        String stringVal;

        for (DataEvent event : events) {
            Uri uri = event.getDataItem().getUri();
            String scheme = uri.getScheme();
            String path = uri.getPath();
            String host = uri.getHost(); // may be null
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                if (ACommon.WEAR_LISTENER_SERVER != mWearListenerMode) {
                    if (null == mServicePeerId) { //null == mLocalPeerId ||
                        if (L) Log.i(TAG, "onDataChanged("+host+path+") SKIPPED, reason: mServicePeerId="+mServicePeerId);
                        continue;
                    }
                }

                stringVal = DataMapItem.fromDataItem(event.getDataItem()).getDataMap().getString(WearDataSender.KEY_TOPEER, null);
                if (Utils.isNotEqual(mLocalPeerId, stringVal)) { //dataMap.getString(WearDataSender.KEY_TOPEER, null)
                    if (L) Log.i(TAG, "onDataChanged() event SKIPPED, reason: data addressed for node=" + stringVal);
                    continue;
                }

                if (Utils.isEqual(mLocalPeerId, host)) {
                    //todo: "dataEvents.release(); return;" or "continue;"?
                    //dataEvents.release(); return;
                    if (L) Log.i(TAG, "onDataChanged() event SKIPPED, reason: data from local sender.");
                    continue;
                }

                if (ACommon.WEAR_LISTENER_CLIENT == mWearListenerMode) {
                    if (path.equals(ACommon.WEAR_CRASH_PATH)) {
                        if (L) Log.i(TAG, "onDataChanged() event SKIPPED, reason: crash report for handheld.");
                        continue;
                    }
//                    else if (ACommon.WEAR_LISTENER_SERVER == mWearListenerMode) {
//                        int evtType = dataMap.getInt(ACommon.KEY_EVENT);
//                        if (ACommon.EVT_WEAR_CRASHREPORT == evtType) {
//                            byte[] crashContent = dataMap.getByteArray(ACommon.KEY_CRASHREPORT_CONTENT);
//                            long crashTime = dataMap.getLong(ACommon.KEY_CRASHREPORT_TIME, 0);
//                            //Log.i(TAG, "((( EVT_WEAR_CRASHREPORT, time=" + crashTime + ", len=" + crashContent.length);
//                            if (0 == crashTime) crashTime = System.currentTimeMillis();
//                            if (null != crashContent && crashContent.length > 0) {
//                                if (L) Log.i(TAG, "onDataChanged() crash report from wear device.");
//                                new Utils.SaveWearCrashReport(getApplicationContext(), crashTime, crashContent, host).start();
//                            } else {
//                                if (L) Log.i(TAG, "!!! onDataChanged() error: EMPTY crash report from wear device; skipped.");
//                            }
//                            continue;
//                        }
//                    }
                }

                dataMap = new DataMap();
                dataMap.putAll(DataMapItem.fromDataItem(event.getDataItem()).getDataMap());

                if (false == catchListenerEvent(host, path, dataMap)) {
                    if (L) Log.i(TAG, "onDataChanged() data from host=" + host + ", path=" + path +
                            ", seq=" + dataMap.getLong(WearDataSender.KEY_SERIAL_SEQUENCE, 0) +
                            ", event=" + dataMap.getInt(ACommon.KEY_EVENT, 0));
                    //int evtType = dataMap.getInt(ACommon.KEY_EVENT);
                    //long time = dataMap.getLong(ACommon.KEY_TIME);
                    if (null != host) dataMap.putString(ACommon.KEY_URI_HOST, host);
                    dataMap.putString(ACommon.KEY_URI_PATH, path);
                    if (((CommonApplication) getApplication()).isReceiverForListenerRegistered()) {
                        BroadcastFromWearListener.broadcastToConsumers(this, dataMap);
                    }
                }


                if (mConsumeData) {

                    if (path.contains(ACommon.PATH_CONSUMED)) {

                        // consume dataItem
                        if (!connected) connected = Utils.waitConnected(mGoogleApiClient, 10);
                        if (connected) {
                            DataApi.DeleteDataItemsResult result =
                                    Wearable.DataApi.deleteDataItems(mGoogleApiClient, event.getDataItem().getUri()).await();
                            if(!result.getStatus().isSuccess()) {
                                if (L) Log.i(TAG, "!!! deleteDataItems() is not successful.");
                            }
                        } else {
                            if (L) Log.i(TAG, "!!! deleteDataItems(): connection to GoogleApiClient FILED.");
                        }

                    }

                }


            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                //if (L) Log.i(TAG, "Data received: deleted URI: " + uri); onDataChanged
                if (Utils.isNotEqual(mLocalPeerId, host)) {
                    if (L) Log.i(TAG, "*** data DELETED, uri=" + uri);
                } else {
                    if (L) Log.i(TAG, "*** data CONSUMED, uri=" + uri);
                }
            }
        }

        dataEvents.release();
    } //onDataChanged


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //super.onMessageReceived(messageEvent);
        mLocalPeerId = lookLocalPeerId();
        if (ACommon.WEAR_LISTENER_CLIENT == mWearListenerMode) mServicePeerId = lookServicePeerId(CapabilityApi.FILTER_ALL);

        String host = messageEvent.getSourceNodeId();
        String path = messageEvent.getPath();
        byte[] messageBytes = messageEvent.getData();
        Bundle bundle = Utils.bytesToBundle(messageBytes);

        DataMap dataMap = DataMap.fromBundle(bundle);
        if (false == catchListenerEvent(host, path, dataMap)) {
            if (null != host) dataMap.putString(ACommon.KEY_URI_HOST, host);
            dataMap.putString(ACommon.KEY_URI_PATH, path);
            if (L) Log.i(TAG, "onMessageReceived() data from host=" + host + ", path=" + path +
                    ", seq=" + dataMap.getLong(WearDataSender.KEY_SERIAL_SEQUENCE) +
                    ", event=" + dataMap.getInt(ACommon.KEY_EVENT));
            if (((CommonApplication) getApplication()).isReceiverForListenerRegistered()) {
                BroadcastFromWearListener.broadcastToConsumers(this, dataMap);
            }
        }
    }


    private boolean catchListenerEvent(String uriHost, String uriPath, DataMap message) {
        boolean eventConsumed = false;

        if (false == (uriPath.equals(ACommon.MESSAGE_PATH) || uriPath.equals(ACommon.TO_LISTENER_PATH) ||
                uriPath.equals(ACommon.FROM_LISTENER_PATH) || uriPath.equals(ACommon.WEAR_CRASH_PATH)))
        {
            return false;
        }

        int event = message.getInt(ACommon.KEY_EVENT);
        int command;

        if (ACommon.EVT_LISTENER_CONTROL == event) {
            command = message.getInt(ACommon.KEY_LISTENER_COMMAND);
            if (ACommon.LC_PING_LISTENER == command) {
                eventConsumed = true;
                Utils.sendListenerMsg(uriHost, ACommon.EVT_LISTENER_CONTROL, ACommon.LC_PING_LISTENER_REPLY, mGoogleApiClient);
                return eventConsumed;
            }
            if (ACommon.LC_PING_LISTENER_REPLY == command) {
                eventConsumed = true;
                if (L) Log.i(TAG, "***** Listener ping REPLY from node=" + uriHost);
                return eventConsumed;
            }
        }

        // ACommon.KEY_EVENT = ACommon.EVT_LISTENER_CONTROL;
        // ACommon.KEY_LISTENER_COMMAND = ACommon.LC_RUN_WATCHFACE
        if (ACommon.WEAR_LISTENER_CLIENT == mWearListenerMode) {
            switch (event) {
                case ACommon.EVT_LISTENER_CONTROL:
                    command = message.getInt(ACommon.KEY_LISTENER_COMMAND);
                    switch (command) {
                        case ACommon.LC_RUN_WATCHFACE:
                            try {
                                ((CommonApplication) getApplication()).runWearWatchFaceChooser();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;

                        case ACommon.LC_IS_WATCHFACE_INSTALLED:
                            try {
                                ((CommonApplication) getApplication()).isWatchFaceInstalled();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;

                        default: break;
                    }
                    eventConsumed = true;
                    break;


                case ACommon.EVT_SERVICE_HERE:
                    command = message.getInt(ACommon.KEY_HANDSHAKE_ACTION);
                    if (ACommon.HANDSHAKE_REQUEST == command) {
                        if (L) Log.i(TAG, "Service handshake REQUEST from service node = " + uriHost);
                        ((CommonApplication) getApplication()).setServicePeerId(uriHost);
                    }
                    eventConsumed = false; // need to continue processing by receiver (if one registered)
                    break;


                default:
                    break;
            }

        } else if (ACommon.WEAR_LISTENER_SERVER == mWearListenerMode) {
            if (ACommon.EVT_WEAR_CRASHREPORT==event && uriPath.equals(ACommon.WEAR_CRASH_PATH)) {
                eventConsumed = true;
                byte[] crashContent = message.getByteArray(ACommon.KEY_CRASHREPORT_CONTENT);
                long crashTime = message.getLong(ACommon.KEY_CRASHREPORT_TIME, 0);
                //Log.i(TAG, "((( EVT_WEAR_CRASHREPORT, time=" + crashTime + ", len=" + crashContent.length);
                if (0 == crashTime) crashTime = System.currentTimeMillis();
                if (null != crashContent && crashContent.length > 0) {
                    if (L) Log.i(TAG, "catchListenerEvent() crash report from wear device.");
                    new Utils.SaveWearCrashReport(getApplicationContext(), crashTime, crashContent, uriHost).start();
                } else {
                    if (L) Log.i(TAG, "!!! catchListenerEvent() error: EMPTY crash report from wear device; skipped.");
                }
            }
        }

        return eventConsumed;
    }


    @Override
    public void onPeerConnected(Node peer) {
        //super.onPeerConnected(peer);
        if (L) Log.i(TAG, "onPeerConnected(): " + peer.getId() + ", " + peer.getDisplayName());
        mLocalPeerId = lookLocalPeerId();
        if (ACommon.WEAR_LISTENER_CLIENT == mWearListenerMode) mServicePeerId = lookServicePeerId(CapabilityApi.FILTER_ALL);
        DataMap dataMap;
        dataMap = new DataMap();
        dataMap.putString(ACommon.KEY_URI_PATH, ACommon.WEAR_EVENT_PATH);
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_NODE_CONNECTED);
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putString(ACommon.KEY_NODE_ID, peer.getId());
        if (((CommonApplication) getApplication()).isReceiverForListenerRegistered()) {
            BroadcastFromWearListener.broadcastToConsumers(this, dataMap);
        }
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        //super.onPeerDisconnected(peer);
        if (L) Log.i(TAG, "onPeerDisconnected(): " + peer.getId() + ", " + peer.getDisplayName());
        mLocalPeerId = lookLocalPeerId();
        if (ACommon.WEAR_LISTENER_CLIENT == mWearListenerMode) mServicePeerId = lookServicePeerId(CapabilityApi.FILTER_ALL);
        DataMap dataMap;
        dataMap = new DataMap();
        dataMap.putString(ACommon.KEY_URI_PATH, ACommon.WEAR_EVENT_PATH);
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_NODE_DISCONNECTED);
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putString(ACommon.KEY_NODE_ID, peer.getId());
        if (((CommonApplication) getApplication()).isReceiverForListenerRegistered()) {
            BroadcastFromWearListener.broadcastToConsumers(this, dataMap);
        }
    }

    @Override
    public void onConnectedNodes(List<Node> connectedNodes) {
        //super.onConnectedNodes(connectedNodes);
        mLocalPeerId = lookLocalPeerId();
        if (ACommon.WEAR_LISTENER_CLIENT == mWearListenerMode) mServicePeerId = lookServicePeerId(CapabilityApi.FILTER_ALL);

        int count = 0;
        ArrayList<String> nodeIds = new ArrayList<>();
        for (Node node : connectedNodes) {
            count++;
            if (L) Log.i(TAG, "onConnectedNodes() node["+ count + "]: " + node.getId() + ", " + node.getDisplayName());
            nodeIds.add(node.getId());
        }
        if (0 == count) if (L) Log.i(TAG, "onConnectedNodes( 0 )");

        DataMap dataMap;
        dataMap = new DataMap();
        dataMap.putString(ACommon.KEY_URI_PATH, ACommon.WEAR_EVENT_PATH);
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_CONNECTED_NODES);
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putInt(ACommon.KEY_NUM_ITEMS, count);
        if (count > 0) dataMap.putStringArrayList(ACommon.KEY_NODE_ID_LIST, nodeIds);
        if (((CommonApplication) getApplication()).isReceiverForListenerRegistered()) {
            BroadcastFromWearListener.broadcastToConsumers(this, dataMap);
        }

//        if (ACommon.WEAR_LISTENER_CLIENT == mWearListenerMode && count > 0) {
//            lookServicePeerId(CapabilityApi.FILTER_REACHABLE);
//        }
    }


    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        mLocalPeerId = lookLocalPeerId();
        if (ACommon.WEAR_LISTENER_CLIENT == mWearListenerMode) {
            if (L) Log.i(TAG, "onCapabilityChanged( " + mServiceCapability + " ) ?????");
            String foundNodeId = Utils.findCapabilityNodeId(mServiceCapability, capabilityInfo);
            if (null != foundNodeId && null == mServicePeerId) {
                if (L) Log.i(TAG, "onCapabilityChanged() SUCCESS, found service at nodeId = " + foundNodeId);
                ((CommonApplication) getApplication()).setServicePeerId(foundNodeId);
            }
        }
    }

//    public static void checkServiceCapability(Context context, String serviceCapabilityName, CapabilityInfo capabilityInfo) {
//        if (L) Log.i(TAG, "checkServiceCapability( " + serviceCapabilityName + " )");
//        if (null==serviceCapabilityName || null==context || null==capabilityInfo) return;
//
//        Set<Node> connectedNodes = capabilityInfo.getNodes();
//        String capabilityName = capabilityInfo.getName();
//        if (!capabilityName.equals(serviceCapabilityName)) return;
//
//        String bestNodeId = null;
//        for (Node node : connectedNodes) {
//            if (node.isNearby()) {
//                bestNodeId = node.getId();
//                break;
//            }
//            bestNodeId = node.getId();
//        }
//        if (null != bestNodeId) {
//            if (L) Log.i(TAG, "checkServiceCapability() SUCCESS, service nodeId = " + bestNodeId);
//            ((CommonApplication) context.getApplicationContext()).setServicePeerId(bestNodeId);
//        }
//    }





//    private boolean waitConnected(int waitSeconds) {
//        ConnectionResult connectionResult;
//
//        if (0 != waitSeconds) {
//            connectionResult = mGoogleApiClient.blockingConnect(waitSeconds, TimeUnit.SECONDS);
//        } else {
//            connectionResult = mGoogleApiClient.blockingConnect();
//        }
//
//        if (!connectionResult.isSuccess()) {
//            if (L) Log.i(TAG, "!!! Connection to GoogleApiClient FILED.");
//            return false;
//        } else {
//            return true;
//        }
//    }



//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (L) Log.i(TAG, "onStartCommand, intent=" + intent + "; flags=" + flags + "; startId=" + startId);
//        return super.onStartCommand(intent, flags, startId);
//    }

}
