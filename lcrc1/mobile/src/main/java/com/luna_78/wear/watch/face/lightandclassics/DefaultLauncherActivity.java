package com.luna_78.wear.watch.face.lightandclassics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.wearable.DataMap;
import com.luna_78.wear.watch.face.lightandclassics.common.ACommon;
import com.luna_78.wear.watch.face.lightandclassics.common.Utils;
import com.luna_78.wear.watch.face.lightandclassics.common.WearDataSender;

public class DefaultLauncherActivity extends AppCompatActivity {

    private static final String TAG = ACommon.TAG_PREFIX + "LAUNCHER_ACT";


    PipeToService mServicePipe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, APhoneService.class));

        setContentView(R.layout.default_launcher_activity);

        mServicePipe = new PipeToService(this, null);



    }


    @Override
    protected void onStart() {
        super.onStart();

        mServicePipe.attachToService();
        mServicePipe.registerIntentFromServiceReceiver(mDataFromService);

        DataMap dataMap = new DataMap();

//        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_PHONE_BATTERY_SAMPLE);
//        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis()); //new Date().getTime()
//        dataMap.putFloat(ACommon.KEY_LEVEL, 50f);
//        new Thread(new WearDataSender.TaskWearNetSend(this, ACommon.FROM_HANDHELD_PATH, dataMap, null, false)).start();


        // ACommon.KEY_EVENT = ACommon.EVT_LISTENER_CONTROL;
        // ACommon.KEY_LISTENER_COMMAND = ACommon.LC_RUN_WATCHFACE
//        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_LISTENER_CONTROL);
//        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//        dataMap.putInt(ACommon.KEY_LISTENER_COMMAND, ACommon.LC_RUN_WATCHFACE);
//        new Thread(new WearDataSender.TaskWearNetSend(this, ACommon.LISTENER_LEVEL_PATH, dataMap, null, false)).start();


        //ACommon.EVT_LISTENER_CONTROL, ACommon.LC_PING_LISTENER _REPLY
//        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_LISTENER_CONTROL);
//        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
//        dataMap.putInt(ACommon.KEY_LISTENER_COMMAND, ACommon.LC_PING_LISTENER);
//        new Thread(new WearDataSender.TaskWearNetSend(this, ACommon.TO_LISTENER_PATH, dataMap, null, false)).start();


        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_LISTENER_CONTROL);
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putInt(ACommon.KEY_LISTENER_COMMAND, ACommon.LC_IS_WATCHFACE_INSTALLED);
        new Thread(new WearDataSender.TaskWearNetSend(this, ACommon.TO_LISTENER_PATH, dataMap, null, Utils.getSerialSeq(), false)).start();


    }

    @Override
    protected void onStop() {
        super.onStop();

        mServicePipe.detachFromService();
        mServicePipe.unregisterIntentFromServiceReceiver(mDataFromService);
    }


    class IntentDataFromService extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
    IntentDataFromService mDataFromService = new IntentDataFromService();
}



