package com.luna_78.wear.watch.face.raf3078;

import org.acra.*;
import org.acra.annotation.*;
import org.acra.collector.CrashReportData;
import org.acra.sender.*;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.luna_78.wear.watch.face.raf3078.common.ACommon;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Created by buba on 01/07/15.
 */
@ReportsCrashes(
        //formUri = "",
        mode = ReportingInteractionMode.SILENT
        //httpMethod = HttpSender.Method.PUT,
        //reportType = HttpSender.Type.JSON,
        //formUri = "http://titov.luna-78.com:5984/acra-raf3078/_design/acra-storage/_update/report",
        //formUriBasicAuthLogin = "",
        //formUriBasicAuthPassword = ""
)
public class WearApplication extends Application {

    private static final String TAG = "WRAPP";

    public Object mLockConfigFile = new Object();

    private String mLocalPeerId;
    //
    public String getLocalPeerId() {
        //Log.i(TAG, "((( getLocalPeerId = " + mLocalPeerId);
        return mLocalPeerId;
    }
    public void setLocalPeerId(String peerId) {
        mLocalPeerId = peerId;
        //Log.i(TAG, "((( setLocalPeerId = " + peerId);
    }

    GoogleApiClient mGoogleApiClient;
    //
    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }


    @Override
    public void onCreate() {

        ACRA.init(this);
        WearCrashSender crashSender = new WearCrashSender();
        ACRA.getErrorReporter().setReportSender(crashSender);

        super.onCreate();
        //Log.i(TAG, "((( onCreate");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //Log.i(TAG, "((( onTerminate");
    }




    public class WearCrashSender implements ReportSender {

        //private static String TAG = "";
        //private static final String LINE_SEPARATOR = "\n";

        public WearCrashSender() {

        }


        @Override
        public void send(Context context, CrashReportData errorContent) throws ReportSenderException {

            // crash report -> byte array
            // byte array -> file
            byteArrayToFile(context, reportToByteArray(errorContent));
            // loop file list
            if (null != mGoogleApiClient) new WearCrashReport.WearSendCrashReports(context, mGoogleApiClient, null).start();

        } // send

        private void byteArrayToFile(Context context, byte[] bytes) {
            if (null == bytes) return;
            long timeStamp = System.currentTimeMillis();

            String fileName = String.valueOf(timeStamp) + ACommon.WEAR_CRASHREPORT_FILE_SUFFIX;
            // example: 1436370239280-acra
            //Log.i(TAG, "((( crash report, length=" + bytes.length + ", file=" + fileName);

            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            File file = new File(context.getFilesDir(), fileName);
            try {
                ACommon.copyFile(bis, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } // byteArrayToFile

        private byte[] reportToByteArray(CrashReportData errorContent) {
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
                    WearCrashReport.dumpString(buffer, key, true);
                    buffer.append('=');
                    WearCrashReport.dumpString(buffer, entry.getValue(), false);
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



    } // class WearCrashSender

} // class WearApplication
