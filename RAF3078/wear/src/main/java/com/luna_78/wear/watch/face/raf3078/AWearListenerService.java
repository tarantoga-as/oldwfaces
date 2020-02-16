package com.luna_78.wear.watch.face.raf3078;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.luna_78.wear.watch.face.raf3078.common.ACommon;
import com.luna_78.wear.watch.face.raf3078.common.AppPreferences;
import com.luna_78.wear.watch.face.raf3078.common.WatchAppearance;

/**
 * Created by buba on 31/01/15.
 */
public class AWearListenerService
        extends WearableListenerService
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener
{

    public String productId;

    private String mLocalPeerId;

    private static final String TAG = "WLS";
    private GoogleApiClient mGoogleApiClient;

    @Override // WearableListenerService
    public void onDestroy() {
        super.onDestroy();
        //Log.i(TAG, "onDestroy: " + mGoogleApiClient.isConnected() + ", " + mGoogleApiClient.isConnecting());
        //Log.i(TAG, "onDestroy");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
        //mGoogleApiClient.disconnect();
    }



    @Override // WearableListenerService
    public void onCreate() {
        super.onCreate();


        //getSharedPreferences();
        //getApplication();

        productId = getResources().getString(R.string.product_id);

        WearApplication application = (WearApplication) getApplication();
        mLocalPeerId = application.getLocalPeerId();
        //Log.i(TAG, "((( onCreate, mLocalPeerId = " + mLocalPeerId);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        }
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
/*
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) return;
*/
        }

//        mLocalPeerId = ACommon.getLocalNodeId(mGoogleApiClient);
//        Log.i(TAG, "((( Local peerId=" + mLocalPeerId);


        /*
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).addApi(Wearable.API).build();
        }

        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) mGoogleApiClient.connect();
        */

        /*
        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult =
                    mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);


            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Failed to connect to GoogleApiClient.");
                return;
            }
        }
        */



    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        //Log.i(TAG, "onConnected");
    }

    @Override  // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        //Log.i(TAG, "onConnectionSuspended");
    }

    @Override  // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Log.i(TAG, "onConnectionFailed");
    }

    @Override // WearableListenerService
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        //Log.i(TAG, "onMessageReceived: " + messageEvent.getPath());
    }

    //private static final String WEARABLE_DATA_PATH = ACommon.WEARABLE_DATA_PATH;


/*
    private boolean savePersistentDataToFile(Bundle bundle) {
        Log.i(TAG, "*** savePersistentDataToFile");

        boolean retVal = true;
        Parcel parcel = Parcel.obtain();        //creating empty parcel object

        try {
            String cfgFileName = getString(R.string.configFileName);
            FileOutputStream fos = openFileOutput(cfgFileName, Context.MODE_PRIVATE);
            //Parcel p = Parcel.obtain();
            bundle.writeToParcel(parcel, 0);     //saving bundle as parcel
            fos.write(parcel.marshall());        //writing parcel to file
            fos.flush();
            fos.close();
        } catch (FileNotFoundException fnfe) {
            retVal = false;
        } catch (IOException ioe) {
            retVal = false;
        } finally {
            parcel.recycle();
        }

        return retVal;
    } // savePersistentDataToFile
*/


    private boolean updateConfigFile(DataMap dataMap) {

        synchronized (((WearApplication) getApplication()).mLockConfigFile) {

            //ArrayList<Integer> colors;
            int color, evtType, index;
            String key;
            WatchAppearance appearance = new WatchAppearance(null, getApplicationContext());

            String cfn = getString(R.string.configFileName);
            Bundle bundle = ACommon.readPersistentDataFromFile(AWearListenerService.this, cfn);
            if (null == bundle) return false;
//        colors = bundle.getIntegerArrayList(ACommon.CFG_COLORS);
//        if (null == colors) {
//            return false;
//        }
            evtType = dataMap.getInt(ACommon.KEY_EVENT);
            switch (evtType) {
                case ACommon.EVT_CHANGE_INDEXED_COLOR:
                    color = dataMap.getInt(ACommon.KEY_COLOR);
    //                index = dataMap.getInt(ACommon.KEY_COLOR_INDEX);
                    key = dataMap.getString(ACommon.KEY_COLOR_KEY);
    //                colors.set(index, color);
                    bundle.putInt(key, color);
                    //Log.i(TAG, "((( updateConfigFile, EVT_CHANGE_INDEXED_COLOR, key=" + key + ", bundle=" + bundle);
                    break;
                //
                case ACommon.EVT_CHLAYOUT_DATE:
                    break;
                case ACommon.EVT_CHLAYOUT_MAINHANDS:
                    break;
                case ACommon.EVT_CHLAYOUT:
                    break;
                default: break;
            }
//        bundle.putIntegerArrayList(ACommon.CFG_COLORS, colors);
            //
            appearance.unBundleConfig(bundle);
            bundle = appearance.bundleConfig(System.currentTimeMillis()); //0L
            //Log.i(TAG, "((( updateConfigFile, cleared bundle=" + bundle);
            //
            long cfg_time = System.currentTimeMillis();
            bundle.putLong(ACommon.CFG_TIME, cfg_time);
            bundle.putLong(ACommon.KEY_TIME, cfg_time);
            bundle.putInt(ACommon.KEY_EVENT, ACommon.EVT_CURRENT_CONFIG);
            bundle.putAll(bundle);
            return ACommon.savePersistentDataToFile(AWearListenerService.this, cfn, bundle);

        } // synchronized

    } // updateConfigFile


    @Override // WearableListenerService
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Bundle config;
        DataMap dataMap;
        boolean needConsume, bool1, notForUs;
        int int1;
        //WearApplication application = (WearApplication) getApplication();
        mLocalPeerId = ((WearApplication) getApplication()).getLocalPeerId();
        for (DataEvent event : dataEvents) {
            needConsume = false;
            Uri uri = event.getDataItem().getUri();
            String scheme = uri.getScheme();
            String path = uri.getPath();
            String host = uri.getHost(); // may be null
            //Log.i(TAG, "((( PEER DATA: " + scheme + ":// " + host + " " + path);
            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                //if (path.equals(WEARABLE_DATA_PATH)) {
                dataMap = new DataMap();
                dataMap.putAll(DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
                //Log.i(TAG, "onDataChanged: dataMap: " + dataMap + "; scheme=" + scheme + ", node=" + host + ", path=" + path);
                //
                //DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                if (path.equals(ACommon.FROM_HANDHELD_PATH)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    int evtType = dataMapItem.getDataMap().getInt(ACommon.KEY_EVENT);
                    long time = dataMapItem.getDataMap().getLong(ACommon.KEY_TIME);

                    String toPeer = dataMapItem.getDataMap().getString(ACommon.KEY_TOPEER, null);
                    if (null != toPeer && null != mLocalPeerId && !toPeer.equals(mLocalPeerId)) notForUs = true;
                    else notForUs = false;

                    //Log.i(TAG, "((( peer: notForUs=" + notForUs + "; toPeer=" + toPeer + ", mLocalPeerId=" + mLocalPeerId);

                    if (!notForUs) {
                        int color, index;
                        //String key;
                        switch (evtType) {
                            case ACommon.EVT_PHONE_BATTERY_SAMPLE:
                                float level = dataMapItem.getDataMap().getFloat(ACommon.KEY_LEVEL);
                                broadcastFloatToWearFaceService(evtType, time, level);
                                needConsume = true;
                                break;

                            case ACommon.EVT_WAKEUP:
                                long delay = dataMapItem.getDataMap().getLong(ACommon.KEY_DELAY);
                                broadcastLongToWearFaceService(evtType, time, delay);
                                needConsume = true;
                                break;
                            //
                            case ACommon.EVT_WAKEUP_AMBIENT_ELEMENT:
                                //long delay = dataMapItem.getDataMap().getLong(ACommon.KEY_DELAY);
                                broadcastLongToWearFaceService(evtType, time, dataMapItem.getDataMap().getLong(ACommon.KEY_DELAY));
                                needConsume = true;
                                break;

                            case ACommon.EVT_CHANGE_INDEXED_COLOR:
                                index = dataMap.getInt(ACommon.KEY_COLOR_INDEX);
                                color = dataMap.getInt(ACommon.KEY_COLOR);
                                //key = dataMap.getString(ACommon.KEY_COLOR_KEY);
                                int evt = -1;
                                needConsume = true;
                                switch (index) {
                                    case ACommon.CFG_MAIN_DIGITS_COLOR:
                                        evt = ACommon.EVT_COLOR_DIGITS;
                                        color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_MAIN_HOURMARK_OUTLINE_COLOR:
                                        evt = ACommon.EVT_COLOR_HOURMARK_OUTLINE;
                                        //color = color;
                                        break;
                                    case ACommon.CFG_MAIN_HOURHAND_COLOR:
                                        evt = ACommon.EVT_COLOR_HOUR_HAND;
                                        break;
                                    case ACommon.CFG_MAIN_MINUTEHAND_COLOR:
                                        evt = ACommon.EVT_COLOR_MINUTE_HAND;
                                        break;
                                    case ACommon.CFG_MAIN_BACKGROUND_COLOR:
                                        evt = ACommon.EVT_COLOR_BACKGROUND;
                                        color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_MAIN_MAINHANDS_COLOR:
                                        evt = ACommon.EVT_COLOR_MAINHANDS;
                                        color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_MAIN_SECONDSHAND_COLOR:
                                        evt = ACommon.EVT_COLOR_SECONDSHAND;
                                        color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_MAIN_DOMBACK_COLOR:
                                        evt = ACommon.EVT_COLOR_DOM_BACK;
                                        color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_MAIN_DOMFRONT_COLOR:
                                        evt = ACommon.EVT_COLOR_DOM_FRONT;
                                        break;
                                    case ACommon.CFG_MAIN_DOM_FRAME_COLOR:
                                        evt = ACommon.EVT_COLOR_DOM_FRAME;
                                        break;
                                    case ACommon.CFG_MAIN_TICK_COLOR:
                                        evt = ACommon.EVT_COLOR_MAIN_TICK;
                                        break;
                                    case ACommon.CFG_MAIN_TICKDIGIT_COLOR:
                                        evt = ACommon.EVT_COLOR_MAIN_TICKDIGIT;
                                        break;
                                    //mMainCalendarDialBackgroundColor  CFG_MAIN_BIGAUX_BACKGROUND_COLOR    EVT_COLOR_BIGAUX_BACKGROUND
                                    //mMainCalendarDialDigitsColor      CFG_MAIN_BIGAUX_DIGITS_COLOR        EVT_COLOR_BIGAUX_DIGITS
                                    //mMainCalendarDialTicksColor       CFG_MAIN_BIGAUX_TICKS_COLOR         EVT_COLOR_BIGAUX_TICKS
                                    case ACommon.CFG_MAIN_BIGAUX_BACKGROUND_COLOR:
                                        evt = ACommon.EVT_COLOR_BIGAUX_BACKGROUND;
                                        color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_MAIN_BIGAUX_DIGITS_COLOR:
                                        evt = ACommon.EVT_COLOR_BIGAUX_DIGITS;
                                        break;
                                    case ACommon.CFG_MAIN_BIGAUX_TICKS_COLOR:
                                        evt = ACommon.EVT_COLOR_BIGAUX_TICKS;
                                        break;
                                    //
                                    // mMainSmallAuxDialBackgroundColor     CFG_MAIN_SMALLAUX_BACKGROUND_COLOR  EVT_COLOR_SMALLAUX_BACKGROUND_COLOR
                                    // mMainSmallAuxDialDigitsColor         CFG_MAIN_SMALLAUX_DIGITS_COLOR      EVT_COLOR_SMALLAUX_DIGITS_COLOR
                                    // mMainSmallAuxDialTick1Color          CFG_MAIN_SMALLAUX_TICKS1_COLOR      EVT_COLOR_SMALLAUX_TICKS1_COLOR
                                    // mMainSmallAuxDialTick2Color          CFG_MAIN_SMALLAUX_TICKS2_COLOR      EVT_COLOR_SMALLAUX_TICKS2_COLOR
                                    case ACommon.CFG_MAIN_SMALLAUX_BACKGROUND_COLOR:
                                        evt = ACommon.EVT_COLOR_SMALLAUX_BACKGROUND_COLOR;
                                        color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_MAIN_SMALLAUX_DIGITS_COLOR:
                                        evt = ACommon.EVT_COLOR_SMALLAUX_DIGITS_COLOR;
                                        break;
                                    case ACommon.CFG_MAIN_SMALLAUX_TICKS1_COLOR:
                                        evt = ACommon.EVT_COLOR_SMALLAUX_TICKS1_COLOR;
                                        break;
                                    case ACommon.CFG_MAIN_SMALLAUX_TICKS2_COLOR:
                                        evt = ACommon.EVT_COLOR_SMALLAUX_TICKS2_COLOR;
                                        break;
                                    //
                                    // mMainAuxHandWeekdayColor   CFG_MAIN_AUXHANDS_WEEKDAY_COLOR  EVT_COLOR_AUXHANDS_WEEKDAY_COLOR
                                    case ACommon.CFG_MAIN_AUXHANDS_WEEKDAY_COLOR:
                                        evt = ACommon.EVT_COLOR_AUXHANDS_WEEKDAY_COLOR;
                                        color = color | 0xff000000;
                                        break;
                                    //
// mMainAuxHandWearBattColor   CFG_MAIN_AUXHANDS_WEARBATT_COLOR     EVT_COLOR_AUXHANDS_WEARBATT_COLOR
// mMainAuxHandPhoneBattColor  CFG_MAIN_AUXHANDS_PHONEBATT_COLOR    EVT_COLOR_AUXHANDS_PHONEBATT_COLOR
// mMainAuxHandMonthColor      CFG_MAIN_AUXHANDS_MONTH_COLOR        EVT_COLOR_AUXHANDS_MONTH_COLOR
                                    case ACommon.CFG_MAIN_AUXHANDS_WEARBATT_COLOR:
                                        evt = ACommon.EVT_COLOR_AUXHANDS_WEARBATT_COLOR;
                                        color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_MAIN_AUXHANDS_PHONEBATT_COLOR:
                                        evt = ACommon.EVT_COLOR_AUXHANDS_PHONEBATT_COLOR;
                                        color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_MAIN_AUXHANDS_MONTH_COLOR:
                                        evt = ACommon.EVT_COLOR_AUXHANDS_MONTH_COLOR;
                                        color = color | 0xff000000;
                                        break;
                                    //
// mAmbientDigitsColor              CFG_AMBIENT_DIGITS_COLOR        EVT_COLOR_AMBIENT_DIGITS
// mAmbientHourHandColor            CFG_AMBIENT_HOURHAND_COLOR      EVT_COLOR_AMBIENT_HOURHAND
// mAmbientMinuteHandColor          CFG_AMBIENT_MINUTEHAND_COLOR    EVT_COLOR_AMBIENT_MINUTEHAND
// mAmbientTicksColor               CFG_AMBIENT_TICK_COLOR          EVT_COLOR_AMBIENT_TICK
// mAmbientTickDigitColor           CFG_AMBIENT_TICKDIGIT_COLOR     EVT_COLOR_AMBIENT_TICKDIGIT
// mAmbientDomAndAuxHandsColor      CFG_AMBIENT_DOM_AUXHANDS_COLOR  EVT_COLOR_AMBIENT_DOM_AUXHANDS
                                    case ACommon.CFG_AMBIENT_DIGITS_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_DIGITS;
                                        //color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_AMBIENT_HOURHAND_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_HOURHAND;
                                        //color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_AMBIENT_MINUTEHAND_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_MINUTEHAND;
                                        //color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_AMBIENT_TICK_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_TICK;
                                        //color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_AMBIENT_TICKDIGIT_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_TICKDIGIT;
                                        //color = color | 0xff000000;
                                        break;
                                    //
                                    case ACommon.CFG_AMBIENT_DECORUPPER_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_DECORUPPER;
                                        //color = color | 0xff000000;
                                        break;
                                    case ACommon.CFG_MAIN_DECORUPPER_COLOR:
                                        evt = ACommon.EVT_COLOR_DECORUPPER;
                                        //color = color | 0xff000000;
                                        break;
                                    //
                                    case ACommon.CFG_AMBIENT_DOM_AUXHANDS_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_DOM_AUXHANDS;
                                        color = color | 0xff000000;
                                        break;
                                    //
                                    case ACommon.CFG_AMBIENT_DOMBACK_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_DOMBACK;
                                        break;
                                    case ACommon.CFG_AMBIENT_DOMFRONT_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_DOMFRONT;
                                        break;
                                    case ACommon.CFG_AMBIENT_DOM_FRAME_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_DOM_FRAME;
                                        break;
                                    case ACommon.CFG_AMBIENT_AUXHANDS_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_AUXHANDS;
                                        break;
                                    //
                                    case ACommon.CFG_MAIN_INSCRIPTION_1_COLOR:
                                        evt = ACommon.EVT_COLOR_INSCRIPTION_1;
                                        break;
                                    case ACommon.CFG_MAIN_INSCRIPTION_2_COLOR:
                                        evt = ACommon.EVT_COLOR_INSCRIPTION_2;
                                        break;
                                    case ACommon.CFG_MAIN_INSCRIPTION_3_COLOR:
                                        evt = ACommon.EVT_COLOR_INSCRIPTION_3;
                                        break;
                                    case ACommon.CFG_MAIN_INSCRIPTION_4_COLOR:
                                        evt = ACommon.EVT_COLOR_INSCRIPTION_4;
                                        break;
                                    case ACommon.CFG_MAIN_INSCRIPTION_5_COLOR:
                                        evt = ACommon.EVT_COLOR_INSCRIPTION_5;
                                        break;
                                    case ACommon.CFG_MAIN_INSCRIPTION_6_COLOR:
                                        evt = ACommon.EVT_COLOR_INSCRIPTION_6;
                                        break;
                                    case ACommon.CFG_MAIN_INSCRIPTION_7_COLOR:
                                        evt = ACommon.EVT_COLOR_INSCRIPTION_7;
                                        break;




                                    case ACommon.CFG_MAIN_TZ_SCRIPTS_COLOR:
                                        evt = ACommon.EVT_COLOR_MAIN_TZ_SCRIPTS;
                                        break;
                                    case ACommon.CFG_MAIN_TZ_CIRCLES_COLOR:
                                        evt = ACommon.EVT_COLOR_MAIN_TZ_CIRCLES;
                                        break;
                                    case ACommon.CFG_MAIN_TZ_SIGN_COLOR:
                                        evt = ACommon.EVT_COLOR_MAIN_TZ_SIGN;
                                        break;
                                    case ACommon.CFG_MAIN_TZ_POINT_COLOR:
                                        evt = ACommon.EVT_COLOR_MAIN_TZ_POINT;
                                        break;
                                    case ACommon.CFG_AMBIENT_TZ_SCRIPTS_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_TZ_SCRIPTS;
                                        break;
                                    case ACommon.CFG_AMBIENT_TZ_CIRCLES_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_TZ_CIRCLES;
                                        break;
                                    case ACommon.CFG_AMBIENT_TZ_SIGN_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_TZ_SIGN;
                                        break;
                                    case ACommon.CFG_AMBIENT_TZ_POINT_COLOR:
                                        evt = ACommon.EVT_COLOR_AMBIENT_TZ_POINT;
                                        break;

                                    //
                                    default:
                                        break;
                                }


                                if (-1 != evt) {
                                    broadcastIntToWearFaceService(evt, time, color);
                                    //Log.i(TAG, "((( EVT_CHANGE_INDEXED_COLOR, dataMap=" + dataMap);
                                    updateConfigFile(dataMap);
                                }
                                break;

//                        case ACommon.EVT_COLOR_HOUR_HAND:
//                        case ACommon.EVT_COLOR_MINUTE_HAND:
//                        case ACommon.EVT_COLOR_HANDS:
//                        case ACommon.EVT_COLOR_DIGITS:
//                        case ACommon.EVT_COLOR_BACKGROUND:
//                        case ACommon.EVT_COLOR_MAINHANDS:
//                        case ACommon.EVT_COLOR_SECONDSHAND:
//                        case ACommon.EVT_COLOR_DOM_BACK:
//                        case ACommon.EVT_COLOR_DOM_FRONT:
//                            color = dataMapItem.getDataMap().getInt(ACommon.KEY_COLOR);
//                            broadcastIntToWearFaceService(evtType, time, color);
//                            updateConfigFile(dataMap);
//                            needConsume = true;
//                            break;

                            case ACommon.EVT_CHLAYOUT_DATE:
                                broadcastEmptyToWearFaceService(evtType, time);
                                updateConfigFile(dataMap);
                                break;
                            case ACommon.EVT_CHLAYOUT_MAINHANDS:
                                broadcastEmptyToWearFaceService(evtType, time);
                                updateConfigFile(dataMap);
                                needConsume = true;
                                break;
                            case ACommon.EVT_CHLAYOUT:
                                broadcastEmptyToWearFaceService(evtType, time);
                                updateConfigFile(dataMap);
                                needConsume = true;
                                break;
                            case ACommon.EVT_REQUEST_STORE_CONFIG:
//                            config = dataMap.toBundle();
//                            //String cfn = getString(R.string.configFileName);
//                            res = ACommon.savePersistentDataToFile(
//                                    AWearListenerService.this, getString(R.string.configFileName), config);
//                            Log.i(TAG, "{{{ STORE CONFIG = " + res);
                                //Log.i(TAG, "((( EVT_REQUEST_STORE_CONFIG");
                                storeConfigToFile(dataMap);
                                needConsume = true;
                                break;
                            case ACommon.EVT_REQUEST_CURRENT_CONFIG:
                                //Log.i(TAG, "((( EVT_REQUEST_CURRENT_CONFIG");
//                            config = ACommon.readPersistentDataFromFile(
//                                    AWearListenerService.this, getString(R.string.configFileName));
//                            if (null == config) {
//                                //todo: send broadcast to watchface
//                            } else {
//                                sendCurrentConfig(config);
//                            }
                                needConsume = true;
                                broadcastEmptyToWearFaceService(evtType, time);
                                break;
                            case ACommon.EVT_REQUEST_CURRENT_CONFIG_FOR_FILE:
                                //Log.i(TAG, "### REQUEST_CURRENT_CONFIG_FOR_FILE");
                                needConsume = true;
                                broadcastEmptyToWearFaceService(evtType, time);
                                break;
                            case ACommon.EVT_SET_FULL_CONFIG:
                                needConsume = true;
                                dataMap.remove(ACommon.BCAST_EXTRA_EVENT_TYPE);
                                dataMap.remove(ACommon.BCAST_EXTRA_BATTERY_TIME);
                                //Log.i(TAG, "### EVT_SET_FULL_CONFIG dataMap=" + dataMap);
                                broadcastDatamapToWearFaceService(evtType, time, ACommon.KEY_CFGPAL_CONFIG, dataMap);
                                config = storeConfigToFile(dataMap);
                                sendCurrentConfig(config);
                                break;
                            case ACommon.EVT_RESET:
                                //Log.i(TAG, "{{{ RESET CONFIG");
                                broadcastEmptyToWearFaceService(evtType, time);
                                needConsume = true;
                                break;

                            case ACommon.EVT_REQUEST_PLATE_BITMAP:
                                needConsume = true;
                                broadcastEmptyToWearFaceService(evtType, time);
                                break;

                            case ACommon.EVT_HHCFG_SET_PHONE_BATTERY:
                                //dataMap.putBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, option);
                                bool1 = dataMap.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false);
                                int1 = (true == bool1) ? 1 : 0;
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;
                            //
                            case ACommon.EVT_HHCFG_SET_RIM_ANIMATION:
                                bool1 = dataMap.getBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, false);
                                int1 = (true == bool1) ? 1 : 0;
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;
                            //
                            case ACommon.EVT_HHCFG_SET_HRDIGITS_RELIEF:
                                bool1 = dataMap.getBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, true);
                                int1 = (true == bool1) ? 1 : 0;
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;
                            //
                            case ACommon.EVT_HHCFG_SET_INSCRIPTIONS_RELIEF:
                                bool1 = dataMap.getBoolean(ACommon.CFG_SHOW_INSCRIPTIONS_RELIEF, true);
                                int1 = (true == bool1) ? 1 : 0;
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;
                            //
                            case ACommon.EVT_HHCFG_SET_DIAL_GRADIENT:
                                bool1 = dataMap.getBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, true);
                                int1 = (true == bool1) ? 1 : 0;
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;

                            case ACommon.EVT_HHCFG_SET_DIAL_GRADIENT_PACK:
                                broadcastDatamapToWearFaceService(evtType, time, ACommon.KEY_GRADIENT_PACK, dataMap);
                                needConsume = true;
                                break;

                            case ACommon.EVT_HHCFG_SET_PLATE_TEXTURE_PACK:
                                broadcastDatamapToWearFaceService(evtType, time, ACommon.KEY_PLATE_TEXTURE_PACK, dataMap);
                                needConsume = true;
                                break;

                            case ACommon.EVT_HHCFG_SET_HOUR_MARKS:
                                broadcastDatamapToWearFaceService(evtType, time, ACommon.KEY_HOUR_MARKS, dataMap);
                                needConsume = true;
                                break;

                            case ACommon.EVT_HHCFG_SET_DEMO_PACK:
                                broadcastDatamapToWearFaceService(evtType, time, ACommon.KEY_DEMO_PACK, dataMap);
                                needConsume = true;
                                break;

                            case ACommon.EVT_HHCFG_SET_INSCRIPTIONS_PACK:
                                broadcastDatamapToWearFaceService(evtType, time, ACommon.KEY_INSCRIPTIONS_PACK, dataMap);
                                needConsume = true;
                                break;

                            case ACommon.EVT_HHCFG_SET_TIMEZONE_PACK:
                                broadcastDatamapToWearFaceService(evtType, time, ACommon.KEY_TIMEZONE_PACK, dataMap);
                                needConsume = true;
                                break;

                            case ACommon.EVT_HHCFG_SET_TZ_HEMISPHERE:
                                //ACommon.CFG_TZ_HEMISPHERE
                                int1 = dataMap.getInt(AppPreferences.KEY_HEMISPHERE, AppPreferences.DEFAULT_HEMISPHERE);
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;

                            //
                            case ACommon.EVT_HHCFG_SET_AUX_BEVEL_COLOR:
                                int1 = dataMap.getInt(ACommon.CFG_AUX_BEVEL_COLOR, ACommon.BEVEL_FROM_AUX);
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;

                            // ACommon.EVT_HHCFG_SET_RESPECT_BURNIN, ACommon.CFG_RESPECT_BURNIN
                            case ACommon.EVT_HHCFG_SET_RESPECT_BURNIN:
                                bool1 = dataMap.getBoolean(ACommon.CFG_RESPECT_BURNIN, false);
                                int1 = (true == bool1) ? 1 : 0;
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;

                            // CFG_RESPECT_LOWBIT   EVT_HHCFG_SET_RESPECT_LOWBIT    EVT_WEARCFG_SET_RESPECT_LOWBIT
                            // CFG_SWEEP_SECONDS    EVT_HHCFG_SET_SWEEP             EVT_WEARCFG_SET_SWEEP
                            //
                            case ACommon.EVT_HHCFG_SET_RESPECT_LOWBIT:
                                bool1 = dataMap.getBoolean(ACommon.CFG_RESPECT_LOWBIT, false);
                                int1 = (true == bool1) ? 1 : 0;
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;
                            //
                            case ACommon.EVT_HHCFG_SET_SWEEP:
                                bool1 = dataMap.getBoolean(ACommon.CFG_SWEEP_SECONDS, false);
                                int1 = (true == bool1) ? 1 : 0;
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;

                            case ACommon.EVT_HHCFG_COLORIZE_BURNIN_MARGIN:
                                bool1 = dataMap.getBoolean(ACommon.CFG_COLORIZE_BURNIN_MARGIN, true);
                                int1 = (true == bool1) ? 1 : 0;
                                broadcastIntToWearFaceService(evtType, time, int1);
                                needConsume = true;
                                break;


                            default:
                                needConsume = true;
                                break;
                        }
                    }
                }

                // consume dataItem
                if (needConsume == true) Wearable.DataApi.deleteDataItems(mGoogleApiClient, event.getDataItem().getUri());

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                //Log.i(TAG, "onDataChanged: deleted URI: " + uri);
            }
        }
        dataEvents.release();
    }

/*
    public void onResult(DataItemBuffer dataItems) {
        for (int i=0; i<dataItems.getCount(); i++) {
            Log.i(TAG, "getDataItems DataItem URI: " + dataItems.get(i).getUri());
                if (dataItems.get(i).getUri().getPath().equals("/rus_airforce")) {
                    Log.i(TAG, "getDataItems DataItem path=" + dataItems.get(i).getUri().getPath() + " need to be deleted!!!");
                    Wearable.DataApi.deleteDataItems(mGoogleApiClient,dataItems.get(i).getUri());
                }
        }
        dataItems.release();
    }
*/

    private Bundle storeConfigToFile(DataMap dataMap) {
        Bundle config = dataMap.toBundle();
        //String cfn = getString(R.string.configFileName);
        boolean res;
        res =  ACommon.savePersistentDataToFile(
                AWearListenerService.this, getString(R.string.configFileName), config);
        //Log.i(TAG, "### STORE FULL CONFIG, result=" + res);
        return config;
    }
    private boolean sendCurrentConfig(Bundle config) {
//        Bundle config = ACommon.readPersistentDataFromFile(
//                AWearListenerService.this, getString(R.string.configFileName));
        //Log.i(TAG, "(((^ SEND CURRENT CONFIG, result=" + (config!=null));
        if (null == config) {
            return false;
        } else {
            //Log.i(TAG, "((( config=" + config);
            DataMap dataMapConfig = DataMap.fromBundle(config);
            long cfg_time = System.currentTimeMillis();
            //dataMapConfig.putLong(ACommon.CFG_TIME, cfg_time);
            dataMapConfig.putLong(ACommon.KEY_TIME, cfg_time);
            dataMapConfig.putInt(ACommon.KEY_EVENT, ACommon.EVT_CURRENT_CONFIG);
            //Log.i(TAG, "(((^ dataMap=" + dataMapConfig);
            //new SendThroughWearNetworkThread(ACommon.FROM_WEAR_PATH, dataMapConfig).start();
            //new SendThroughWearNetworkThread(ACommon.ASYNC_REPLY_PATH, dataMapConfig).start();
            new ACommon.WearNetSend(ACommon.ASYNC_REPLY_PATH, dataMapConfig, mGoogleApiClient, null).start();
        }
        return true;
    }

    private void broadcastFloatToWearFaceService(int event, long time, float level) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        //intent.setComponent(new ComponentName("com.luna_78.airforceru", "com.luna_78.airforceru.AWearFaceService"));
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_LEVEL, level);
        //sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void broadcastLongToWearFaceService(int event, long time, long delay) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        //intent.setComponent(new ComponentName("com.luna_78.airforceru", "com.luna_78.airforceru.AWearFaceService"));
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
        intent.putExtra(ACommon.KEY_DELAY, delay);
        //sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void broadcastIntToWearFaceService(int event, long time, int value) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        //intent.setComponent(new ComponentName("com.luna_78.airforceru", "com.luna_78.airforceru.AWearFaceService"));
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
        intent.putExtra(ACommon.KEY_COLOR, value);
        //sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void broadcastEmptyToWearFaceService(int event, long time) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        //intent.setComponent(new ComponentName("com.luna_78.airforceru", "com.luna_78.airforceru.AWearFaceService"));
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
        intent.putExtra(ACommon.KEY_VALUE, 0);
        //sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    //broadcastDatamapToWearFaceService(evtType, time, dataMap);
    private void broadcastDatamapToWearFaceService(int event, long time, String key, DataMap dataMap) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
        intent.putExtra(key, dataMap.toBundle()); // ACommon.KEY_CFGPAL_CONFIG
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


//    class SendThroughWearNetworkThread extends Thread {
//        String path;
//        DataMap dataMap;
//
//        // Constructor for sending data objects to the data layer
//        SendThroughWearNetworkThread(String p, DataMap data) {
//            path = p;
//            dataMap = data;
//        }
//
//        public void run() {
//            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//                for (Node node : nodes.getNodes()) {
//
//                    // Construct a DataRequest and send over the data layer
//                    PutDataMapRequest putDMR = PutDataMapRequest.create(path);
//                    putDMR.getDataMap().putAll(dataMap);
//                    PutDataRequest request = putDMR.asPutDataRequest();
//                    DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
//                    if (result.getStatus().isSuccess()) {
//                        //Log.i(TAG, "SendThroughWearNetworkThread DataMap: " + dataMap + " sent to: " + node.getDisplayName());
//                    } else {
//                        // Log an error
//                        //Log.i(TAG, "SendThroughWearNetworkThread ERROR: failed to send DataMap");
//                    }
//                }
//            }
//        }
//    } // class SendTroughWearNetworkThread





    @Override // WearableListenerService        ???
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        //Log.i(TAG, "ListenerService: onPeerConnected: " + peer);
    }

    @Override // WearableListenerService        ???
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        //Log.i(TAG, "ListenerService: onPeerDisconnected: " + peer);
    }

} // class AirForceRuListenerService
