package com.luna_78.wear.watch.face.lightandclassics;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;
import com.luna_78.wear.watch.face.lightandclassics.common.CommonApplication;
import com.luna_78.wear.watch.face.lightandclassics.common.OnWatchFaceCommand;
import com.luna_78.wear.watch.face.lightandclassics.common.OnWearListenerCommand;
import com.luna_78.wear.watch.face.lightandclassics.common.SynBoolean;
import com.luna_78.wear.watch.face.lightandclassics.common.Utils;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

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
public class WearApplication
        extends CommonApplication
        implements OnWearListenerCommand, OnWatchFaceCommand
{

    private static final boolean L = ACommon.L;
    private static final String TAG = ACommon.TAG_PREFIX + "WEAR_APP";

    //public Object mLockConfigFile = new Object();


    @Override
    public void onCreate() {

        ACRA.init(this);
        //ACRA.getErrorReporter().setReportSender(new WearCrashSender());
        ACRA.getErrorReporter().setReportSender(new WearCrashSend());

        super.onCreate();

        setWearListenerMode(ACommon.WEAR_LISTENER_CLIENT);

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



//    public void runWearWatchFaceChooser() {
//        Intent intent = new Intent(
//                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
//        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
//                new ComponentName(this, AWearFaceService.class));
//        startActivity(intent);
//    }

    @Override //OnWearListenerCommand
    public void runWearWatchFaceChooser() {
        //super.runWearWatchFaceChooser();
        if (L) Log.i(TAG, "***** runWearWatchFaceChooser() worker *****");

        Intent intent;
        String pkgName, className;

        intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pkgName = AWearFaceService.class.getPackage().getName(); //this;
        className = AWearFaceService.class.getCanonicalName(); //AWearFaceService.class;
        if (L) Log.i(TAG, "***** runWearWatchFaceChooser(): package="+pkgName+", class="+className+" *****");
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(pkgName, className));
        startActivity(intent);

        //startService(new Intent(this, AWearFaceService.class));


//        intent.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
//        String p = AWearFaceService.class.getPackage().getName();
//        String c = AWearFaceService.class.getCanonicalName();
//        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(p, c));
//        startActivityForResult(intent, 0);

    }


//    private boolean isWatchFaceInstalled() {
//        String pkgName = AWearFaceService.class.getPackage().getName();
//        PackageManager pm = getPackageManager();
//        boolean app_installed;
//        try {
//            pm.getPackageInfo(pkgName, PackageManager.GET_SERVICES); //.GET_ACTIVITIES
//            app_installed = true;
//        }
//        catch (PackageManager.NameNotFoundException e) {
//            app_installed = false;
//        }
//        return app_installed;
//    }

    @Override //OnWearListenerCommand
    public boolean isWatchFaceInstalled() {
        //return super.isWatchFaceInstalled();
        String pkgName = AWearFaceService.class.getPackage().getName();
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            PackageInfo pkgInfo = pm.getPackageInfo(pkgName, PackageManager.GET_SERVICES); //.GET_ACTIVITIES
            ServiceInfo[] srvInfo = pkgInfo.services;
            if (L) Log.i(TAG, "***** isWatchFaceInstalled(): package="+pkgName+" INSTALLED *****");
            for (int i=0; i < srvInfo.length; i++) {
                if (L) Log.i(TAG, "***** srvInfo["+i+"].name = "+srvInfo[i].name+" *****");
            }
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
            if (L) Log.i(TAG, "***** isWatchFaceInstalled(): package="+pkgName+" MISSED *****");
        }
        return app_installed;
    }




    SynBoolean consumeDataTrigger = new SynBoolean(true);

    @Override //OnWatchFaceCommand
    public void setConsumeDataTrigger(boolean value) {
        //super.setConsumeDataTrigger(value);
        consumeDataTrigger.setValue(value);
    }

    @Override //OnWatchFaceCommand
    public boolean getConsumeDataTrigger() {
        //return super.getConsumeDataTrigger();
        return consumeDataTrigger.getValue();
    }







    SynBoolean handheldBatteryTrigger = new SynBoolean(false);

    @Override //OnWatchFaceCommand
    public void setHandheldBatteryTrigger(boolean value) {
        //super.setHandheldBatteryTrigger(value);
        handheldBatteryTrigger.setValue(value);
        shareHandheldBatteryTrigger();
    }

    @Override //OnWatchFaceCommand
    public boolean getHandheldBatteryTrigger() {
        //return super.getHandheldBatteryTrigger();
        return handheldBatteryTrigger.getValue();
    }

    private void shareHandheldBatteryTrigger() {
        DataMap dataMap = new DataMap();
        dataMap.putBoolean(ACommon.KEY_TRIGGER, handheldBatteryTrigger.getValue());
        Utils.shareNodeData(this, ACommon.HHBATT_TRIGGER_PATH, dataMap);
    }




    @Override //CommonApplication
    public void publishSharedValues() {
        if (ACommon.L) Log.i(TAG, "publishSharedValues(), Local=" + getLocalPeerId() + ", Mode=" + getWearListenerMode()
                        + ", Service=" + getServicePeerId() + ", Capability=" + getServiceCapability()
                        + ", Receiver=" + isReceiverForListenerRegistered()
        );
        shareHandheldBatteryTrigger();
        super.publishSharedValues();
    }

} // class WearApplication
