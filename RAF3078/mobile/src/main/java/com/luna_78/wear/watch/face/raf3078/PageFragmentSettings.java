package com.luna_78.wear.watch.face.raf3078;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.luna_78.wear.watch.face.raf3078.common.ACommon;
import com.luna_78.wear.watch.face.raf3078.common.AppPreferences;
import com.luna_78.wear.watch.face.raf3078.common.DemoPackData;
import com.luna_78.wear.watch.face.raf3078.common.Inscription;
import com.luna_78.wear.watch.face.raf3078.common.WatchAppearance;

/**
 * Created by buba on 11/03/15.
 */
public class PageFragmentSettings extends Fragment {

    private static final String TAG = "PFSET";

    private int mPage;
    private View mView;
    HandheldCompanionConfigActivity mActivity;
    private APhoneService mService;
    private GoogleApiClient mGoogleApiClient;

    SeekBar mSeekWakeDelay;
    //Button mButtonWake;
    TextView mTxtWakeDelay;

    public Switch mShowHandheldBatteryTrigger;
    //public Switch mShowAnimationTrigger;
    public Switch mShowHrDigitsReliefTrigger;
    public Switch mShowInscriptionsReliefTrigger;
    //switchShowDialGardient
    public Switch mShowDialGardientTrigger;
    public Switch mRespectBurnInTrigger;
    //public Switch mRespectLowBitTrigger;
    public Switch mSweepTrigger;
    //
    SeekBar mSeekFirstStop;
    SeekBar mSeekHalfEdgeStop;
    SeekBar mSeekEdgeAlpha;
    TextView mTxtEdgeAlpha;
    TextView mTxtFirstStop;
    TextView mTxtHalfEdgeStop;
    //
    SeekBar mSeekFirstStop1;
    SeekBar mSeekHalfEdgeStop1;
    SeekBar mSeekEdgeAlpha1;
    TextView mTxtEdgeAlpha1;
    TextView mTxtFirstStop1;
    TextView mTxtHalfEdgeStop1;
    //
    Button mBtnPutGradientValues;
    //
    Spinner mSpnrAuxBevelColor;
    Integer mSpnrAuxBevelColorTag = new Integer(-1);
    //
    Spinner mSpnrInvertGradient;
    Integer mSpnrInvertGradientTag = new Integer(-1);

    EditInscriptions mInscrPackEditor;

    InscriptionClipboard mInscrClipboard;

    Button mBtnPutHourMarks;
    Spinner mSpnrHourMarks[] = new Spinner[WatchAppearance.NUM_HOUR_MARKS];
    Spinner mSpnrHourMarksRelief[] = new Spinner[WatchAppearance.NUM_HOUR_MARKS];
    SeekBar mSeekHourMarksReliefStrength;
    TextView mTxtHourMarksReliefStrength;

    SeekBar mSeekInscriptionsReliefStrength;
    TextView mTxtInscriptionsReliefStrength;

    Switch mShowBurnInMargin;


    class GradientDrawable extends Drawable {

        SeekBar     firstStop, halfEdgeStop, halfEdgeAlpha;
        //int         dialColor;
        //int         color1, color2;
        int[]       backColors = new int[2];
        boolean     inverted = false;

        public GradientDrawable(SeekBar firstStop, SeekBar halfEdgeStop, SeekBar halfEdgeAlpha) {
            this.firstStop = firstStop;
            this.halfEdgeStop = halfEdgeStop;
            this.halfEdgeAlpha = halfEdgeAlpha;
        }

        public void setDialColor(int color) {
            //dialColor = color;
//            this.color1 = color;
//            this.color2 = color;
            backColors[0] = color;
            backColors[1] = color;
            this.invalidateSelf();
        }

        public void setDialColors(int color1, int color2) {
//            this.color1 = color1;
//            this.color2 = color2;
            backColors[0] = color1;
            backColors[1] = color2;
            this.invalidateSelf();
        }

        public void setInverted(boolean value) {
            inverted = value;
            this.invalidateSelf();
        }

        public void inversionChanged(int position) {

        }

        @Override
        public void draw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            //Log.i(TAG, "((( GradientDrawable draw, w=" + width + ", h=" + height);

            float margin = 7f;
            float radius = width / 2f;

            int edgeAlpha = getSeekValueInt(halfEdgeAlpha);
            float fs = getDgSeekValueFloat(firstStop);
            float he = getDgSeekValueFloat(halfEdgeStop);

            int i = 0;
            Shader[] grdShader = new Shader[2];
            for (int color: backColors) {

                int pixR = Color.red(color);
                int pixG = Color.green(color);
                int pixB = Color.blue(color);

                // from center to edge
                int[] colors = new int[4];
                float[] stops = new float[4];
                colors[0] = Color.argb(255, pixR, pixG, pixB);
                stops[0] = 0f;
                colors[1] = Color.argb(250, pixR, pixG, pixB);
                stops[1] = fs; //appearance.dgFirstStop;
                colors[2] = Color.argb(255 - (255-edgeAlpha)/2, pixR, pixG, pixB);
                stops[2] = he; //appearance.dgHalfEdgeStop;
                colors[3] = Color.argb(edgeAlpha, pixR, pixG, pixB);
                stops[3] = 1.0f;

                if (inverted) {
                    // from edge to center
                    colors[0] = Color.argb(edgeAlpha, pixR, pixG, pixB);
                    colors[1] = Color.argb(Math.min((int) (edgeAlpha + 255 * 0.02f), 255), pixR, pixG, pixB);
                    colors[3] = Color.argb(255, pixR, pixG, pixB);
                }

                grdShader[i] = new RadialGradient(width / 2f, height / 2f, radius-margin, colors, stops, Shader.TileMode.CLAMP);
                i++;
            }




            //int color = dialColor;


            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG + Paint.DITHER_FLAG);
            paint.setStyle(Paint.Style.FILL);
            //canvas.drawCircle(mVars.centerX, mVars.centerY, radius, paint);
            canvas.drawColor(Color.argb(255, 127, 127, 127));

            paint.setColor(Color.BLACK);
            canvas.drawRect(0 + margin, 0 + margin, width - margin, height - margin, paint);

            paint.setShader(grdShader[0]);
            canvas.drawRect(0+margin, 0+margin, (width/2f), height-margin, paint);

            paint.setShader(grdShader[1]);
            canvas.drawRect((width/2f), margin, width-margin, height-margin, paint);


        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    } // class GradientDrawable
    //
    GradientDrawable mGradientDrawableMainDial;
    GradientDrawable mGradientDrawableAuxDial;
    //
    SurfaceView mSrfcMainDialGradient, mSrfcAuxDialGradient;



    SeekBar mSeekPlateTextureStrength;
    TextView mTxtPlateTextureStrength;
    SeekBar mSeekAuxDialTextureStrength;
    TextView mTxtAuxDialTextureStrength;
    Button btnSendPlateTexturePack;
    SeekBar mSeekPlateReliefStrength;
    TextView mTxtPlateReliefStrength;
    //
    class MultiSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String s = String.valueOf(progress);
            int id = seekBar.getId();
            if (id == R.id.seekPlateTextureStrength) {
                mTxtPlateTextureStrength.setText(s);
            }
            if (id == R.id.seekAuxDialTextureStrength) {
                mTxtAuxDialTextureStrength.setText(s);
            }
            if (id == R.id.seekPlateReliefStrength) {
                mTxtPlateReliefStrength.setText(s);
            }
            if (id == R.id.seekHourMarksReliefStrength) {
                mTxtHourMarksReliefStrength.setText(s);
            }
            if (id == R.id.seekInscriptionsReliefStrength) {
                mTxtInscriptionsReliefStrength.setText(s);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
    //
    MultiSeekBarListener multiSeekBarListener = new MultiSeekBarListener();
    //
    private void initPlateTextureViews(Bundle config) {
        if (null == config) return;
        float strength;
        int progress;

        strength = config.getFloat(ACommon.CFG_PLATE_TEXTURE_STRENGTH, WatchAppearance.DEFAULT_PLATE_TEXTURE_STRENGTH);
        progress = (int) (strength * 100f);
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        mSeekPlateTextureStrength.setProgress(progress);

        strength = config.getFloat(ACommon.CFG_AUXDIAL_TEXTURE_STRENGTH, WatchAppearance.DEFAULT_AUXDIAL_TEXTURE_STRENGTH);
        progress = (int) (strength * 100f);
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        mSeekAuxDialTextureStrength.setProgress(progress);

        progress = config.getInt(ACommon.CFG_PLATE_RELIEF_STRENGTH, WatchAppearance.DEFAULT_PLATE_RELIEF_STRENGTH);
        mSeekPlateReliefStrength.setProgress(progress);

    }






    class DgSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String s = String.valueOf(progress);
            int id = seekBar.getId();
            if (id == R.id.seekEdgeAlpha) { mTxtEdgeAlpha.setText(s); mSrfcMainDialGradient.postInvalidate(); }
            if (id == R.id.seekFirstStop) { mTxtFirstStop.setText(s); mSrfcMainDialGradient.postInvalidate(); }
            if (id == R.id.seekHalfEdgeStop) { mTxtHalfEdgeStop.setText(s); mSrfcMainDialGradient.postInvalidate(); }
            if (id == R.id.seekEdgeAlpha1) { mTxtEdgeAlpha1.setText(s); mSrfcAuxDialGradient.postInvalidate(); }
            if (id == R.id.seekFirstStop1) { mTxtFirstStop1.setText(s); mSrfcAuxDialGradient.postInvalidate(); }
            if (id == R.id.seekHalfEdgeStop1) { mTxtHalfEdgeStop1.setText(s); mSrfcAuxDialGradient.postInvalidate(); }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    } // class DgSeekBarListener
    DgSeekBarListener mDgSeekBarListener = new DgSeekBarListener();

    class DgButtonListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            sendPhoneConfigGradientPack();
            //
            mService.putDgEdgeAlpha(getSeekValueInt(mSeekEdgeAlpha));
            mService.putDgFirstStop(getDgSeekValueFloat(mSeekFirstStop));
            mService.putDgHalfEdgeStop(getDgSeekValueFloat(mSeekHalfEdgeStop));
            //
            mService.putDgEdgeAlpha1(getSeekValueInt(mSeekEdgeAlpha1));
            mService.putDgFirstStop1(getDgSeekValueFloat(mSeekFirstStop1));
            mService.putDgHalfEdgeStop1(getDgSeekValueFloat(mSeekHalfEdgeStop1));
        }
    }
    DgButtonListener mDgButtonListener = new DgButtonListener();





    private void setDgSeekValue(SeekBar seekBar, float value) {
        int i = (int) (value * 100);
        seekBar.setProgress(i);
    }
    private void setDgSeekValue(SeekBar seekBar, int value) {
        seekBar.setProgress(value);
    }
    private int getSeekValueInt(SeekBar seekBar) {
        return seekBar.getProgress();
    }
    private float getDgSeekValueFloat(SeekBar seekBar) {
        int value = seekBar.getProgress();
        return (float) ((float)(value) / 100f);
    }





    public static PageFragmentSettings newInstance(int page) {
        //Log.i(TAG, "((( PageFragmentSettings newInstance = " + page);
        Bundle args = new Bundle();
        args.putInt(ACommon.ARG_PAGE, page);
        PageFragmentSettings fragment = new PageFragmentSettings();
        fragment.setArguments(args);
        return fragment;
    }

    private void initExternalGlobals() {
        //Log.i(TAG, "((( initExternalGlobals");
        mActivity = (HandheldCompanionConfigActivity) getActivity();
        mGoogleApiClient = mActivity.getGoogleApiClient();
        mService = mActivity.getBoundService();
    } // initExternalGlobals



    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "((( onCreate");
        super.onCreate(savedInstanceState);
        //mPage = getArguments().getInt(ACommon.ARG_PAGE);
//        mActivity = (HandheldCompanionConfigActivity) getActivity();
//        mGoogleApiClient = mActivity.getGoogleApiClient();
//        mService = mActivity.getBoundService();
        initExternalGlobals();
        //mActivity.fragmentSettings = this;
        mPlatePainter.setContext(getActivity().getApplicationContext());
        mInscrClipboard = new InscriptionClipboard();
    }

    @Override
    public void onDestroy() {
        //Log.i(TAG, "((( onDestroy");
        super.onDestroy();
        System.gc();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.i(TAG, "((( onCreateView");

        initExternalGlobals();

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mView = view;

        ScrollView scrollView = (ScrollView) mView.findViewById(R.id.scrollViewSettings);
        scrollView.setScrollbarFadingEnabled(false);

        mTxtWakeDelay = (TextView) mView.findViewById(R.id.txtWakeDelay);
        mSeekWakeDelay = (SeekBar) mView.findViewById(R.id.seekBarWakeDelay);
        //mSeekWakeDelay.setProgress(mImporterActivity.getSettingsWakeDelay());
        mSeekWakeDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mService != null) {
                    mService.setSettingsWakeDelay(progress);
                    String txt = String.valueOf(progress) + " sec";
                    mTxtWakeDelay.setText(txt);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

//        mButtonWake = (Button) mView.findViewById(R.id.buttonWake);
//        mButtonWake.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "onClick, view=" + v);
//                SeekBar seekBar = (SeekBar) mView.findViewById(R.id.seekBarWakeDelay);
//                if (seekBar != null) {
//                    int delay = seekBar.getProgress();
//                    //sendWakeup((long) delay);
//                    mService.setSettingsWakeDelay(delay);
//                }
//            }
//        });

        mShowHandheldBatteryTrigger = (Switch) mView.findViewById(R.id.switchShowHandheldBattery);
        //mShowHandheldBatteryTrigger.setChecked(mService.getShowHandheldBatteryTrigger());
        //Log.i(TAG, "###### mService.getShowHandheldBatteryTrigger()= " + mService.getShowHandheldBatteryTrigger());
        mShowHandheldBatteryTrigger.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //Log.i(TAG, "@@++@@ mShowHandheldBatteryTrigger onCheckedChanged=" + isChecked);
                        if (null == mService) return;
                        if (mService.getShowHandheldBatteryTrigger() != isChecked) {
                            mService.setShowHandheldBatteryTrigger(isChecked, TAG);
                            sendPhoneConfigBooleanOption(ACommon.EVT_HHCFG_SET_PHONE_BATTERY, ACommon.CFG_SHOW_HANDHELD_BATTERY, isChecked);
//                            if (isChecked == true) {
//                                // inform wear
//                            } else {
//                                // inform wear
//                            }
                        }
                    }
                }
        );
        //Log.i(TAG, "###### mService.getShowHandheldBatteryTrigger()= " + mService.getShowHandheldBatteryTrigger());

//        mShowAnimationTrigger = (Switch) mView.findViewById(R.id.switchShowAnimation);
//        //Log.i(TAG, "###### mService.getShowAnimationTrigger()= " + mService.getShowAnimationTrigger());
//        //mShowAnimationTrigger.setChecked(mService.getShowAnimationTrigger());
//        mShowAnimationTrigger.setOnCheckedChangeListener(
//                new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        Log.i(TAG, "@@@@@@ mShowAnimationTrigger onCheckedChanged=" + isChecked);
//                        if (null == mService) return;
//                        if (mService.getShowAnimationTrigger() != isChecked) {
//                            mService.setShowAnimationTrigger(isChecked, TAG);
//                            sendPhoneConfigBooleanOption(ACommon.EVT_HHCFG_SET_RIM_ANIMATION,
//                                    ACommon.CFG_SHOW_RIM_ANIMATION, isChecked);
////                            if (isChecked == true) {
////                                // inform wear
////                            } else {
////                                // inform wear
////                            }
//                        }
//                    }
//                }
//        );
        //Log.i(TAG, "###### mService.getShowAnimationTrigger()= " + mService.getShowAnimationTrigger());

        mShowHrDigitsReliefTrigger = (Switch) mView.findViewById(R.id.switchShowHrDigitsRelief);
        mShowHrDigitsReliefTrigger.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //Log.i(TAG, "@@@@@@ mShowHrDigitsReliefTrigger onCheckedChanged=" + isChecked);
                        if (null == mService) return;
                        if (mService.getShowHrDigitsReliefTrigger() != isChecked) {
                            mService.setShowHrDigitsReliefTrigger(isChecked, TAG);
                            sendPhoneConfigBooleanOption(ACommon.EVT_HHCFG_SET_HRDIGITS_RELIEF,
                                    ACommon.CFG_SHOW_HRDIGITS_RELIEF, isChecked);
                        }
                    }
                }
        );

        mShowInscriptionsReliefTrigger = (Switch) mView.findViewById(R.id.switchShowInscriptionsRelief);
        mShowInscriptionsReliefTrigger.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (null == mService) return;
                        if (mService.getShowInscriptionsReliefTrigger() != isChecked) {
                            mService.setShowInscriptionsReliefTrigger(isChecked, TAG);
                            sendPhoneConfigBooleanOption(ACommon.EVT_HHCFG_SET_INSCRIPTIONS_RELIEF,
                                    ACommon.CFG_SHOW_INSCRIPTIONS_RELIEF, isChecked);
                        }
                    }
                }
        );



        mRespectBurnInTrigger = (Switch) mView.findViewById(R.id.switchRespectBurnIn);
        mRespectBurnInTrigger.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //Log.i(TAG, "@@@@@@ mRespectBurnInTrigger onCheckedChanged=" + isChecked);
                        if (null == mService) return;
                        if (mService.getRespectBurnInTrigger() != isChecked) {
                            mService.setRespectBurnInTrigger(isChecked, TAG);
                            sendPhoneConfigBooleanOption(
                                    ACommon.EVT_HHCFG_SET_RESPECT_BURNIN, ACommon.CFG_RESPECT_BURNIN, isChecked);
                        }
                    }
                }
        );


//        mRespectLowBitTrigger = (Switch) mView.findViewById(R.id.switchRespectLowBit);
//        mRespectLowBitTrigger.setOnCheckedChangeListener(
//                new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        Log.i(TAG, "@@@@@@ mRespectLowBitTrigger onCheckedChanged=" + isChecked);
//                        if (null == mService) return;
//                        if (mService.getRespectLowBitTrigger() != isChecked) {
//                            mService.setRespectLowBitTrigger(isChecked, TAG);
//                            sendPhoneConfigBooleanOption(
//                                    ACommon.EVT_HHCFG_SET_RESPECT_LOWBIT, ACommon.CFG_RESPECT_LOWBIT, isChecked);
//                        }
//                    }
//                }
//        );
        mSweepTrigger = (Switch) mView.findViewById(R.id.switchSweep);
        mSweepTrigger.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //Log.i(TAG, "@@@@@@ mSweepTrigger onCheckedChanged=" + isChecked);
                        if (null == mService) return;
                        if (mService.getSweepTrigger() != isChecked) {
                            mService.setSweepTrigger(isChecked, TAG);
                            sendPhoneConfigBooleanOption(
                                    ACommon.EVT_HHCFG_SET_SWEEP, ACommon.CFG_SWEEP_SECONDS, isChecked);
                        }
                    }
                }
        );





        //switchShowDialGardient mShowDialGardientTrigger
        mShowDialGardientTrigger = (Switch) mView.findViewById(R.id.switchShowDialGardient);
        mShowDialGardientTrigger.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //Log.i(TAG, "@@@@@@ mShowDialGardientTrigger onCheckedChanged=" + isChecked);
                        if (null == mService) return;
                        if (mService.getShowDialGradientTrigger() != isChecked) {
                            mService.setShowDialGradientTrigger(isChecked, TAG);
                            sendPhoneConfigBooleanOption(ACommon.EVT_HHCFG_SET_DIAL_GRADIENT, ACommon.CFG_SHOW_DIAL_GRADIENT, isChecked);
                        }
                        mBtnPutGradientValues.setEnabled(isChecked);
                    }
                }
        );
        //
        mTxtEdgeAlpha = (TextView) mView.findViewById(R.id.txtEdgeAlpha);
        mTxtFirstStop = (TextView) mView.findViewById(R.id.txtFirstStop);
        mTxtHalfEdgeStop = (TextView) mView.findViewById(R.id.txtHalfEdgeStop);
        mSeekFirstStop = (SeekBar) mView.findViewById(R.id.seekFirstStop);
        mSeekFirstStop.setOnSeekBarChangeListener(mDgSeekBarListener);
        mSeekHalfEdgeStop = (SeekBar) mView.findViewById(R.id.seekHalfEdgeStop);
        mSeekHalfEdgeStop.setOnSeekBarChangeListener(mDgSeekBarListener);
        mSeekEdgeAlpha = (SeekBar) mView.findViewById(R.id.seekEdgeAlpha);
        mSeekEdgeAlpha.setOnSeekBarChangeListener(mDgSeekBarListener);
        mSrfcMainDialGradient = (SurfaceView) mView.findViewById(R.id.srfcMainDialGradient);
        mGradientDrawableMainDial = new GradientDrawable(mSeekFirstStop, mSeekHalfEdgeStop, mSeekEdgeAlpha);
        mSrfcMainDialGradient.setBackground(mGradientDrawableMainDial);
        //
        mTxtEdgeAlpha1 = (TextView) mView.findViewById(R.id.txtEdgeAlpha1);
        mTxtFirstStop1 = (TextView) mView.findViewById(R.id.txtFirstStop1);
        mTxtHalfEdgeStop1 = (TextView) mView.findViewById(R.id.txtHalfEdgeStop1);
        mSeekFirstStop1 = (SeekBar) mView.findViewById(R.id.seekFirstStop1);
        mSeekFirstStop1.setOnSeekBarChangeListener(mDgSeekBarListener);
        mSeekHalfEdgeStop1 = (SeekBar) mView.findViewById(R.id.seekHalfEdgeStop1);
        mSeekHalfEdgeStop1.setOnSeekBarChangeListener(mDgSeekBarListener);
        mSeekEdgeAlpha1 = (SeekBar) mView.findViewById(R.id.seekEdgeAlpha1);
        mSeekEdgeAlpha1.setOnSeekBarChangeListener(mDgSeekBarListener);
        mSrfcAuxDialGradient = (SurfaceView) mView.findViewById(R.id.srfcAuxDialGradient);
        mGradientDrawableAuxDial = new GradientDrawable(mSeekFirstStop1, mSeekHalfEdgeStop1, mSeekEdgeAlpha1);
        mSrfcAuxDialGradient.setBackground(mGradientDrawableAuxDial);
        //
        mBtnPutGradientValues = (Button) mView.findViewById(R.id.btnPutGradient);
        mBtnPutGradientValues.setOnClickListener(mDgButtonListener);
        //

        mSpnrAuxBevelColor = (Spinner) mView.findViewById(R.id.spnrAuxBevelColor);
        mSpnrAuxBevelColor.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //Object tagPos = parent.getTag();
                        Integer tagPos = (Integer) parent.getTag();
                        //Log.i(TAG, "((((( spinner, position=" + position + ", id=" + id + ", tag2=" + tagPos);

                        if (tagPos == null || (tagPos != null && position != tagPos)) {
                            //Log.i(TAG, "((((( spinner AuxBevelColor, NEW POSITION = " + position);
                            mService.setAuxBevelColor(position, TAG);
                            sendPhoneConfigIntegerOption(
                                    ACommon.EVT_HHCFG_SET_AUX_BEVEL_COLOR,
                                    ACommon.CFG_AUX_BEVEL_COLOR, position);
                        }

                        parent.setTag(null);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

        mSpnrInvertGradient = (Spinner) mView.findViewById(R.id.spnrInvertGradient);
        mSpnrInvertGradient.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //Object tagPos = parent.getTag();
                        Integer tagPos = (Integer) parent.getTag();
                        if ((tagPos != null && position != tagPos)) {
                            //Log.i(TAG, "((((( spinner InvertGradient, NEW POSITION = " + position);
                            mService.setInvertGradient(position, TAG);
//                            sendPhoneConfigIntegerOption(
//                                    ACommon.EVT_HHCFG_SET_INVERT_GRADIENT,
//                                    ACommon.CFG_INVERT_GRADIENT, position);

                            switch (position) {
                                case ACommon.GD_INVERT_ALL:
                                    mGradientDrawableMainDial.setInverted(true);
                                    mGradientDrawableAuxDial.setInverted(true);
                                    break;
                                case ACommon.GD_INVERT_AUX_DIALS:
                                    mGradientDrawableMainDial.setInverted(false);
                                    mGradientDrawableAuxDial.setInverted(true);
                                    break;
                                case ACommon.GD_INVERT_MAIN_DIAL:
                                    mGradientDrawableMainDial.setInverted(true);
                                    mGradientDrawableAuxDial.setInverted(false);
                                    break;
                                case ACommon.GD_INVERT_NONE:
                                    mGradientDrawableMainDial.setInverted(false);
                                    mGradientDrawableAuxDial.setInverted(false);
                                    break;
                            }

                        }

                        parent.setTag(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

        mInscrPackEditor = new EditInscriptions(mView);

        mBtnPutHourMarks = (Button) mView.findViewById(R.id.btnPutHourMarks);
        mBtnPutHourMarks.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendPhoneConfigHourMarksPack();
                    }
                }
        );
        mSpnrHourMarks[0] = (Spinner) mView.findViewById(R.id.spnrHourMark12);
        mSpnrHourMarks[1] = (Spinner) mView.findViewById(R.id.spnrHourMark1);
        mSpnrHourMarks[2] = (Spinner) mView.findViewById(R.id.spnrHourMark2);
        mSpnrHourMarks[3] = (Spinner) mView.findViewById(R.id.spnrHourMark3);
        mSpnrHourMarks[4] = (Spinner) mView.findViewById(R.id.spnrHourMark4);
        mSpnrHourMarks[5] = (Spinner) mView.findViewById(R.id.spnrHourMark5);
        mSpnrHourMarks[6] = (Spinner) mView.findViewById(R.id.spnrHourMark6);
        mSpnrHourMarks[7] = (Spinner) mView.findViewById(R.id.spnrHourMark7);
        mSpnrHourMarks[8] = (Spinner) mView.findViewById(R.id.spnrHourMark8);
        mSpnrHourMarks[9] = (Spinner) mView.findViewById(R.id.spnrHourMark9);
        mSpnrHourMarks[10] = (Spinner) mView.findViewById(R.id.spnrHourMark10);
        mSpnrHourMarks[11] = (Spinner) mView.findViewById(R.id.spnrHourMark11);
        //
        mSpnrHourMarksRelief[0] = (Spinner) mView.findViewById(R.id.spnrHourMark12Relief);
        mSpnrHourMarksRelief[1] = (Spinner) mView.findViewById(R.id.spnrHourMark1Relief);
        mSpnrHourMarksRelief[2] = (Spinner) mView.findViewById(R.id.spnrHourMark2Relief);
        mSpnrHourMarksRelief[3] = (Spinner) mView.findViewById(R.id.spnrHourMark3Relief);
        mSpnrHourMarksRelief[4] = (Spinner) mView.findViewById(R.id.spnrHourMark4Relief);
        mSpnrHourMarksRelief[5] = (Spinner) mView.findViewById(R.id.spnrHourMark5Relief);
        mSpnrHourMarksRelief[6] = (Spinner) mView.findViewById(R.id.spnrHourMark6Relief);
        mSpnrHourMarksRelief[7] = (Spinner) mView.findViewById(R.id.spnrHourMark7Relief);
        mSpnrHourMarksRelief[8] = (Spinner) mView.findViewById(R.id.spnrHourMark8Relief);
        mSpnrHourMarksRelief[9] = (Spinner) mView.findViewById(R.id.spnrHourMark9Relief);
        mSpnrHourMarksRelief[10] = (Spinner) mView.findViewById(R.id.spnrHourMark10Relief);
        mSpnrHourMarksRelief[11] = (Spinner) mView.findViewById(R.id.spnrHourMark11Relief);
        //
        mSeekHourMarksReliefStrength = (SeekBar) mView.findViewById(R.id.seekHourMarksReliefStrength);
        mTxtHourMarksReliefStrength = (TextView) mView.findViewById(R.id.txtHourMarksReliefStrength);
        mSeekHourMarksReliefStrength.setOnSeekBarChangeListener(multiSeekBarListener);
        //
        mSeekInscriptionsReliefStrength = (SeekBar) mView.findViewById(R.id.seekInscriptionsReliefStrength);
        mTxtInscriptionsReliefStrength = (TextView) mView.findViewById(R.id.txtInscriptionsReliefStrength);
        mSeekInscriptionsReliefStrength.setOnSeekBarChangeListener(multiSeekBarListener);

        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            ((ViewStub) mView.findViewById(R.id.stubDemo)).inflate();
            findDemoParametersViews(mView);
        }

        mSeekPlateTextureStrength = (SeekBar) mView.findViewById(R.id.seekPlateTextureStrength);
        mTxtPlateTextureStrength = (TextView) mView.findViewById(R.id.txtPlateTextureStrength);
        mSeekPlateTextureStrength.setOnSeekBarChangeListener(multiSeekBarListener);
        //
        mSeekAuxDialTextureStrength = (SeekBar) mView.findViewById(R.id.seekAuxDialTextureStrength);
        mTxtAuxDialTextureStrength = (TextView) mView.findViewById(R.id.txtAuxDialTextureStrength);
        mSeekAuxDialTextureStrength.setOnSeekBarChangeListener(multiSeekBarListener);
        //
        mSeekPlateReliefStrength = (SeekBar) mView.findViewById(R.id.seekPlateReliefStrength);
        mTxtPlateReliefStrength = (TextView) mView.findViewById(R.id.txtPlateReliefStrength);
        mSeekPlateReliefStrength.setOnSeekBarChangeListener(multiSeekBarListener);
        //
        btnSendPlateTexturePack = (Button) mView.findViewById(R.id.btnSendPlateTexturePack);
        btnSendPlateTexturePack.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendPlateTexturePack();
                    }
                }
        );

        mShowBurnInMargin = (Switch) mView.findViewById(R.id.swColorizeBurnInMargin);
//        mShowBurnInMargin.setOnCheckedChangeListener(showBurnInMarginListener);

        findTzInfoViews(mView);

        return mView;
    }


    class ShowBurnInMarginListener implements Switch.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            sendPhoneConfigBooleanOption(
                    ACommon.EVT_HHCFG_COLORIZE_BURNIN_MARGIN, ACommon.CFG_COLORIZE_BURNIN_MARGIN, isChecked);
        }
    }
    ShowBurnInMarginListener showBurnInMarginListener = new ShowBurnInMarginListener();


    Button mBtnSendDemoParameters;
    //
    TextView mTxtDemoDayOfMonth;
    TextView mTxtDemoHours;
    TextView mTxtDemoMinutes;
    TextView mTxtDemoMonth;
    TextView mTxtDemoPhoneBatt;
    TextView mTxtDemoResolution;
    TextView mTxtDemoSeconds;
    TextView mTxtDemoWearBatt;
    TextView mTxtDemoWeekday;
    TextView mTxtClipShotFps;
    TextView mTxtClipShotDuration;
    //
    Switch mSwDemoUseDate;
    Switch mSwDemoUsePack;
    Switch mSwDemoUsePhoneBatt;
    Switch mSwDemoResolution;
    Switch mSwDemoUseTime;
    Switch mSwDemoUseWearBatt;
    Switch mSwClipShot;
    //
    SeekBar mSeekDemoDayOfMonth;
    SeekBar mSeekDemoHour;
    SeekBar mSeekDemoMinutes;
    SeekBar mSeekDemoMonth;
    SeekBar mSeekDemoPhoneBatt;
    SeekBar mSeekDemoResolution;
    SeekBar mSeekDemoSeconds;
    SeekBar mSeekDemoWearBatt;
    SeekBar mSeekDemoWeekday;
    SeekBar mSeekClipShotFps;
    SeekBar mSeekClipShotDuration;
    //
    CheckBox mCheckBoxIconFramed;
    //
    class DemoSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            
            if (null == mService) return;
            
            String s = String.valueOf(progress);
            int id = seekBar.getId();
            if (id == R.id.seekDemoWeekday) {
                mTxtDemoWeekday.setText("weekday "+s);
                mService.demoPackData[DemoPackData.INDEX_WEEKDAY].value = progress;
            }
            if (id == R.id.seekDemoWearBatt) {
                mTxtDemoWearBatt.setText(s);
                mService.demoPackData[DemoPackData.INDEX_WEARBATT].value = progress;
            }
            if (id == R.id.seekDemoSeconds) {
                mTxtDemoSeconds.setText("sec "+s);
                mService.demoPackData[DemoPackData.INDEX_SECONDS].value = progress;
            }
            if (id == R.id.seekDemoResolution) {
                //mTxtDemoResolution.setText(s);
                mService.demoPackData[DemoPackData.INDEX_RESOLUTION].value = progress;
                int res = DemoPackData.getResolution(mService.demoPackData);
                if (DemoPackData.RESOLUTION_NATURAL == res) s = "natural";
                else s = String.valueOf(res);
                mTxtDemoResolution.setText(s);
            }
            if (id == R.id.seekDemoDayOfMonth) {
                mTxtDemoDayOfMonth.setText("day "+s);
                mService.demoPackData[DemoPackData.INDEX_DAYOFMONTH].value = progress;
            }
            if (id == R.id.seekDemoHours) {
                mTxtDemoHours.setText("hour "+s);
                mService.demoPackData[DemoPackData.INDEX_HOUR].value = progress;
            }
            if (id == R.id.seekDemoMinutes) {
                mTxtDemoMinutes.setText("min "+s);
                mService.demoPackData[DemoPackData.INDEX_MINUTES].value = progress;
            }
            if (id == R.id.seekDemoMonth) {
                mTxtDemoMonth.setText("month "+s);
                mService.demoPackData[DemoPackData.INDEX_MONTH].value = progress;
            }
            if (id == R.id.seekDemoPhoneBatt) {
                mTxtDemoPhoneBatt.setText(s);
                mService.demoPackData[DemoPackData.INDEX_PHONEBATT].value = progress;
            }
            if (id == R.id.seekClipShotDuration) {
                mTxtClipShotDuration.setText(s + " sec");
                mService.demoPackData[DemoPackData.INDEX_CLIP_SHOT_DURATION].value = progress;
            }
            if (id == R.id.seekClipShotFps) {
                mTxtClipShotFps.setText(s + " fps");
                mService.demoPackData[DemoPackData.INDEX_CLIP_SHOT_FPS].value = progress;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
    DemoSeekBarListener demoSeekBarListener = new DemoSeekBarListener();
    //
    class DemoSwitchListener implements Switch.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (null == mService) return;

            int id = buttonView.getId();

            switch (id) {
//                case R.id.swDemoUsePack:
//                    break;
                case R.id.swDemoUseDate:
                    mService.demoPackData[DemoPackData.TRIGGER_DATE].trigger = isChecked;
                    break;
                case R.id.swDemoUsePhoneBatt:
                    mService.demoPackData[DemoPackData.TRIGGER_PHONEBATT].trigger = isChecked;
                    break;
                case R.id.swDemoUseResolution:
                    mService.demoPackData[DemoPackData.TRIGGER_RESOLUTION].trigger = isChecked;
                    break;
                case R.id.swDemoUseTime:
                    mService.demoPackData[DemoPackData.TRIGGER_TIME].trigger = isChecked;
                    break;
                case R.id.swDemoUseWearBatt:
                    mService.demoPackData[DemoPackData.TRIGGER_WEARBATT].trigger = isChecked;
                    break;
                case R.id.swClipShot:
                    mService.demoPackData[DemoPackData.TRIGGER_CLIP_SHOT].trigger = isChecked;
                    break;
                default: break;
            }
        }
    }
    DemoSwitchListener demoSwitchListener = new DemoSwitchListener();
    
    

    private void findDemoParametersViews(View container) {
        if (!BuildConfig.BUILD_TYPE.equals("debug")) return;

        mBtnSendDemoParameters = (Button) container.findViewById(R.id.btnSendDemoParameters);
        mBtnSendDemoParameters.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendPhoneConfigDemoParametersPack();
                    }
                }
        );
        
        mTxtDemoDayOfMonth = (TextView) container.findViewById(R.id.txtDemoDayOfMonth);
        mTxtDemoHours = (TextView) container.findViewById(R.id.txtDemoHours);
        mTxtDemoMinutes = (TextView) container.findViewById(R.id.txtDemoMinutes);
        mTxtDemoMonth = (TextView) container.findViewById(R.id.txtDemoMonth);
        mTxtDemoPhoneBatt = (TextView) container.findViewById(R.id.txtDemoPhoneBatt);
        mTxtDemoResolution = (TextView) container.findViewById(R.id.txtDemoResolution);
        mTxtDemoSeconds = (TextView) container.findViewById(R.id.txtDemoSeconds);
        mTxtDemoWearBatt = (TextView) container.findViewById(R.id.txtDemoWearBatt);
        mTxtDemoWeekday = (TextView) container.findViewById(R.id.txtDemoWeekday);
        mTxtClipShotFps = (TextView) container.findViewById(R.id.txtClipShotFps);
        mTxtClipShotDuration = (TextView) container.findViewById(R.id.txtClipShotDuration);

        mSwDemoUseDate = (Switch) container.findViewById(R.id.swDemoUseDate);
        mSwDemoUsePack = (Switch) container.findViewById(R.id.swDemoUsePack);
        mSwDemoUsePhoneBatt = (Switch) container.findViewById(R.id.swDemoUsePhoneBatt);
        mSwDemoResolution = (Switch) container.findViewById(R.id.swDemoUseResolution);
        mSwDemoUseTime = (Switch) container.findViewById(R.id.swDemoUseTime);
        mSwDemoUseWearBatt = (Switch) container.findViewById(R.id.swDemoUseWearBatt);
        mSwClipShot = (Switch) container.findViewById(R.id.swClipShot);

        mSwDemoUseDate.setOnCheckedChangeListener(demoSwitchListener);
        mSwDemoUsePhoneBatt.setOnCheckedChangeListener(demoSwitchListener);
        mSwDemoResolution.setOnCheckedChangeListener(demoSwitchListener);
        mSwDemoUseTime.setOnCheckedChangeListener(demoSwitchListener);
        mSwDemoUseWearBatt.setOnCheckedChangeListener(demoSwitchListener);
        mSwClipShot.setOnCheckedChangeListener(demoSwitchListener);

        mSeekDemoDayOfMonth = (SeekBar) container.findViewById(R.id.seekDemoDayOfMonth);
        mSeekDemoHour = (SeekBar) container.findViewById(R.id.seekDemoHours);
        mSeekDemoMinutes = (SeekBar) container.findViewById(R.id.seekDemoMinutes);
        mSeekDemoMonth = (SeekBar) container.findViewById(R.id.seekDemoMonth);
        mSeekDemoPhoneBatt = (SeekBar) container.findViewById(R.id.seekDemoPhoneBatt);
        mSeekDemoResolution = (SeekBar) container.findViewById(R.id.seekDemoResolution);
        mSeekDemoSeconds = (SeekBar) container.findViewById(R.id.seekDemoSeconds);
        mSeekDemoWearBatt = (SeekBar) container.findViewById(R.id.seekDemoWearBatt);
        mSeekDemoWeekday = (SeekBar) container.findViewById(R.id.seekDemoWeekday);
        mSeekClipShotFps = (SeekBar) container.findViewById(R.id.seekClipShotFps);
        mSeekClipShotDuration = (SeekBar) container.findViewById(R.id.seekClipShotDuration);

        mSeekDemoDayOfMonth.setOnSeekBarChangeListener(demoSeekBarListener);
        mSeekDemoHour.setOnSeekBarChangeListener(demoSeekBarListener);
        mSeekDemoMinutes.setOnSeekBarChangeListener(demoSeekBarListener);
        mSeekDemoMonth.setOnSeekBarChangeListener(demoSeekBarListener);
        mSeekDemoPhoneBatt.setOnSeekBarChangeListener(demoSeekBarListener);
        mSeekDemoResolution.setOnSeekBarChangeListener(demoSeekBarListener);
        mSeekDemoSeconds.setOnSeekBarChangeListener(demoSeekBarListener);
        mSeekDemoWearBatt.setOnSeekBarChangeListener(demoSeekBarListener);
        mSeekDemoWeekday.setOnSeekBarChangeListener(demoSeekBarListener);
        mSeekClipShotFps.setOnSeekBarChangeListener(demoSeekBarListener);
        mSeekClipShotDuration.setOnSeekBarChangeListener(demoSeekBarListener);

        mCheckBoxIconFramed = (CheckBox) mView.findViewById(R.id.checkBoxIconFramed);
        Button buttonGetIcon = (Button) mView.findViewById(R.id.btnGetIcon);
        buttonGetIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap dialIcon, resultBitmap;//, dialBitmap, frameIcon, frameBitmap;
                //final float BASE_DIMENSION = 500f;

                dialIcon = mService.getLastDenseScreenshot();
                if (null == dialIcon) return;

//                int density = dialIcon.getDensity();
//                float dialWidth = dialIcon.getWidth();
//                float dialHeight = dialIcon.getHeight();
//                float scale = (dialWidth / BASE_DIMENSION);
//
//                Resources resources = mActivity.getApplicationContext().getResources();
//                BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
//                bmfOptions.inDensity = density; // !!!!!!!
//                bmfOptions.inScaled = false;
//                bmfOptions.inDither = false;
//                bmfOptions.inSampleSize = 1;
//                bmfOptions.inJustDecodeBounds = false;
//                //bmfOptions.inPreferQualityOverSpeed = true;
//                //bmfOptions.inPremultiplied = false;
//                bmfOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                //bmfOptions.outWidth = 564;
//                //bmfOptions.outHeight = 564;
//                frameBitmap = BitmapFactory.decodeResource(resources, R.drawable.adv_frame, bmfOptions);
//                frameIcon = Bitmap.createScaledBitmap(
//                        BitmapFactory.decodeResource(resources, R.drawable.adv_frame, bmfOptions),
//                        (int) (frameBitmap.getWidth() * scale) - 2, (int) (frameBitmap.getHeight() * scale) - 2, true
//                );
//
//                float resultWidth, resultHeight;
//                resultWidth = frameIcon.getWidth();
//                resultHeight = frameIcon.getHeight();
//                resultBitmap = Bitmap.createBitmap((int) resultWidth, (int) resultHeight, Bitmap.Config.ARGB_8888);
//
//                Canvas canvas = new Canvas();
//                Path clip = new Path();
//                Paint paint = new Paint(); paint.setAntiAlias(true); paint.setDither(true); paint.setFilterBitmap(true);
//
//                dialBitmap = Bitmap.createBitmap((int) dialWidth, (int) dialHeight, Bitmap.Config.ARGB_8888);
//                canvas.setBitmap(dialBitmap);
//                dialBitmap.eraseColor(Color.TRANSPARENT); // !!!!!!!!
//
//                canvas.save();
//                clip.addCircle(dialWidth / 2f, dialHeight / 2f, dialWidth / 2f, Path.Direction.CW);
//                canvas.clipPath(clip);
//                canvas.drawBitmap(dialIcon, 0f, 0f, null);
//                canvas.restore();
//                //
//                paint.setColor(Color.argb(255, 127, 127, 127));
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(1.5f);
//                canvas.drawPath(clip, paint);
//
//                resultBitmap.eraseColor(Color.TRANSPARENT);
//                //canvas = new Canvas();
//                canvas.setBitmap(resultBitmap);
//                canvas.drawBitmap(dialBitmap, (resultWidth - dialWidth) / 2f, (resultHeight - dialHeight) / 2f, null);
//                canvas.drawBitmap(frameIcon, 0, 0, null);

                if (mCheckBoxIconFramed.isChecked()) {
                    resultBitmap = ACommon.produceFramedIcon(mActivity.getApplicationContext(), dialIcon);
                } else {
                    resultBitmap = dialIcon;
                }

                String timeStamp = ACommon.bmpToPicturesDir(mActivity.getApplicationContext(), resultBitmap, "adv_", "_1");

                dialIcon = mService.getLastAmbientScreenshot();
                if (null != dialIcon) {
//                    dialBitmap.eraseColor(Color.TRANSPARENT); // !!!!!!!!
//                    //canvas = new Canvas();
//                    canvas.setBitmap(dialBitmap);
//                    canvas.save();
//                    clip.addCircle(dialWidth / 2f, dialHeight / 2f, dialWidth / 2f, Path.Direction.CW);
//                    canvas.clipPath(clip);
//                    canvas.drawBitmap(dialIcon, 0f, 0f, null);
//                    canvas.restore();
//                    //
//                    paint.setColor(Color.argb(255, 0, 0, 0));
//                    paint.setStyle(Paint.Style.STROKE);
//                    paint.setStrokeWidth(1.5f);
//                    canvas.drawPath(clip, paint);
//
//                    resultBitmap.eraseColor(Color.TRANSPARENT);
//                    //canvas = new Canvas();
//                    canvas.setBitmap(resultBitmap);
//                    canvas.drawBitmap(dialBitmap, (resultWidth - dialWidth) / 2f, (resultHeight - dialHeight) / 2f, null);
//                    canvas.drawBitmap(frameIcon, 0, 0, null);

                    //resultBitmap = ACommon.produceFramedIcon(mActivity.getApplicationContext(), dialIcon);
                    if (mCheckBoxIconFramed.isChecked()) {
                        resultBitmap = ACommon.produceFramedIcon(mActivity.getApplicationContext(), dialIcon);
                    } else {
                        resultBitmap = dialIcon;
                    }

                    ACommon.bmpToPicturesDir(mActivity.getApplicationContext(), resultBitmap, "adv_", "_2", timeStamp);
                }
            }
        });

    }
    //
    private void setProgressValue(SeekBar seekBar, int index) {
        int serviceProgress = mService.demoPackData[index].value;
        seekBar.setProgress(0);
        seekBar.setProgress(1);
        seekBar.setProgress(serviceProgress);
    }
    //
    private void initDemoParametersViews() {

        if (!BuildConfig.BUILD_TYPE.equals("debug")) return;

        if (null == mService) return;

        mSwDemoUseDate.setChecked(mService.demoPackData[DemoPackData.TRIGGER_DATE].trigger);
        mSwDemoUsePhoneBatt.setChecked(mService.demoPackData[DemoPackData.TRIGGER_PHONEBATT].trigger);
        mSwDemoResolution.setChecked(mService.demoPackData[DemoPackData.TRIGGER_RESOLUTION].trigger);
        mSwDemoUseTime.setChecked(mService.demoPackData[DemoPackData.TRIGGER_TIME].trigger);
        mSwDemoUsePhoneBatt.setChecked(mService.demoPackData[DemoPackData.TRIGGER_WEARBATT].trigger);
        mSwClipShot.setChecked(mService.demoPackData[DemoPackData.TRIGGER_CLIP_SHOT].trigger);

        setProgressValue(mSeekDemoWeekday, DemoPackData.INDEX_WEEKDAY);
        setProgressValue(mSeekDemoWearBatt, DemoPackData.INDEX_WEARBATT);
        setProgressValue(mSeekDemoSeconds, DemoPackData.INDEX_SECONDS);
        setProgressValue(mSeekDemoResolution, DemoPackData.INDEX_RESOLUTION);
        setProgressValue(mSeekDemoDayOfMonth, DemoPackData.INDEX_DAYOFMONTH);
        setProgressValue(mSeekDemoHour, DemoPackData.INDEX_HOUR);
        setProgressValue(mSeekDemoMinutes, DemoPackData.INDEX_MINUTES);
        setProgressValue(mSeekDemoMonth, DemoPackData.INDEX_MONTH);
        setProgressValue(mSeekDemoPhoneBatt, DemoPackData.INDEX_PHONEBATT);
        setProgressValue(mSeekClipShotFps, DemoPackData.INDEX_CLIP_SHOT_FPS);
        setProgressValue(mSeekClipShotDuration, DemoPackData.INDEX_CLIP_SHOT_DURATION);
    }





    Spinner mSpnrWatchTimeSource;
    Spinner mSpnrTzHemisphere;
    Button mBtnTzSave;
    EditText mEditDeviceTzLabel;
    EditText mEditUtcTzLabel;
    RadioGroup grpTzAlt;
    RadioButton rbtnTzAlt[] = new RadioButton[AppPreferences.NUM_ALT_TZS];
    Switch swTzAltActive[] = new Switch[AppPreferences.NUM_ALT_TZS];
    Spinner spnrTzAltIndex;
    EditText editTzAltLabel;

    class SwitchChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int index = -1;
            if (buttonView.equals(swTzAltActive[0])) index = 0;
            if (buttonView.equals(swTzAltActive[1])) index = 1;
            if (buttonView.equals(swTzAltActive[2])) index = 2;
            if (buttonView.equals(swTzAltActive[3])) index = 3;
            if (buttonView.equals(swTzAltActive[4])) index = 4;
            if (buttonView.equals(swTzAltActive[5])) index = 5;
            if (buttonView.equals(swTzAltActive[6])) index = 6;
            if (buttonView.equals(swTzAltActive[7])) index = 7;
            if (buttonView.equals(swTzAltActive[8])) index = 8;
            if (-1 == index) return;

            if (null == mService) return;
            AppPreferences appPreferences = mService.getWearAppPreference();
            if (null != appPreferences) {
                appPreferences.setAltTzActive(index, isChecked);
            }

        }
    }
    SwitchChangeListener switchTzAltActiveChangeListener = new SwitchChangeListener();

    private void findTzInfoViews(View container) {
        //mBtnSendDemoParameters = (Button) container.findViewById(R.id.btnSendDemoParameters);
        //spnrWatchTimeSource
        mSpnrWatchTimeSource = (Spinner) container.findViewById(R.id.spnrWatchTimeSource);
        mSpnrTzHemisphere = (Spinner) container.findViewById(R.id.spnrTzHemisphere);
        mEditDeviceTzLabel = (EditText) container.findViewById(R.id.editDeviceTzLabel);
        mEditUtcTzLabel = (EditText) container.findViewById(R.id.editUtcTzLabel);

        grpTzAlt = (RadioGroup) container.findViewById(R.id.grpTzAlt);
        rbtnTzAlt[0] = (RadioButton) container.findViewById(R.id.rbtnTzAlt1);
        rbtnTzAlt[1] = (RadioButton) container.findViewById(R.id.rbtnTzAlt2);
        rbtnTzAlt[2] = (RadioButton) container.findViewById(R.id.rbtnTzAlt3);
        rbtnTzAlt[3] = (RadioButton) container.findViewById(R.id.rbtnTzAlt4);
        rbtnTzAlt[4] = (RadioButton) container.findViewById(R.id.rbtnTzAlt5);
        rbtnTzAlt[5] = (RadioButton) container.findViewById(R.id.rbtnTzAlt6);
        rbtnTzAlt[6] = (RadioButton) container.findViewById(R.id.rbtnTzAlt7);
        rbtnTzAlt[7] = (RadioButton) container.findViewById(R.id.rbtnTzAlt8);
        rbtnTzAlt[8] = (RadioButton) container.findViewById(R.id.rbtnTzAlt9);
        swTzAltActive[0] = (Switch) container.findViewById(R.id.swTzAltActive1);
        swTzAltActive[1] = (Switch) container.findViewById(R.id.swTzAltActive2);
        swTzAltActive[2] = (Switch) container.findViewById(R.id.swTzAltActive3);
        swTzAltActive[3] = (Switch) container.findViewById(R.id.swTzAltActive4);
        swTzAltActive[4] = (Switch) container.findViewById(R.id.swTzAltActive5);
        swTzAltActive[5] = (Switch) container.findViewById(R.id.swTzAltActive6);
        swTzAltActive[6] = (Switch) container.findViewById(R.id.swTzAltActive7);
        swTzAltActive[7] = (Switch) container.findViewById(R.id.swTzAltActive8);
        swTzAltActive[8] = (Switch) container.findViewById(R.id.swTzAltActive9);

        spnrTzAltIndex = (Spinner) container.findViewById(R.id.spnrTzAltIndex);
        editTzAltLabel = (EditText) container.findViewById(R.id.editTzAltLabel);

        mBtnTzSave = (Button) container.findViewById(R.id.btnTzSave);



        mBtnTzSave.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null == mService) return;
                        AppPreferences appPreferences = mService.getWearAppPreference();
                        if (null != appPreferences) {
                            sendTimezonePack();
                        }
                    }
                }
        );



        for (int i=0; i<AppPreferences.NUM_ALT_TZS; i++) {
            swTzAltActive[i].setOnCheckedChangeListener(switchTzAltActiveChangeListener);
        }



        mSpnrWatchTimeSource.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (null == mService) return;
                        AppPreferences appPreferences = mService.getWearAppPreference();
                        if (null != appPreferences) {
                            appPreferences.setTzSourceForWatch(position);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );


        mSpnrTzHemisphere.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (null == mService) return;
                        AppPreferences appPreferences = mService.getWearAppPreference();
                        if (null != appPreferences) {
                            appPreferences.setTzHemisphere(position);
                            sendPhoneConfigIntegerOption(
                                    ACommon.EVT_HHCFG_SET_TZ_HEMISPHERE,
                                    AppPreferences.KEY_HEMISPHERE, position); //ACommon.CFG_TZ_HEMISPHERE
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );


        mEditDeviceTzLabel.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (null == mService) return;
                        AppPreferences appPreferences = mService.getWearAppPreference();
                        if (null != appPreferences) {
                            String txt = String.format("%.3s", mEditDeviceTzLabel.getText().toString());
                            appPreferences.setTzLabelForDevice(txt);
                        }
                    }
                }
        );


        mEditUtcTzLabel.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (null == mService) return;
                        AppPreferences appPreferences = mService.getWearAppPreference();
                        if (null != appPreferences) {
                            String txt = String.format("%.3s", mEditUtcTzLabel.getText().toString());
                            appPreferences.setTzLabelForUtc(txt);
                        }
                    }
                }
        );


        editTzAltLabel.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        int id = grpTzAlt.getCheckedRadioButtonId();
                        if (-1 == id) return;
                        if (null == mService) return;
                        AppPreferences appPreferences = mService.getWearAppPreference();
                        if (null != appPreferences) {
                            int index = findTzButtIndexBySelectedId(id);

                            String txt = String.format("%.3s", editTzAltLabel.getText().toString());
                            rbtnTzAlt[index].setText(txt);
                            appPreferences.setAltTzLabel(index, txt);
                        }
                    }
                }
        );


        spnrTzAltIndex.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int grpId = grpTzAlt.getCheckedRadioButtonId();
                        if (-1 == grpId) return;
                        if (null == mService) return;
                        AppPreferences appPreferences = mService.getWearAppPreference();
                        AppPreferences.TzList[] tzList = mService.getTzList();
                        if (null != appPreferences && null != tzList) {
                            int index = findTzButtIndexBySelectedId(grpId);

                            String txt = tzList[position].tzName;
                            appPreferences.setAltTzSystemSource(index, txt);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );


        grpTzAlt.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (null == mService) return;
                        AppPreferences appPreferences = mService.getWearAppPreference();
                        AppPreferences.TzList[] tzList = mService.getTzList();
                        if (null != appPreferences && null != tzList) {
                            int index = findTzButtIndexBySelectedId(checkedId);
                            //Log.i(TAG, "((( TZ index = " + index);
                            AppPreferences.AltTz[] altTzs = appPreferences.getAltTzs();
                            editTzAltLabel.setText(altTzs[index + 1].label);
                            int position = findTzPositionInTzList(altTzs[index + 1].systemSource, tzList);
                            spnrTzAltIndex.setSelection(position);
                        }
                    }
                }
        );
    }

    public int findTzPositionInTzList(String tz, AppPreferences.TzList[] tzList) {
        int result = -1;

        for (int i=0; i< tzList.length; i++) {
            if (tz.equals(tzList[i].tzName)) {
                result = i;
                break;
            }
        }

        if (-1 == result) return tzList.length-1;

        return result;
    }

    public int findTzButtIndexBySelectedId(int idSelected) {
        int result = 0;
        if (-1 != idSelected) {
            switch (idSelected) {
                case R.id.rbtnTzAlt1:
                    result = 0; break;
                case R.id.rbtnTzAlt2:
                    result = 1; break;
                case R.id.rbtnTzAlt3:
                    result = 2; break;
                case R.id.rbtnTzAlt4:
                    result = 3; break;
                case R.id.rbtnTzAlt5:
                    result = 4; break;
                case R.id.rbtnTzAlt6:
                    result = 5; break;
                case R.id.rbtnTzAlt7:
                    result = 6; break;
                case R.id.rbtnTzAlt8:
                    result = 7; break;
                case R.id.rbtnTzAlt9:
                    result = 8; break;
            }
        } else {
            grpTzAlt.check(R.id.rbtnTzAlt1);
        }
        return result;
    } // findTzButtIndexBySelectedId




    private void initTzInfoViews(AppPreferences appPreferences) {
        if (null == appPreferences) return;
        mSpnrWatchTimeSource.setSelection(appPreferences.getTzSourceForWatch());
        mSpnrTzHemisphere.setSelection(appPreferences.getTzHemisphere());
        mEditDeviceTzLabel.setText(appPreferences.getTzLabelForDevice());
        mEditUtcTzLabel.setText(appPreferences.getTzLabelForUtc());
        AppPreferences.AltTz altTzs[] = appPreferences.getAltTzs();
        for (int i=0; i<AppPreferences.NUM_ALT_TZS; i++) {
            rbtnTzAlt[i].setText(altTzs[i+1].label);
            swTzAltActive[i].setChecked(altTzs[i+1].isActive);
        }
        spnrTzAltIndex.setAdapter(new TzListAdapter(mService.getTzList()));

        if (null != mService) {
            AppPreferences.TzList[] tzList = mService.getTzList();
            if (null != tzList) {
                int id = grpTzAlt.getCheckedRadioButtonId();
                grpTzAlt.check(R.id.rbtnTzAlt1);
                if (id == R.id.rbtnTzAlt1) {
                    editTzAltLabel.setText(altTzs[1].label);
                    int position = findTzPositionInTzList(altTzs[1].systemSource, tzList);
                    spnrTzAltIndex.setSelection(position);
                }
            }
        }
    }

    class TzListAdapter extends BaseAdapter implements SpinnerAdapter {

        private static final String TAG = "TLA";

        AppPreferences.TzList[] tzList;
        LayoutInflater layoutInflater;

        public TzListAdapter(AppPreferences.TzList[] tzList) {
            this.tzList = tzList;
            if (null == this.tzList) return;

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //Log.i(TAG, "SIZE = " + this.tzList.length);
        }

        @Override
        public int getCount() {
            if (null == this.tzList) return 0;

            return tzList.length;
        }

        @Override
        public Object getItem(int position) {
            if (null == this.tzList) return null;

            return tzList[position];
        }

        @Override
        public long getItemId(int position) {
            if (null == this.tzList) return 0;

            return position;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (null == this.tzList) return null;

            View rowView = layoutInflater.inflate(R.layout.tz_list_row, parent, false);

            TextView txtTzName = (TextView) rowView.findViewById(R.id.txtTzName);
            txtTzName.setText(tzList[position].tzName);

            TextView txtSign = (TextView) rowView.findViewById(R.id.txtSign);
            String sign = "";
            if (tzList[position].tzOffs > 0) sign = "+";
            if (tzList[position].tzOffs < 0) sign = "-";
            txtSign.setText(sign);
            //
            TextView txtOffset = (TextView) rowView.findViewById(R.id.txtOffset);
            int hourOffs = Math.abs(tzList[position].tzOffs) / 3600000;
            int minuteOffs = Math.abs(tzList[position].tzOffs) / 60000;
            int nMinutes = minuteOffs - hourOffs * 60;
            String hours = String.format("%02d", hourOffs);
            String minutes = String.format("%02d", nMinutes);
            txtOffset.setText(hours + ":" + minutes); //String.valueOf(tzList[position].tzOffs)

            TextView txtDst = (TextView) rowView.findViewById(R.id.txtDst);
            txtDst.setText((tzList[position].tzDst ? "DST" : "-"));

            return rowView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //return super.getDropDownView(position, convertView, parent);
            if (null == this.tzList) return null;

            View rowView = layoutInflater.inflate(R.layout.tz_inline_view, parent, false);

            TextView txtDescr = (TextView) rowView.findViewById(R.id.txtTzDescr);
            String sign = "";
            if (tzList[position].tzOffs > 0) sign = "+";
            if (tzList[position].tzOffs < 0) sign = "-";
            //
            int hourOffs = Math.abs(tzList[position].tzOffs) / 3600000;
            int minuteOffs = Math.abs(tzList[position].tzOffs) / 60000;
            int nMinutes = minuteOffs - hourOffs * 60;

            String descr = tzList[position].tzName + ", " + sign + String.format("%02d", hourOffs) + ":" + String.format("%02d", nMinutes) +
                    ", " + (tzList[position].tzDst ? "DST" : "no DST");

            txtDescr.setText(descr);

            return rowView;
        }
    } // class TzListAdapter


    private void initHourMarksViews(Bundle config) {
        if (null == config) return;

        long[] longArr = config.getLongArray(ACommon.CFG_HOUR_MARKS);
        if (null == longArr) {
            for(int i=0; i<WatchAppearance.NUM_HOUR_MARKS; i++) mSpnrHourMarks[i].setSelection(WatchAppearance.DEFAULT_HOUR_MARK);
        } else {
            for (int i=0; i<WatchAppearance.NUM_HOUR_MARKS; i++) {
                mSpnrHourMarks[i].setSelection((int) longArr[i]);
            }
        }

        longArr = config.getLongArray(ACommon.CFG_HOUR_MARKS_RELIEF);
        if (null == longArr) {
            for(int i=0; i<WatchAppearance.NUM_HOUR_MARKS; i++) mSpnrHourMarksRelief[i].setSelection(WatchAppearance.DEFAULT_HOUR_MARK_RELIEF);
        } else {
            for (int i=0; i<WatchAppearance.NUM_HOUR_MARKS; i++) {
                mSpnrHourMarksRelief[i].setSelection((int) longArr[i]);
            }
        }

        int strength = config.getInt(ACommon.CFG_HOUR_MARKS_RELIEF_STRENGTH, -1);
        if (-1 == strength) mSeekHourMarksReliefStrength.setProgress(WatchAppearance.DEFAULT_HOUR_MARK_RELIEF_STRENGTH);
        else mSeekHourMarksReliefStrength.setProgress(strength);

    } // initHourMarksViews


    private void initPlateTriggerViews(Bundle config) {
        if (null == config) return;

        mShowBurnInMargin.setOnCheckedChangeListener(null);
        mShowBurnInMargin.setChecked(config.getBoolean(ACommon.CFG_COLORIZE_BURNIN_MARGIN, true));
        mShowBurnInMargin.setOnCheckedChangeListener(showBurnInMarginListener);

    }



    private void initOtherViews(Bundle config) {
        if (null == config) return;

        int strength = config.getInt(ACommon.CFG_INSCRIPTIONS_RELIEF_STRENGTH, -1);
        if (-1 == strength) mSeekInscriptionsReliefStrength.setProgress(WatchAppearance.DEFAULT_HOUR_MARK_RELIEF_STRENGTH);
        else mSeekInscriptionsReliefStrength.setProgress(strength);

        int colorBack = config.getInt(ACommon.CFG_COLOR_MAIN_BACKGROUND);
        int colorBigAuxBack = config.getInt(ACommon.CFG_COLOR_MAIN_BIGAUX_BACKGROUND);
        int colorSmlAuxBack = config.getInt(ACommon.CFG_COLOR_MAIN_SMALLAUX_BACKGROUND);
        mGradientDrawableMainDial.setDialColor(colorBack);
        mGradientDrawableAuxDial.setDialColors(colorBigAuxBack, colorSmlAuxBack);
        //
        int dgInvert = config.getInt(ACommon.CFG_INVERT_GRADIENT);
        switch (dgInvert) {
            case ACommon.GD_INVERT_ALL:
                mGradientDrawableMainDial.setInverted(true);
                mGradientDrawableAuxDial.setInverted(true);
                break;
            case ACommon.GD_INVERT_AUX_DIALS:
                mGradientDrawableMainDial.setInverted(false);
                mGradientDrawableAuxDial.setInverted(true);
                break;
            case ACommon.GD_INVERT_MAIN_DIAL:
                mGradientDrawableMainDial.setInverted(true);
                mGradientDrawableAuxDial.setInverted(false);
                break;
            case ACommon.GD_INVERT_NONE:
                mGradientDrawableMainDial.setInverted(false);
                mGradientDrawableAuxDial.setInverted(false);
                break;
        }
    }



    @Override
    public void onResume() {
        //Log.i(TAG, "((( onResume");
        super.onResume();

        initExternalGlobals();

        if (null == mService) return;

        mShowHandheldBatteryTrigger.setChecked(mService.getShowHandheldBatteryTrigger());
//        mShowAnimationTrigger.setChecked(mService.getShowAnimationTrigger());
        mShowHrDigitsReliefTrigger.setChecked(mService.getShowHrDigitsReliefTrigger());
        mShowInscriptionsReliefTrigger.setChecked(mService.getShowInscriptionsReliefTrigger());
        mSeekWakeDelay.setProgress(mService.getSettingsWakeDelay());
        mShowDialGardientTrigger.setChecked(mService.getShowDialGradientTrigger());
        mRespectBurnInTrigger.setChecked(mService.getRespectBurnInTrigger());
        //
        setDgSeekValue(mSeekFirstStop, mService.getDgFirstStop());
        setDgSeekValue(mSeekHalfEdgeStop, mService.getDgHalfEdgeStop());
        setDgSeekValue(mSeekEdgeAlpha, mService.getDgEdgeAlpha());
        //
        setDgSeekValue(mSeekFirstStop1, mService.getDgFirstStop1());
        setDgSeekValue(mSeekHalfEdgeStop1, mService.getDgHalfEdgeStop1());
        setDgSeekValue(mSeekEdgeAlpha1, mService.getDgEdgeAlpha1());
        //
        mSpnrAuxBevelColorTag = mService.getAuxBevelColor();
        mSpnrAuxBevelColor.setTag(mSpnrAuxBevelColorTag);
        mSpnrAuxBevelColor.setSelection(mSpnrAuxBevelColorTag);
        //
        mSpnrInvertGradientTag = mService.getInvertGradient();
        mSpnrInvertGradient.setTag(mSpnrInvertGradientTag);
        mSpnrInvertGradient.setSelection(mSpnrInvertGradientTag);
        //
        Bundle config = mService.getCurrentConfig();

        mInscrPackEditor.retrieveInscriptionPackFromBundle(config);

        initOtherViews(config);
        initHourMarksViews(config);
        initPlateTextureViews(config);
        initPlateTriggerViews(config);

        initTzInfoViews(mService.getWearAppPreference());
        //
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mDataFromService, new IntentFilter(ACommon.EVENT_ACTION));

        initDemoParametersViews();
    }


    @Override
    public void onPause() {
        //Log.i(TAG, "((( onPause");
        super.onPause();
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mDataFromService);
    }



    private BroadcastReceiver mDataFromService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean trigger;
            int intVal;
            if (!action.equals(ACommon.EVENT_ACTION)) return;
            Bundle bundle = intent.getExtras();
            //Log.i(TAG, "*** onReceive" );
            if (bundle != null) {
                int event = bundle.getInt(ACommon.BCAST_EXTRA_EVENT_TYPE);
                switch (event) {
                    case ACommon.EVT_WEARCFG_TOGGLE_PHONE_BATTERY:
                        trigger = bundle.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false);
                        //Log.i(TAG, "### EVT_WEARCFG_TOGGLE_PHONE_BATTERY=" + trigger);
                        mShowHandheldBatteryTrigger.setChecked(trigger);
                        break;
                    //
//                    case ACommon.EVT_WEARCFG_TOGGLE_ANIMATION:
//                        trigger = bundle.getBoolean(ACommon.CFG_SHOW_RIM_ANIMATION, false);
//                        Log.i(TAG, "### EVT_WEARCFG_TOGGLE_ANIMATION=" + trigger);
//                        mShowAnimationTrigger.setChecked(trigger);
//                        break;
                    //
                    case ACommon.EVT_WEARCFG_TOGGLE_HRDIGITS_RELIEF:
                        trigger = bundle.getBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, true);
                        //Log.i(TAG, "### EVT_WEARCFG_TOGGLE_HRDIGITS_RELIEF=" + trigger);
                        mShowHrDigitsReliefTrigger.setChecked(trigger);
                        break;
                    //
                    case ACommon.EVT_WEARCFG_TOGGLE_DIAL_GRADIENT:
                        trigger = bundle.getBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, true);
                        //Log.i(TAG, "### EVT_WEARCFG_TOGGLE_DIAL_GRADIENT=" + trigger);
                        mShowDialGardientTrigger.setChecked(trigger);
                        break;
                    //
                    case ACommon.EVT_WEARCFG_SET_AUX_BEVEL_COLOR:
                        mSpnrAuxBevelColorTag = bundle.getInt(ACommon.CFG_AUX_BEVEL_COLOR, ACommon.BEVEL_FROM_AUX);
                        mSpnrAuxBevelColor.setTag(mSpnrAuxBevelColorTag);
                        mSpnrAuxBevelColor.setSelection(mSpnrAuxBevelColorTag); // 0=from aux dial, 1=from main dial
                        break;
                    //
                    case ACommon.EVT_WEARCFG_SET_INVERT_GRADIENT:
                        mSpnrInvertGradientTag = bundle.getInt(ACommon.CFG_INVERT_GRADIENT, ACommon.GD_INVERT_NONE);
                        mSpnrInvertGradient.setTag(mSpnrInvertGradientTag);
                        mSpnrInvertGradient.setSelection(mSpnrInvertGradientTag);
                        break;
                    //
                    case ACommon.EVT_WEARCFG_SET_RESPECT_BURNIN:
                        trigger = bundle.getBoolean(ACommon.CFG_RESPECT_BURNIN, true);
                        //Log.i(TAG, "### EVT_WEARCFG_SET_RESPECT_BURNIN=" + trigger);
                        mRespectBurnInTrigger.setChecked(trigger);
//                        mPlatePainter.setDialPlateBitmap(null);
//                        mInscrPackEditor.mBtnDialPlate.invalidate();
                        break;
                    //
//                    case ACommon.EVT_WEARCFG_SET_RESPECT_LOWBIT:
//                        trigger = bundle.getBoolean(ACommon.CFG_RESPECT_LOWBIT, true);
//                        Log.i(TAG, "### EVT_WEARCFG_SET_RESPECT_LOWBIT=" + trigger);
//                        mRespectLowBitTrigger.setChecked(trigger);
//                        break;
                    //
                    case ACommon.EVT_WEARCFG_SET_SWEEP:
                        trigger = bundle.getBoolean(ACommon.CFG_SWEEP_SECONDS, true);
                        //Log.i(TAG, "### EVT_WEARCFG_SET_SWEEP=" + trigger);
                        mSweepTrigger.setChecked(trigger);
                        break;
                    //


                    case ACommon.EVT_CURRENT_CONFIG:
                        //Log.i(TAG, "((( EVT_CURRENT_CONFIG");
                        trigger = bundle.getBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT, true);
                        //Log.i(TAG, "### EVT_CURRENT_CONFIG, DIAL_GRADIENT=" + trigger);
                        mShowDialGardientTrigger.setChecked(trigger);
                        //
                        trigger = bundle.getBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF, true);
                        //Log.i(TAG, "### EVT_CURRENT_CONFIG, HRDIGITS_RELIEF=" + trigger);
                        mShowHrDigitsReliefTrigger.setChecked(trigger);
                        //
                        trigger = bundle.getBoolean(ACommon.CFG_SHOW_INSCRIPTIONS_RELIEF, true);
                        mShowInscriptionsReliefTrigger.setChecked(trigger);
                        //
                        mSpnrAuxBevelColorTag = bundle.getInt(ACommon.CFG_AUX_BEVEL_COLOR, ACommon.BEVEL_FROM_AUX);
                        mSpnrAuxBevelColor.setTag(mSpnrAuxBevelColorTag);
                        mSpnrAuxBevelColor.setSelection(mSpnrAuxBevelColorTag); // 0=from aux dial, 1=from main dial
                        //
                        mSpnrInvertGradientTag = bundle.getInt(ACommon.CFG_INVERT_GRADIENT, ACommon.GD_INVERT_NONE);
                        mSpnrInvertGradient.setTag(mSpnrInvertGradientTag);
                        mSpnrInvertGradient.setSelection(mSpnrInvertGradientTag);
                        //
                        setDgSeekValue(mSeekFirstStop, mService.getDgFirstStop());
                        setDgSeekValue(mSeekHalfEdgeStop, mService.getDgHalfEdgeStop());
                        setDgSeekValue(mSeekEdgeAlpha, mService.getDgEdgeAlpha());
                        //
                        setDgSeekValue(mSeekFirstStop1, mService.getDgFirstStop1());
                        setDgSeekValue(mSeekHalfEdgeStop1, mService.getDgHalfEdgeStop1());
                        setDgSeekValue(mSeekEdgeAlpha1, mService.getDgEdgeAlpha1());
                        //
                        Bundle config = mService.getCurrentConfig();

                        mInscrPackEditor.retrieveInscriptionPackFromBundle(config);

                        initOtherViews(config);
                        initHourMarksViews(config);
                        initPlateTextureViews(config);
                        initPlateTriggerViews(config);
                        break;


                    case ACommon.EVT_CURRENT_PREFERENCES:
                        initTzInfoViews(mService.getWearAppPreference());
                        //
                        mRespectBurnInTrigger.setChecked(mService.getWearAppPreference().getRespectBurnIn());
                        mSweepTrigger.setChecked(mService.getWearAppPreference().getSweepSeconds());
                        mShowHandheldBatteryTrigger.setChecked(mService.getWearAppPreference().getShowHandheldBattery());
                        break;


                    case ACommon.EVT_PLATE_BITMAP:
                        //Log.i(TAG, "((( EVT_PLATE_BITMAP");
                        mPlatePainter.setDialPlateBitmap(mService.getPlateBitmap());
                        mInscrPackEditor.mBtnDialPlate.invalidate();
                        break;

                    case ACommon.EVT_NEW_CONFIG_SENT:
                        //Log.i(TAG, "((( EVT_NEW_CONFIG_SENT");
                        mPlatePainter.setDialPlateBitmap(null);
                        mInscrPackEditor.mBtnDialPlate.invalidate();
                        break;

                    case ACommon.EVT_CURRENT_WATCHFACE_VALUES:
                        //Log.i(TAG, "((( EVT_CURRENT_WATCHFACE_VALUES");
                        mPlatePainter.setWatchFaceValues(
                                mService.getWFVIsWearRTL(),
                                mService.getWFVScreenWidth(),
                                mService.getWFVScreenHeight(),
                                mService.getWFVBurninMargin(),
                                mService.getWFVScreenCenterX(),
                                mService.getWFVScreenCenterY(),
                                mService.getWFVScreenRadius(),
                                mService.getWFVDialRadius()
                        );
                        mPlatePainter.setWatchFaceAux(
                                mService.getWFVauxAcx(),
                                mService.getWFVauxAcy(),
                                mService.getWFVauxAdim(),
                                mService.getWFVauxBcx(),
                                mService.getWFVauxBcy(),
                                mService.getWFVauxBdim(),
                                mService.getWFVauxCcx(),
                                mService.getWFVauxCcy(),
                                mService.getWFVauxCdim()
                        );
                        mInscrPackEditor.mBtnDialPlate.invalidate();
                        break;


                    default:
                        break;
                }
            }
        }
    };








    // sendPhoneConfigBooleanOption(ACommon.EVT_HHCFG_SET_DIAL_GRADIENT, ACommon.CFG_SHOW_DIAL_GRADIENT, isChecked);
    private void sendPhoneConfigBooleanOption(int event, String key, boolean option) {
        DataMap dataMap = new DataMap();
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putInt(ACommon.KEY_EVENT, event);
        dataMap.putBoolean(key, option);
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendPhoneConfigIntegerOption(int event, String key, int value) {
        DataMap dataMap = new DataMap();
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putInt(ACommon.KEY_EVENT, event);
        dataMap.putInt(key, value);
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendPhoneConfigGradientPack() {
        // sendPhoneConfigBooleanOption(ACommon.EVT_HHCFG_SET_DIAL_GRADIENT, ACommon.CFG_SHOW_DIAL_GRADIENT, isChecked);
        // private void sendPhoneConfigBooleanOption(int event, String key, boolean option) {
        DataMap dataMap = new DataMap();
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_HHCFG_SET_DIAL_GRADIENT_PACK);
        //dataMap.putBoolean(key, option);
        int i = getSeekValueInt(mSeekEdgeAlpha);
        float fs = getDgSeekValueFloat(mSeekFirstStop);
        float he = getDgSeekValueFloat(mSeekHalfEdgeStop);
        dataMap.putInt(ACommon.CFG_DG_EDGE_ALPHA, i);
        dataMap.putFloat(ACommon.CFG_DG_FIRST_STOP, fs);
        dataMap.putFloat(ACommon.CFG_DG_HALF_EDGE_STOP, he);
        int i1 = getSeekValueInt(mSeekEdgeAlpha1);
        float fs1 = getDgSeekValueFloat(mSeekFirstStop1);
        float he1 = getDgSeekValueFloat(mSeekHalfEdgeStop1);
        dataMap.putInt(ACommon.CFG_DG_EDGE_ALPHA_1, i1);
        dataMap.putFloat(ACommon.CFG_DG_FIRST_STOP_1, fs1);
        dataMap.putFloat(ACommon.CFG_DG_HALF_EDGE_STOP_1, he1);
        //
        dataMap.putInt(ACommon.CFG_INVERT_GRADIENT, mService.getInvertGradient());
        //
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendPlateTexturePack() {
        int progress;
        float valueFloat;

        DataMap dataMap = new DataMap();
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_HHCFG_SET_PLATE_TEXTURE_PACK);

        progress = getSeekValueInt(mSeekPlateTextureStrength);
        valueFloat = ((float) progress) / 100f;
        dataMap.putFloat(ACommon.CFG_PLATE_TEXTURE_STRENGTH, valueFloat);

        progress = getSeekValueInt(mSeekAuxDialTextureStrength);
        valueFloat = ((float) progress) / 100f;
        dataMap.putFloat(ACommon.CFG_AUXDIAL_TEXTURE_STRENGTH, valueFloat);

        progress = getSeekValueInt(mSeekPlateReliefStrength);
        dataMap.putInt(ACommon.CFG_PLATE_RELIEF_STRENGTH, progress);

        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendTimezonePack() {
        AppPreferences appPreferences = mService.getWearAppPreference();
        DataMap dataMap = DataMap.fromBundle(AppPreferences.bundlePreferences(appPreferences));
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_HHCFG_SET_TIMEZONE_PACK);
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendPhoneConfigHourMarksPack() {
        long[] longArr = new long[WatchAppearance.NUM_HOUR_MARKS];
        for(int i=0; i<WatchAppearance.NUM_HOUR_MARKS; i++) {
            int selection = mSpnrHourMarks[i].getSelectedItemPosition();
            longArr[i] = (selection == Spinner.INVALID_POSITION) ? WatchAppearance.DEFAULT_HOUR_MARK : selection;
        }
        long[] longArr2 = new long[WatchAppearance.NUM_HOUR_MARKS];
        for(int i=0; i<WatchAppearance.NUM_HOUR_MARKS; i++) {
            int selection = mSpnrHourMarksRelief[i].getSelectedItemPosition();
            longArr2[i] = (selection == Spinner.INVALID_POSITION) ? WatchAppearance.DEFAULT_HOUR_MARK_RELIEF : selection;
        }
        int strength = mSeekHourMarksReliefStrength.getProgress();

        DataMap dataMap = new DataMap();
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_HHCFG_SET_HOUR_MARKS);
        dataMap.putLongArray(ACommon.CFG_HOUR_MARKS, longArr);
        dataMap.putLongArray(ACommon.CFG_HOUR_MARKS_RELIEF, longArr2);
        dataMap.putInt(ACommon.CFG_HOUR_MARKS_RELIEF_STRENGTH, strength);
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendPhoneConfigDemoParametersPack() {
        long triggers[] = new long[DemoPackData.NUM_DEMOPACK_PARAMETERS];
        long values[] = new long[DemoPackData.NUM_DEMOPACK_PARAMETERS];
        for (int i=0; i<DemoPackData.NUM_DEMOPACK_PARAMETERS; i++) {
            triggers[i] = (mService.demoPackData[i].trigger) ? 1 : 0;
            values[i] = mService.demoPackData[i].value;
        } triggers[DemoPackData.TRIGGER_DEMOPACK] = (mSwDemoUsePack.isChecked()) ? 1 : 0;

        DataMap dataMap = new DataMap();
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_HHCFG_SET_DEMO_PACK);
        dataMap.putLongArray(ACommon.CFG_DEMOPACK_TRIGGERS, triggers);
        dataMap.putLongArray(ACommon.CFG_DEMOPACK_VALUES, values);
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendPhoneConfigInscriptionsPack() {
        Bundle pack = new Bundle();
        mInscrPackEditor.placeInscriptionPackToBundle(pack);
        //Log.i(TAG, "(((( sendPhoneConfigInscriptionsPack, pack=" + pack);
        DataMap dataMap = DataMap.fromBundle(pack);
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_HHCFG_SET_INSCRIPTIONS_PACK);
        //
        int strength = mSeekInscriptionsReliefStrength.getProgress();
        dataMap.putInt(ACommon.CFG_INSCRIPTIONS_RELIEF_STRENGTH, strength);
        //
        //dataMap.putS
        //Log.i(TAG, "(((( sendPhoneConfigInscriptionsPack, dataMap=" + dataMap);
        //
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void requestPlateBitmap() {
        //Log.i(TAG, "((( requestPlateBitmap");
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_REQUEST_PLATE_BITMAP);
        dataMap.putLong(ACommon.KEY_TIME, System.currentTimeMillis());
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    } // requestPlateBitmap

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
//            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//            Log.i(TAG, "*** SendThroughWearNetworkThread dataMap=" + dataMap + ", path=" + path);
//            //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
//            List<Node> nodeList;
//            int numNodes = 0;
//            if (nodes != null) {
//                nodeList = nodes.getNodes();
//                if (nodeList != null) numNodes = nodeList.size();
//            }
//            for (int i=0; i<numNodes; i++) {
//                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
//                putDMR.getDataMap().putAll(dataMap);
//                PutDataRequest request = putDMR.asPutDataRequest();
//                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient,request).await();
//            }
//        }
//    } // class SendTroughWearNetworkThread





    InscriptionAppearance mPlatePainter = new InscriptionAppearance();

    public class EditInscriptions {

        Inscription mPrint;
        //
        int mCurrentInscriptionIndex;
        //
        private void setCurrentInscriptionIndex(int i) {
            mCurrentInscriptionIndex = i;
            //if (mCurrentInscriptionIndex >= 0 && mCurrentInscriptionIndex < Inscription.NUM_INSCRIPTIONS) mBtnDialPlate.invalidate();
        }
        //
        private int nextInscriptionIndex() {
            if (mCurrentInscriptionIndex == -1) return -1;
            mCurrentInscriptionIndex++;
            if (mCurrentInscriptionIndex >= Inscription.NUM_INSCRIPTIONS) setCurrentInscriptionIndex(0);
            return mCurrentInscriptionIndex;
        }
        private int previousInscriptionIndex() {
            if (mCurrentInscriptionIndex == -1) return -1;
            mCurrentInscriptionIndex--;
            if (mCurrentInscriptionIndex < 0) setCurrentInscriptionIndex(Inscription.NUM_INSCRIPTIONS - 1);
            return mCurrentInscriptionIndex;
        }

        RadioGroup gTogglers;
        RadioButton rTogglers[] = new RadioButton[Inscription.NUM_INSCRIPTIONS]; // todo: +colors
        Switch mSwitchInscrEnabled;
        EditText mEditInscrText;
        SpinnedEditText mCpndTextSize;
        SpinnedEditText mCpndTextScaleX;
        SpinnedEditText mCpndRadius;
        SpinnedEditText mCpndAngle;
        SpinnedEditText mCpndIncline;

        Spinner mSpnrBend;
        Spinner mSpnrDirection;

        Spinner mSpnrFx;

        Spinner mSpnrFontFamily;
        Spinner mSpnrFontStyle;

        Button mBtnPutInscriptionsPack;

        public ImageButton mBtnDialPlate;
        //public Drawable mPlatePainter = new InscriptionAppearance();

        Button btnPrevInscription, btnNextInscription, btnToggleShowABC, btnToggleShowLegend;
        Button btnCopyInscription, btnPasteInscription;


        public EditInscriptions(View view) {

            mPrint = new Inscription(mActivity.getApplicationContext());

            setCurrentInscriptionIndex(-1);

            gTogglers = (RadioGroup) view.findViewById(R.id.rgrpInscrTogglers);
            rTogglers[0] = (RadioButton) view.findViewById(R.id.rbtnInscription0);
            rTogglers[1] = (RadioButton) view.findViewById(R.id.rbtnInscription1);
            rTogglers[2] = (RadioButton) view.findViewById(R.id.rbtnInscription2);
            rTogglers[3] = (RadioButton) view.findViewById(R.id.rbtnInscription3);
            rTogglers[4] = (RadioButton) view.findViewById(R.id.rbtnInscription4);
            rTogglers[5] = (RadioButton) view.findViewById(R.id.rbtnInscription5);
            rTogglers[6] = (RadioButton) view.findViewById(R.id.rbtnInscription6);

            mSwitchInscrEnabled = (Switch) view.findViewById(R.id.switchInscrEnabled);
            mEditInscrText = (EditText) view.findViewById(R.id.editInscrText);
            mCpndTextSize = (SpinnedEditText) view.findViewById(R.id.cpndTextSize);
            mCpndTextScaleX = (SpinnedEditText) view.findViewById(R.id.cpndTextScaleX);
            mCpndRadius = (SpinnedEditText) view.findViewById(R.id.cpndRadius);
            mCpndAngle = (SpinnedEditText) view.findViewById(R.id.cpndAngle);
            mCpndIncline = (SpinnedEditText) view.findViewById(R.id.cpndIncline);
            mSpnrFontFamily = (Spinner) view.findViewById(R.id.spnrFontFamily);
            mSpnrFontStyle = (Spinner) view.findViewById(R.id.spnrFontStyle);
            mBtnPutInscriptionsPack = (Button) view.findViewById(R.id.btnPutInscription);
            mSpnrBend = (Spinner) view.findViewById(R.id.spnrBend);
            mSpnrFx = (Spinner) view.findViewById(R.id.spnrFx);
            mSpnrDirection = (Spinner) view.findViewById(R.id.spnrDirection);
            mBtnDialPlate = (ImageButton) view.findViewById(R.id.btnDialPlate);
            btnPrevInscription = (Button) view.findViewById(R.id.btnPrevInscription);
            btnNextInscription = (Button) view.findViewById(R.id.btnNextInscription);
            btnToggleShowABC = (Button) view.findViewById(R.id.btnToggleShowABC);
            btnToggleShowLegend = (Button) view.findViewById(R.id.btnToggleShowLegend);
            btnCopyInscription = (Button) view.findViewById(R.id.btnCopyInscription);
            btnPasteInscription = (Button) view.findViewById(R.id.btnPasteInscription);

            gTogglers.setOnCheckedChangeListener(
                    new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            setCurrentInscriptionIndex(findCurrentIndexBySelectedTogglerId(checkedId));
                            setControlsValuesForIndex(mCurrentInscriptionIndex);
                        }
                    }
            );

            btnPrevInscription.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //setControlsValuesForIndex(previousInscriptionIndex());
                            int i = previousInscriptionIndex();
                            if (i != -1) gTogglers.check(rTogglers[i].getId());
                        }
                    }
            );

            btnNextInscription.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //setControlsValuesForIndex(nextInscriptionIndex());
                            int i = nextInscriptionIndex();
                            if (i != -1) gTogglers.check(rTogglers[i].getId());
                        }
                    }
            );

            btnToggleShowABC.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPlatePainter.setShowABC(!mPlatePainter.showABC);
                            mBtnDialPlate.invalidate();
                        }
                    }
            );

            btnToggleShowLegend.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPlatePainter.setShowLegend(!mPlatePainter.showLegend);
                            mBtnDialPlate.invalidate();
                        }
                    }
            );

            btnCopyInscription.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mInscrClipboard.putCopy(mPrint);
                        }
                    }
            );
            btnPasteInscription.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Inscription pasted = mInscrClipboard.getCopy(mPrint);
                            //Log.i(TAG, "((( paste inscription = " + pasted);
                            if (null != pasted) {
                                mPrint = pasted;
                                for (int i=0; i<Inscription.NUM_INSCRIPTIONS; i++) {
                                    rTogglers[i].setText(mPrint.text[i]);
                                }
                                setCurrentInscriptionIndex(findCurrentIndexBySelectedTogglerId(gTogglers.getCheckedRadioButtonId()));
                                setControlsValuesForIndex(mCurrentInscriptionIndex);
                            }
                        }
                    }
            );

            mSwitchInscrEnabled.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (-1 == mCurrentInscriptionIndex) return;
                            mPrint.enabled[mCurrentInscriptionIndex] = (isChecked) ? Inscription.DEFAULT_YES : Inscription.DEFAULT_NO;
                        }
                    }
            );

            mEditInscrText.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void afterTextChanged(Editable s) {
                            if (-1 == mCurrentInscriptionIndex) return;
                            String txt = mEditInscrText.getText().toString();
                            rTogglers[mCurrentInscriptionIndex].setText(txt);
                            mPrint.text[mCurrentInscriptionIndex] = txt;
                            //
                            mPlatePainter.setiText(mPrint.text[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }
                    }
            );

            mCpndTextSize.setRange(5.0f, 30.0f);
            mCpndTextSize.addOnValueChangedListener(
                    new OnSpinnedEditValueChanged() {
                        @Override
                        public void onValueChanged(float value) {
                            if (-1 == mCurrentInscriptionIndex) return;
                            mPrint.textSize[mCurrentInscriptionIndex] = value;
                            //
                            mPlatePainter.setiTextSize(mPrint.textSize[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }
                    }
            );

            mCpndTextScaleX.setRange(0.01f, 5.0f);
            mCpndTextScaleX.addOnValueChangedListener(
                    new OnSpinnedEditValueChanged() {
                        @Override
                        public void onValueChanged(float value) {
                            if (-1 == mCurrentInscriptionIndex) return;
                            mPrint.textScaleX[mCurrentInscriptionIndex] = value;
                            //
                            mPlatePainter.setiTextScaleX(mPrint.textScaleX[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }
                    }
            );

            mCpndRadius.setRange(0.01f, 350.0f);
            mCpndRadius.addOnValueChangedListener(
                    new OnSpinnedEditValueChanged() {
                        @Override
                        public void onValueChanged(float value) {
                            if (-1 == mCurrentInscriptionIndex) return;
                            mPrint.radius[mCurrentInscriptionIndex] = value;
                            //
                            mPlatePainter.setiRadius(mPrint.radius[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }
                    }
            );

            mCpndAngle.setRange(0.0f, 360.0f);
            mCpndAngle.addOnValueChangedListener(
                    new OnSpinnedEditValueChanged() {
                        @Override
                        public void onValueChanged(float value) {
                            if (-1 == mCurrentInscriptionIndex) return;
                            mPrint.angle[mCurrentInscriptionIndex] = value;
                            //
                            mPlatePainter.setiAngle(mPrint.angle[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }
                    }
            );

            mCpndIncline.setRange(0.0f, 360.0f);
            mCpndIncline.addOnValueChangedListener(
                    new OnSpinnedEditValueChanged() {
                        @Override
                        public void onValueChanged(float value) {
                            if (-1 == mCurrentInscriptionIndex) return;
                            mPrint.incline[mCurrentInscriptionIndex] = value;
                            //
                            mPlatePainter.setiIncline(mPrint.incline[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }
                    }
            );

            mSpnrFontFamily.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                            Log.i(TAG, "((((( mSpnrFontFamily, position=" + position + ", id=" + id +
//                                            ", val=" + Inscription.fontFamilyEnum[((int) id)]
//                            );
                            if (-1 == mCurrentInscriptionIndex) return;
                            //mPrint.fontFamily[mCurrentInscriptionIndex] = fontFamily[((int) id)];
                            mPrint.fontFamily[mCurrentInscriptionIndex] = Inscription.fontFamilyEnum[((int) id)];
                            //
                            mPlatePainter.setiFontFamily(mPrint.fontFamily[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    }
            );

            mSpnrFontStyle.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                            Log.i(TAG, "((((( mSpnrFontStyle, position=" + position + ", id=" + id +
//                                    ", val=" + fontStyle[((int) id)] +
//                                    ", style=" + fontStyleInt[((int) id)]
//                            );
                            if (-1 == mCurrentInscriptionIndex) return;
                            //Log.i(TAG, "((((( mSpnrFontStyle, id=" + id + ", fontStyle[id]=" + fontStyle[((int) id)]);
                            //mPrint.fontStyle[mCurrentInscriptionIndex] = fontStyle[((int) id)];
                            mPrint.fontStyle[mCurrentInscriptionIndex] = Inscription.fontStyleEnum[((int) id)];
                            //
                            mPlatePainter.setiFontStyle(mPrint.fontStyle[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    }
            );

            mSpnrBend.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (-1 == mCurrentInscriptionIndex) return;
                            mPrint.bend[mCurrentInscriptionIndex] = id;
                            //
                            mPlatePainter.setiBend(mPrint.bend[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    }
            );

            mSpnrFx.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (-1 == mCurrentInscriptionIndex) return;
                            mPrint.fx[mCurrentInscriptionIndex] = id;
                            //
                            mPlatePainter.setiFx(mPrint.fx[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    }
            );

            mSpnrDirection.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (-1 == mCurrentInscriptionIndex) return;
                            mPrint.direction[mCurrentInscriptionIndex] = id;
                            //
                            mPlatePainter.setiDirection(mPrint.direction[mCurrentInscriptionIndex]);
                            mBtnDialPlate.invalidate();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    }
            );

            mBtnPutInscriptionsPack.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPrint.watchLayoutIndex = mService.getWatchLayoutIndex(); // ???
                            sendPhoneConfigInscriptionsPack();
                            //todo: изменить описание элемента (в скобках) при смене текста надписи ???
                        }
                    }
            );

            mBtnDialPlate.setBackground(mPlatePainter);
            mBtnDialPlate.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            final View finalView = v;
                            finalView.animate().setDuration(500).alpha(0.5f)
                                    .withEndAction(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (mActivity.getGoogleApiClient() != null) {
                                                        ACommon.requestCurrentConfig(mActivity.getGoogleApiClient(), mActivity.mPeerId);
                                                    }
                                                    //requestPlateBitmap();
                                                    finalView.setAlpha(1f);
                                                }
                                            }
                                    );


                            //requestCurrentConfig();
                            mActivity.mVibrator.vibrate(20);

                            return true;
                        }
                    }
            );
            mBtnDialPlate.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final View finalView = v;
                            finalView.animate().setDuration(500).alpha(0.5f)
                                    .withEndAction(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    //requestCurrentConfig();
                                                    requestPlateBitmap();
                                                    finalView.setAlpha(1f);
                                                }
                                            }
                                    );
                            //requestCurrentConfig();
                            mActivity.mVibrator.vibrate(20);
                        }
                    }
            );

        } // EditInscriptions()


        public void retrieveInscriptionPackFromBundle(Bundle config) {
            if (null == config) return;

            WatchAppearance appearance = new WatchAppearance(null, mActivity.getApplicationContext());
            appearance.unBundleConfig(config);

            Inscription.unBundleInscription(config, mPrint);

            //boolean trigger = mService.getRespectBurnInTrigger();
            for (int i=0; i<Inscription.NUM_INSCRIPTIONS; i++) {
                rTogglers[i].setText(mPrint.text[i]);

                // todo: +colors
                int textColor;
                switch (i) {
                    case 0:
                        textColor = appearance.mMainInscription_1_Color; break;
                    case 1:
                        textColor = appearance.mMainInscription_2_Color; break;
                    case 2:
                        textColor = appearance.mMainInscription_3_Color; break;
                    case 3:
                        textColor = appearance.mMainInscription_4_Color; break;
                    case 4:
                        textColor = appearance.mMainInscription_5_Color; break;
                    case 5:
                        textColor = appearance.mMainInscription_6_Color; break;
                    case 6:
                        textColor = appearance.mMainInscription_7_Color; break;
                    default:
                        textColor = Color.WHITE; break;
                }
                mPrint.textColor[i] = textColor;
            }

//            int checkedId = gTogglers.getCheckedRadioButtonId();
//            if (-1 == checkedId) gTogglers.check(R.id.rbtnInscription0);
//            else gTogglers.check(checkedId);
            setCurrentInscriptionIndex(findCurrentIndexBySelectedTogglerId(gTogglers.getCheckedRadioButtonId()));
            setControlsValuesForIndex(mCurrentInscriptionIndex);

        } // retrieveInscriptionPackFromBundle


        public void placeInscriptionPackToBundle(Bundle config) {
            Inscription.bundleInscription(config, mPrint);
        } // placeInscriptionPackToBundle


        public int findCurrentIndexBySelectedTogglerId(int idSelected) {
            int retv = 0;
            if (-1 != idSelected) {
                switch (idSelected) {
                    case R.id.rbtnInscription0:
                        retv = 0; break;
                    case R.id.rbtnInscription1:
                        retv = 1; break;
                    case R.id.rbtnInscription2:
                        retv = 2; break;
                    case R.id.rbtnInscription3:
                        retv = 3; break;
                    case R.id.rbtnInscription4:
                        retv = 4; break;
                    case R.id.rbtnInscription5:
                        retv = 5; break;
                    case R.id.rbtnInscription6:
                        retv = 6; break;
                }
            } else {
                gTogglers.check(R.id.rbtnInscription0);
            }
            return retv;
        } // findIndById


        public void setControlsValuesForIndex(int index) {

            if (!(mCurrentInscriptionIndex >= 0 && mCurrentInscriptionIndex < Inscription.NUM_INSCRIPTIONS)) return;

            mSwitchInscrEnabled.setChecked((mPrint.enabled[index] == Inscription.DEFAULT_YES) ? true : false);
            rTogglers[index].setText(mPrint.text[index]);

            mEditInscrText.setText(mPrint.text[index]); mPlatePainter.setiText(mPrint.text[index]);
            mCpndTextSize.setValue(mPrint.textSize[index]); mPlatePainter.setiTextSize(mPrint.textSize[index]);
            mCpndTextScaleX.setValue(mPrint.textScaleX[index]); mPlatePainter.setiTextScaleX(mPrint.textScaleX[index]);
            mCpndRadius.setValue(mPrint.radius[index]); mPlatePainter.setiRadius(mPrint.radius[index]);
            mCpndAngle.setValue(mPrint.angle[index]); mPlatePainter.setiAngle(mPrint.angle[index]);
            mCpndIncline.setValue(mPrint.incline[index]); mPlatePainter.setiIncline(mPrint.incline[index]);

            mSpnrFontFamily.setSelection(Inscription.getFamilyIndex(mPrint.fontFamily[index]));
            mPlatePainter.setiFontFamily(mPrint.fontFamily[index]);

            mSpnrFontStyle.setSelection(Inscription.getStyleIndex(mPrint.fontStyle[index])); mPlatePainter.setiFontStyle(mPrint.fontStyle[index]);
            mSpnrBend.setSelection(((int) mPrint.bend[index])); mPlatePainter.setiBend(mPrint.bend[index]);
            mSpnrFx.setSelection(((int) mPrint.fx[index])); mPlatePainter.setiFx(mPrint.fx[index]);
            mSpnrDirection.setSelection(((int) mPrint.direction[index])); mPlatePainter.setiDirection(mPrint.direction[index]);
            mPlatePainter.setiTextColor(mPrint.textColor[index]);

            mPlatePainter.setDigitsColor(Color.WHITE);
            mPlatePainter.setLegendCircleColor(Color.WHITE);
            Bundle config = null;
            if (mService != null) config = mService.getCurrentConfig();
            if (null != config) {
                mPlatePainter.setDigitsColor(config.getInt(ACommon.CFG_COLOR_MAIN_DIGITS));
                mPlatePainter.setLegendCircleColor(config.getInt(ACommon.CFG_COLOR_MAIN_TICK));
            }

            mBtnDialPlate.invalidate();
        } // setControlsValuesForIndex
    } // class EditInscriptions





    class InscriptionClipboard {
        Bundle copiedInscription;
        Bundle replacedInscription;

        public InscriptionClipboard() {
            copiedInscription = null;
            replacedInscription = null;
        }

        public void putCopy(Inscription toCopy) {
            copiedInscription = new Bundle();
            Inscription.bundleInscription(copiedInscription, toCopy);
        }

        public Inscription getCopy(Inscription replaced) {
            Inscription result = null;

            if (null != copiedInscription) {
                result = new Inscription(mActivity.getApplicationContext());
                Inscription.unBundleInscription(copiedInscription, result);
                replacedInscription = new Bundle();
                Inscription.bundleInscription(replacedInscription, replaced);
            }

            return result;
        }
    } // class InscriptionClipboard

} // class PageFragmentSettings
