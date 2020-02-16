package com.luna_78.wear.watch.face.lightandclassics.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by buba on 29/10/15.
 */
public class Utils {

    //private static final boolean L = ACommon.L;
    private static final String TAG = ACommon.TAG_PREFIX + "UTILS";





    public static boolean isEqual(String peer1, String peer2) {
        return (null!=peer2 && null!=peer1 && peer2.equals(peer1));
    }


    public static boolean isNotEqual(String peer1, String peer2) {
        return (null!=peer2 && null!=peer1 && !peer2.equals(peer1));
    }


    public static void copyFile(InputStream sourceInputStream, File destination) throws IOException {
        FileOutputStream destinationOutputStream = new FileOutputStream(destination);
        byte[] buf = new byte[1024];
        int len;

        while ((len = sourceInputStream.read(buf)) > 0) {
            destinationOutputStream.write(buf, 0, len);
        }

        sourceInputStream.close();
        destinationOutputStream.close();
    }




    // https://gist.github.com/aprock/2037883
    public static String bundleToBase64String(final Bundle bundle) {
        //Log.i(TAG, "*** SERIALIZE BUNDLE");
        String base64 = null;
        final Parcel parcel = Parcel.obtain();
        try {
            parcel.writeBundle(bundle);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
            zos.write(parcel.marshall());
            zos.close();
            base64 = Base64.encodeToString(bos.toByteArray(), 0);
        } catch(IOException ioe) {
            ioe.printStackTrace();
            base64 = null;
        } finally {
            parcel.recycle();
        }
        return base64;
    }

    // https://gist.github.com/aprock/2037883
    public static Bundle base64StringToBundle(final String base64) {
        //Log.i(TAG, "*** DESERIALIZE BUNDLE");
        Bundle bundle = null;
        final Parcel parcel = Parcel.obtain();
        try {
            final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            final GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(base64, 0)));
            int len = 0;
            while ((len = zis.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            zis.close();
            parcel.unmarshall(byteBuffer.toByteArray(), 0, byteBuffer.size());
            parcel.setDataPosition(0);
            bundle = parcel.readBundle();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            bundle = null;
        }  finally {
            parcel.recycle();
        }
        return bundle;
    }


    public static byte[] bundleToBytes(final Bundle bundle) {
        byte[] bytes = null;
        final Parcel parcel = Parcel.obtain();
        try {
            parcel.writeBundle(bundle);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
            zos.write(parcel.marshall());
            zos.close();
            bytes = bos.toByteArray(); //Base64.encodeToString(bos.toByteArray(), 0);
        } catch(IOException ioe) {
            ioe.printStackTrace();
            bytes = null;
        } finally {
            parcel.recycle();
        }
        return bytes;
    }
    public static Bundle bytesToBundle(final byte[] bytes) {
        Bundle bundle = null;
        final Parcel parcel = Parcel.obtain();
        try {
            final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            final GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(bytes)); //Base64.decode(base64, 0)
            int len = 0;
            while ((len = zis.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            zis.close();
            parcel.unmarshall(byteBuffer.toByteArray(), 0, byteBuffer.size());
            parcel.setDataPosition(0);
            bundle = parcel.readBundle();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            bundle = null;
        }  finally {
            parcel.recycle();
        }
        return bundle;
    }









    public static boolean waitConnected(GoogleApiClient googleApiClient, int waitSeconds) {
        ConnectionResult connectionResult;

        if (null == googleApiClient) return false;

        if (0 != waitSeconds) {
            connectionResult = googleApiClient.blockingConnect(waitSeconds, TimeUnit.SECONDS);
        } else {
            connectionResult = googleApiClient.blockingConnect();
        }

        if (!connectionResult.isSuccess()) {
            return false;
        } else {
            return true;
        }
    }


    public static int getNumNodesConnected(GoogleApiClient googleApiClient, int timeOut) {
        int result = -1;
        NodeApi.GetConnectedNodesResult nodes;
        if (0 == timeOut) {
            nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        } else {
            nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await(timeOut, TimeUnit.SECONDS);
        }
        List<Node> nodeList;
        if (nodes != null) {
            nodeList = nodes.getNodes();
            if (nodeList != null) result = nodeList.size();
        }
        return result;
    }


    public static String findLocalPeerId(GoogleApiClient gApiClient, int timeOut) {
        if (!Utils.waitConnected(gApiClient, timeOut)) return null;
        NodeApi.GetLocalNodeResult result = Wearable.NodeApi.getLocalNode(gApiClient).await(timeOut, TimeUnit.SECONDS);
        if (result.getStatus().isSuccess()) {
            return result.getNode().getId();
        }
        return null;
    }


    public static String findCapabilityNodeId(String capabilityNameToFind, CapabilityInfo capabilityInfo) {
        if (null==capabilityNameToFind || null==capabilityInfo) return null;

        String capabilityName = capabilityInfo.getName();
        if (!capabilityName.equals(capabilityNameToFind)) return null;
        Set<Node> connectedNodes = capabilityInfo.getNodes();

        String bestNodeId = null;
        for (Node node : connectedNodes) {
            if (node.isNearby()) {
                bestNodeId = node.getId();
                break;
            }
            bestNodeId = node.getId();
        }
//        if (null != bestNodeId) {
//            return bestNodeId;
//        }

        return bestNodeId;
    }


    // CapabilityApi.FILTER_REACHABLE
    public static String findCapabilityNodeId(String capabilityNameToFind, GoogleApiClient gApiClient, int timeOut, int capabilityApiFilter) {
        if (!Utils.waitConnected(gApiClient, timeOut)) return null;
        if (null == capabilityNameToFind) return null; //  || null == googleApiClient

        String bestNodeId = null;

        CapabilityApi.GetCapabilityResult result =
                Wearable.CapabilityApi.getCapability(
                        gApiClient, capabilityNameToFind, capabilityApiFilter).await(timeOut, TimeUnit.SECONDS);

        if (result.getStatus().isSuccess()) {
            CapabilityInfo capabilityInfo = result.getCapability();
            Set<Node> connectedNodes = capabilityInfo.getNodes();
            for (Node node : connectedNodes) {
                if (node.isNearby()) {
                    return node.getId();
                    //if (L) Log.i(TAG, "SERVICE nodeId = " + node.getId());
                }
                bestNodeId = node.getId();
            }
        }
        return bestNodeId;

//        if (null != capabilityName) {
//            CapabilityApi.GetCapabilityResult result =
//                    Wearable.CapabilityApi.getCapability(
//                            googleApiClient, capabilityName, CapabilityApi.FILTER_REACHABLE).await();
//            if (result.getStatus().isSuccess()) {
//                CapabilityInfo capabilityInfo = result.getCapability();
//                Set<Node> connectedNodes = capabilityInfo.getNodes();
//                for (Node node : connectedNodes) {
//                    if (node.isNearby()) {
//                        return node.getId();
//                        //if (L) Log.i(TAG, "SERVICE nodeId = " + node.getId());
//                    }
//                    bestNodeId = node.getId();
//                }
//            }
//            return bestNodeId;
//        } else {
//            CapabilityApi.GetAllCapabilitiesResult result =
//                    Wearable.CapabilityApi.getAllCapabilities(
//                            googleApiClient, CapabilityApi.FILTER_REACHABLE).await();
//            //Map<String, CapabilityInfo> getAllCapabilities ()
//            if (result.getStatus().isSuccess()) {
//                Map<String, CapabilityInfo> capInfoMap = result.getAllCapabilities();
//                Set<String> capNames = capInfoMap.keySet();
//                for (String capName : capNames) {
//                    CapabilityInfo capabilityInfo = capInfoMap.get(capName);
//                    Set<Node> connectedNodes = capabilityInfo.getNodes();
//                    for (Node node : connectedNodes) {
//                        if (node.isNearby()) {
//                            return node.getId();
//                        }
//                        bestNodeId = node.getId();
//                        //if (L) Log.i(TAG, "AllCapability " + capName + " nodeId = " + node.getId());
//                    }
//                }
//            }
//            return bestNodeId;
//        }
//        //return null;
    }


    // sendHandshakeMsg(nodeId, ACommon.EVT_SERVICE_HERE, ACommon.HANDSHAKE_REQUEST, gApiClient)
    public static void sendHandshakeMsg(String toNodeId, int handshakeEvent, int handshakeAction, GoogleApiClient gApiClient) {
        Bundle bundle = new Bundle();
        bundle.putInt(ACommon.KEY_EVENT, handshakeEvent); //ACommon.EVT_SERVICE_HERE
        bundle.putInt(ACommon.KEY_HANDSHAKE_ACTION, handshakeAction);
        //if (L) Log.i(TAG, "sendHandshakeMsg(), toNode=" + toNodeId + ", bundle=" + bundle);
        (new Thread(new WearDataSender.TaskWearMessageSend(gApiClient, bundle, toNodeId, ACommon.MESSAGE_PATH))).start();
    }


    public static void sendListenerMsg(String toNodeId, int listenerEvent, int listenerAction, GoogleApiClient gApiClient) {
        Bundle bundle = new Bundle();
        bundle.putInt(ACommon.KEY_EVENT, listenerEvent);
        bundle.putInt(ACommon.KEY_LISTENER_COMMAND, listenerAction);
        //if (L) Log.i(TAG, "sendHandshakeMsg(), toNode=" + toNodeId + ", bundle=" + bundle);
        (new Thread(new WearDataSender.TaskWearMessageSend(gApiClient, bundle, toNodeId, ACommon.MESSAGE_PATH))).start();
    }





    private volatile static long            mCounter = 0;
    private static final Object             mLockCounter = new Object();
    public static long getSerialSeq() {
        synchronized (mLockCounter) {
            mCounter++;
            return mCounter;
        }
    }





//    dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_LISTENER_CONTROL);
//    dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//    dataMap.putInt(ACommon.KEY_LISTENER_COMMAND, ACommon.LC_IS_WATCHFACE_INSTALLED);
//    new Thread(new WearDataSender.TaskWearNetSend(this, ACommon.HHBATT_TRIGGER_PATH, dataMap, null, Utils.getSerialSeq(), true)).start();
    public static void shareNodeData(Context context, String path, DataMap dataMap) {
        new Thread(new WearDataSender.TaskWearNetSend(context, path, dataMap, null, 0, true)).start();
    }



    public static class SaveWearCrashReport extends Thread {

        long mCrashTime;
        byte[] mCrashContent;
        Context mContext;
        String mUriHost;

        public SaveWearCrashReport(Context context, long crashTime, byte[] reportContent, String uriHost) {
            mCrashTime = crashTime;
            mCrashContent = reportContent;
            mContext = context;
            mUriHost = uriHost;
            if (ACommon.L) Log.i(TAG, "SaveWearCrashReport() constructor, crash time = " + crashTime);
        }

        public void run() {
            //extract content of wear crash report and put it in "stacktrace" file for ACRA later sending
            // example ACRA name: 436289854000-IS_SILENT.stacktrace, 1436289300000-approved.stacktrace
            if (ACommon.L) Log.i(TAG, "SaveWearCrashReport() enter run()");
            final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            final GZIPInputStream zis;
            int len = 0;

            try {
                zis = new GZIPInputStream(new ByteArrayInputStream(mCrashContent));
                while ((len = zis.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                zis.close();

                ByteArrayInputStream bis = new ByteArrayInputStream(byteBuffer.toByteArray());
                String fileName = String.valueOf(mCrashTime) + ACommon.APPROVED_STACKTRACE;
                File file = new File(mContext.getFilesDir(), fileName);
                Utils.copyFile(bis, file);

                informConsumers();

            } catch (IOException e) {
                if (ACommon.L) Log.i(TAG, "!!! SaveWearCrashReport() ERROR, IOException.");
                e.printStackTrace();
            }
        }

        private void informConsumers() {
            DataMap dataMap = new DataMap();
            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WEAR_CRASHREPORT); //ACommon.EVT_WEAR_CRASHREPORT
            if (null != mUriHost) dataMap.putString(ACommon.KEY_URI_HOST, mUriHost);
            dataMap.putString(ACommon.KEY_URI_PATH, ACommon.WEAR_EVENT_PATH); //ACommon.WEAR_CRASH_PATH WEAR_EVENT_PATH

            if (((CommonApplication) mContext.getApplicationContext()).isReceiverForListenerRegistered()) {
                BroadcastFromWearListener.broadcastToConsumers(mContext, dataMap);
            }
        }

    } // class SaveWearCrashReport

}
