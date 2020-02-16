package com.luna_78.wear.watch.face.lightandclassics;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;
import com.luna_78.wear.watch.face.lightandclassics.common.OnWearEvent;
import com.luna_78.wear.watch.face.lightandclassics.common.Utils;
import com.luna_78.wear.watch.face.lightandclassics.common.WearDataSender;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Created by buba on 01/11/15.
 */
public class WearCrashSend
    implements
        ReportSender
//        OnWearEvent
{

    private static final boolean        L       = ACommon.L;
    private static final String         TAG     = ACommon.TAG_PREFIX + "WEAR_CRASH_SEND";

    private static final String LINE_SEPARATOR = "\n";



    @Override
    public void send(Context context, CrashReportData errorContent) throws ReportSenderException {

        if (ACommon.L) Log.i(TAG, "WearCrashSend send(), errorContent = " + (errorContent != null));

        // loop file list
        //new WearSendCrashReports(context, mWearSender.getGoogleApiClient(), null).start();
        //todo: mToPeer не должен быть null, иначе listener вторых часов может удалить этот отчёт
        (new Thread(new WearSendAllCrashReports(context, errorContent, null, Utils.getSerialSeq()))).start();

    } // send


    private static void byteArrayToFile(Context context, byte[] bytes) {
        if (null == bytes) return;
        long timeStamp = System.currentTimeMillis();

        String fileName = String.valueOf(timeStamp) + ACommon.WEAR_CRASHREPORT_FILE_SUFFIX;
        // example: 1436370239280-acra
        //Log.i(TAG, "((( crash report, length=" + bytes.length + ", file=" + fileName);

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        File file = new File(context.getFilesDir(), fileName);
        try {
            Utils.copyFile(bis, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // byteArrayToFile


    private static byte[] reportToByteArray(CrashReportData errorContent) {
        byte[] result = null;

        if (null == errorContent) return result;

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //bos = new ByteArrayOutputStream();
        try {
            final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
            final OutputStreamWriter writer = new OutputStreamWriter(zos, "ISO8859_1"); //$NON-NLS-1$
            final StringBuilder buffer = new StringBuilder(200);

            for (final Map.Entry<ReportField, String> entry : errorContent.entrySet()) {
                final String key = entry.getKey().toString();
                dumpString(buffer, key, true);
                buffer.append('=');
                dumpString(buffer, entry.getValue(), false);
                buffer.append("\n");
                writer.write(buffer.toString());
                buffer.setLength(0);
                //zos.write(parcel.marshall());
            }

            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        result = bos.toByteArray();
        //String base64 = Base64.encodeToString(bos.toByteArray(), 0);

        return result;
    } // reportToByteArray








    /**
     * from ACRA, class CrashReportPersister !
     * Stores the mappings in this Properties to the specified OutputStream,
     * putting the specified comment at the beginning. The output from this
     * method is suitable for being read by the load() method.
     *
     * @param crashData    CrashReportData to save.
     * @param fileName      Name of the file to which to store the CrashReportData.
     * @throws java.io.IOException if the CrashReportData could not be written to the OutputStream.
     */
    private void store(Context context, CrashReportData crashData, String fileName) throws IOException {

        final OutputStream out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        try {
            final StringBuilder buffer = new StringBuilder(200);
            final OutputStreamWriter writer = new OutputStreamWriter(out, "ISO8859_1"); //$NON-NLS-1$

            for (final Map.Entry<ReportField, String> entry : crashData.entrySet()) {
                final String key = entry.getKey().toString();
                dumpString(buffer, key, true);
                buffer.append('=');
                dumpString(buffer, entry.getValue(), false);
                buffer.append(LINE_SEPARATOR);
                writer.write(buffer.toString());
                buffer.setLength(0);
            }
            writer.flush();
        } finally {
            out.close();
        }
    } // store()


    /**
     * from ACRA, class CrashReportPersister !
     * Constructs a new {@code Properties} object.
     *
     * @param buffer    StringBuilder to populate with the supplied property.
     * @param string    String to append to the buffer.
     * @param key       Whether the String is a key value or not.
     */
    private static void dumpString(StringBuilder buffer, String string, boolean key) {
        int i = 0;
        if (!key && i < string.length() && string.charAt(i) == ' ') {
            buffer.append("\\ "); //$NON-NLS-1$
            i++;
        }

        for (; i < string.length(); i++) {
            char ch = string.charAt(i);
            switch (ch) {
                case '\t':
                    buffer.append("\\t"); //$NON-NLS-1$
                    break;
                case '\n':
                    buffer.append("\\n"); //$NON-NLS-1$
                    break;
                case '\f':
                    buffer.append("\\f"); //$NON-NLS-1$
                    break;
                case '\r':
                    buffer.append("\\r"); //$NON-NLS-1$
                    break;
                default:
                    if ("\\#!=:".indexOf(ch) >= 0 || (key && ch == ' ')) {
                        buffer.append('\\');
                    }
                    if (ch >= ' ' && ch <= '~') {
                        buffer.append(ch);
                    } else {
                        final String hex = Integer.toHexString(ch);
                        buffer.append("\\u"); //$NON-NLS-1$
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            buffer.append("0"); //$NON-NLS-1$
                        }
                        buffer.append(hex);
                    }
            }
        }
    } // dumpString





    private static final Object mLockCrashFiles = new Object();

    //private volatile long mSerialSeqCounter = 0;


    public static class WearSendAllCrashReports
        implements
            Runnable,
            OnWearEvent
    {
        //String path = ACommon.WEAR_CRASH_PATH;
        //DataMap dataMap;
        String mToPeer;
        //GoogleApiClient mGoogleApiClient;
        Context mContext;

        final WearDataSender        mWearSender;
        volatile Integer            mConnResult;
        CrashReportData             mErrorContent;
        long mSerialSeq;



        // Constructor for sending data objects to the data layer
        public WearSendAllCrashReports(Context context, CrashReportData errorContent, String toPeer, long serialSeq) {
            //path = p;
            //dataMap = data;
            //mGoogleApiClient = googleApiClient;
            mToPeer = toPeer;
            mContext = context;
            mWearSender = new WearDataSender(context, this, null);
            mErrorContent = errorContent;
            mSerialSeq = serialSeq;
            if (ACommon.L) Log.i(TAG, "WearSendAllCrashReports() CONSTRUCTOR, errorContent = " + (errorContent != null));
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


        @Override
        public void onWearConnResult(int i) {
            //if (L) Log.i(TAG, "WearSendAllCrashReports enter onWearConnResult(), i=" + i + ", mConnResult=" + mConnResult);
            synchronized (mWearSender) {
                mConnResult = i;
                //if (ACommon.WEAR_CONN_CONNECTED != mConnResult) mWearSender.requestDisconnect();
                mWearSender.notify();
            }
        }


        public void run() {

            if (ACommon.L) Log.i(TAG, "WearSendAllCrashReports enter run()");

            mConnResult = new Integer(WearDataSender.WEAR_CONN_UNKNOWN);
            mWearSender.requestConnect();


            // crash report -> byte array
            // byte array -> file
            if (null != mErrorContent) {
                synchronized (mLockCrashFiles) {
                    byteArrayToFile(mContext, reportToByteArray(mErrorContent));
                }
            }

            synchronized (mWearSender) {
                try {
                    for(int i=0; i<3; i++) {
                        if (WearDataSender.WEAR_CONN_UNKNOWN == mConnResult) {
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
                case WearDataSender.WEAR_CONN_UNKNOWN:
                    if (ACommon.L) Log.i(TAG, "TID-"+android.os.Process.myTid()+", WearSendAllCrashReports connection timeout; abort.");
                    mWearSender.requestDisconnect();
                    return;
                case WearDataSender.WEAR_CONN_FILED:
                    if (ACommon.L) Log.i(TAG, "TID-"+android.os.Process.myTid()+", WearSendAllCrashReports connection filed; abort.");
                    mWearSender.requestDisconnect();
                    return;
                case WearDataSender.WEAR_CONN_SUSPENDED:
                    if (ACommon.L) Log.i(TAG, "TID-"+android.os.Process.myTid()+", WearSendAllCrashReports connection suspended; abort.");
                    // todo: а может продолжать?
                    mWearSender.requestDisconnect();
                    return;
                default: break;
            }
            if (ACommon.L) Log.i(TAG, "WearSendAllCrashReports connected, proceed.");

//            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mWearSender.getGoogleApiClient()).await();
//            List<Node> nodeList;
//            int numNodes = 0;
//            if (nodes != null) {
//                nodeList = nodes.getNodes();
//                if (nodeList != null) numNodes = nodeList.size();
//            }
//            if (0 == numNodes) {
//                mWearSender.requestDisconnect();
//                return;
//            }

            String[] files = mContext.fileList();
            int count=0;
            boolean sended;
            synchronized (mLockCrashFiles) {
                for (String fileName : files) {
                    sended = false;
                    File file = mContext.getFileStreamPath(fileName);

                    if (ACommon.L) Log.i(TAG, "--- WearSendAllCrashReports, file = " + fileName);

                    if (fileName.matches("^[0-9]+"+ ACommon.WEAR_CRASHREPORT_FILE_SUFFIX)) {
                        count++;
                        //Log.i(TAG, "((( WearSendCrashReports, file["+count+"] = " + fileName + ", size=" + file.length());
                        byte[] fileContent = new byte[(int) file.length()];
                        DataInputStream dis;

                        try {
                            dis = new DataInputStream(new FileInputStream(file));
                            dis.readFully(fileContent);
                            dis.close();
                            sended = sendCrashReportContent(fileContent, fileName);
                        } catch (IOException e) {
                            if (ACommon.L) Log.i(TAG, "!!! WearSendAllCrashReports IOException, file = " + fileName);
                            e.printStackTrace();
                        }

                        int numNodes;
                        numNodes = Utils.getNumNodesConnected(mWearSender.getGoogleApiClient(), 30);
                        //numNodes = mWearSender.getNumNodesConnected();
                        //if (L) Log.i(TAG, "WearSendAllCrashReports: numNodesStatic=" + numNodesStatic + "; numNodesEvt=" + numNodesEvt);

                        if (sended && numNodes > 0) mContext.deleteFile(fileName);
                        if (ACommon.L) Log.i(TAG, "WearSendAllCrashReports report send result = " + sended + "; numNodes = " + numNodes);
                    }
                }
            }

            mWearSender.requestDisconnect();
            if (ACommon.L) Log.i(TAG, "WearSendAllCrashReports leave run()");
        } // run


        private boolean sendCrashReportContent(byte[] content, String fileName) {
            DataMap dataMap = new DataMap();
            long crashTimeStamp;
            //String s = fileName.substring(0, fileName.lastIndexOf("-acra"));

            crashTimeStamp = Long.parseLong(fileName.substring(0, fileName.indexOf(ACommon.WEAR_CRASHREPORT_FILE_SUFFIX)));

            if (null != mToPeer) dataMap.putString(WearDataSender.KEY_TOPEER, mToPeer);
            dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WEAR_CRASHREPORT);
            dataMap.putLong(ACommon.KEY_CRASHREPORT_TIME, crashTimeStamp);
            dataMap.putByteArray(ACommon.KEY_CRASHREPORT_CONTENT, content);
            dataMap.putLong(WearDataSender.KEY_SERIAL_SEQUENCE, mSerialSeq);

            PutDataMapRequest putDMR = PutDataMapRequest.create(ACommon.WEAR_CRASH_PATH);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            //DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mWearSender.getGoogleApiClient(), request).await();
            if(result.getStatus().isSuccess()) return true;
            else return false;
        } // sendFileContent

    }

}
