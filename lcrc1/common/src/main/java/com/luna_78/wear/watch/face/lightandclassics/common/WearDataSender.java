package com.luna_78.wear.watch.face.lightandclassics.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by buba on 01/11/15.
 *
 * класс только для отправки данных
 */
public class WearDataSender
    implements
        NodeApi.NodeListener,
        GoogleApiClient.ConnectionCallbacks,
        //CapabilityApi.CapabilityListener,
        GoogleApiClient.OnConnectionFailedListener
{

    public static final int WEAR_CONN_UNKNOWN = 0;
    public static final int WEAR_CONN_FILED = 1;
    public static final int WEAR_CONN_SUSPENDED = 2;
    public static final int WEAR_CONN_CONNECTED = 3;

    public static final String KEY_TOPEER = "recv2peer";
    public static final String KEY_SERIAL_SEQUENCE = "serialSeq";










    //private static final boolean    L = ACommon.L;
    private static final String     TAG = ACommon.TAG_PREFIX + "WEAR_SENDER";

    Context mContext;
    GoogleApiClient         mGoogleApiClient = null;
    int                     mGoogleApiClientUseCount;
    OnWearEvent             mOnWearEventCallback;
    Handler                 mDelayedTasksHandler;
    DelayedOps              mBuzzer;

    //private String mLocalCapabilityName;
    //private String mRemoteCapabilityName;


    public WearDataSender() {}


    public WearDataSender(Context context, OnWearEvent onWearEventCallback, String remoteCapName) {
        mContext = context;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(WearDataSender.this)
                .addOnConnectionFailedListener(WearDataSender.this)
                .addApi(Wearable.API)
                .build();
        mOnWearEventCallback = onWearEventCallback;
//        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(getLocalNodeCallback);
//        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(getConnectedNodesCallback);
        //mRemoteCapabilityName = remoteCapName;
        mGoogleApiClientUseCount = 0;
        //mSendHard = false;
        //Looper.prepare();
        mDelayedTasksHandler = new Handler(Looper.getMainLooper());
        mBuzzer = new DelayedOps(mDelayedTasksHandler);
    }


    public GoogleApiClient getGoogleApiClient() { return mGoogleApiClient; }


    //public void setHardSend(boolean val) { mSendHard = val; }


    public boolean isConnected() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }











    private long mConnectFinishTime = 0;

    public int requestDisconnect() {
        if (ACommon.L) Log.i(TAG, "***** requestDisconnect() ENTER, mGoogleApiClientUseCount = " + mGoogleApiClientUseCount);
        mGoogleApiClientUseCount--;
        if (mGoogleApiClientUseCount < 0) mGoogleApiClientUseCount = 0;
        if (mGoogleApiClient != null) {
//            if (mGoogleApiClient.isConnected()) {
//                // remove listeners which was added in onConnected
//                Wearable.DataApi.removeListener(mGoogleApiClient, WearDataSender.this);
//                Wearable.NodeApi.removeListener(mGoogleApiClient, WearDataSender.this);
//                Wearable.MessageApi.removeListener(mGoogleApiClient, WearDataSender.this);
//            }
            if (ACommon.L) Log.i(TAG, "***** requestDisconnect(), connected = " + mGoogleApiClient.isConnected());
            if (0 == mGoogleApiClientUseCount) {
                disconnectUnconditionally(); //mGoogleApiClient.disconnect();
            }
        }
        if (ACommon.L) Log.i(TAG, "***** requestDisconnect() LEAVE, mGoogleApiClientUseCount = " + mGoogleApiClientUseCount);
        return mGoogleApiClientUseCount;
    }


    public void disconnectUnconditionally() {
        if (ACommon.L) Log.i(TAG, "***** disconnectUnconditionally()");
        if (mGoogleApiClient != null) {
//            if (null != mRemoteCapabilityName) {
//                Wearable.CapabilityApi.removeCapabilityListener(mGoogleApiClient, WearDataSender.this, mRemoteCapabilityName);
//            }
            mGoogleApiClient.disconnect();
        }
        mGoogleApiClientUseCount = 0;
        mConnectFinishTime = 0;
    }


    public int requestConnect() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            //Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(getLocalNodeCallback);
            mGoogleApiClient.connect(); //see onConnected
        }
        mGoogleApiClientUseCount++;
        if (ACommon.L) Log.i(TAG, "***** requestConnect(), mGoogleApiClientUseCount = " + mGoogleApiClientUseCount);
        return mGoogleApiClientUseCount;
    }

    public int requestConnect(long delayMS) {
        //if (0 == delayMS) delayMS = 10000;
        if (delayMS < 10000) delayMS = 10000;

        long newFinish = System.currentTimeMillis() + delayMS;
        if (newFinish > mConnectFinishTime) {
            mConnectFinishTime = newFinish;
            if (ACommon.L) Log.i(TAG, "***** requestConnect( " + delayMS + " )");
            requestConnect();
            disconnectDelayed(delayMS);
        } else {
            if (ACommon.L) Log.i(TAG, "*___* requestConnect( " + (mConnectFinishTime - System.currentTimeMillis()) + " )");
        }

        return mGoogleApiClientUseCount;
    }


    public void disconnectDelayed(long delayMS) {
        if (ACommon.L) Log.i(TAG, "***** disconnectDelayed( " + delayMS + " )");
        mDelayedTasksHandler.removeCallbacks(taskDisconnectSender);
        if (delayMS == 0) {
            disconnectUnconditionally();
        } else {
            mDelayedTasksHandler.postDelayed(taskDisconnectSender, delayMS);
            if (ACommon.L) Log.i(TAG, "***** disconnectDelayed post taskDisconnectSender delayed by " + delayMS + "ms");
        }
    }

    Runnable taskDisconnectSender = new Runnable() {
        @Override
        public void run() {
            if (ACommon.L) Log.i(TAG, "***** taskDisconnectSender enters run()");
            disconnectUnconditionally();
        }
    };

    public long wakeUpScreen(long delayMS, Context context) {
        long result;

        if (ACommon.L) Log.i(TAG, "***** wakeUpScreen( " + delayMS + " )");
        result = mBuzzer.wakeUpScreen(delayMS, context);
        requestConnect(delayMS);

        return result;
    }












    @Override //GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle bundle) {
        //Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(getConnectedNodesCallback);
//        if (null != mRemoteCapabilityName) {
//            Wearable.CapabilityApi.addCapabilityListener(mGoogleApiClient, WearDataSender.this, mRemoteCapabilityName).await();
//        }

        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(getLocalNodeCallback);
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(getConnectedNodesCallback);


        if (null != mOnWearEventCallback) mOnWearEventCallback.onWearConnResult(WEAR_CONN_CONNECTED);
    }


    @Override //GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // todo: see here: https://developers.google.com/android/guides/permissions
        if (null != mOnWearEventCallback) mOnWearEventCallback.onWearConnResult(WEAR_CONN_FILED);
    }


    @Override //GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int i) {
        if (null != mOnWearEventCallback) mOnWearEventCallback.onWearConnResult(WEAR_CONN_SUSPENDED);
    }


    Node                    mLocalNode;
    List<Node>              mConnectedNodes;
    String                  mLocalPeerId = null;
    int                     mNumConnectedNodes = 0;


    ResultCallback<NodeApi.GetLocalNodeResult> getLocalNodeCallback = new ResultCallback<NodeApi.GetLocalNodeResult>() {
        @Override
        public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
            mLocalNode = getLocalNodeResult.getNode();
            mLocalPeerId = mLocalNode.getId();
            if (ACommon.L) Log.i(TAG, "getLocalNodeCallback(): mLocalPeerId = " + mLocalPeerId);
            if (null != mOnWearEventCallback) mOnWearEventCallback.rememberLocalPeerId(mLocalPeerId);
        }
    };


    ResultCallback<NodeApi.GetConnectedNodesResult> getConnectedNodesCallback = new ResultCallback<NodeApi.GetConnectedNodesResult>() {
        @Override
        public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
            mConnectedNodes = getConnectedNodesResult.getNodes();
            mNumConnectedNodes = mConnectedNodes.size();
            if (ACommon.L) Log.i(TAG, "getConnectedNodesCallback(): mNumConnectedNodes = " + mNumConnectedNodes);
//            //List<Node> nodeList = getConnectedNodesResult.getNodes();
//            //int numNodes = nodeList.size();
//            int count = 0;
//            for (Node node : mConnectedNodes) {
//                count++;
//                String peerId = node.getId();
//                //Log.i(TAG, "((( Remote peer id["+ count + "]: " + peerId + ", " + node.getDisplayName());
//            }
        }
    };


    @Override //NodeApi.NodeListener
    public void onPeerConnected(Node node) {
        mConnectedNodes.add(node);
        mNumConnectedNodes = mConnectedNodes.size();
        if (ACommon.L) Log.i(TAG, "onPeerConnected(): mNumConnectedNodes = " + mNumConnectedNodes);
    }


    @Override //NodeApi.NodeListener
    public void onPeerDisconnected(Node node) {
        //mConnectedNodes.
        int location = mConnectedNodes.indexOf(node);
        mConnectedNodes.remove(location);
        mNumConnectedNodes = mConnectedNodes.size();
        if (ACommon.L) Log.i(TAG, "onPeerDisconnected(): mNumConnectedNodes = " + mNumConnectedNodes);
    }


    public int getNumNodesConnected() {
        return mNumConnectedNodes;
    }


//    public static int getNumNodesConnected(GoogleApiClient googleApiClient, int timeOut) {
//        int result = -1;
//        NodeApi.GetConnectedNodesResult nodes;
//        if (0 == timeOut) {
//            nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
//        } else {
//            nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await(timeOut, TimeUnit.SECONDS);
//        }
//        List<Node> nodeList;
//        if (nodes != null) {
//            nodeList = nodes.getNodes();
//            if (nodeList != null) result = nodeList.size();
//        }
//        return result;
//    }






    public void addLocalCapability(String capabilityName) {
        if (ACommon.L) Log.i(TAG, "addLocalCapability( "+capabilityName+" )");
        final String localCapabilityName = capabilityName;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mGoogleApiClient != null) {
                    CapabilityApi.AddLocalCapabilityResult result =
                            Wearable.CapabilityApi.addLocalCapability(mGoogleApiClient, localCapabilityName).await(10, TimeUnit.SECONDS);
                }
            }
        }).start();
    }

    public void removeLocalCapability(String capabilityName) {
        if (ACommon.L) Log.i(TAG, "removeLocalCapability( "+capabilityName+" )");
        final String localCapabilityName = capabilityName;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mGoogleApiClient != null) {
                    CapabilityApi.RemoveLocalCapabilityResult result =
                            Wearable.CapabilityApi.removeLocalCapability(mGoogleApiClient, localCapabilityName).await(10, TimeUnit.SECONDS);
                }
            }
        }).start();
    }

//    @Override //CapabilityApi.CapabilityListener
//    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
//        //CapabilityInfo capabilityInfo = result.getCapability();
//        Set<Node> connectedNodes = capabilityInfo.getNodes();
//        for (Node node : connectedNodes) {
//            if (node.isNearby()) {
//                //return node.getId();
//                if (ACommon.L) Log.i(TAG, "Capability " + mRemoteCapabilityName + " nodeId = " + node.getId());
//            }
//            //bestNodeId = node.getId();
//        }
//    }













//    volatile static long            mCounter = 0;
//    static final Object             mLockCounter = new Object();



    public static class WearNetSend extends Thread {
        String              path, wearPeer;
        DataMap             dataMap;
        GoogleApiClient     mGoogleApiClient;
        long                counter;
        boolean             mSendHard;

        // Constructor for sending data objects to the data layer
        public WearNetSend(String p, DataMap data, GoogleApiClient googleApiClient, String wearPeer, long serialSeq, boolean hardSend) {
            mGoogleApiClient = googleApiClient;
            if (null != mGoogleApiClient) {
                path = p;
                dataMap = data;
                this.wearPeer = wearPeer;
//                synchronized (mLockCounter) {
//                    mCounter++;
//                    counter = mCounter;
//                }
                counter = serialSeq; //Utils.getSerialSeq();
                mSendHard = hardSend;
            }
        }

        public void run() {
            if (null == mGoogleApiClient) {
                if (ACommon.L) Log.i(TAG, "!!! WearNetSend: mGoogleApiClient is null; abort.");
                return;
            }
            Utils.waitConnected(mGoogleApiClient, 10);
            if (!mGoogleApiClient.isConnected()) {
                if (ACommon.L) Log.i(TAG, "!!! WearNetSend: mGoogleApiClient NOT CONNECTED; abort.");
                return;
            }

            int numNodes = 0;
            numNodes = Utils.getNumNodesConnected(mGoogleApiClient, 30);
//            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//            //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//            List<Node> nodeList;
//            if (nodes != null) {
//                nodeList = nodes.getNodes();
//                if (nodeList != null) numNodes = nodeList.size();
//            }
            if (numNodes > 0) {
                if (0 != counter) {
                    dataMap.putLong(KEY_SERIAL_SEQUENCE, counter);
                }

                if (null != wearPeer) {
                    dataMap.putString(KEY_TOPEER, wearPeer);
                }
                //Log.i(TAG, "((( WearNetSend dataMap=" + dataMap + ", path=" + path);
                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
                putDMR.getDataMap().putAll(dataMap);
                PutDataRequest request = putDMR.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();

                //dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WEARCFG_TOGGLE_LAYOUT);
//                int event = dataMap.getInt(ACommon.KEY_EVENT);
//                if (ACommon.EVT_WEARCFG_TOGGLE_LAYOUT == event) {
//                    boolean success = result.getStatus().isSuccess();
//                    //Log.i(TAG, "#TOGGLE_LAYOUT success=" + success + ", serialSeq=" + counter); //dataMap.getLong(ACommon.KEY_SERIAL_SEQUENCE)
//                }
            } else {
                if (ACommon.L) Log.i(TAG, "!!! WearNetSend: NO connected nodes, numNodes=" + numNodes);
            }
        }
    } // class WearNetSend




    public static class TaskWearNetSend
            implements
            Runnable,
            OnWearEvent
    {
        String              path, wearPeer;
        DataMap             dataMap;
        long                counter;
        WearDataSender      mWearSender;
        volatile Integer    mConnResult; // = new Integer(ACommon.WEAR_CONN_UNKNOWN);
        boolean             mSendHard;

        public TaskWearNetSend(Context context, String p, DataMap data, String wearPeer, long serialSeq, boolean hardSend) {
            mWearSender = new WearDataSender(context, this, null);
            //mWearSender.requestConnect();
            path = p;
            dataMap = new DataMap();
            dataMap.putAll(data);
            this.wearPeer = wearPeer;
//            synchronized (mLockCounter) {
//                mCounter++;
//                counter = mCounter;
//            }
            counter = serialSeq; //Utils.getSerialSeq();
            mConnResult = new Integer(WEAR_CONN_UNKNOWN);
            mSendHard = hardSend;
            if (ACommon.L) Log.i(TAG, "TaskWearNetSend constructor, mSendHard=" + mSendHard);
        }

        @Override
        public void run() {
            if (ACommon.L) Log.i(TAG, "TaskWearNetSend enter run(), mConnResult=" + mConnResult);
            mWearSender.requestConnect();
            synchronized (mWearSender) {
                try {
                    for (int i=0; i<3; i++) {
                        if (WEAR_CONN_UNKNOWN == mConnResult) {
                            mWearSender.wait(10000);
                        } else break;
                    }
//                    while (ACommon.WEAR_CONN_UNKNOWN == mConnResult) {
//                        mWearSender.wait(10000);
//                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            switch (mConnResult) {
                case WEAR_CONN_UNKNOWN:
                    if (ACommon.L) Log.i(TAG, "!!! TaskWearNetSend timeout; abort.");
                    mWearSender.requestDisconnect();
                    return;
                case WEAR_CONN_FILED:
                    if (ACommon.L) Log.i(TAG, "!!! TaskWearNetSend connection filed; abort.");
                    mWearSender.requestDisconnect();
                    return;
                case WEAR_CONN_SUSPENDED:
                    if (ACommon.L) Log.i(TAG, "!!! TaskWearNetSend connection suspended; abort.");
                    mWearSender.requestDisconnect();
                    return;
            }
            if (ACommon.L) Log.i(TAG, "TaskWearNetSend connected, proceed.");

            int numNodes = 0;
            if (!mSendHard) numNodes = Utils.getNumNodesConnected(mWearSender.getGoogleApiClient(), 30);
//            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mWearSender.getGoogleApiClient()).await();
//            //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//            List<Node> nodeList;
//            if (nodes != null) {
//                nodeList = nodes.getNodes();
//                if (nodeList != null) numNodes = nodeList.size();
//            }
            if (mSendHard || numNodes > 0) {
                if (0 != counter) {
                    dataMap.putLong(KEY_SERIAL_SEQUENCE, counter);
                }

                if (null != wearPeer) {
                    dataMap.putString(KEY_TOPEER, wearPeer);
                }
                //Log.i(TAG, "((( WearNetSend dataMap=" + dataMap + ", path=" + path);
                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
                putDMR.getDataMap().putAll(dataMap);
                PutDataRequest request = putDMR.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mWearSender.getGoogleApiClient(), request).await();
                if (ACommon.L) Log.i(TAG, "TaskWearNetSend result=" + result.getStatus().isSuccess());

                //dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WEARCFG_TOGGLE_LAYOUT);
//                int event = dataMap.getInt(ACommon.KEY_EVENT);
//                if (ACommon.EVT_WEARCFG_TOGGLE_LAYOUT == event) {
//                    boolean success = result.getStatus().isSuccess();
//                    //Log.i(TAG, "#TOGGLE_LAYOUT success=" + success + ", serialSeq=" + counter); //dataMap.getLong(ACommon.KEY_SERIAL_SEQUENCE)
//                }
            }
            mWearSender.requestDisconnect();
        }


        @Override
        public void onWearConnResult(int i) {
            if (ACommon.L) Log.i(TAG, "TaskWearNetSend enter onWearConnResult(), i=" + i + ", mConnResult=" + mConnResult);
            synchronized (mWearSender) {
                mConnResult = i;
                //if (ACommon.WEAR_CONN_CONNECTED != mConnResult) mWearSender.requestDisconnect();
                mWearSender.notify();
            }
        }


        @Override
        public boolean isPeerIdKnown() {
            return false;
        }


        @Override
        public void rememberPeerId(String peerId) {

        }


        @Override
        public boolean isLocalPeerIdKnown() {
            return false;
        }


        @Override
        public void rememberLocalPeerId(String peerId) {

        }

    }




    public static class TaskWearMessageSend
        implements
            Runnable
    {

        GoogleApiClient     mGoogleApiClient;
        byte[]              mMessage;
        String              mToNodeId;
        String              mUriPath;
        //long                counter;

        public TaskWearMessageSend(GoogleApiClient googleApiClient, Bundle messageBundle, String toNodeId, String uriPath) {
            mGoogleApiClient = googleApiClient;
//            synchronized (mLockCounter) {
//                mCounter++;
//                counter = mCounter;
//            }
            if (null != messageBundle) {
                messageBundle.putLong(KEY_SERIAL_SEQUENCE, Utils.getSerialSeq());
                mMessage = Utils.bundleToBytes(messageBundle);
            }
            mToNodeId = toNodeId;
            mUriPath = uriPath;
        }

        @Override
        public void run() {
            if (ACommon.L) Log.i(TAG, "TaskWearMessageSend enter run().");
            if (null==mGoogleApiClient || null==mMessage || null==mToNodeId || null==mUriPath) {
                if (ACommon.L) Log.i(TAG, "!!! TaskWearMessageSend FAILED, message=" + mMessage + ", toNode=" + mToNodeId + ", path=" + mUriPath);
                return;
            }

            if (!Utils.waitConnected(mGoogleApiClient, 10)) {
                if (ACommon.L) Log.i(TAG, "!!! TaskWearMessageSend: Connection to GoogleApiClient FILED.");
                return;
            }

            MessageApi.SendMessageResult result =
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, mToNodeId, mUriPath, mMessage).await();
            if (!result.getStatus().isSuccess()) {
                if (ACommon.L) Log.i(TAG, "!!! TaskWearMessageSend: Send message FILED.");
            }
        }
    }

}
