package com.luna_78.wear.watch.face.lightandclassics;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;


import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;
import com.luna_78.wear.watch.face.lightandclassics.common.CommonApplication;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.HttpSender;
import org.acra.sender.ReportSenderException;

import java.util.Map;


/**
 * Created by buba on 05/07/15.
 */

@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
//        formUri = "http://titov.luna-78.com/acra/raf3078"
        //formUri = "http://titov.luna-78.com:3078/acra/raf3078"
        formUri = "http://titov.luna-78.com/acra/lacf1144"
//        formUri = "http://titov.luna-78.com:5984/acra-raf3078/_design/acra-storage/_update/report",
//        formUriBasicAuthLogin = "raf3078",
//        formUriBasicAuthPassword = "99IuyTgfE32DfgHjOpuYt54"
)
public class HandheldApplication extends CommonApplication {

    private static final boolean L = ACommon.L;
    private static final String TAG = ACommon.TAG_PREFIX + "PHONE_APP";


    @Override
    public void onCreate() {

        ACRA.init(this);
        ExtendedHttpCrashSender crashSender = new ExtendedHttpCrashSender(HttpSender.Method.PUT, HttpSender.Type.JSON, null);
        ACRA.getErrorReporter().setReportSender(crashSender);

        super.onCreate();

        setWearListenerMode(ACommon.WEAR_LISTENER_SERVER);
        startService(new Intent(this, APhoneService.class));

//        if (L) Log.i(TAG, "onCreate(), LocalPeer=" + getLocalPeerId() + ", Mode=" + getWearListenerMode() +
//                ", ServicePeer=" + getServicePeerId() + ", ServiceCapability=" + getServiceCapability());

        if (ACommon.L) Log.i(TAG, "onCreate(), Local=" + getLocalPeerId() + ", Mode=" + getWearListenerMode()
                        + ", Service=" + getServicePeerId() + ", Capability=" + getServiceCapability()
                        + ", Receiver=" + isReceiverForListenerRegistered()
        );
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
//        if (L) Log.i(TAG, "onTerminate(), LocalPeer=" + getLocalPeerId() + ", Mode=" + getWearListenerMode() +
//                ", ServicePeer=" + getServicePeerId() + ", ServiceCapability=" + getServiceCapability());
        if (ACommon.L) Log.i(TAG, "onTerminate(), Local=" + getLocalPeerId() + ", Mode=" + getWearListenerMode()
                        + ", Service=" + getServicePeerId() + ", Capability=" + getServiceCapability()
                        + ", Receiver=" + isReceiverForListenerRegistered()
        );
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        if (L) Log.i(TAG, "onConfigurationChanged(), LocalPeer=" + getLocalPeerId() + ", Mode=" + getWearListenerMode() +
//                ", ServicePeer=" + getServicePeerId() + ", ServiceCapability=" + getServiceCapability());
        if (ACommon.L) Log.i(TAG, "onConfigurationChanged(), Local=" + getLocalPeerId() + ", Mode=" + getWearListenerMode()
                        + ", Service=" + getServicePeerId() + ", Capability=" + getServiceCapability()
                        + ", Receiver=" + isReceiverForListenerRegistered()
        );
    }



//    class AcraHttpCrashSender implements ReportSender {
//
//        @Override
//        public void send(Context context, CrashReportData errorContent) throws ReportSenderException {
//            //errorContent.
//        }
//    }

    class ExtendedHttpCrashSender extends HttpSender {

        public ExtendedHttpCrashSender(Method method, Type type, Map<ReportField, String> mapping) {
            super(method, type, mapping);
        }

        public ExtendedHttpCrashSender(Method method, Type type, String formUri, Map<ReportField, String> mapping) {
            super(method, type, formUri, mapping);
        }

        @Override
        public void send(Context context, CrashReportData report) throws ReportSenderException {

            String sReport = report.toString();
            if (sReport.contains(ACommon.PENDING_CRASH_REPORT)) {
                //Log.i(TAG, "#CRASH, report=" + report.toString());
                return;
            }

            super.send(context, report);
        }
    }

} // class HandheldApplication
