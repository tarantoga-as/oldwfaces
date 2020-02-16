package com.luna_78.wear.watch.face.raf3078;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.luna_78.wear.watch.face.raf3078.common.ACommon;
import com.luna_78.wear.watch.face.raf3078.common.Layout;

import java.io.File;

/**
 * Created by buba on 11/03/15.
 */
public class PageFragmentCollection extends Fragment {

    private static final String TAG = "PFCLL";

    HandheldCompanionConfigActivity mActivity;
    private APhoneService mService;
    private GoogleApiClient mGoogleApiClient;

    //private View mView;
    private int mPage;

    public ExtendedEditText mEditElementName;
    private InputFilter[] mFilterArray = new InputFilter[1];
    private View.OnKeyListener mOnKeyListener;
    //private ImageButton mButtonSetElementName;
    private ListView mListView;

    //private Bitmap mBmpShare;

    DrawableButton mShareDrawable;

    LinearLayout ltFakeFocus;


    public static PageFragmentCollection newInstance(int page) {
        //Log.i(TAG, "((( PageFragmentCollection newInstance = " + page);
        Bundle args = new Bundle();
        args.putInt(ACommon.ARG_PAGE, page);
        PageFragmentCollection fragment = new PageFragmentCollection();
        fragment.setArguments(args);
        return fragment;
    }


    private BroadcastReceiver mDataFromService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean trigger;
            if (!action.equals(ACommon.EVENT_ACTION)) return;
            Bundle bundle = intent.getExtras();
            //Log.i(TAG, "*** onReceive" );
            if (bundle != null) {
                int event = bundle.getInt(ACommon.BCAST_EXTRA_EVENT_TYPE);
                switch (event) {
//                    case ACommon.EVT_WEARCFG_TOGGLE_PHONE_BATTERY:
//                        trigger = bundle.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false);
//                        Log.i(TAG, "### EVT_WEARCFG_TOGGLE_PHONE_BATTERY=" + trigger);
//                        mShowHandheldBatteryTrigger.setChecked(trigger);
//                        break;
//
//                    case ACommon.EVT_WEARCFG_TOGGLE_ANIMATION:
//                        trigger = bundle.getBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, false);
//                        Log.i(TAG, "### EVT_WEARCFG_TOGGLE_ANIMATION=" + trigger);
//                        mShowAnimationTrigger.setChecked(trigger);
//                        break;

                    case ACommon.EVT_WEARCFG_TOGGLE_LAYOUT:
                        //Log.i(TAG, "### EVT_WEARCFG_TOGGLE_LAYOUT");
                        //toggleLayout();
                        break;


                    case ACommon.EVT_SIGNAL_HOLDOFF_UPDATE:
                        //Log.i(TAG, "((( EVT_SIGNAL_HOLDOFF_UPDATE, mStartChooser=" + mStartChooser);
                        if (mStartChooser) {
                            mCommonHandler.postDelayed(taskSendConfigFileByMail, 100);
                            mStartChooser = false;
                        }
                        break;


                    default:
                        break;
                }
            }
        }
    };



//    private void toggleLayout() {
//        int numConfigs = mService.mLayoutsPalette.size();
//        if (numConfigs == 0) return;
//        int position = mService.mLayoutsPaletteIndex;
//        if (++position >= numConfigs) {
//            position = 0;
//        }
//        mService.mLayoutsPaletteIndex = position;
//        Bundle configPaletteElement = ACommon.deserializeBundle(mService.mLayoutsPalette.get(position));
//        configPaletteElement.remove(ACommon.KEY_CFGPAL_NAME);
//        configPaletteElement.remove(ACommon.KEY_CFGPAL_ICON);
//        sendFullConfigToSet(configPaletteElement);
//    }



    private void initExternalGlobals() {
        //Log.i(TAG, "((( initExternalGlobals");
        mActivity = (HandheldCompanionConfigActivity) getActivity();
        mGoogleApiClient = mActivity.getGoogleApiClient();
        mService = mActivity.getBoundService();
        //Log.i(TAG, "((( initExternalGlobals, mActivity=" + mActivity + ", mService=" + mService);
    } // initExternalGlobals


    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "((( onCreate");
        super.onCreate(savedInstanceState);
        //mPage = getArguments().getInt(ACommon.ARG_PAGE);
        initExternalGlobals();
        mFilterArray[0] = new InputFilter.LengthFilter(50);

        mShareDrawable = new DrawableButton(ACommon.loadBitmap(mActivity, R.drawable.ic_share), 5);

    }

    @Override
    public void onPause() {
        //Log.i(TAG, "((( onPause");
        super.onPause();

//        mEditElementName.isShown();
//        mEditElementName.getVisibility();
//        mEditElementName.getWindowVisibility();
//        Log.i(TAG, "((( onPause, mEditElementName isShown=" + mEditElementName.isShown() + ", getVisibility=" + mEditElementName.getVisibility() +
//                ", getWindowVisibility=" + mEditElementName.getWindowVisibility());

        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditElementName.getApplicationWindowToken(), 0);
        mEditElementName.clearEditElementNameTuple();

        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mDataFromService);
    }

    @Override
    public void onResume() {
        //Log.i(TAG, "((( onResume");
        super.onResume();
        initExternalGlobals();
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mDataFromService, new IntentFilter(ACommon.EVENT_ACTION));
        mEditElementName.clearEditElementNameTuple();

//        View current = getView().findFocus();
//        if (current != null) current.clearFocus();
//
//        ltFakeFocus.requestFocus();

        //mEditElementName.requestFocus();
        mEditElementName.clearFocus();
        ltFakeFocus.requestFocus();

//        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(mEditElementName.getApplicationWindowToken(), 0);

        View view = mActivity.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        //Log.i(TAG, "((( onDestroyView");
        super.onDestroyView();
        mListView = null;
        mEditElementName = null;
        mActivity.mAdapter.setEditElementNameTuple(null);
        mActivity.mAdapter.setCommonHandler(null);
        mOnKeyListener = null;
    }


    @Override
    public void onDestroy() {
        //Log.i(TAG, "((( onDestroy");
        super.onDestroy();
        mCommonHandler.removeMessages(ACommon.MSG_DELETION_TIMEOUT);

        mShareDrawable.clearBitmapReference(); mShareDrawable = null;

        System.gc();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.i(TAG, "((( onCreateView");

        initExternalGlobals();

        View view = inflater.inflate(R.layout.fragment_collection, container, false);
        //mView = view;

        ltFakeFocus = (LinearLayout) view.findViewById(R.id.ltFakeFocusCollection);

        mListView = (ListView) view.findViewById(R.id.listConfigPalette);

        mEditElementName = (ExtendedEditText) view.findViewById(R.id.editElementName);
        mEditElementName.setListView(mListView);
        mEditElementName.setFilters(mFilterArray);
        mOnKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Log.i(TAG, "((( OnKeyListener, event.getAction()=" + event.getAction() + ", keyCode=" + keyCode);
                //OnKeyListener, event.getAction()=0, keyCode=66
//                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
//                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(mEditElementName.getApplicationWindowToken(), 0);
//
//                    Integer pos = (Integer) mEditElementName.getTag(R.id.tag_element_name);
//                    mActivity.setConfigPaletteElementName(pos, mEditElementName.getText().toString());
//                    clearEditElementNameTuple();
//
//                    return true;
//                }
                return false;
            }
        };
        mEditElementName.setOnKeyListener(mOnKeyListener);
        //
        mEditElementName.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                        //Log.i(TAG, "((( onEditorAction, actionId=" + actionId);
                        if (event != null) {
                            //Log.i(TAG, "((( onEditorAction, event.getAction()=" + event.getAction());
                        }

                        if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mEditElementName.getApplicationWindowToken(), 0);

                            Integer pos = (Integer) mEditElementName.getTag(R.id.tag_element_name);
                            mActivity.setConfigPaletteElementName(pos, mEditElementName.getText().toString());
                            mEditElementName.clearEditElementNameTuple();

                            return true;
                        }
                        return false;
                    }
                }
        );
        //
        mEditElementName.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        //Log.i(TAG, "((( onFocusChange, hasFocus=" + hasFocus + ", mAdapter.mLocked=" + mActivity.mAdapter.mLocked);
                        if (hasFocus == true && !mActivity.mAdapter.mLocked) {
                            //mImporterActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(/*v*/mEditElementName, InputMethodManager.SHOW_FORCED);
                        } else if (hasFocus == false) {
//                            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
//                            imm.showSoftInput(/*v*/mEditElementName, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        }
                    }
                }
        );

//        mButtonSetElementName = (ImageButton) mView.findViewById(R.id.btnSetElementName);
//        mButtonSetElementName.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Integer pos = (Integer) mEditElementName.getTag(R.id.tag_element_name);
//                        Log.i(TAG, "#### SetElementName, pos=" + pos);
//                        if (null != pos) {
//                            //Editable text = mEditElementName.getText();
//                            //String cpName = String.valueOf(pos + 1) + text.toString();
//                            mImporterActivity.setConfigPaletteElementName(pos, mEditElementName.getText().toString());
//                            clearEditElementNameTuple();
//                        }
//                    }
//                }
//        );

//        mImporterActivity.mAdapter.setEditElementNameTuple(mEditElementName, mButtonSetElementName);
        mActivity.mAdapter.setEditElementNameTuple(mEditElementName);

        mEditElementName.clearEditElementNameTuple();

        mActivity.mAdapter.setCommonHandler(mCommonHandler);

        //mAdapter = new ConfigPaletteAdapter(getActivity());
        mListView.setScrollbarFadingEnabled(false);
        mListView.setAdapter(mActivity.mAdapter);
        mActivity.mAdapter.setListView(mListView);
//        mListView.setOnItemLongClickListener(
//                new AdapterView.OnItemLongClickListener() {
//                    @Override
//                    public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
//                        Log.i(TAG, "*** onItemLongClick, pos=" + position + ", view=" + view);
//                        final int pos = position;
//                        view.animate().setDuration(1000).alpha(0)
//                                .withEndAction(
//                                        new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                //mImporterActivity.mLayoutsPalette.remove(pos);
//                                                mActivity.removeConfigPaletteElement(pos);
//                                                clearEditElementNameTuple();
//                                                //mImporterActivity.mAdapter.notifyDataSetChanged();
//                                                view.setAlpha(1);
//                                            }
//                                        }
//                                );
//
////                        mImporterActivity.mLayoutsPalette.remove(position);
////                        mAdapter.notifyDataSetChanged();
//                        //ACommon.SaveConfigPalette(mService.mLayoutsPalette, getString(R.string.configFileName), mImporterActivity);
//                        return true;
//                        //return false;
//                    }
//                }
//        );

        mListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Layout layoutElement = mService.gLayoutsPalette.get(position);
                        if (null != mEditElementName) {
                            mEditElementName.setEnabled(true);
                            mEditElementName.setText(layoutElement.name);
                            mEditElementName.setTag(R.id.tag_element_name, position);
                            mEditElementName.requestFocus();
                        }
                    }
                }
        );


        //mBmpShare = ACommon.loadBitmap(mActivity, R.drawable.ic_share);
        ImageButton buttonShare = (ImageButton) view.findViewById(R.id.btnShare);
        mShareDrawable.clearScaledBitmapReference();
        buttonShare.setBackground(mShareDrawable);
        //ACommon.loadBitmap(mActivity, R.drawable.ic_share)
        //buttonShare.setImageBitmap();
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // put code here
                String fname = getString(R.string.configFileName) + "." + getString(R.string.product_id);
                sendConfigFileByMail(mActivity, fname);

                final View view = v;
                v.animate().setDuration(500).alpha(0.5f).withEndAction(
                        new Runnable() {
                            @Override
                            public void run() {
                                view.setAlpha(1);
                            }
                        }
                );
                mActivity.mVibrator.vibrate(50);
            }
        });


        return view;
    } // onCreateView

//    private void clearEditElementNameTuple() {
//        if (mListView != null) mListView.requestFocus();
//        if (mEditElementName != null) {
//            mEditElementName.setTag(R.id.tag_element_name, null);
//            mEditElementName.setText("");
//            mEditElementName.setEnabled(false);
//        }
//        //deactivateEditElementNameTuple();
//    }
    private void deactivateEditElementNameTuple() {
        if (mListView != null) mListView.requestFocus();
        if (mEditElementName != null) {
            //mEditElementName.clearFocus();
            mEditElementName.setEnabled(false);
        }
//        if (mButtonSetElementName != null) {
//            mButtonSetElementName.setEnabled(false);
//        }
    }
    private void activateEditElementNameTuple() {
        if (mEditElementName != null) {
            mEditElementName.setEnabled(true);
        }
//        if (mButtonSetElementName != null) {
//            mButtonSetElementName.setEnabled(true);
//        }
    }



//    private void sendFullConfigToSet(Bundle config) {
//        boolean trigger;
//        //
//        DataMap dataMap = DataMap.fromBundle(config);
//        DataMap dataMapToSend = dataMap.getDataMap(ACommon.KEY_CFGPAL_CONFIG);
//        //dataMap.putAll(DataMap.fromBundle(config));
//        //
//        trigger = mService.getShowAnimationTrigger();
//        dataMapToSend.putBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, trigger);
//        trigger = mService.getShowHandheldBatteryTrigger();
//        dataMapToSend.putBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, trigger);
//        trigger = mService.getShowHrDigitsReliefTrigger();
//        dataMapToSend.putBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, trigger);
//        //
//        dataMapToSend.putInt(ACommon.KEY_EVENT, ACommon.EVT_SET_FULL_CONFIG);
//        dataMapToSend.putLong(ACommon.KEY_TIME, new Date().getTime());
//        //dataMapToSend.putDataMap();
//        //dataMap.putInt(ACommon.KEY_COLOR, color);
//        //dataMap.putInt(ACommon.KEY_COLOR_INDEX, index);
//        //dataMap
//        new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMapToSend, mGoogleApiClient).start();
//
////        trigger = dataMap.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false);
////        mService.setShowHandheldBatteryTrigger(trigger, TAG);
////        if (mImporterActivity.fragmentSettings != null && mImporterActivity.fragmentSettings.mShowHandheldBatteryTrigger != null) {
////            mImporterActivity.fragmentSettings.mShowHandheldBatteryTrigger.setChecked(trigger);
////        }
////        trigger = dataMap.getBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, false);
////        mService.setShowAnimationTrigger(trigger, TAG);
//
//    } // sendFullConfigToSet


//    static class SendThroughWearNetworkThread extends Thread {
//        String path;
//        DataMap dataMap;
//        GoogleApiClient mGoogleApiClient;
//
//        // Constructor for sending data objects to the data layer
//        SendThroughWearNetworkThread(String p, DataMap data, GoogleApiClient googleApiClient) {
//            path = p;
//            dataMap = data;
//            mGoogleApiClient = googleApiClient;
//        }
//
//        public void run() {
//            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//            for (Node node : nodes.getNodes()) {
//
//                // Construct a DataRequest and send over the data layer
//                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
//                putDMR.getDataMap().putAll(dataMap);
//                PutDataRequest request = putDMR.asPutDataRequest();
//                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient,request).await();
//                if (result.getStatus().isSuccess()) {
//                    Log.i(TAG, "SendThroughWearNetworkThread DataMap: " + dataMap + " sent to: " + node.getDisplayName());
//                } else {
//                    // Log an error
//                    Log.i(TAG, "SendThroughWearNetworkThread ERROR: failed to send DataMap");
//                }
//            }
//        }
//    } // class SendTroughWearNetworkThread




    private volatile boolean mStartChooser = false;

    private void sendConfigFileByMail(Context context, String fname) {
        // also see here: http://richardleggett.co.uk/blog/2013/01/26/registering_for_file_types_in_android/
        mActivity.requestSaveLayoutPalette(true);
        //
        tFileName = fname;
        tContext = context;
        mStartChooser = true;
        //mCommonHandler.postDelayed(taskSendConfigFileByMail, 500);
    }
    private volatile String tFileName = null;
    private volatile Context tContext = null;
    private Runnable taskSendConfigFileByMail = new Runnable() {
        @Override
        public void run() {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            File newFile = new File(tContext.getFilesDir(), tFileName);
            //Uri contentUri = FileProvider.getUriForFile(tContext, "com.luna_78.wear.watch.face.raf3078.fileprovider", newFile);
            Uri contentUri = FileProvider.getUriForFile(tContext, tContext.getString(R.string.fp_authority_0), newFile);
            //Log.i(TAG, "(((( contentUri = " + contentUri.toString());
            ACommon.grantUriPermissionToMailPackages(tContext, contentUri, emailIntent);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch face ✮" + mActivity.productId + "✮ collection (" +
                    mService.gLayoutsPalette.size() + " elements).");
            //
            emailIntent.putExtra(Intent.EXTRA_STREAM, /*attachmentFileName*/ /*Uri.fromFile(file)*/ contentUri);
            //emailIntent.setDataAndType(contentUri, "application/raf3078");
            //
            //emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
            //
            tFileName = null;
            tContext = null;
            mStartChooser = false;
//        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivityForResult(Intent.createChooser(emailIntent, "Send email..."), ACommon.PICK_SENDER_REQUEST);
        }
    };
//    public static void grantUriPermissionToMailPackages(Context context, Uri contentUri, Intent emailIntent) {
//        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(emailIntent, 0);
//        if (!resInfo.isEmpty()) {
//            for (ResolveInfo resolveInfo : resInfo) {
//                String packageName = resolveInfo.activityInfo.packageName;
//                Log.i(TAG, "((((( packageName = " + packageName);
//                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            }
//        }
//    }






    class DrawableButton extends Drawable {

        Bitmap      mBitmap, mBitmapS = null;
        int         mSub = 0;
        int         mAlpha = 150;
        boolean     isSquare = true;
        int         bmpWidth, bmpHeight;
        int         bmpWidthScaled, bmpHeightScaled;
        float       bmpScale;

        public void setSquare(boolean value) { isSquare = value; }

        DrawableButton(Bitmap bmp, int sub) {
            mBitmap = bmp;
            mSub = sub;

            if (null != mBitmap) {
                bmpWidth = mBitmap.getWidth();
                bmpHeight = mBitmap.getHeight();
            }
        }


        public void clearBitmapReference() {
            mBitmap = null;
        }

        public void clearScaledBitmapReference() {
            mBitmapS = null;
        }


        public void drawSquare(Canvas canvas) {
            //            int width = canvas.getWidth();
//            int height = canvas.getHeight();
//            int dimMin = Math.min(width, height);
//            float centerX = width / 2f;
//            float centerY = height / 2f;
//            int bX = (int) (centerX - (width-mSub)/2);
//            int bY = (int) (centerY - (height-mSub)/2);
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int dimMin = Math.min(width, height);
            int bmpSide = dimMin - mSub;
            float centerX = width / 2f;
            float centerY = height / 2f;
            float bX = (mSub + (width - dimMin)) / 2f;
            float bY = (mSub + (height - dimMin)) / 2f;
            if (bX < 1f) bX = 0f;
            if (bY < 1f) bY = 0f;
//            Log.i(TAG, "((( DrawableNextColorButton, w=" + width + ", h=" + height + ", min=" + dimMin + ", side=" + bmpSide +
//                    ", cx=" + centerX + ", cy=" + centerY + ", bx=" + bX + ", by=" + bY);
            Bitmap bitmap = null;

            //canvas.drawColor(0x77ff0000);

            if (null == mBitmapS) {
                if (null != mBitmap) {
                    mBitmapS = Bitmap.createScaledBitmap(mBitmap, bmpSide, bmpSide, true);
                    //mBitmap.recycle();
                    //recycleBitmapOnDestroy.remove(mBitmap);
                    mBitmap = null;
                }
            }

            bitmap = mBitmapS;

            if (null != bitmap) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setDither(true);
                paint.setFilterBitmap(true);
                paint.setAlpha(mAlpha);
                canvas.drawBitmap(bitmap, bX, bY, paint);
            }
        } // drawSquare


        public void drawOther(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            float centerX = width / 2f;
            float centerY = height / 2f;

            if (null == mBitmapS) {
                if (null != mBitmap) {
                    if (isSquare) {
                        bmpScale = (float)(width - 2*mSub) / (float)bmpWidth;
                    } else {
                        bmpScale = (float)(width/2 - 2*mSub) / ((float)bmpWidth / 2f);
                    }
                    bmpWidthScaled = (int) (bmpWidth * bmpScale);
                    bmpHeightScaled = (int) (bmpHeight * bmpScale);

                    mBitmapS = Bitmap.createScaledBitmap(mBitmap, bmpWidthScaled, bmpHeightScaled, true);
                    mBitmap = null;
                }
            }

            float bX = centerX - (bmpWidthScaled / 2f); if (bX < 1f) bX = 0f;
            float bY = centerY - (bmpHeightScaled / 2f); if (bY < 1f) bY = 0f;

//            Log.i(TAG, "((( DrawableButton, w=" + width + ", h=" + height + ", sc=" + String.format("%.3f", bmpScale) +
//                    ", bwSc=" + bmpWidthScaled + ", bhSc=" + bmpHeightScaled +
//                    ", cx=" + centerX + ", cy=" + centerY + ", bx=" + bX + ", by=" + bY);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
            paint.setAlpha(mAlpha);

            if (null != mBitmapS) {
                canvas.drawBitmap(mBitmapS, bX, bY, paint);
            }
        } // drawOther


        @Override
        public void draw(Canvas canvas) {
//            if (isSquare) drawSquare(canvas);
//            else drawOther(canvas);
            drawOther(canvas);
        }

        @Override
        public void setAlpha(int alpha) {
            //Log.i(TAG, "%%% DrawableButton, setAlpha=" + alpha);
            mAlpha = alpha;
        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    } // class DrawableButton













    public final Handler mCommonHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case ACommon.MSG_DELETION_TIMEOUT:
                    //Log.i(TAG, "((( DELETION TIMEOUT");
                    mListView.invalidateViews();
                    break;
            }
        }
    };


} // class PageFragmentCollection
