package com.luna_78.wear.watch.face.raf3078;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.luna_78.wear.watch.face.raf3078.common.ACommon;
import com.luna_78.wear.watch.face.raf3078.common.Layout;
import com.luna_78.wear.watch.face.raf3078.common.SerializerXML;

import org.acra.ACRA;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by buba on 11/03/15.
 */
public class PageFragmentLayout extends Fragment {

    private static final String TAG = "PFLAY";

    private GoogleApiClient mGoogleApiClient;
    private HandheldCompanionConfigActivity mActivity;
    private APhoneService mService;

    private View mView;

    private TextView mTxtSeekVal;
    private SeekBar mTargetSeekBar;
    private TextView mTargetTxt;

    private TextView mTxtRed;
    private SeekBar mSeekBarR;
    private TextView mTxtGreen;
    private SeekBar mSeekBarG;
    private TextView mTxtBlue;
    private SeekBar mSeekBarB;
    private TextView mTxtAlpha;
    private SeekBar mSeekBarA;

    private ImageButton mBtnGet;
    private TextView mTxtElement;
    private ImageButton mImageButtonColor;
    private int mPage;




    class ColorToEdit {
        private int mColor;
        private boolean mLoaded = false, mOverLoaded = false;

        ColorToEdit() { this.mColor = 0; mLoaded = false; mOverLoaded = false; }
        ColorToEdit(int color) {
//            Log.i(TAG, "((( ColorToEdit, CONSTRUCT: mImageButtonColor=" + (mImageButtonColor != null) +
//                    ", mCurrentColorDrawable=" + (mCurrentColorDrawable != null));
            this.mColor = color; mLoaded = false; mOverLoaded = false;
            if (null != mImageButtonColor) {
                mImageButtonColor.postInvalidate();
                //mImageButtonColor.invalidate();
            }
        }

        public void setColor(int color) {
//            Log.i(TAG, "((( ColorToEdit, setColor: mImageButtonColor=" + (mImageButtonColor != null) +
//                    ", mCurrentColorDrawable=" + (mCurrentColorDrawable != null));
            this.mColor = color;
            if (null != mSeekBarA) mSeekBarA.setProgress((color & 0xff000000) >>> 24);
            if (null != mSeekBarR) mSeekBarR.setProgress((color & 0x00ff0000) >> 16);
            if (null != mSeekBarG) mSeekBarG.setProgress((color & 0x0000ff00) >> 8);
            if (null != mSeekBarB) mSeekBarB.setProgress((color & 0x000000ff));
            //mTxtElement.setText(mElementName[mElementIndex]);
            //if (mLoaded == true) mOverLoaded = true;
            if (null != mImageButtonColor) {
                mImageButtonColor.postInvalidate();
                //mImageButtonColor.invalidate();
            }
        }

        public int getColor() {
            return this.mColor;
        }
        public boolean getLoaded() { return mLoaded; }
        public void setLoaded () { mLoaded = true; }
        public boolean getOverLoaded() { return mOverLoaded; }
        public void clearOverLoaded () { mOverLoaded = false; }
        public void setOverLoaded () { if (mLoaded == true) mOverLoaded = true; }

        public int rgbToY() {
            // возвращает яркость или -1 если цвет не загружен
            if (mLoaded != true) return -1;
            float a, r, g, b, y, rgby;
            a = ((mColor & 0xff000000) >>> 24) / 255f;
            r = ((mColor & 0x00ff0000) >> 16) / 255f;
            g = ((mColor & 0x0000ff00) >> 8) / 255f;
            b = ((mColor & 0x000000ff)) / 255f;
            rgby = (float) (255f * (0.21 * r + 0.72 * g + 0.07 * b));
            if (rgby < 128f) {
                y = (float) (255f * (0.35 * a + 0.65 * (rgby / 255f)));
            } else {
                y = (float) (255f * (0.65 * a + 0.35 * (rgby / 255f)));
            }
            //Log.i(TAG, "***** rgbToY = " + (int) y);
            return (int) y;
        }

    } // class ColorToEdit
    public ColorToEdit mColorToEdit = new ColorToEdit(0);







    class ColorImgButton extends ImageButton implements View.OnClickListener, View.OnLongClickListener {

        //private HandheldCompanionConfigActivity mActivity;

//        private int mColor = 0;
//        //
//        public void setColor(int c) {mColor = c;}
//        public int getColor() {return mColor;}


        Paint mPaint = new Paint();
        long mDeleteRequestMs = 0, mTimeOut = 5000;

        public void clearDeletionTimer() { mDeleteRequestMs = 0; }

//        LinearLayout mColorPaletteLinearLayout;
//        List<ColorImgButton> mColorPaletteViewList;
//
//        public void setGlobals(LinearLayout layout, List<ColorImgButton> list) {
//            mColorPaletteLinearLayout = layout;
//            mColorPaletteViewList = list;
//        }


        private void init() {
            setOnClickListener(this);
            setOnLongClickListener(this);
        }

        private void setPaint() {
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setFilterBitmap(true);
        }

        public ColorImgButton(HandheldCompanionConfigActivity context) {
            super(context);
            init();
            //mActivity = context;
            setPaint();
        }

        public ColorImgButton(HandheldCompanionConfigActivity context, AttributeSet attrs) {
            super(context, attrs);
            init();
            //mActivity = context;
            setPaint();
        }

        public ColorImgButton(HandheldCompanionConfigActivity context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
            //mActivity = context;
            setPaint();
        }
/*

    public ColorImgButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
*/

/*
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
*/

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            canvas.drawColor(Color.argb(255, 127, 127, 127));

            //View v = getView();
            final int index = mColorPaletteViewList.indexOf(this);
            //Log.i(TAG, "((( ColorPalette draw, index=" + index);
            int color = mActivity.gColorPalette.get(index);

            int width = canvas.getWidth();
            int height = canvas.getHeight();
            float centerX = width / 2f;
            float centerY = height / 2f;
            float radiusMax = Math.min(centerX, centerY);
            //
            Path colorFrame = new Path();
            float frameTop = 10f, frameLeft = 10f, frameRight = width-10f, frameBottom = height-10f;
            colorFrame.addRect(frameLeft, frameTop, frameRight, frameBottom, Path.Direction.CW);
            //colorFrame.addCircle(centerX, centerY, radiusMax - 1f, Path.Direction.CW);
            //
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.BLACK);
            //canvas.drawCircle(centerX, centerY, radiusMax - 1f, mPaint);
            canvas.drawPath(colorFrame, mPaint);
            mPaint.setColor(color);
//            canvas.drawCircle(centerX, centerY, radiusMax - 1f, mPaint);
            canvas.drawPath(colorFrame, mPaint);
            //
            int frameY = rgbToY(color);
            int frameColor = Color.argb(255, 255 - frameY, 255 - frameY, 255 - frameY);
            mPaint.setColor(frameColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(2f);
            //canvas.drawCircle(centerX, centerY, radiusMax-2f, mPaint);
            canvas.drawPath(colorFrame, mPaint);
            //
            float hsv[] = new float[3], alpha = Color.alpha(color), coeff;
            coeff = alpha / 255f;
            float newRed = (int)((float) Color.red(color) * coeff);
            float newGreen = (int)((float) Color.green(color) * coeff);
            float newBlue = (int)((float) Color.blue(color) * coeff);
            int colorEff = Color.argb(255, (int) newRed, (int) newGreen, (int) newBlue);
            //
            Color.colorToHSV(colorEff, hsv);
            mPaint.setColor((frameY > 150) ? Color.BLACK : Color.WHITE);
            mPaint.setTextSize(24f);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(1f);
            String val = String.format(Locale.US, "%.2f", hsv[0]) + "\u00b0"; //"H=" +
            ACommon.drawHvAlignedText(canvas, centerX, centerY - 18f, val, mPaint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
//            int reference = Color.argb(255, 255, 255, 255);
//            float distance = ACommon.colorDistance(colorEff, reference);
//            val = String.format(Locale.US, "%.2f", distance);
//            ACommon.drawHvAlignedText(canvas, centerX, centerY+15f, val, mPaint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
            val = "S:" + String.format(Locale.US, "%3.1f", hsv[1] * 100f) + "%";
            ACommon.drawHvAlignedText(canvas, centerX, centerY + 6f, val, mPaint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
            val = "V:" + String.format(Locale.US, "%3.1f", hsv[2] * 100f) + "%";
            ACommon.drawHvAlignedText(canvas, centerX, centerY + 30f, val, mPaint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);

            if (0 != mDeleteRequestMs) {
                long currentMs = System.currentTimeMillis();
                if (mTimeOut < (currentMs - mDeleteRequestMs)) {
                    mDeleteRequestMs = 0;
                    return;
                }
                colorFrame.reset();
                colorFrame.moveTo(frameLeft, frameTop); colorFrame.lineTo(frameRight, frameBottom);
                colorFrame.moveTo(frameLeft, frameBottom); colorFrame.lineTo(frameRight, frameTop);
                mPaint.setStrokeWidth(3f);
                mPaint.setColor((frameY > 150) ? Color.BLACK : Color.WHITE);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(colorFrame, mPaint);
            }

        } // draw

        private static final int DEFAULT_SIZE = 100;

        private int calculateMeasure(int measureSpec) {
            //Log.i(TAG, "((( calculateMeasure, measureSpec=" + measureSpec);
            int result = (int) (DEFAULT_SIZE * getResources().getDisplayMetrics().density);
            int specMode = View.MeasureSpec.getMode(measureSpec);
            int specSize = View.MeasureSpec.getSize(measureSpec);
            if (specMode == View.MeasureSpec.EXACTLY) {
                result = specSize;
            } else if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
            //Log.i(TAG, "((( calculateMeasure, measureSpec=" + measureSpec + ", result=" + result);
            return result;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            //Log.i(TAG, "((( onMeasure, widthMeasureSpec=" + widthMeasureSpec + ", heightMeasureSpec=" + heightMeasureSpec);
            //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int width, height;
            //width = calculateMeasure(widthMeasureSpec);
            height = calculateMeasure(heightMeasureSpec);
            setMeasuredDimension(height, height);
        }


        public int rgbToY(int color) {
            float a, r, g, b, y, rgby;
            a = ((color & 0xff000000) >>> 24) / 255f;
            r = ((color & 0x00ff0000) >> 16) / 255f;
            g = ((color & 0x0000ff00) >> 8) / 255f;
            b = ((color & 0x000000ff)) / 255f;
            rgby = (float) (255f * (0.21 * r + 0.72 * g + 0.07 * b));
            if (rgby < 128f) {
                y = (float) (255f * (0.35 * a + 0.65 * (rgby / 255f)));
            } else {
                y = (float) (255f * (0.65 * a + 0.35 * (rgby / 255f)));
            }
            //Log.i(TAG, "***** rgbToY = " + (int) y);
            return (int) y;
        }


        @Override
        public void onClick(View v) {
            final int index = mColorPaletteViewList.indexOf(v);
            //Log.i(TAG, "((( ColorPalette onClick, index=" + index);
            int color = mActivity.gColorPalette.get(index);

            mColorToEdit.setColor(color);
            mColorToEdit.setOverLoaded();
            mActivity.mVibrator.vibrate(20);

            float[] leftHSV = new float[3], rightHSV = new float[3];
            int alpha, red, green, blue;
            int newAlpha, newRed, newGreen, newBlue;
            alpha = Color.alpha(color); red = Color.red(color); green = Color.green(color); blue = Color.blue(color);
            float coeff;
            Integer left, right, reference;
            reference = Color.argb(255, 255, 255, 255);
            //
            right = Color.argb(255, red, green, blue);
            Color.colorToHSV(right, rightHSV);
            float distanceR2REF = ACommon.colorDistance(right, reference);
            //
            coeff = (float)alpha / 255f;
            newRed = (int)((float)red * coeff); newGreen = (int)((float)green * coeff); newBlue = (int)((float)blue * coeff);
            left = Color.argb(255, newRed, newGreen, newBlue);
            Color.colorToHSV(left, leftHSV);
            float distanceL2REF = ACommon.colorDistance(left, reference);
            ACommon.ColorDominant dominant = ACommon.colorDominant(color);
            //
            float y = rgbToY(left);
            float kR, nR, kG, nG, kB, nB;
            kR = newRed / 255f; nR = kR * 0.21f;
            kG = newGreen / 255f; nG = kG * 0.72f;
            kB = newBlue / 255f; nB = kB * 0.07f;
            float r = (kR + nR) / 2f, g = (kG + nG) / 2f, b = (kB + nB) / 2f;
//            Log.i(TAG, "((( onClick, index=" + index + ": dominant=" + dominant + ";  R=" + red + " G=" + green + " B=" + blue + ";  eff: R=" + newRed + " G=" + newGreen + " B=" + newBlue);
//            //Log.i(TAG, "((( onClick, index=" + index + ": kR=" + kR + " kG=" + kG  + " kB=" + kB + ";  nR=" + nR + " nG=" + nG  + " nB=" + nB);
//            //Log.i(TAG, "((( onClick, index=" + index + ": r=" + r + " g=" + g  + " b=" + b);
//            Log.i(TAG, "((( onClick, index=" + index + ": eff " + "H=" + leftHSV[0] + " S=" + leftHSV[1] + " V=" + leftHSV[2] + ";  pure H=" + rightHSV[0] + " S=" + rightHSV[1] + " V=" + rightHSV[2]);
//            Log.i(TAG, "((( onClick, index=" + index + ": " + " distance eff=" + distanceL2REF + ";  distance pure=" + distanceR2REF);

        }

        @Override
        public boolean onLongClick(View v) {
            final int index = mColorPaletteViewList.indexOf(v);
            //Log.i(TAG, "((( ColorPalette onLongClick, index=" + index);

            long currentMs = System.currentTimeMillis();
            final View finalView = v;

            if (0 == mDeleteRequestMs || mTimeOut < (currentMs - mDeleteRequestMs)) {
                mDeleteRequestMs = currentMs;
                mActivity.mVibrator.vibrate(20);
                finalView.animate().setDuration(300).alpha(0)
                        .withEndAction(
                                new Runnable() {
                                    @Override
                                    public void run() { finalView.setAlpha(1); }
                                }
                        );
                postInvalidate();
                return true;
            } else {
                finalView.animate().setDuration(1000).alpha(0)
                        .withEndAction(
                                new Runnable() {
                                    @Override
                                    public void run() {

                                        mColorPaletteLinearLayout.removeView(finalView);
                                        mColorPaletteViewList.remove(finalView);
                                        mActivity.gColorPalette.remove(index);

                                        finalView.setAlpha(1);
                                    }
                                }
                        );
            }

            return true;
        }

    } // class ColorImgButton





    private ImageButton mBtnNextColor, mBtnPrevColor;
    //Bitmap mIconBtnNext;
    //Bitmap mIconBtnNextS;
    //Bitmap mIconBtnDate;
    //Bitmap mIconBtnDateS;
    private ImageButton mBtnUp, mBtnDn;
    private DrawableButtonUpDn mBtnUpDrawable, mBtnDnDrawable;



    HrzScrollView mHrzScrollView;
    ImageButton mBtnColorMemAdd;
    LinearLayout mColorPaletteLinearLayout;
    List<ColorImgButton> mColorPaletteViewList;// = new LinkedList<>();


    private ImageButton mBtnSaveToConfigPalette;

    private ImageButton mBtnShareSingle;


    //private volatile boolean mStartChooser = false;
    //
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
                    case ACommon.EVT_DENSE_SCREENSHOT:
                    case ACommon.EVT_AMBIENT_SCREENSHOT:
                        //Log.i(TAG, "*** FRAME_SCREENSHOT");
//                        ImageButton imageButtonWatch = (ImageButton) mView.findViewById(R.id.imageButtonWatch);
//                        imageButtonWatch.invalidate();
                        mBtnWatchAppearance.invalidate();
                        //Log.i(TAG, "*** invalidate" );
                        break;
                    case ACommon.EVT_CURRENT_CONFIG:
                        //Log.i(TAG, "*** CURRENT_CONFIG=" + bundle);
                        inflateCurrentConfigRepresentation(bundle);
                        break;
                    case ACommon.EVT_CONFIG_CHANGED:
                        restoreCurrentConfigRepresentation();
                        break;

//                    case ACommon.EVT_SIGNAL_HOLDOFF_UPDATE:
//                        Log.i(TAG, "((( EVT_SIGNAL_HOLDOFF_UPDATE, mStartChooser=" + mStartChooser);
//                        if (mStartChooser) {
//                            mCommonHandler.postDelayed(taskSendConfigFileByMail, 100);
//                            mStartChooser = false;
//                        }
//                        break;


                    default:
                        break;
                }
            }
        }
    }; // mDataFromService BroadcastReceiver()

    void inflateCurrentConfigRepresentation(Bundle config) {
        //Log.i(TAG, "((( inflateCurrentConfigRepresentation");
        mActivity.mIndexedColor.loadColors(config);
        mTxtElement.setText(mActivity.mIndexedColor.getCurrentDescription());
        mColorToEdit.setColor(mActivity.mIndexedColor.getCurrentColor());
        mColorToEdit.setLoaded();
        mColorToEdit.clearOverLoaded();
    }
    void restoreCurrentConfigRepresentation() {
        //Log.i(TAG, "((( restoreCurrentConfigRepresentation");
        if (null != mActivity.mIndexedColor && mActivity.mIndexedColor.isColorsLoaded()) {
            mTxtElement.setText(mActivity.mIndexedColor.getCurrentDescription());
            mTxtElement.invalidate();
            mColorToEdit.setColor(mActivity.mIndexedColor.getCurrentColor());
            mColorToEdit.setLoaded();
            mColorToEdit.clearOverLoaded();
        }
    }



    ImageButton mBtnWatchAppearance;
    //
    Drawable mWatchFaceAppearance = new Drawable() {
        @Override
        public void draw(Canvas canvas) {
            //Log.i(TAG, "*** draw 1 (" + mImporterActivity.isServiceBound() + "): mService=" + mService);

            //canvas.drawColor(Color.argb(100, 0, 0, 127));

            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int dim = Math.min(width, height);
            float centerX = width / 2f;
            float centerY = height / 2f;
            float radiusMax = Math.min(centerX, centerY) - 10f;
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            paint.setColor(0x99999999);
            paint.setStrokeWidth(2f);
            paint.setStyle(Paint.Style.STROKE);
            if (mService != null) { // mImporterActivity.isServiceBound() &&
                Bitmap lastFrameScreenshot;
                //mImporterActivity.mElementIndex
                if (mActivity.mIndexedColor.isInAmbientRange()) {
                    lastFrameScreenshot = mService.getLastAmbientScreenshot();
                } else {
                    lastFrameScreenshot = mService.getLastDenseScreenshot();
                }
                //Log.i(TAG, "*** draw: lastFrameScreenshot=" + lastFrameScreenshot);
                if (lastFrameScreenshot != null) {
                    //Log.i(TAG, "*** draw");
//                    Path clippingPath = new Path();
//                    clippingPath.addCircle(width/2F, height/2F, dim/2f-5f, Path.Direction.CW);
//                    canvas.clipPath(clippingPath);
                    Bitmap sb = Bitmap.createScaledBitmap(lastFrameScreenshot, dim, dim, true);
                    canvas.drawBitmap(sb, centerX-dim/2, centerY-dim/2, /*new Paint(*/null);
                    sb.recycle();
                } else {
                    //canvas.drawCircle(centerX, centerY, radiusMax, paint);
                    drawHint(canvas, paint, centerX, centerY, radiusMax);
                }
            } else {
                mService = mActivity.getBoundService();
                drawHint(canvas, paint, centerX, centerY, radiusMax);
            }
        }

        private void drawHint(Canvas canvas, Paint paint, float centerX, float centerY, float radiusMax) {
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(2f);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(centerX, centerY, radiusMax, paint);
            //
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(24f);
            paint.setAlpha(255);

//            String text = mActivity.getResources().getString(R.string.string_click) + "\n" +
//                    mActivity.getResources().getString(R.string.string_and) + "\n" +
//                    mActivity.getResources().getString(R.string.string_long_click);
//            ACommon.drawHvAlignedText(canvas, centerX, centerY , text,
//                    paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);

            ACommon.drawHvAlignedText(canvas, centerX, centerY - 24f, mActivity.getResources().getString(R.string.string_click),
                    paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
            ACommon.drawHvAlignedText(canvas, centerX, centerY, mActivity.getResources().getString(R.string.string_and),
                    paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
            ACommon.drawHvAlignedText(canvas, centerX, centerY + 24f, mActivity.getResources().getString(R.string.string_long_click),
                    paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
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
    };

/*

    Drawable mCurrentColorDrawable = new Drawable() {

        Bitmap mBitmapLight, mBitmapDark, mBitmapLightS, mBitmapDarkS;

        public void setBitmapLight(Bitmap bmp) { mBitmapLight = bmp; }
        public void setBitmapDark(Bitmap bmp) { mBitmapDark = bmp; }

        @Override
        public void draw(Canvas canvas) {

            int width = canvas.getWidth();
            int height = canvas.getHeight();
            float centerX = width / 2f;
            float centerY = height / 2f;
            float radiusMax = Math.min(centerX, centerY);

            if (mBitmapLight != null) {
                if (mBitmapLightS == null) {
                    mBitmapLightS = Bitmap.createScaledBitmap(mBitmapLight, (int) radiusMax, (int) radiusMax, true);
                }
            }
            if (mBitmapDark != null) {
                if (mBitmapDarkS == null) {
                    mBitmapDarkS = Bitmap.createScaledBitmap(mBitmapDark, (int) radiusMax, (int) radiusMax, true);
                }
            }

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            paint.setColor(mColorToEdit.getColor());
            canvas.drawCircle(centerX, centerY, radiusMax, paint);

            Log.i(TAG, "*** mCurrentColorDrawable.draw, mColorToEdit.getOverLoaded() = " + mColorToEdit.getOverLoaded());

            if (mColorToEdit.getOverLoaded() == true) {
                int color = mColorToEdit.rgbToY();
                Bitmap bmp;
                if (color < 128) bmp = mBitmapLightS;
                else bmp = mBitmapDarkS;
                canvas.drawBitmap(bmp, centerX-(int)(radiusMax/2), centerY-(int)(radiusMax/2), paint);
            }

            //canvas.drawColor(mColorToEdit.getColor());
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
    };

*/

    class CurrentColorDrawable extends Drawable {

        Bitmap mBitmapLight, mBitmapDark;
        Bitmap mBitmapLightS = null, mBitmapDarkS = null;
        Paint mPaint;

        int[] mRingColors;
        float[] mRingPositions;
        Shader mHsvRingShader;

        public CurrentColorDrawable() {
//            Shader rimShaderA = new SweepGradient(centerX, centerY, /*mVars.*/rimColors, /*mVars.*/rimColorOnPathPos);
//            rimShaderA.getLocalMatrix(mVars.hrMatrix);
//            mVars.hrMatrix.postRotate(rotAngleDeg, centerX, centerY);
//            rimShaderA.setLocalMatrix(mVars.hrMatrix);

            //Log.i(TAG, "((( CurrentColorDrawable");

            int ringInterval = 5, numRingPoints = (360 / ringInterval) + 1;
            mRingColors = new int[numRingPoints];
            mRingPositions = new float[numRingPoints];
            float[] hsv = new float[3];
            hsv[1] = 1.0f;
            hsv[2] = 1.0f;
            for (int i=0; i < numRingPoints; i++) {
                float position;
                int color;
                hsv[0] = i * ringInterval;
                position = hsv[0] / 360f;
                color = Color.HSVToColor(hsv);

                String strColor, strPosition;
                strPosition = String.format(Locale.US, "%.3f", position);

                //color = Integer.parseInt(strColor);
                position = Float.parseFloat(strPosition);

                mRingColors[i] = color;
                strColor = String.format(Locale.US, "%08x", color);
                mRingPositions[i] = position;

                //Log.i(TAG, "((( CurrentColorDrawable, mRingColors[" + i +"]=" + strColor + ", mRingPositions[" + i + "]=" + mRingPositions[i]);
            }

            mPaint = null;

            mHsvRingShader = null;
        }

        public void setBitmapLight(Bitmap bmp) { mBitmapLight = bmp; }
        public void setBitmapDark(Bitmap bmp) { mBitmapDark = bmp; }

        public void clearBitmapReferences() {
            mBitmapLight = null;
            mBitmapDark = null;
        }

        final static float RAD2DEG = (float) (180f/ Math.PI);
        final static float DEG2RAD = (float) (Math.PI/180f);

        @Override
        public void draw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            float centerX = width / 2f;
            float centerY = height / 2f;
            float radiusMax = Math.min(centerX, centerY);

            //Log.i(TAG, "((( CurrentColorDrawable, draw: mHsvRingShader=" + mHsvRingShader);

            //canvas.drawColor(Color.argb(100, 127, 0, 127));

            if (mBitmapLightS == null) {
                if (mBitmapLight != null) {
                    mBitmapLightS = Bitmap.createScaledBitmap(mBitmapLight, (int) radiusMax, (int) radiusMax, true);
                    //mBitmapLight.recycle();
                    mBitmapLight = null;
                }
            }
            if (mBitmapDarkS == null) {
                if (mBitmapDark != null) {
                    mBitmapDarkS = Bitmap.createScaledBitmap(mBitmapDark, (int) radiusMax, (int) radiusMax, true);
                    //mBitmapDark.recycle();
                    mBitmapDark = null;
                }
            }
            if (null == mHsvRingShader) {
                mHsvRingShader = new SweepGradient(centerX, centerY, mRingColors, mRingPositions);
                Matrix matrix = new Matrix();
                matrix.reset();
                mHsvRingShader.getLocalMatrix(matrix);
                matrix.postRotate(-90, centerX, centerY);
                mHsvRingShader.setLocalMatrix(matrix);
            }
            if (mPaint == null) {
                mPaint = new Paint();
                mPaint.setAntiAlias(true);
                mPaint.setDither(true);
                mPaint.setFilterBitmap(true);
            }


            Rect rect = new Rect();


            // draw color HSV scale ring
            canvas.getClipBounds(rect);
            Shader shader = mPaint.setShader(mHsvRingShader);
//            Log.i(TAG, "((( CurrentColorDrawable, draw: shader=" + shader);
//            Log.i(TAG, "((( CurrentColorDrawable, draw: canvas clip bounds, l=" + rect.left + " t=" + rect.top +
//                            " r=" + rect.right + " b=" + rect.bottom);
            mPaint.setStrokeWidth(10f);
            mPaint.setAlpha(255);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(centerX, centerY, radiusMax - 5f, mPaint);
            mPaint.setShader(null);


            int colorY = mColorToEdit.rgbToY();
            int color = mColorToEdit.getColor();
            //
            int alpha, red, green, blue;
            int normColor, newRed, newGreen, newBlue;
            float coeff;
            alpha = Color.alpha(color); red = Color.red(color); green = Color.green(color); blue = Color.blue(color);
            coeff = (float)alpha / 255f;
            newRed = (int)((float)red * coeff); newGreen = (int)((float)green * coeff); newBlue = (int)((float)blue * coeff);
            normColor = Color.argb(255, newRed, newGreen, newBlue);
            //
            float hsv[] = new float[3];
            //
            Color.colorToHSV(normColor, hsv);



            // draw rotated arrow H-angle pointer
            Path pointerPath = new Path();
            pointerPath.moveTo(centerX, centerY);
            float pointX, pointY;
//            mVars.innerX = (float) Math.sin(mVars.tickRot) * mVars.innerTickRadius;
//            mVars.innerY = (float) -Math.cos(mVars.tickRot) * mVars.innerTickRadius;
            pointX = centerX + (float) Math.sin(hsv[0] * DEG2RAD) * (radiusMax - 9f);
            pointY = centerY + (float) -Math.cos(hsv[0] * DEG2RAD) * (radiusMax - 9f);
            pointerPath.lineTo(pointX, pointY);
            mPaint.setStrokeWidth(15f);
            //mPaint.setStrokeCap(Paint.Cap.BUTT);
            //mPaint.setStrokeJoin(Paint.Join.MITER);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.BLACK);
            canvas.drawPath(pointerPath, mPaint);
            mPaint.setColor(color);
            canvas.drawPath(pointerPath, mPaint);
            mPaint.setStrokeWidth(3f);
            if (colorY < 150) mPaint.setColor(Color.WHITE);
            else mPaint.setColor(Color.BLACK);
            canvas.drawPath(pointerPath, mPaint);



            // draw color circle
            mPaint.setStrokeWidth(1f);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.BLACK);
            canvas.drawCircle(centerX, centerY, radiusMax - 30f, mPaint);
            mPaint.setColor(color);
            canvas.drawCircle(centerX, centerY, radiusMax - 30f, mPaint);




            // draw HSV inscriptions
//            mVars.mPathOuter.reset();
//            mVars.mPathInner.addCircle(mVars.centerX, mVars.centerY, mVars.mDigitsRadiusPathInner, Path.Direction.CW);
//            canvas.drawTextOnPath(secondDigit, mVars.mPathOuter, mVars.digitPos, -1f, mTickDigitPaint);
//            mVars.hrMatrix.setRotate(-96.f, mVars.centerX, mVars.centerY);
//            mVars.mPathInner.transform(mVars.hrMatrix);
//            mTickDigitPaint.getTextBounds(firstDigit, 0, 1, mVars.textBounds);
//            float fw = mVars.textBounds.width();
            Path pathInner = new Path(), pathOuter = new Path();
            Matrix matrix = new Matrix();
            String strVal = "H: " + String.format(Locale.US, "%.2f", hsv[0]) + "\u00b0";
            mPaint.setTextSize(24f);
            mPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            mPaint.getTextBounds(strVal, 0, strVal.length(), rect);
            float rotPortion = (float) (rect.width() / (4 * Math.PI * (radiusMax - 30f - 30f)));
            float rotDeg = rotPortion * 360f;
//            Log.i(TAG, "((( CurrentColorDrawable, draw: rotPortion=" + rotPortion + ", rotDeg=" + rotDeg +
//                    ", radius=" + (radiusMax - 30f - 30f) + ", width=" + rect.width()
//            );
            matrix.setRotate(-(90f + rotDeg), centerX, centerY);
            pathInner.addCircle(centerX, centerY, radiusMax - 30f - 30f, Path.Direction.CW);
            pathInner.transform(matrix);
            if (colorY < 150) mPaint.setColor(Color.WHITE);
            else mPaint.setColor(Color.BLACK);
            canvas.drawTextOnPath(strVal, pathInner, 0f, 0f, mPaint); // -(rect.width() / 2f)
            //
            matrix.reset();
            strVal = "S: " + String.format(Locale.US, "%3.1f", (hsv[1] * 100f)) + "%";
            mPaint.setTextSize(24f);
            mPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            mPaint.getTextBounds(strVal, 0, strVal.length(), rect);
            rotPortion = (float) (rect.width() / (4 * Math.PI * (radiusMax - 30f - 10f)));
            rotDeg = rotPortion * 360f;
//            Log.i(TAG, "((( CurrentColorDrawable, draw: rotPortion=" + rotPortion + ", rotDeg=" + rotDeg +
//                            ", radius=" + (radiusMax - 30f - 10f) + ", width=" + rect.width()
//            );
            matrix.setRotate((135f + rotDeg), centerX, centerY);
            pathOuter.addCircle(centerX, centerY, radiusMax - 30f - 10f, Path.Direction.CCW);
            pathOuter.transform(matrix);
            if (colorY < 150) mPaint.setColor(Color.WHITE);
            else mPaint.setColor(Color.BLACK);
            canvas.drawTextOnPath(strVal, pathOuter, 0f, 0f, mPaint); // -(rect.width() / 2f)
            //
            matrix.reset();
            pathOuter.reset();
            strVal = "V: " + String.format(Locale.US, "%3.1f", (hsv[2] * 100f)) + "%";
            mPaint.setTextSize(24f);
            mPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            mPaint.getTextBounds(strVal, 0, strVal.length(), rect);
            rotPortion = (float) (rect.width() / (4 * Math.PI * (radiusMax - 30f - 10f)));
            rotDeg = rotPortion * 360f;
//            Log.i(TAG, "((( CurrentColorDrawable, draw: rotPortion=" + rotPortion + ", rotDeg=" + rotDeg +
//                            ", radius=" + (radiusMax - 30f - 10f) + ", width=" + rect.width()
//            );
            matrix.setRotate((45f + rotDeg), centerX, centerY);
            pathOuter.addCircle(centerX, centerY, radiusMax - 30f - 10f, Path.Direction.CCW);
            pathOuter.transform(matrix);
            if (colorY < 150) mPaint.setColor(Color.WHITE);
            else mPaint.setColor(Color.BLACK);
            canvas.drawTextOnPath(strVal, pathOuter, 0f, 0f, mPaint); // -(rect.width() / 2f)




            // draw "save to watches" icon
            if (mColorToEdit.getOverLoaded() == true) {
                Bitmap bmp;

                if (colorY < 128) bmp = mBitmapLightS;
                else bmp = mBitmapDarkS;
                mPaint.setAlpha(255);
                if(null != bmp) canvas.drawBitmap(bmp, centerX-(int)(radiusMax/2), centerY-(int)(radiusMax/2), mPaint);
            }
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
    } // CurrentColorDrawable

    CurrentColorDrawable mCurrentColorDrawable;// = new CurrentColorDrawable();





    class DrawableSegmentButton extends Drawable {

        Bitmap mBitmap, mBitmapS = null;
        int mSub = 0;
        int mAlpha = 150;
        int mColor = 0x9600e4fc; //0x9633ffff; // 0x96 == 150
        int mNumberSegments = 1;
        boolean isSquare = true;
        int         bmpWidth, bmpHeight;
        int         bmpWidthScaled, bmpHeightScaled;
        float       bmpScale;

        public void setSquare(boolean value) { isSquare = value; }

        DrawableSegmentButton(Bitmap bmp, int sub, int nsegm) {
            mBitmap = bmp;
            mSub = sub;
            mNumberSegments = nsegm;
            currentSegment = 0;

            if (null != mBitmap) {
                bmpWidth = mBitmap.getWidth();
                bmpHeight = mBitmap.getHeight();
            }
        }

        public void clearBitmapReference() {
            mBitmap = null;
        }

        private float mAngleGapDeg = 6f;
        private int currentSegment;


        public void toggleSegment() {
            if (++currentSegment >= mNumberSegments) currentSegment = 0;
            this.invalidateSelf();
        }


        public void drawSquare(Canvas canvas) {
//            int width = canvas.getWidth();
//            int height = canvas.getHeight();
//            int dimMin = Math.min(width, height);
//            float centerX = width / 2f;
//            float centerY = height / 2f;
//            int bX = (int) (centerX - (dimMin-mSub)/2);
//            int bY = (int) (centerY - (dimMin-mSub)/2);
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

            //canvas.drawColor(0x7700aa00);

            if (null == mBitmapS) {
                if (null != mBitmap) {
                    mBitmapS = Bitmap.createScaledBitmap(mBitmap, bmpSide, bmpSide, true);
                    //mBitmap.recycle();
                    //recycleBitmapOnDestroy.remove(mBitmap);
                    mBitmap = null;
                }
            }

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            paint.setAlpha(mAlpha);

            if (null != mBitmapS) {
                canvas.drawBitmap(mBitmapS, bX, bY, paint);
                //
            }

            paint.setStyle(Paint.Style.STROKE);
            //paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(4f);
            //canvas.drawCircle(centerX, centerY, (dimMin-mSub-8f)/2f, paint);


            float radius = (bmpSide - 8f) / 2f;
            RectF circleRect = new RectF(centerX-radius, centerY-radius, centerX+radius, centerY+radius);

            float segmentSweepRad = (float) ((2f * Math.PI) / mNumberSegments);
            float segmentSweepDeg = (float) (segmentSweepRad * 180f / Math.PI);
            float angleStartDeg, angleSweepDeg;

            paint.setStrokeCap(Paint.Cap.SQUARE);
            for (int i=0; i<mNumberSegments; i++) {
                if (currentSegment == i) {
                    paint.setColor(mColor);
                } else {
                    paint.setColor(0x96ffffff);
                }
                angleStartDeg = segmentSweepDeg * i + (mAngleGapDeg / 2);
                angleSweepDeg = segmentSweepDeg - mAngleGapDeg;
                canvas.drawArc(circleRect, angleStartDeg-90f, angleSweepDeg, false, paint);
            }
//                //
//                float angleStartRad = (float) ((angleGapDeg / 2f * Math.PI) / 180f) + angleStartPointRad; //(float) (angleStartDeg * Math.PI / 180f);
//                float angleStartDeg = (float) (angleStartRad * 180f / Math.PI); // angleGapDeg / 2f; // /*-60f*/ angleStartPointDeg + angleGapDeg / 2f;
//                float angleSweepDeg = /*300f*/ 360f - angleGapDeg; // 360f - angleGapDeg
//                float angleSweepRad = (float) (angleSweepDeg * Math.PI / 180f);
//                float angleTickRad = angleSweepRad / (divisorMain /*- 1*/);
//                //
//                //canvas.drawArc(dialRect, angleStartDeg-90f, angleSweepDeg, false, mBattDialPaint);
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

            paint.setStyle(Paint.Style.STROKE);
            //paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(4f);
            //canvas.drawCircle(centerX, centerY, (dimMin-mSub-8f)/2f, paint);

            float radius = (bmpHeightScaled - 8f) / 2f; //(bmpSide - 8f) / 2f;
            RectF circleRect = new RectF(centerX-radius, centerY-radius, centerX+radius, centerY+radius);

            float segmentSweepRad = (float) ((2f * Math.PI) / mNumberSegments);
            float segmentSweepDeg = (float) (segmentSweepRad * 180f / Math.PI);
            float angleStartDeg, angleSweepDeg;

            paint.setStrokeCap(Paint.Cap.SQUARE);
            for (int i=0; i<mNumberSegments; i++) {
                if (currentSegment == i) {
                    paint.setColor(mColor);
                } else {
                    paint.setColor(0x96ffffff);
                }
                angleStartDeg = segmentSweepDeg * i + (mAngleGapDeg / 2);
                angleSweepDeg = segmentSweepDeg - mAngleGapDeg;
                canvas.drawArc(circleRect, angleStartDeg-90f, angleSweepDeg, false, paint);
            }

        } // drawOther




        @Override
        public void draw(Canvas canvas) {
            //if (isSquare) drawSquare(canvas);
            drawOther(canvas);
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
    } // class DrawableSegmentButton





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


        public void clearBitmapreference() {
            mBitmap = null;
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





    class DrawableNextColorButton extends Drawable {

        int mSub = 0;
        int mAlpha = 190;

        DrawableNextColorButton(int sub) { mSub = sub; }

        Bitmap mBitmapForward, mBitmapBackward;
        Bitmap mBitmapForwardS = null, mBitmapBackwardS = null;

        public void setBitmapForward(Bitmap bmp) { mBitmapForward = bmp; }
        public void setBitmapBackward(Bitmap bmp) { mBitmapBackward = bmp; }

        public void clearBitmapReferences() {
            mBitmapForward = null;
            mBitmapBackward = null;
        }

        boolean mForward = true;

        public boolean isForward() { return mForward; }
        public void toggleDirection() {
            mForward = !mForward;
            this.invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
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

            //canvas.drawColor(Color.GRAY);

            if (mBitmapForwardS == null) {
                if (mBitmapForward != null) {
                    mBitmapForwardS = Bitmap.createScaledBitmap(mBitmapForward, bmpSide, bmpSide, true);
                    //mBitmapForward.recycle();
                    mBitmapForward = null;
                }
            }
            if (mBitmapBackwardS == null) {
                if (mBitmapBackward != null) {
                    mBitmapBackwardS = Bitmap.createScaledBitmap(mBitmapBackward, bmpSide, bmpSide, true);
                    //mBitmapBackward.recycle();
                    mBitmapBackward = null;
                }
            }

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);

            Bitmap bmp;
            if (true == mForward) bmp = mBitmapForwardS;
            else bmp = mBitmapBackwardS;
            paint.setAlpha(mAlpha);
            if (null != bmp) canvas.drawBitmap(bmp, bX, bY, paint);
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
    } // class DrawableNextColorButton





    class DrawableButtonUpDn extends Drawable {
        Bitmap mBitmap, mBitmapS = null;
        Bitmap mBitmap3, mBitmap3S = null;
        //int mSub = 0;
        int mAlpha = 150;
        boolean mUpward;
        boolean mTriple = false;

        DrawableButtonUpDn(Bitmap bmp, Bitmap bmp3, boolean up) { mBitmap = bmp; mBitmap3 = bmp3; mUpward = up; }

        public void clearBitmapReferences() {
            mBitmap = null;
            mBitmap3 = null;
        }

        public void setTriple(boolean t) {mTriple = t;}
        public boolean isTriple() {return mTriple;}
        public void toggleTriple() {mTriple = !mTriple; invalidateSelf(); }

        @Override
        public void draw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int dimMin = Math.min(width, height);
            float centerX = width / 2f;
            float centerY = height / 2f;
            //int bX = (int) (centerX - (dimMin)/2);
            //int bY = (int) (centerY - (dimMin)/2);
            Bitmap /*bitmap, */bitmapS;

            if (null==mBitmapS) {
                if (null!=mBitmap) {
                    mBitmapS = Bitmap.createScaledBitmap(mBitmap, dimMin, dimMin, true);
                    //mBitmap.recycle();
                    mBitmap = null;
                }
            }
            if (null==mBitmap3S) {
                if (null!=mBitmap3) {
                    mBitmap3S = Bitmap.createScaledBitmap(mBitmap3, dimMin, dimMin, true);
                    //mBitmap3.recycle();
                    mBitmap3 = null;
                }
            }

            if (mTriple) {
                bitmapS = mBitmap3S;
            } else {
                bitmapS = mBitmapS;
            }

            if (null != bitmapS) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setDither(true);
                paint.setFilterBitmap(true);
                paint.setAlpha(mAlpha);

                int bmpWidth = bitmapS.getWidth();
                int bmpHeight = bitmapS.getHeight();
                Matrix hrMatrix = new Matrix();
                hrMatrix.setTranslate(-bmpWidth/2, -bmpHeight/2);
                if (!mUpward) hrMatrix.postRotate(180f);
                hrMatrix.postTranslate(centerX, centerY);

                canvas.drawBitmap(bitmapS, hrMatrix, paint);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            mAlpha = alpha;
        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    } // class DrawableButtonUpDn

/*

    Drawable mNextColorDrawable = new Drawable() {
        @Override
        public void draw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int dimMin = Math.min(width, height);
            if (null == mIconBtnNextS) {
                mIconBtnNextS = Bitmap.createScaledBitmap(mIconBtnNext, dimMin-50, dimMin-50, true);
            }
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            paint.setAlpha(150);
            canvas.drawBitmap(mIconBtnNextS, 0, 0, paint);
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
    };

*/




//    String getCurrentDescription() {
//        String s = "";
//        if (null != mImporterActivity.mColors) {
//            s = mImporterActivity.mElementName[mImporterActivity.mElementIndex] + "  [" + (mImporterActivity.mElementIndex + 1) + " из " + mImporterActivity.mColors.length + "]";
//        }
//        return s;
//    }



    DrawableButton mDrawableButtonResetToDefaults;
//    DrawableButton mDrawableButtonLayout;
    DrawableSegmentButton mDrawableButtonLayout;
//    DrawableButton mDrawableButtonMainHands;
    DrawableSegmentButton mDrawableButtonMainHands;
//    DrawableButton mDrawableButtonDate;
    DrawableSegmentButton mDrawableButtonDate;
    DrawableButton mDrawableBtnColorMemAdd;
    DrawableButton mDrawableBtnSaveToConfigPalette;

    DrawableNextColorButton mBtnNextColorDrawable, mBtnPrevColorDrawable;


    DrawableButton mDrawableBtnSortColorPalette;

    DrawableButton mDrawableBtnRateUs;

    DrawableButton mDrawableShareSingle;










    public static PageFragmentLayout newInstance(int page) {
        //Log.i(TAG, "((( PageFragmentLayout newInstance = " + page);
        Bundle args = new Bundle();
        args.putInt(ACommon.ARG_PAGE, page);
        PageFragmentLayout fragment = new PageFragmentLayout();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onResume() {
        //Log.i(TAG, "((( onResume");
        super.onResume();

        initExternalGlobals();

        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mDataFromService, new IntentFilter(ACommon.EVENT_ACTION));

//        Log.i(TAG, "((( mColorPaletteViewList size=" + mColorPaletteViewList.size());
//        Log.i(TAG, "((( mColorPaletteLinearLayout=" + mColorPaletteLinearLayout);
//        Log.i(TAG, "((( gColorPalette size=" + mActivity.gColorPalette.size());


        restoreCurrentConfigRepresentation();
    }


    @Override
    public void onPause() {
        //Log.i(TAG, "((( onPause");
        super.onPause();
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mDataFromService);
    }


    private void initExternalGlobals() {
        //Log.i(TAG, "((( initExternalGlobals");
        mActivity = (HandheldCompanionConfigActivity) getActivity();
        mGoogleApiClient = mActivity.getGoogleApiClient();
        mService = mActivity.getBoundService();
    } // initExternalGlobals




    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bitmap bitmap1, bitmap2;

        super.onCreate(savedInstanceState);

        recycleBitmapOnDestroy = new ArrayList<>();

        //Log.i(TAG, "((( onCreate");
        //mPage = getArguments().getInt(ACommon.ARG_PAGE);

        initExternalGlobals();
        //Log.i(TAG, "((( onCreate, activity ColorPalette size=" + mActivity.gColorPalette.size());



        int btnMargin = 5;

        mDrawableButtonResetToDefaults = new DrawableButton(ACommon.loadBitmap(mActivity, R.drawable.ic_reset), btnMargin);

        mDrawableBtnRateUs = new DrawableButton(ACommon.loadBitmap(mActivity, R.drawable.ic_rateus), btnMargin);
        mDrawableBtnRateUs.setSquare(false);
        mDrawableBtnRateUs.setAlpha(255);

        mDrawableButtonDate = new DrawableSegmentButton(ACommon.loadBitmap(mActivity, R.drawable.ic_dom_segm), btnMargin, 2);
        mDrawableButtonMainHands = new DrawableSegmentButton(
                ACommon.loadBitmap(mActivity, R.drawable.ic_hands_segm), btnMargin, ACommon.NUM_MAIN_HAND_SETS);
        mDrawableButtonLayout = new DrawableSegmentButton(ACommon.loadBitmap(mActivity, R.drawable.ic_layout_segm), btnMargin, 11);

        mDrawableBtnSaveToConfigPalette = new DrawableButton(ACommon.loadBitmap(mActivity, R.drawable.ic_collect), btnMargin);

        mDrawableShareSingle = new DrawableButton(ACommon.loadBitmap(mActivity, R.drawable.ic_share), btnMargin);




        mDrawableBtnColorMemAdd = new DrawableButton(ACommon.loadBitmap(mActivity, R.drawable.ic_mplus), 10);
        mDrawableBtnSortColorPalette = new DrawableButton(ACommon.loadBitmap(mActivity, R.drawable.ic_sort), 10);




        bitmap1 = ACommon.loadBitmap(mActivity, R.drawable.ic_up);
        bitmap2 = ACommon.loadBitmap(mActivity, R.drawable.ic_up3);
        mBtnUpDrawable = new DrawableButtonUpDn(bitmap1, bitmap2, true);
        mBtnDnDrawable = new DrawableButtonUpDn(bitmap1, bitmap2, false);

        mCurrentColorDrawable = new CurrentColorDrawable();
        bitmap1 = ACommon.loadBitmap(mActivity, R.drawable.ic_save_dark);
        bitmap2 = ACommon.loadBitmap(mActivity, R.drawable.ic_save_light);
        mCurrentColorDrawable.setBitmapDark(bitmap1);
        mCurrentColorDrawable.setBitmapLight(bitmap2);

        mBtnNextColorDrawable = new DrawableNextColorButton(10);
        bitmap1 = ACommon.loadBitmap(mActivity, R.drawable.ic_colors_backward);
        bitmap2 = ACommon.loadBitmap(mActivity, R.drawable.ic_colors_forward);
        mBtnNextColorDrawable.setBitmapBackward(bitmap1);
        mBtnNextColorDrawable.setBitmapForward(bitmap2);
        //
        mBtnPrevColorDrawable = new DrawableNextColorButton(10);
        mBtnPrevColorDrawable.setBitmapBackward(bitmap1);
        mBtnPrevColorDrawable.setBitmapForward(bitmap2);
        mBtnPrevColorDrawable.toggleDirection();

        //Log.i(TAG, "((( recycleBitmapOnDestroy.size = " + recycleBitmapOnDestroy.size());

    } // onCreate


    ArrayList<Bitmap> recycleBitmapOnDestroy;// = new ArrayList<>();


    @Override
    public void onDestroy() {
        //Log.i(TAG, "((( onDestroy, activity ColorPalette size=" + mActivity.gColorPalette.size());
        super.onDestroy();
        //Log.i(TAG, "((( onDestroy");

        mDrawableButtonLayout.clearBitmapReference(); mDrawableButtonLayout = null;
        mDrawableButtonResetToDefaults.clearBitmapreference(); mDrawableButtonResetToDefaults = null;
        mDrawableButtonMainHands.clearBitmapReference(); mDrawableButtonMainHands = null;
        mDrawableButtonDate.clearBitmapReference(); mDrawableButtonDate = null;
        mBtnUpDrawable.clearBitmapReferences(); mBtnUpDrawable = null;
        mBtnDnDrawable.clearBitmapReferences(); mBtnDnDrawable = null;
        mCurrentColorDrawable.clearBitmapReferences(); mCurrentColorDrawable = null;
        mDrawableBtnColorMemAdd.clearBitmapreference(); mDrawableBtnColorMemAdd = null;
        mDrawableBtnSaveToConfigPalette.clearBitmapreference(); mDrawableBtnSaveToConfigPalette = null;
        mDrawableShareSingle.clearBitmapreference(); mDrawableShareSingle = null;
        mBtnNextColorDrawable.clearBitmapReferences(); mBtnNextColorDrawable = null;
        //mBtnPrevColorDrawable
        mBtnPrevColorDrawable.clearBitmapReferences(); mBtnPrevColorDrawable = null;
        mDrawableBtnSortColorPalette.clearBitmapreference(); mDrawableBtnSortColorPalette = null;
        mDrawableBtnRateUs.clearBitmapreference(); mDrawableBtnRateUs = null;

        //Log.i(TAG, "((( recycleBitmapOnDestroy.size = " + recycleBitmapOnDestroy.size());
//        for (Bitmap bitmap: recycleBitmapOnDestroy) {
//            bitmap.recycle();
//        }
        recycleBitmapOnDestroy.clear();
        recycleBitmapOnDestroy = null;

        mCommonHandler.removeMessages(ACommon.MSG_SCROLL_TO_END);

        System.gc();
    }




    private void listLocalFiles(String prefix) {
        String[] files = mActivity.fileList();
        int count=0;
        for (String fileName : files) {
            count++;
            File file = mActivity.getFileStreamPath(fileName);
            //Log.i(TAG, "((( localFiles " + prefix + ", file[" + count + "] = " + fileName + ", size=" + file.length());

//            if (file.matches("^[0-9]+_[AD]$")) {
//                Log.i(TAG, "((( removeDedicatedIcons, DEDICATED ICON found");
//                deleteFile(file);
//            }

            // example: 436289854000-IS_SILENT.stacktrace, 1436289300000-approved.stacktrace
//            if (fileName.matches("^[0-9]+-approved.stacktrace") || fileName.matches("^ACRA-INSTALLATION")) {
//                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//                File fileOut = new File(path, fileName);
//                Log.i(TAG, "((((( listLocalFiles, file=" + path + "/" + fileName);
//                //adb -s 0a3d818c pull /sdcard/Pictures/1436193606000-approved.stacktrace
//                //adb -s 0a3d818c pull /sdcard/Pictures/ACRA-INSTALLATION
//                try {
//                    ACommon.copyFile(file, fileOut);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

        }
    } // listLocalFiles





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.i(TAG, "((( onCreateView");

        initExternalGlobals();

        View view = inflater.inflate(R.layout.fragment_layout, container, false);
        mView = view;

        Resources resources = this.getResources();
        Drawable drawable;




        mTxtSeekVal = (TextView) mView.findViewById(R.id.txtSeekVal);
        mTxtSeekVal.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBtnDnDrawable.toggleTriple();
                        mBtnUpDrawable.toggleTriple();
                    }
                }
        );
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            mTxtSeekVal.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            //return false;
                            //String crashString = null;
                            //Log.i(TAG, crashString.toString());
                            //ACRA.getErrorReporter().handleException(null);

                            ACRA.getErrorReporter().reportBuilder().forceSilent().message("TEST DELIVERY").send();
                            Toast.makeText(mActivity.getApplicationContext(), "Test report delivery sent.", Toast.LENGTH_LONG).show();

                            //ACRA.getErrorReporter().reportBuilder().send();
                            //ACRA.getErrorReporter().
//                        listLocalFiles("BEFORE");
//                        ErrorReporter.ReportBuilder errRep = ACRA.getErrorReporter().reportBuilder().message("DUMMY: pending reports follows");
//                        listLocalFiles("AFTER");
                            //errRep.
                            return true;
                        }
                    }
            );
        }
        mTxtRed = (TextView) mView.findViewById(R.id.txtRed);
        mSeekBarR = (SeekBar) mView.findViewById(R.id.seekBarR);
        mTxtGreen = (TextView) mView.findViewById(R.id.txtGreen);
        mSeekBarG = (SeekBar) mView.findViewById(R.id.seekBarG);
        mTxtBlue = (TextView) mView.findViewById(R.id.txtBlue);
        mSeekBarB = (SeekBar) mView.findViewById(R.id.seekBarB);
        mTxtAlpha = (TextView) mView.findViewById(R.id.txtAlpha);
        mSeekBarA = (SeekBar) mView.findViewById(R.id.seekBarAlpha);
        String s;
        s = String.valueOf(mSeekBarR.getProgress());
        mTxtRed.setText(s);
        s = String.valueOf(mSeekBarG.getProgress());
        mTxtGreen.setText(s);
        s = String.valueOf(mSeekBarB.getProgress());
        mTxtBlue.setText(s);
        s = String.valueOf(mSeekBarA.getProgress());
        mTxtAlpha.setText(s);
        if (null != mTargetSeekBar) {
            mTxtSeekVal.setText(String.valueOf(mTargetSeekBar.getProgress()));
        } else {
            mTxtSeekVal.setText("");
        }
        if (null != mTargetTxt) mTxtSeekVal.setTextColor(mTargetTxt.getCurrentTextColor());

        mTxtRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTargetSeekBar = mSeekBarR;
                mTargetTxt = (TextView) v;
                mTxtSeekVal.setTextColor(mTargetTxt.getCurrentTextColor());
                String s = String.valueOf(mTargetSeekBar.getProgress());
                mTxtSeekVal.setText(s);
                mTargetTxt.setText(s);
                if (mBtnUpDrawable.isTriple()) mBtnUpDrawable.toggleTriple();
                if (mBtnDnDrawable.isTriple()) mBtnDnDrawable.toggleTriple();
            }
        });
        mTxtGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTargetSeekBar = mSeekBarG;
                mTargetTxt = (TextView) v;
                mTxtSeekVal.setTextColor(mTargetTxt.getCurrentTextColor());
                String s = String.valueOf(mTargetSeekBar.getProgress());
                mTxtSeekVal.setText(s);
                mTargetTxt.setText(s);
                if (mBtnUpDrawable.isTriple()) mBtnUpDrawable.toggleTriple();
                if (mBtnDnDrawable.isTriple()) mBtnDnDrawable.toggleTriple();
            }
        });
        mTxtBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTargetSeekBar = mSeekBarB;
                mTargetTxt = (TextView) v;
                mTxtSeekVal.setTextColor(mTargetTxt.getCurrentTextColor());
                String s = String.valueOf(mTargetSeekBar.getProgress());
                mTxtSeekVal.setText(s);
                mTargetTxt.setText(s);
                if (mBtnUpDrawable.isTriple()) mBtnUpDrawable.toggleTriple();
                if (mBtnDnDrawable.isTriple()) mBtnDnDrawable.toggleTriple();
            }
        });
        mTxtAlpha.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        mTargetSeekBar = mSeekBarA;
                        mTargetTxt = (TextView) v;
                        mTxtSeekVal.setTextColor(mTargetTxt.getCurrentTextColor());
                        String s = String.valueOf(mTargetSeekBar.getProgress());
                        mTxtSeekVal.setText(s);
                        mTargetTxt.setText(s);
                        if (mBtnUpDrawable.isTriple()) mBtnUpDrawable.toggleTriple();
                        if (mBtnDnDrawable.isTriple()) mBtnDnDrawable.toggleTriple();
                    }
                }
        );

        mSeekBarR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int idCur = seekBar.getId();
                int idTarget = 0;
                String s = String.valueOf(seekBar.getProgress());
                if (null != mTargetSeekBar) {
                    idTarget = mTargetSeekBar.getId();
                    if (idTarget == idCur) {
                        mTxtSeekVal.setText(s);
                        mTxtSeekVal.setTextColor(mTxtRed.getCurrentTextColor());
                    }
                }
                mTxtRed.setText(s);
/*
                if (null != mSeekBarA) mSeekBarA.setProgress((color & 0xff000000) >>> 24);
                if (null != mSeekBarR) mSeekBarR.setProgress((color & 0x00ff0000) >> 16);
                if (null != mSeekBarG) mSeekBarG.setProgress((color & 0x0000ff00) >> 8);
                if (null != mSeekBarB) mSeekBarB.setProgress((color & 0x000000ff));
*/
                //int color = mColorToEdit.getColor();
                //int ecolor = (progress & 0x000000ff) << 16;
                //mColorToEdit.setColor((color & 0xff00ffff) & (ecolor));
                mColorToEdit.setColor((mColorToEdit.getColor() & 0xff00ffff) | ((progress & 0x000000ff) << 16));
                if (true == fromUser) mColorToEdit.setOverLoaded();
                //Log.i(TAG, "***R onProgressChanged(" + (idCur == idTarget) + "): fromUser=" + fromUser + ", progress=" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Log.i(TAG, "***R onStartTrackingTouch: ");
                mTargetSeekBar = seekBar;
                mTargetTxt = mTxtRed;
                mTxtSeekVal.setTextColor(mTargetTxt.getCurrentTextColor());
                mTxtSeekVal.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Log.i(TAG, "***R onStopTrackingTouch: ");
            }
        });

        mSeekBarG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int idCur = seekBar.getId();
                int idTarget = 0;
                String s = String.valueOf(seekBar.getProgress());
                if (null != mTargetSeekBar) {
                    idTarget = mTargetSeekBar.getId();
                    if (idTarget == idCur) {
                        mTxtSeekVal.setText(s);
                        mTxtSeekVal.setTextColor(mTxtGreen.getCurrentTextColor());
                    }
                }
                mTxtGreen.setText(s);
                mColorToEdit.setColor((mColorToEdit.getColor() & 0xffff00ff) | ((progress & 0x000000ff) << 8));
                if (true == fromUser) mColorToEdit.setOverLoaded();
                //Log.i(TAG, "***G onProgressChanged(" + (idCur==idTarget) + "): fromUser=" + fromUser + ", progress=" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Log.i(TAG, "***G onStartTrackingTouch: ");
                mTargetSeekBar = seekBar;
                mTargetTxt = mTxtGreen;
                mTxtSeekVal.setTextColor(mTargetTxt.getCurrentTextColor());
                mTxtSeekVal.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int idCur = seekBar.getId();
                int idTarget = 0;
                String s = String.valueOf(seekBar.getProgress());
                if (null != mTargetSeekBar) {
                    idTarget = mTargetSeekBar.getId();
                    if (idTarget == idCur) {
                        mTxtSeekVal.setText(s);
                        mTxtSeekVal.setTextColor(mTxtBlue.getCurrentTextColor());
                    }
                }
                mTxtBlue.setText(s);
                mColorToEdit.setColor((mColorToEdit.getColor() & 0xffffff00) | ((progress & 0x000000ff)));
                if (true == fromUser) mColorToEdit.setOverLoaded();
                //Log.i(TAG, "***B onProgressChanged(" + (idCur==idTarget) + "): fromUser=" + fromUser + ", progress=" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Log.i(TAG, "***B onStartTrackingTouch: ");
                mTargetSeekBar = seekBar;
                mTargetTxt = mTxtBlue;
                mTxtSeekVal.setTextColor(mTargetTxt.getCurrentTextColor());
                mTxtSeekVal.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int idCur = seekBar.getId();
                int idTarget = 0;
                String s = String.valueOf(seekBar.getProgress());
                if (null != mTargetSeekBar) {
                    idTarget = mTargetSeekBar.getId();
                    if (idTarget == idCur) {
                        mTxtSeekVal.setText(s);
                        mTxtSeekVal.setTextColor(mTxtAlpha.getCurrentTextColor());
                    }
                }
                mTxtAlpha.setText(s);
                mColorToEdit.setColor((mColorToEdit.getColor() & 0x00ffffff) | ((progress & 0x000000ff) << 24));
                if (true == fromUser) mColorToEdit.setOverLoaded();
                //Log.i(TAG, "***A onProgressChanged(" + (idCur==idTarget) + "): fromUser=" + fromUser + ", progress=" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Log.i(TAG, "***A onStartTrackingTouch: ");
                mTargetSeekBar = seekBar;
                mTargetTxt = mTxtAlpha;
                mTxtSeekVal.setTextColor(mTargetTxt.getCurrentTextColor());
                mTxtSeekVal.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



//        drawable = resources.getDrawable(R.drawable.ic_up);
//        Drawable drawable1 = resources.getDrawable(R.drawable.ic_up3);
//        mBtnUpDrawable = new DrawableButtonUpDn(((BitmapDrawable) drawable).getBitmap(), ((BitmapDrawable) drawable1).getBitmap(), true);
//        mBtnDnDrawable = new DrawableButtonUpDn(((BitmapDrawable) drawable).getBitmap(), ((BitmapDrawable) drawable1).getBitmap(), false);
        mBtnUp = (ImageButton) mView.findViewById(R.id.btnUp);
        mBtnUp.setBackground(mBtnUpDrawable);
        mBtnDn = (ImageButton) mView.findViewById(R.id.btnDn);
        mBtnDn.setBackground(mBtnDnDrawable);
        mBtnUp.setOnTouchListener(
                new RepeatListener(400, 100,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Log.i(TAG, "onClick UP: mTargetSeekBar=" + mTargetSeekBar);
                                if (mBtnUpDrawable.isTriple()) {
                                    mSeekBarR.setProgress(mSeekBarR.getProgress()+1);
                                    mSeekBarG.setProgress(mSeekBarG.getProgress()+1);
                                    mSeekBarB.setProgress(mSeekBarB.getProgress()+1);
                                    mColorToEdit.setOverLoaded();
                                } else {
                                    if (null != mTargetSeekBar) {
                                        int idTarget = mTargetSeekBar.getId();
                                        mTargetSeekBar = (SeekBar) mView.findViewById(idTarget);
                                        int prg = mTargetSeekBar.getProgress();
                                        mTargetSeekBar.setProgress(prg + 1);
                                        mColorToEdit.setOverLoaded();
                                    }
                                }
                            }
                        })
        );
        mBtnDn.setOnTouchListener(
                new RepeatListener(400, 100,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Log.i(TAG, "onClick DN: mTargetSeekBar=" + mTargetSeekBar);
                                if (mBtnDnDrawable.isTriple()) {
                                    mSeekBarR.setProgress(mSeekBarR.getProgress()-1);
                                    mSeekBarG.setProgress(mSeekBarG.getProgress()-1);
                                    mSeekBarB.setProgress(mSeekBarB.getProgress()-1);
                                    mColorToEdit.setOverLoaded();
                                } else {
                                    if (null != mTargetSeekBar) {
                                        int idTarget = mTargetSeekBar.getId();
                                        mTargetSeekBar = (SeekBar) mView.findViewById(idTarget);
                                        int prg = mTargetSeekBar.getProgress();
                                        mTargetSeekBar.setProgress(prg - 1);
                                        mColorToEdit.setOverLoaded();
                                    }
                                }
                            }
                        })
        );


/*
        mBtnGet = (ImageButton) mView.findViewById(R.id.btnGet);
        mBtnGet.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestCurrentConfig();
                    }
                }
        );
*/





        mTxtElement = (TextView) mView.findViewById(R.id.txtElement);
        mTxtElement.setText(mActivity.mIndexedColor.getCurrentDescription());
/*
        if (null != mColors) {
            mTxtElement.setText(mElementName[mElementIndex]);
        } else {
            mTxtElement.setText("");
        }
*/
        //mTxtElement.setText(mElementName[4]);

        mImageButtonColor = (ImageButton) mView.findViewById(R.id.imageButtonColor);
//        drawable = resources.getDrawable(R.drawable.ic_save_dark);
//        mCurrentColorDrawable.setBitmapDark(((BitmapDrawable) drawable).getBitmap());
//        drawable1 = resources.getDrawable(R.drawable.ic_save_light);
//        mCurrentColorDrawable.setBitmapLight(((BitmapDrawable) drawable1).getBitmap());
        mImageButtonColor.setBackground(mCurrentColorDrawable);
        mImageButtonColor.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Log.i(TAG, "*** onClick(" + mColorToEdit.getLoaded() + "), mElementIndex=" +
//                                mActivity.mIndexedColor.getCurrentIndex() + ", text=" +
//                                mActivity.mIndexedColor.getCurrentDescription());
                        //mElementName.
                        //ACommon.colorDescription;
//                        int i;
//                        //mColorToEdit.getColor()
//                        for (String s: ACommon.colorDescription) {
//                            i = ACommon.colorDescription.indexOf(s);
//                            Log.i(TAG, "******* index=" + i + ", object=" + s);
//                        }
                        //
                        if (mColorToEdit.getLoaded() == true) {
                            sendIndexedColor(mActivity.mIndexedColor.getIndexToSend(),
                                    mActivity.mIndexedColor.getKeyToSend(), mColorToEdit.getColor());
                            mColorToEdit.clearOverLoaded();
                            v.invalidate();
                            //mImporterActivity.mIndexedColor.mColors[mImporterActivity.mIndexedColor.mElementIndex] = mColorToEdit.getColor();
                            mActivity.mIndexedColor.setCurrentColor(mColorToEdit.getColor());
                        }
                    }
                }
        );

        //
        mBtnNextColor = (ImageButton) mView.findViewById(R.id.btnNextElement);
        mBtnNextColor.setBackground(mBtnNextColorDrawable);
//        mBtnNextColor.setOnLongClickListener(
//                new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        final View finalView = v;
//                        finalView.animate().setDuration(1000).alpha(0)
//                                .withEndAction(
//                                        new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                mBtnNextColorDrawable.toggleDirection();
//                                                finalView.setAlpha(1);
//                                            }
//                                        }
//                                );
//                        return true;
//                    }
//                }
//        );
        mBtnNextColor.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Integer mColors[]    String mElementName[]
                        if (null != mActivity.mIndexedColor && mActivity.mIndexedColor.isColorsLoaded()) {
//                            if (true == mBtnNextColorDrawable.isForward()) {
////                                mImporterActivity.mElementIndex++;
////                                if (mImporterActivity.mElementIndex >= mImporterActivity.mColors.length) mImporterActivity.mElementIndex = 0;
//                                mActivity.mIndexedColor.nextElement();
//                            } else {
////                                mImporterActivity.mElementIndex--;
////                                if (mImporterActivity.mElementIndex < 0) mImporterActivity.mElementIndex = mImporterActivity.mColors.length - 1;
//                                mActivity.mIndexedColor.previousElement();
//                            }
                            mActivity.mIndexedColor.nextElement();
                            mTxtElement.setText(mActivity.mIndexedColor.getCurrentDescription());
//                            String s = "" + (mElementIndex+1) + " из " + mColors.length + " " + mElementName[mElementIndex];
//                            mTxtElement.setText(s);
                            mColorToEdit.setColor(mActivity.mIndexedColor.getCurrentColor());
                            mColorToEdit.clearOverLoaded();
                            //mImageButtonColor.invalidate();
                            mBtnWatchAppearance.invalidate();
                        } else {
                            requestCurrentConfig();
                        }
                        if (mBtnUpDrawable.isTriple()) mBtnUpDrawable.toggleTriple();
                        if (mBtnDnDrawable.isTriple()) mBtnDnDrawable.toggleTriple();
                    }
                }
        );
        //
        mBtnPrevColor = (ImageButton) mView.findViewById(R.id.btnPrevElement);
        mBtnPrevColor.setBackground(mBtnPrevColorDrawable);
        mBtnPrevColor.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Integer mColors[]    String mElementName[]
                        if (null != mActivity.mIndexedColor && mActivity.mIndexedColor.isColorsLoaded()) {
                            mActivity.mIndexedColor.previousElement();
                            mTxtElement.setText(mActivity.mIndexedColor.getCurrentDescription());
//                            String s = "" + (mElementIndex+1) + " из " + mColors.length + " " + mElementName[mElementIndex];
//                            mTxtElement.setText(s);
                            mColorToEdit.setColor(mActivity.mIndexedColor.getCurrentColor());
                            mColorToEdit.clearOverLoaded();
                            //mImageButtonColor.invalidate();
                            mBtnWatchAppearance.invalidate();
                        } else {
                            requestCurrentConfig();
                        }
                        if (mBtnUpDrawable.isTriple()) mBtnUpDrawable.toggleTriple();
                        if (mBtnDnDrawable.isTriple()) mBtnDnDrawable.toggleTriple();
                    }
                }
        );




        mHrzScrollView = (HrzScrollView) mView.findViewById(R.id.hrzScrollView);
        mHrzScrollView.setScrollbarFadingEnabled(false);
        //mHrzScrollView.setScrollBarStyle(S);

//        LinearLayout oldColorPalette = mColorPaletteLinearLayout;
        mColorPaletteLinearLayout = (LinearLayout) mView.findViewById(R.id.colorPalette); //colorPalette

        mColorPaletteViewList = new LinkedList<>();
        //
//        for (ColorImgButton color : mColorPaletteViewList) {
//            int index = 0;
//            ViewParent parent = color.getParent();
//            Log.i(TAG, "*** parent(" + index + ") = " + parent);
//            Log.i(TAG, "*** mColorPaletteLinearLayout = " + mColorPaletteLinearLayout);
//            Log.i(TAG, "*** oldColorPalette = " + oldColorPalette);
//            if (null != parent) {
//                ((LinearLayout) parent).removeView(color);
//                //ViewParent parent1 = parent.getParent();
//                //((HrzScrollView) parent1).removeView((View) parent);
//            }
//            mColorPaletteLinearLayout.addView(color);
//        }
        //
        //int height = mColorPaletteLinearLayout.getHeight();
        for (Integer color : mActivity.gColorPalette) {
            int index = 0;
            //Log.i(TAG, "((( activity ColorPalette[ " + index + " ] = " + color);
            ColorImgButton btnColor = new ColorImgButton(mActivity);
            //btnColor.setColor(color);
            mColorPaletteViewList.add(btnColor);
            mColorPaletteLinearLayout.addView(btnColor);
        }

        mBtnColorMemAdd = (ImageButton) mView.findViewById(R.id.btnColorMemAdd);
//        drawable = resources.getDrawable(R.drawable.ic_mplus);
//        DrawableButton mDrawableBtnColorMemAdd = new DrawableButton(((BitmapDrawable) drawable).getBitmap(), 10);
        mBtnColorMemAdd.setBackground(mDrawableBtnColorMemAdd);
        mBtnColorMemAdd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mColorToEdit.getLoaded() == true) {

                            int rgbColor = mColorToEdit.getColor();

                            ColorImgButton color = new ColorImgButton(mActivity);
                            //color.setColor(rgbColor);
                            //color.setOnClickListener(color.onClick(color));
//                            color.setOnClickListener(
//                                    new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//
//                                            int index = mColorPaletteViewList.indexOf(v);
//                                            Log.i(TAG, "((( ColorPalette onClick, index=" + index);
//
//                                            mColorToEdit.setColor(((ColorImgButton) v).getColor());
//                                            mColorToEdit.setOverLoaded();
//                                            mActivity.mVibrator.vibrate(20);
//                                        }
//                                    }
//                            );
//                            color.setOnLongClickListener(
//                                    new View.OnLongClickListener() {
//                                        @Override
//                                        public boolean onLongClick(View v) {
//
//                                            final int index = mColorPaletteViewList.indexOf(v);
//                                            Log.i(TAG, "((( ColorPalette onLongClick, index=" + index);
//
//                                            final View finalView = v;
//                                            finalView.animate().setDuration(1000).alpha(0)
//                                                    .withEndAction(
//                                                            new Runnable() {
//                                                                @Override
//                                                                public void run() {
//                                                                    mActivity.gColorPalette.remove(index);
//
//                                                                    mColorPaletteLinearLayout.removeView(finalView);
//                                                                    mColorPaletteViewList.remove(finalView);
//
//                                                                    finalView.setAlpha(1);
//                                                                }
//                                                            }
//                                                    );
////                                            mColorPaletteLinearLayout.removeView(v);
////                                            mColorPaletteViewList.remove(v);
//                                            return true;
//                                        }
//                                    }
//                            );

                            final View view = v;
                            v.animate().setDuration(250).alpha(0.5f)
                                    .withEndAction(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    view.setAlpha(1);
                                                }
                                            }
                                    );
                            mActivity.mVibrator.vibrate(20);

                            mActivity.gColorPalette.add(rgbColor);
                            mColorPaletteViewList.add(color);
                            mColorPaletteLinearLayout.addView(color);

                            mCommonHandler.sendEmptyMessageDelayed(ACommon.MSG_SCROLL_TO_END, 500);

//                            Log.i(TAG, "((( mColorPaletteViewList size=" + mColorPaletteViewList.size() +
//                                            ", activity ColorPalette size=" + mActivity.gColorPalette.size()
//                            );
                        }
                    }
                }
        );





        mBtnWatchAppearance = (ImageButton) mView.findViewById(R.id.imageButtonWatch);
        //imageButtonWatch.setBackgroundResource(R.drawable.preview_airforceru_circular);
        mBtnWatchAppearance.setBackground(mWatchFaceAppearance);
        mBtnWatchAppearance.invalidate();
        mBtnWatchAppearance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity.mIndexedColor.isInAmbientRange()) {
                    sendAmbientWakeup((long) mService.getSettingsWakeDelay());
                } else {
                    sendWakeup((long) mService.getSettingsWakeDelay());
                }
                //v.postInvalidate();
            }
        });
        mBtnWatchAppearance.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        final View finalView = v;
                        finalView.animate().setDuration(500).alpha(0.5f)
                                .withEndAction(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                requestCurrentConfig();
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






        ImageButton buttonDate = (ImageButton) mView.findViewById(R.id.btnDate);
        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChangeLayoutDate();

                final View view = v;
                v.animate().setDuration(500).alpha(0.5f)
                        .withEndAction(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        view.setAlpha(1);
                                    }
                                }
                        );
                mActivity.mVibrator.vibrate(50);

                DrawableSegmentButton backgr = (DrawableSegmentButton) v.getBackground();
                backgr.toggleSegment();

            }
        });
//        drawable = resources.getDrawable(R.drawable.ic_dom);
//        DrawableButton mDrawableButtonDate = new DrawableButton(((BitmapDrawable) drawable).getBitmap(), 10);
        //mIconBtnDate = ((BitmapDrawable) drawable).getBitmap();
        buttonDate.setBackground(mDrawableButtonDate);

        ImageButton buttonMainHands = (ImageButton) mView.findViewById(R.id.btnMainHands);
//        drawable = resources.getDrawable(R.drawable.ic_hands);
//        DrawableButton mDrawableButtonMainHands = new DrawableButton(((BitmapDrawable) drawable).getBitmap(), 10);
        buttonMainHands.setBackground(mDrawableButtonMainHands);
        buttonMainHands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChangeLayoutMainHands();

                final View view = v;
                v.animate().setDuration(500).alpha(0.5f)
                        .withEndAction(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        view.setAlpha(1);
                                    }
                                }
                        );
                mActivity.mVibrator.vibrate(50);

                DrawableSegmentButton backgr = (DrawableSegmentButton) v.getBackground();
                backgr.toggleSegment();
            }
        });

        ImageButton buttonLayout = (ImageButton) mView.findViewById(R.id.btnLayout);
//        drawable = resources.getDrawable(R.drawable.ic_layout);
//        DrawableButton mDrawableButtonLayout = new DrawableButton(((BitmapDrawable) drawable).getBitmap(), 10);
        buttonLayout.setBackground(mDrawableButtonLayout);
        buttonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mImporterActivity.mTabCollection.
                sendChangeLayout();

                final View view = v;
                v.animate().setDuration(500).alpha(0.5f)
                        .withEndAction(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        view.setAlpha(1);
                                    }
                                }
                        );
                mActivity.mVibrator.vibrate(50);

                DrawableSegmentButton backgr = (DrawableSegmentButton) v.getBackground();
                backgr.toggleSegment();
            }
        });


        ImageButton buttonResetToDefaults = (ImageButton) mView.findViewById(R.id.buttonResetToDefaults);
//        drawable = resources.getDrawable(R.drawable.ic_reset);
//        DrawableButton mDrawableButtonResetToDefaults = new DrawableButton(((BitmapDrawable) drawable).getBitmap(), 10);
        buttonResetToDefaults.setBackground(mDrawableButtonResetToDefaults);
        buttonResetToDefaults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View view = v;
                v.animate().setDuration(500).alpha(0.5f)
                        .withEndAction(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        view.setAlpha(1);
                                    }
                                }
                        );
                mActivity.mVibrator.vibrate(50);

                android.app.FragmentManager fm = mActivity.getFragmentManager();
                if (fm.findFragmentByTag("dialog") == null) {
                    ConfirmResetToDefaultsDialogFragment fragment = new ConfirmResetToDefaultsDialogFragment();
                    //ConfirmResetToDefaultsDialogFragment fragment = ConfirmResetToDefaultsDialogFragment.instantiate(this, "dialog");
                    fragment.show(fm, "dialog");
                }
            }
        });

        mBtnSaveToConfigPalette = (ImageButton) mView.findViewById(R.id.btnSaveToConfigPalette);
//        drawable = resources.getDrawable(R.drawable.ic_collect);
//        DrawableButton mDrawableBtnSaveToConfigPalette = new DrawableButton(((BitmapDrawable) drawable).getBitmap(), 10);
        mBtnSaveToConfigPalette.setBackground(mDrawableBtnSaveToConfigPalette);
        mBtnSaveToConfigPalette.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

//                        Log.i(TAG, "### ADD TO CONFIG PALETTE BUTTON PRESSED (" + mActivity.isInGetConfigTransaction() +
//                                "), mConfigForFile=" + mActivity.mConfigForFile);
                        mActivity.beginGetConfigTransaction();
                        requestCurrentConfigForFile();

                        final View view = v;
                        v.animate().setDuration(500).alpha(0.5f)
                                .withEndAction(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                view.setAlpha(1);
                                            }
                                        }
                                );
                        mActivity.mVibrator.vibrate(50);

                    }
                }
        );

        mBtnShareSingle = (ImageButton) mView.findViewById(R.id.btnShareSingle);
        mBtnShareSingle.setBackground(mDrawableShareSingle);
        mBtnShareSingle.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        shareSingleConfig();

                        final View view = v;
                        v.animate().setDuration(500).alpha(0.5f)
                                .withEndAction(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                view.setAlpha(1);
                                            }
                                        }
                                );
                        mActivity.mVibrator.vibrate(50);
                    }
                }
        );


//        ImageButton buttonAux = (ImageButton) mView.findViewById(R.id.btnAux);
//        //buttonAux.setBackground(mDrawableButtonLayout);
//        buttonAux.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // put code here
//                String fname = getString(R.string.configFileName) + "." + getString(R.string.product_id);
//                sendConfigFileByMail(mActivity, fname);
//
//                final View view = v;
//                v.animate().setDuration(500).alpha(0.5f).withEndAction(
//                        new Runnable() {
//                            @Override
//                            public void run() {
//                                view.setAlpha(1);
//                            }
//                        }
//                );
//                mActivity.mVibrator.vibrate(50);
//            }
//        });


        ImageButton buttonSort = (ImageButton) mView.findViewById(R.id.btnSort);
        buttonSort.setBackground(mDrawableBtnSortColorPalette);
        buttonSort.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mActivity.gColorPalette.size() == 0) return;

                        ACommon.bubbleSort(mActivity.gColorPalette);

                        //mColorPaletteLinearLayout.invalidate();
//                        for (View btnColor : mColorPaletteLinearLayout) {
//
//                        }
                        for (ColorImgButton btnColor : mColorPaletteViewList) {
                            btnColor.clearDeletionTimer();
                            btnColor.postInvalidate();
                        }

                        final View view = v;
                        v.animate().setDuration(300).alpha(0.5f).withEndAction(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        view.setAlpha(1);
                                    }
                                }
                        );
                        mActivity.mVibrator.vibrate(50);


                    }
                }
        );



        ImageButton buttonRateUs = (ImageButton) mView.findViewById(R.id.btnRateUs);
        buttonRateUs.setBackground(mDrawableBtnRateUs);
        buttonRateUs.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.0.245")));
                        openAppRating(getActivity().getApplicationContext());
                    }
                }
        );
        //Log.i(TAG, "((( Button, w=" + buttonRateUs.getWidth() + ", h=" + buttonRateUs.getHeight() );

        //restoreCurrentConfigRepresentation();


        return view;
    } // onCreateView



    public static void openAppRating(Context context) {
        // here: http://stackoverflow.com/questions/11753000/how-to-open-the-google-play-store-directly-from-my-android-application
        //
        // if you want to redirect to all Developer's apps use
        // market://search?q=pub:"+devName and http://play.google.com/store/search?q=pub:"+devName

        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName()));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager().queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp: otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                rateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+context.getPackageName()));
            context.startActivity(webIntent);
        }
    }




//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        //super.onActivityResult(requestCode, resultCode, data);
//        Log.i(TAG, "((( onActivityResult");
//        if (requestCode == ACommon.PICK_SENDER_REQUEST) {
//            if (resultCode == Activity.RESULT_CANCELED) {
//
//            }
//        }
//    }

//    private void sendConfigFileByMail(Context context, String fname) {
//        // also see here: http://richardleggett.co.uk/blog/2013/01/26/registering_for_file_types_in_android/
//        Intent emailIntent = new Intent(Intent.ACTION_SEND);
//        emailIntent.setType("message/rfc822");
//        File newFile = new File(context.getFilesDir(), fname);
//        Uri contentUri = FileProvider.getUriForFile(context, "com.luna_78.airforceru.fileprovider", newFile);
//        Log.i(TAG, "(((( contentUri = " + contentUri.toString());
//        grantUriPermissionToMailPackages(context, contentUri, emailIntent);
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Android wear watch face " + mActivity.productId + " layouts palette");
//        emailIntent.putExtra(Intent.EXTRA_STREAM, /*attachmentFileName*/ /*Uri.fromFile(file)*/ contentUri);
//        //emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivity(Intent.createChooser(emailIntent, "Send email..."));
////        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        startActivityForResult(Intent.createChooser(emailIntent, "Send email..."), ACommon.PICK_SENDER_REQUEST);
//    }



//    private void sendConfigFileByMail(Context context, String fname) {
//        // also see here: http://richardleggett.co.uk/blog/2013/01/26/registering_for_file_types_in_android/
//        mActivity.requestSaveLayoutPalette(true);
//        //
//        tFileName = fname;
//        tContext = context;
//        mStartChooser = true;
//        //mCommonHandler.postDelayed(taskSendConfigFileByMail, 500);
//    }
//    private volatile String tFileName = null;
//    private volatile Context tContext = null;
//    private Runnable taskSendConfigFileByMail = new Runnable() {
//        @Override
//        public void run() {
//            Intent emailIntent = new Intent(Intent.ACTION_SEND);
//            emailIntent.setType("message/rfc822");
//            File newFile = new File(tContext.getFilesDir(), tFileName);
//            Uri contentUri = FileProvider.getUriForFile(tContext, "com.luna_78.wear.watch.face.raf3078.fileprovider", newFile);
//            Log.i(TAG, "(((( contentUri = " + contentUri.toString());
//            grantUriPermissionToMailPackages(tContext, contentUri, emailIntent);
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch face ✮" + mActivity.productId + "✮ collection (" +
//                    mService.gLayoutsPalette.size() + " elements).");
//            emailIntent.putExtra(Intent.EXTRA_STREAM, /*attachmentFileName*/ /*Uri.fromFile(file)*/ contentUri);
//            //emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(Intent.createChooser(emailIntent, "Send email..."));
//            //
//            tFileName = null;
//            tContext = null;
//            mStartChooser = false;
////        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        startActivityForResult(Intent.createChooser(emailIntent, "Send email..."), ACommon.PICK_SENDER_REQUEST);
//        }
//    };
//    private void grantUriPermissionToMailPackages(Context context, Uri contentUri, Intent emailIntent) {
//        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(emailIntent, 0);
//        if (!resInfo.isEmpty()) {
//            for (ResolveInfo resolveInfo : resInfo) {
//                String packageName = resolveInfo.activityInfo.packageName;
//                Log.i(TAG, "((((( packageName = " + packageName);
//                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            }
//        }
//    }





    private void sendIndexedColor(int index, String key, int color) {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_CHANGE_INDEXED_COLOR);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putInt(ACommon.KEY_COLOR, color);
        dataMap.putInt(ACommon.KEY_COLOR_INDEX, index); // нужен!!! используется в switch в onDataChanged в WearListenerService
        dataMap.putString(ACommon.KEY_COLOR_KEY, key);
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendWakeup(long delay) {
        DataMap dataMap = new DataMap();
        //dataMap.putLong("delay", 10000);
        //new SendThroughWearNetworkThread(ACommon.WEAR_WAKEUP_PATH, dataMap).start();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WAKEUP);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putLong(ACommon.KEY_DELAY, delay * 1000);
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, null).start(); //mActivity.mPeerId
    }
    private void sendAmbientWakeup(long delay) {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_WAKEUP_AMBIENT_ELEMENT);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putLong(ACommon.KEY_DELAY, delay * 1000);
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void requestCurrentConfig() {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_REQUEST_CURRENT_CONFIG);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient).start();
        //new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void requestCurrentConfigForFile() {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_REQUEST_CURRENT_CONFIG_FOR_FILE);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendChangeLayoutDate() {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_CHLAYOUT_DATE);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putInt(ACommon.KEY_VALUE, 0);
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendChangeLayoutMainHands() {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_CHLAYOUT_MAINHANDS);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putInt(ACommon.KEY_VALUE, 0);
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private void sendChangeLayout() {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_CHLAYOUT);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putInt(ACommon.KEY_VALUE, 0);
        //
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, mGoogleApiClient, mActivity.mPeerId).start();
    }
    private static void sendReset(HandheldCompanionConfigActivity activity) {
        DataMap dataMap = new DataMap();
        dataMap.putInt(ACommon.KEY_EVENT, ACommon.EVT_RESET);
        dataMap.putLong(ACommon.KEY_TIME, new Date().getTime());
        dataMap.putInt(ACommon.KEY_VALUE, 0);
        //new SendThroughWearNetworkThread(ACommon.PHONE_BATTERY_PATH, dataMap, mGoogleApiClient).start();
        //new SendThroughWearNetworkThread(ACommon.FROM_HANDHELD_PATH, dataMap, activity.getGoogleApiClient()).start();
        new ACommon.WearNetSend(ACommon.FROM_HANDHELD_PATH, dataMap, activity.getGoogleApiClient(), activity.mPeerId).start();
    }







    private void showToast(String toastText) {
        Toast.makeText(mService.getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
    }

    private void shareSingleConfig() {
        // see: APhoneService.taskPeriodicDeleteOldSharedFiles for additional functionality
        Bitmap bmpDense;
        Bitmap bmpAmbient;
        Bundle config;
        String singleLayoutXmlFileName;
        String framedScreenshotFileName;
        Bitmap framedScreenshot;
        FileOutputStream fos;
        Context context;
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String productId = getString(R.string.product_id);

        if (mService == null) {
            return;
        }
        bmpDense = mService.getLastDenseScreenshot();
        if (null == bmpDense) {
            showToast(getString(R.string.toast_missing_dense));
            return;
        }
        bmpAmbient = mService.getLastAmbientScreenshot();
        if (null == bmpAmbient) {
            showToast(getString(R.string.toast_missing_ambient));
            return;
        }
        config = mService.getCurrentConfig();
        if (null == config) {
            showToast(getString(R.string.toast_missing_config));
            return;
        }

        context = mService.getApplicationContext();

        Layout layout = new Layout();
        layout.config = config;
        layout.iconDense = bmpDense;
        layout.iconAmbient = bmpAmbient;
        layout.name = "SHARED LAYOUT";

        //adb -s 0a3d818c pull /mnt/shell/emulated/0/Pictures/test.xml
        //adb -s 0a3d818c pull /sdcard/Pictures/config.xml.RAF3078
        //adb -s 0a3d818c shell rm /sdcard/Pictures/config.xml.RAF3078
        //String fname = fileName;
        //singleLayoutXmlFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/layout.hex." + productId;
        //if (!fileName.endsWith(productId)) fname = fileName + "." + productId;
//        boolean resultXml = serializerXml.bundleToXmlFile(config, fname);

        //^layout_[0-9]+.hex.%s
        singleLayoutXmlFileName = context.getFilesDir() + "/layout_" + timeStamp + ".hex." + productId;
        SerializerXML serializerXml = new SerializerXML(mService.getApplicationContext(), null);
        boolean resultXml = serializerXml.layoutToXmlFile(layout, singleLayoutXmlFileName);


        //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        File fileOut = new File(fname);
//        MediaScannerConnection.scanFile(mService.getApplicationContext(), new String[]{fileOut.getAbsolutePath()}, null, null);
        //MediaScannerConnection.scanFile(mService.getApplicationContext(), new String[]{singleLayoutXmlFileName}, null, null);

        //ACommon.bmpToPicturesDir(mService.getApplicationContext(), framedScreenshot, "adv_", "_1", timeStamp);
                //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/adv_" + timeStamp + "_1.png";
//        resultBitmap = ACommon.produceFramedIcon(mActivity.getApplicationContext(), bmpAmbient);
//        ACommon.bmpToPicturesDir(mActivity.getApplicationContext(), resultBitmap, "adv_", "_2", timeStamp);

        framedScreenshot = ACommon.produceFramedIcon(context, bmpDense);
        //^shared_[0-9]+.png
        framedScreenshotFileName = context.getFilesDir() + "/shared_" + timeStamp + ".png";
        File fileOut = new File(framedScreenshotFileName);
        try {
            fos = new FileOutputStream(fileOut);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(framedScreenshot.getByteCount());
            framedScreenshot.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            fos.write(byteArray);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //BuildConfig.FLAVOR



        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("message/rfc822"); //multipart/mixed
//        File newFile = new File(mService.getApplicationContext().getFilesDir(), tFileName);
//        Uri contentUri = FileProvider.getUriForFile(tContext, "com.luna_78.wear.watch.face.raf3078.fileprovider", newFile);
//        Log.i(TAG, "(((( contentUri = " + contentUri.toString());
//        ACommon.grantUriPermissionToMailPackages(tContext, contentUri, emailIntent);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch face ✮" + productId + "✮ layout (1 element).");
//        emailIntent.putExtra(Intent.EXTRA_STREAM, /*attachmentFileName*/ /*Uri.fromFile(file)*/ contentUri);

        Uri contentUriXml, contentUriBmp;
        File fileXml, fileBmp;
        ArrayList<Uri> contentUris = new ArrayList<Uri>();
        //convert from paths to Android friendly Parcelable Uri's
        //File fileIn = new File(file);
        fileXml = new File(singleLayoutXmlFileName);
        //contentUriXml = Uri.fromFile(fileXml);
        //contentUriXml = FileProvider.getUriForFile(context, "com.luna_78.wear.watch.face.raf3078.fileprovider", fileXml);
        contentUriXml = FileProvider.getUriForFile(context, context.getString(R.string.fp_authority_0), fileXml);
        //Log.i(TAG, "#MAIL xml=" + singleLayoutXmlFileName + ", file=" + fileXml + ", uri=" + contentUriXml);
        ACommon.grantUriPermissionToMailPackages(context, contentUriXml, emailIntent);
        contentUris.add(contentUriXml);
        fileBmp = new File(framedScreenshotFileName);
        //contentUriBmp = Uri.fromFile(fileBmp);
        //contentUriBmp = FileProvider.getUriForFile(context, "com.luna_78.wear.watch.face.raf3078.fileprovider", fileBmp);
        contentUriBmp = FileProvider.getUriForFile(context, context.getString(R.string.fp_authority_0), fileBmp);
        //Log.i(TAG, "#MAIL bmp=" + framedScreenshotFileName + ", file=" + fileBmp + ", uri=" + contentUriBmp);
        ACommon.grantUriPermissionToMailPackages(context, contentUriBmp, emailIntent);
        contentUris.add(contentUriBmp);
        //
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, contentUris);

        //emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(emailIntent, "Send email..."));

        //Log.i(TAG, "#MAIL after start activity");
    }




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


    static public class ConfirmResetToDefaultsDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //return super.onCreateDialog(savedInstanceState);
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.confirm_reset_to_defaults_title)
                    .setMessage(R.string.confirm_reset_to_defaults_message)
                    .setPositiveButton(R.string.string_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // do the job here
                            CharSequence text = "Reset confirmed";
                            //Toast toast = Toast.makeText(mImporterActivity.getApplicationContext(), text, Toast.LENGTH_SHORT);
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT);
                            toast.show();
                            sendReset((HandheldCompanionConfigActivity) getActivity());
                        }
                    })
                    .setNegativeButton(R.string.string_cancel, null)
                    .create();
        }

        public ConfirmResetToDefaultsDialogFragment() {
            //super();
        }
    } // class ConfirmResetToDefaultsDialogFragment


    private final Handler mCommonHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case ACommon.MSG_SCROLL_TO_END:
                    //Log.i(TAG, "((( DELETION TIMEOUT");
                    mHrzScrollView.fullScroll(View.FOCUS_RIGHT);
                    break;
            }
        }
    };



} // class PageFragmentLayout
