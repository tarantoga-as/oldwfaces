package com.luna_78.wear.watch.face.raf3078;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.luna_78.wear.watch.face.raf3078.common.ACommon;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by buba on 07/07/15.
 */
public class WearCrashReport {
    private static final String TAG = "WCR";

    private static final String LINE_SEPARATOR = "\n";

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
    public void store(Context context, CrashReportData crashData, String fileName) throws IOException {

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
    public static void dumpString(StringBuilder buffer, String string, boolean key) {
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


    public static class WearSendCrashReports extends Thread {
        //String path = ACommon.WEAR_CRASH_PATH;
        //DataMap dataMap;
        String wearPeer;
        GoogleApiClient mGoogleApiClient;
        Context mContext;

        // Constructor for sending data objects to the data layer
        public WearSendCrashReports(Context context, GoogleApiClient googleApiClient, String wearPeer) { // String p, DataMap data,
            //path = p;
            //dataMap = data;
            mGoogleApiClient = googleApiClient;
            this.wearPeer = wearPeer;
            mContext = context;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            List<Node> nodeList;
            int numNodes = 0;
            if (nodes != null) {
                nodeList = nodes.getNodes();
                if (nodeList != null) numNodes = nodeList.size();
            }
            if (0 == numNodes) return;

//            if (null != wearPeer) dataMap.putString(ACommon.KEY_TOPEER, wearPeer);
//            Log.i(TAG, "((( WearSendCrashReports dataMap=" + dataMap + ", path=" + path);
//            PutDataMapRequest putDMR = PutDataMapRequest.create(path);
//            putDMR.getDataMap().putAll(dataMap);
//            PutDataRequest request = putDMR.asPutDataRequest();
//            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();


            String[] files = mContext.fileList();
            int count=0;
            for (String fileName : files) {
                File file = mContext.getFileStreamPath(fileName);

                if (fileName.matches("^[0-9]+"+ ACommon.WEAR_CRASHREPORT_FILE_SUFFIX)) {
                    count++;
                    //Log.i(TAG, "((( WearSendCrashReports, file["+count+"] = " + fileName + ", size=" + file.length());
//                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//                    File fileOut = new File(path, fileName);
//                    Log.i(TAG, "((((( listLocalFiles, file=" + path + "/" + fileName);
//                    //adb -s 0a3d818c pull /sdcard/Pictures/1436193606000-approved.stacktrace
//                    //adb -s 0a3d818c pull /sdcard/Pictures/ACRA-INSTALLATION
//                    try {
//                        ACommon.copyFile(file, fileOut);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    byte[] fileContent = new byte[(int) file.length()];
                    DataInputStream dis;

                    try {
                        dis = new DataInputStream(new FileInputStream(file));
                        dis.readFully(fileContent);
                        dis.close();
                        sendCrashReportContent(fileContent, fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mContext.deleteFile(fileName);
                }
            }
        } // run


        private void sendCrashReportContent(byte[] content, String fileName) {
            DataMap dataMap = new DataMap();
            long crashTimeStamp;
            //String s = fileName.substring(0, fileName.lastIndexOf("-acra"));

            crashTimeStamp = Long.parseLong(fileName.substring(0, fileName.indexOf(ACommon.WEAR_CRASHREPORT_FILE_SUFFIX)));

            if (null != wearPeer) dataMap.putString(ACommon.KEY_TOPEER, wearPeer);
            dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
            dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WEAR_CRASHREPORT);
            dataMap.putLong(ACommon.KEY_CRASHREPORT_TIME, crashTimeStamp);
            dataMap.putByteArray(ACommon.KEY_CRASHREPORT_CONTENT, content);

            PutDataMapRequest putDMR = PutDataMapRequest.create(ACommon.WEAR_CRASH_PATH);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            //DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
        } // sendFileContent

    } // class WearSendCrashReports

} // class WearCrashReport
