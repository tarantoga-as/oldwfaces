package com.luna_78.wear.watch.face.raf3078;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.luna_78.wear.watch.face.raf3078.common.ACommon;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by buba on 13/09/15.
 */
public class ArrivedDataImporterActivity2 extends FragmentActivity {

    private final static String TAG = "DIA";

    public String productId;

    String arrivedFilePath;
    InputStream arrivedInputStream;

    //ArrayList<String> arrivedConfigPalette;

    public APhoneService mService;
    //public APhoneService getBoundService() { return mService; }
    boolean mServiceBound = false;
    //boolean isServiceBound() { return mServiceBound; }
    //
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            APhoneService.LocalBinder binder = (APhoneService.LocalBinder) service;
            mService = binder.getService();
            mServiceBound = true;
            //Log.i(TAG, "*** onServiceConnected: mService=" + mService);

            //startCompanionConfigActivity();

            //mService.setPeerId(mPeerId); // !!!

            startAddOrReplaceDialog();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Log.i(TAG, "*** onServiceDisconnected");
            mServiceBound = false;
        }
    };


    private void startCompanionConfigActivity() {
//        Intent hhccActivity = new Intent(this, HandheldCompanionConfigActivity.class);
//        hhccActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(hhccActivity);

        //Toast.makeText(getApplicationContext(), "Success.", Toast.LENGTH_LONG).show();
    } // startCompanionConfigActivity



    private void startAddOrReplaceDialog() {

        //android.app.FragmentManager fm = getFragmentManager();
        FragmentManager fm = getSupportFragmentManager();



        String dialogTag = "dialogAR";
        ConfirmAddOrReplaceDialogFragment fragment = (ConfirmAddOrReplaceDialogFragment) fm.findFragmentByTag(dialogTag);

        if (null == fragment) {
            fragment = new ConfirmAddOrReplaceDialogFragment();
            fragment.setActivity(this);
            //ConfirmResetToDefaultsDialogFragment fragment = ConfirmResetToDefaultsDialogFragment.instantiate(this, "dialog");
            fragment.show(fm, dialogTag);
        }
    } // startAddOrReplaceDialog


    public static class ConfirmAddOrReplaceDialogFragment extends DialogFragment {
        ArrivedDataImporterActivity2 mImporterActivity;

        public void setActivity(ArrivedDataImporterActivity2 activity) { mImporterActivity = activity; }

        private void cancel(DialogInterface dialog) {
            dialog.dismiss();
//            CharSequence text = "Cancel confirmed";
//            //Toast toast = Toast.makeText(mImporterActivity.getApplicationContext(), text, Toast.LENGTH_SHORT);
//            Toast toast = Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_LONG);
//            toast.show();
            // do the job here
            mImporterActivity.finish();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            //super.onCancel(dialog);
            cancel(dialog);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //return super.onCreateDialog(savedInstanceState);
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.confirm_add_or_replace_title)
                    .setMessage(R.string.confirm_add_or_replace_message)
                    .setPositiveButton(R.string.string_replace, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
//                            CharSequence text = "Replace confirmed";
//                            //Toast toast = Toast.makeText(mImporterActivity.getApplicationContext(), text, Toast.LENGTH_SHORT);
//                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_LONG);
//                            toast.show();
                            // do the job here
                            mImporterActivity.replacePaletteElements();
                            //mImporterActivity.finish();
                        }
                    })
                    .setNeutralButton(R.string.string_add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
//                            CharSequence text = "Add confirmed";
//                            //Toast toast = Toast.makeText(mImporterActivity.getApplicationContext(), text, Toast.LENGTH_SHORT);
//                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_LONG);
//                            toast.show();
                            // do the job here
                            mImporterActivity.addPaletteElements();
                            //mImporterActivity.finish();
                        }
                    })
                    .setNegativeButton(R.string.string_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancel(dialog);
                        }
                    })
                    .create();
        }


    } //class ConfirmAddOrReplaceDialogFragment



    private void addPaletteElements() {
        //if (tryUnpackArrivedData()) {

        //Log.i(TAG, "((( addPaletteElements, number of elements = " + arrivedConfigPalette.size());

        //mSplash = new SplashDialogFragment();
        mSplash.setType(SplashDialogFragment.FragmentType.ORDINAL);
        mSplash.showSplash(getSupportFragmentManager(), false);

        if (null != arrivedFilePath) {
            mService.requestConcatenateLayoutsPalette(arrivedFilePath);
        } else if(null != arrivedInputStream) {
            mService.requestConcatenateLayoutsPalette(arrivedInputStream);
//            Toast.makeText(getApplicationContext(), "CONTENT STREAM", Toast.LENGTH_LONG).show();
//            finish();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_input_error), Toast.LENGTH_LONG);
        }

        //startCompanionConfigActivity();

        //}
    } // addPaletteElements

    private void replacePaletteElements() {
        //if (tryUnpackArrivedData()) {

        //Log.i(TAG, "((( replacePaletteElements, number of elements = " + arrivedConfigPalette.size());

        //mSplash = new SplashDialogFragment();
        mSplash.setType(SplashDialogFragment.FragmentType.ORDINAL);
        mSplash.showSplash(getSupportFragmentManager(), false);

        if (null != arrivedFilePath) {
            mService.requestReplaceLayoutsPalette(arrivedFilePath);
        } else if(null != arrivedInputStream) {
            mService.requestReplaceLayoutsPalette(arrivedInputStream);
//            Toast.makeText(getApplicationContext(), "CONTENT STREAM", Toast.LENGTH_LONG).show();
//            finish();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toast_input_error), Toast.LENGTH_LONG);
        }

        //startCompanionConfigActivity();

        //}
    } // replacePaletteElements


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        productId = getResources().getString(R.string.product_id);

        Intent service = new Intent(this, APhoneService.class);
        startService(service);

        Uri uri = getIntent().getData();
        //Log.i(TAG, "#URI onCreate, uri=" + uri.toString());

        // when activity started with file from email attachment or just file with encoded layouts palette
        // see here: http://richardleggett.co.uk/blog/2013/01/26/registering_for_file_types_in_android/
        if (null != uri) {
            if (uri.getScheme().equals("content")) {
                arrivedFilePath = null;
                try {
                    arrivedInputStream = getContentResolver().openInputStream(uri);
                    //Log.i(TAG, "#URI onCreate, stream=" + arrivedInputStream.toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                arrivedInputStream = null;
                arrivedFilePath = uri.getEncodedPath();
                //Log.i(TAG, "#URI onCreate, file=" + arrivedFilePath);
            }
            getIntent().setData(null);
        }
    } // onCreate


    @Override
    protected void onStart() {
        super.onStart();
        Intent service = new Intent(this, APhoneService.class);
        bindService(service, mServiceConnection, BIND_AUTO_CREATE);
        registerDataFromServiceReceiver();
    } // onStart


    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            unbindService(mServiceConnection);
        }
        unregisterDataFromServiceReceiver();
    } // onStop










    private boolean isDataFromServiceReceiverRegistered = false;
    //
    private void registerDataFromServiceReceiver() {
        if (!isDataFromServiceReceiverRegistered) {
            LocalBroadcastManager.getInstance(ArrivedDataImporterActivity2.this).
                    registerReceiver(mDataFromService, new IntentFilter(ACommon.EVENT_ACTION));
            isDataFromServiceReceiverRegistered = true;
        }
    } // registerDataFromServiceReceiver
    private void unregisterDataFromServiceReceiver() {
        if (isDataFromServiceReceiverRegistered) {
            LocalBroadcastManager.getInstance(ArrivedDataImporterActivity2.this).unregisterReceiver(mDataFromService);
        }
        isDataFromServiceReceiverRegistered = false;
    } // unregisterDataFromServiceReceiver




    private BroadcastReceiver mDataFromService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!action.equals(ACommon.EVENT_ACTION)) return;
            Bundle bundle = intent.getExtras();
            //Log.i(TAG, "*** onReceive");
            if (bundle != null) {
                int event = bundle.getInt(ACommon.BCAST_EXTRA_EVENT_TYPE);
                switch (event) {
//                    case ACommon.EVT_CURRENT_CONFIG_FOR_FILE:
//                        Log.i(TAG, "### EVT_CURRENT_CONFIG_FOR_FILE = " + bundle);
//                        if (mConfigForFile != null && isInGetConfigTransaction()) mConfigForFile.addConfig(bundle);
//                        break;
//                    case ACommon.EVT_DENSE_SCREENSHOT:
//                        Log.i(TAG, "### FRAME_SCREENSHOT READY IN SERVICE");
//                        if (mConfigForFile != null && isInGetConfigTransaction()) mConfigForFile.addDenseIcon();
//                        break;
//                    case ACommon.EVT_CURRENT_CONFIG:
//                        Log.i(TAG, "### CURRENT_CONFIG=" + bundle);
//                        setCurrentColors(bundle);
//                        break;
//
////                    private void broadcastBooleanToActivity(int event, long time, boolean value) {
////                        Intent intent = new Intent();
////                        intent.setAction(ACommon.EVENT_ACTION);
////                        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
////                        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
////                        intent.putExtra(ACommon.CFG_SHOW_HANDHELD_BATTERY, value);
////                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
////                    }
//                    case ACommon.EVT_WEARCFG_TOGGLE_PHONE_BATTERY:
//                        boolean trgb = bundle.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false);
//                        Log.i(TAG, "### EVT_WEARCFG_TOGGLE_PHONE_BATTERY=" + trgb);
//                        break;
//
//                    case ACommon.EVT_WEARCFG_TOGGLE_LAYOUT:
//                        Log.i(TAG, "### EVT_WEARCFG_TOGGLE_LAYOUT");
//                        break;
//
//                    case ACommon.EVT_LAYOUTS_PALETTE_CHANGED:
//                        Log.i(TAG, "((( EVT_LAYOUTS_PALETTE_CHANGED");
//                        fireLayoutsPaletteChanged();
//                        break;
//
//
//                    case ACommon.EVT_SIGNAL_HOLDOFF:
//                        Log.i(TAG, "((( EVT_SIGNAL_HOLDOFF");
//                        if (mSplash != null) {
//                            mSplash.dismissSplash(getSupportFragmentManager(), mScreenWideEnough);
//                            mSplash = null;
//                        }
//                        setSlidingTabLayout();
//                        break;
//
//                    case ACommon.EVT_SIGNAL_HOLDOFF_UPDATE:
//                        Log.i(TAG, "((( EVT_SIGNAL_HOLDOFF_UPDATE");
//                        if (mSplash != null) {
//                            mSplash.dismissSplash(getSupportFragmentManager(), mScreenWideEnough);
//                            mSplash = null;
//                        }
//                        mAdapter.unlock();
//                        mAdapter.notifyDataSetChanged();
//                        break;

                    case ACommon.EVT_SIGNAL_HOLDOFF_2:
                        //Log.i(TAG, "((( EVT_SIGNAL_HOLDOFF_2");
                        if (mSplash != null) {
                            mSplash.dismissSplash(getSupportFragmentManager(), false);
                            mSplash = null;
                        }
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.string_done), Toast.LENGTH_LONG).show();
                        finish();
                        break;


                    default:
                        break;
                }
            }
        }
    }; // BroadcastReceiver mDataFromService


    SplashDialogFragment mSplash = new SplashDialogFragment();



} //class ArrivedDataImporterActivity2
