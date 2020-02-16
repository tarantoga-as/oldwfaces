package com.luna_78.wear.watch.face.raf3078;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.luna_78.wear.watch.face.raf3078.common.ACommon;
import com.luna_78.wear.watch.face.raf3078.common.Inscription;

/**
 * Created by buba on 19/05/15.
 */
public class InscriptionAppearance extends Drawable {

    private static final String TAG = "IAP";

    private Bitmap plateBitmapOriginal, plateBitmap;

    float dim = 0;

    boolean     wfvIsWearRTL;
    int         wfvScreenWidth, wfvScreenHeight;
    float       wfvBurninMargin, wfvScreenCenterX, wfvScreenCenterY, wfvScreenRadius, wfvDialRadius;
    boolean     wfvIsLoaded = false;
    //
    float       wfvAuxAcx, wfvAuxAcy, wfvAuxAdim, wfvAuxBcx, wfvAuxBcy, wfvAuxBdim, wfvAuxCcx, wfvAuxCcy, wfvAuxCdim;
    boolean     wfvAuxLoaded = false;

    int dialDigitsColor = Color.WHITE;
    public void setDigitsColor(int color) { dialDigitsColor = color; }

    int[] colorsCW = new int[] {Color.GREEN, Color.GREEN, Color.WHITE, Color.WHITE};
    float[] positionsCW = new float[] {0f, 0.20f, 0.20f, 1.0f};
    int[] colorsCCW = new int[] {Color.WHITE, Color.WHITE, Color.GREEN, Color.GREEN};
    float[] positionsCCW = new float[] {0f, 0.80f, 0.80f, 1.0f};

    int legendCircleColor;
    public void setLegendCircleColor(int color) {
        //Log.i(TAG, "((( setLegendCircleColor, color=" + String.format("%X", color));
        legendCircleColor = color;
        colorsCW[2] = legendCircleColor;
        colorsCW[3] = legendCircleColor;
        colorsCCW[0] = legendCircleColor;
        colorsCCW[1] = legendCircleColor;
    }

    public String iText;
    public void setiText(String txt) { iText = txt; }
    public float       iTextSize;
    public void setiTextSize(float f) { iTextSize = f; }
    public float       iTextScaleX;
    public void setiTextScaleX(float f) { iTextScaleX = f; }
    public int         iTextColor;
    public void setiTextColor(long c) { iTextColor = (int) c; }
    public float       iRadius; // процент удаления точки начала надписи от центра к краю (циферблата) по радиусу
    public void setiRadius(float f) { iRadius = f; }
    public float       iAngle; // градус угола поворота радиуса; 0 = 12 часов
    public void setiAngle(float f) { iAngle = f; }
    public float       iIncline; // наклон базовой линии; 0 = 3 часа
    public void setiIncline(float f) { iIncline = f; }
    // в каком режиме создан - burnIn или полный циферблат
    public boolean     iIsBurnIn;
    public void setiIsBurnIn(long l) { iIsBurnIn = (Inscription.DEFAULT_YES==l)?true:false; }
    // индекс вида циферблата, для которого (вида) определен набор надписей
//    public int         watchLayoutIndex;
    // sans-serif, sans-serif-light, sans-serif-condensed, sans-serif-thin, sans-serif-medium, monospaced
    public String iFontFamily;
    public void setiFontFamily(String s) { iFontFamily = s; }
    // Typeface.create(String familyName, int style)
    // 0:NORMAL, 1:BOLD, 2:ITALIC, 3:BOLD_ITALIC
    public int         iFontStyle;
    public void setiFontStyle(String style) {
        iFontStyle = Inscription.getStyleValue(style);
    }
    // 0:STRAIGHT, 1:ROUND_MC, 2:ROUND_AC, 3:ROUND_BC, 4:ROUND_CC
    public int         iBend;
    public void setiBend(long bend) { iBend = (int) bend; }
    // 0:CW, 1:CCW
    public long        iDirection;
    public void setiDirection(long l) { iDirection = l; }

    // inscription appearance: 0:NONE 1:DARK_SHADOW 2:LIGHT_SHADOW 3:EMBOSS 4:DEBOSS
    public int         iFx;
    public void setiFx(long fx) { iFx = (int) fx; }


    boolean showABC = true;
    public void setShowABC(boolean b) { showABC = b; }

    boolean showLegend = true;
    public void setShowLegend(boolean b) { showLegend = b; }


    Context mContext;


    public InscriptionAppearance() {
        super();
        initPaint(true);
    } // InscriptionAppearance()

    public void setContext(Context context) {
        mContext = context;
    }

    private Paint paintText, paintLegend;
    private void initPaint(boolean create) {
        if (create) {
            paintText = new Paint();
            paintText.setAntiAlias(true);
            paintText.setDither(true);
            paintText.setFilterBitmap(true);
            //
            paintLegend = new Paint();
            paintLegend.setAntiAlias(true);
            paintLegend.setDither(true);
            paintLegend.setFilterBitmap(true);
            paintLegend.setColor(0x99999999);
            paintLegend.setStrokeWidth(2f);
            paintLegend.setStyle(Paint.Style.STROKE);
        }
        paintText.setColor(0x99999999);
        paintText.setStrokeWidth(2f);
        paintText.setStyle(Paint.Style.STROKE);
    } // initPaint

    public void setDialPlateBitmap(Bitmap bmp) {
        plateBitmapOriginal = bmp;
        if (null == plateBitmapOriginal) {
            wfvAuxLoaded = false;
            wfvIsLoaded = false;
        } else {
            if (0 != dim) plateBitmap = Bitmap.createScaledBitmap(plateBitmapOriginal, (int) dim, (int) dim, true);
        }
    }

    public void setWatchFaceValues(boolean rtl, int w, int h, float bimargin, float cx, float cy, float sr, float dr) {
        wfvIsWearRTL = rtl;
        wfvScreenWidth = w;
        wfvScreenHeight = h;
        wfvBurninMargin = bimargin;
        wfvScreenCenterX = cx;
        wfvScreenCenterY =cy;
        wfvScreenRadius = sr;
        wfvDialRadius = dr;
        //
        wfvIsLoaded = true;
    }

    public void setWatchFaceAux(float acx, float acy, float adim, float bcx, float bcy, float bdim, float ccx, float ccy, float cdim) {
        wfvAuxAcx = acx;
        wfvAuxAcy = acy;
        wfvAuxAdim = adim;
        wfvAuxBcx = bcx;
        wfvAuxBcy = bcy;
        wfvAuxBdim = bdim;
        wfvAuxCcx = ccx;
        wfvAuxCcy = ccy;
        wfvAuxCdim = cdim;
        //
        wfvAuxLoaded = true;
    }

    private void drawHint(Canvas canvas, Paint paint, float centerX, float centerY, float radiusMax) {
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(centerX, centerY, radiusMax, paint);
        //
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(24f);
        paint.setAlpha(255);
        ACommon.drawHvAlignedText(canvas, centerX, centerY - 24f, mContext.getResources().getString(R.string.string_click),
                paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
        ACommon.drawHvAlignedText(canvas, centerX, centerY, mContext.getResources().getString(R.string.string_and),
                paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
        ACommon.drawHvAlignedText(canvas, centerX, centerY + 24f, mContext.getResources().getString(R.string.string_long_click),
                paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
    }

    @Override
    public void draw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        dim = (float) Math.min(width, height);
        float centerX = width / 2f;
        float centerY = height / 2f;

        //Log.i(TAG, "((( draw, width=" + width + ", height=" + height + ", dim=" + dim);

        initPaint(false);

        canvas.drawColor(Color.BLACK);
        
        if (null == plateBitmapOriginal) {
            float radiusMax = Math.min(centerX, centerY) - 5f;
//            paintLegend.setAlpha(255);
//            paintLegend.setColor(Color.WHITE);
//            paintLegend.setStrokeWidth(1f);
//            canvas.drawCircle(centerX, centerY, radiusMax, paintLegend);
            drawHint(canvas, paintLegend, centerX, centerY, radiusMax);
            return;
        }

        paintLegend.setAlpha(180);

        if (null == plateBitmap) plateBitmap = Bitmap.createScaledBitmap(plateBitmapOriginal, (int) dim, (int) dim, true);

        float scale, offX, offY;
        scale = dim / (float) Math.min(wfvScreenWidth, wfvScreenHeight);
        //wfvDialRadius
        //scale = dim / (wfvDialRadius * 2f);
        offX = centerX - dim / 2f;
        offY = centerY - dim / 2f;
        //Log.i(TAG, "((( draw, scale=" + scale + ", offX=" + offX + ", offY=" + offY);



        paintLegend.setAlpha(255); //255
        paintLegend.setColor(dialDigitsColor);
        paintLegend.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, (dim / 2f) - ((wfvBurninMargin + 1) * scale), paintLegend);
        //
        canvas.drawBitmap(plateBitmap, offX, offY, paintLegend);



        if (showABC) drawABC(canvas, paintLegend, scale, offX, offY);


        paintLegend.setAlpha(255);
        //paintLegend.setColor(Color.WHITE);
        //Log.i(TAG, "((( setLegendCircleColor, legendCircleColor=" + String.format("%X", legendCircleColor));
        paintLegend.setColor(legendCircleColor);
        paintLegend.setStyle(Paint.Style.STROKE);
        paintLegend.setStrokeWidth(1.5f);
        //
        //float scale = centerX /*dim / 2f*/ / wfvScreenCenterX;
        float pathRadiusCenterX, pathRadiusCenterY, pathRadius100, pathRadius;
        pathRadius100 = scale * wfvDialRadius;
        pathRadiusCenterX = scale * wfvScreenCenterX;
        pathRadiusCenterY = scale * wfvScreenCenterY;
        switch (iBend) {
            case Inscription.BEND_STRAIGHT:
            case Inscription.BEND_ROUND_MC:
                pathRadius100 = scale * wfvDialRadius;
                pathRadiusCenterX = scale * wfvScreenCenterX;
                pathRadiusCenterY = scale * wfvScreenCenterY;
                break;
            case Inscription.BEND_ROUND_AC:
                pathRadius100 = scale * wfvAuxAdim;
                pathRadiusCenterX = scale * wfvAuxAcx;
                pathRadiusCenterY = scale * wfvAuxAcy;
                break;
            case Inscription.BEND_ROUND_BC:
                pathRadius100 = scale * wfvAuxBdim;
                pathRadiusCenterX = scale * wfvAuxBcx;
                pathRadiusCenterY = scale * wfvAuxBcy;
                break;
            case Inscription.BEND_ROUND_CC:
                pathRadius100 = scale * wfvAuxCdim;
                pathRadiusCenterX = scale * wfvAuxCcx;
                pathRadiusCenterY = scale * wfvAuxCcy;
                break;
        }
        pathRadius = pathRadius100 * iRadius / 100f;
        if (showLegend) canvas.drawCircle(offX + pathRadiusCenterX, offY + pathRadiusCenterY, pathRadius, paintLegend);
        //
//        float innerX = (float) Math.sin(tickRotRad) * (innerTickRadius - mVars.pixelEquivalent(6f));
//        float innerY = (float) -Math.cos(tickRotRad) * (innerTickRadius - mVars.pixelEquivalent(6f));
        float pathOffsetX, pathOffsetY;
        float left, top, right, bottom;
        Path path = new Path();
        path.reset();
        Matrix matrix = new Matrix();
        paintLegend.setColor(Color.GREEN);
        switch (iBend) {
            case Inscription.BEND_STRAIGHT:
                pathOffsetX = pathRadiusCenterX + (float) Math.sin(iAngle * Math.PI / 180f) * pathRadius;
                pathOffsetY = pathRadiusCenterY + (float) -Math.cos(iAngle * Math.PI / 180f) * pathRadius;
                path.moveTo(offX + pathOffsetX, offY + pathOffsetY);
                path.rLineTo(width, 0f);
                //path.lineTo(pathOffsetX, pathOffsetY);
                //path.lineTo(pathOffsetX + width, pathOffsetY);
                //path.setLastPoint(pathOffsetX + width, pathOffsetY);
//                canvas.drawPath(path, paintText);
                //matrix.setTranslate(pathOffsetX, pathOffsetY);
                matrix.reset();
                matrix.postRotate(iIncline, offX + pathOffsetX, offY + pathOffsetY);
                path.transform(matrix);
                break;
            case Inscription.BEND_ROUND_MC:
            case Inscription.BEND_ROUND_AC:
            case Inscription.BEND_ROUND_BC:
            case Inscription.BEND_ROUND_CC:
                //addArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle)
                left = pathRadiusCenterX - pathRadius;
                top = pathRadiusCenterY - pathRadius;
                right = pathRadiusCenterX + pathRadius;
                bottom = pathRadiusCenterY + pathRadius;
                RectF oval = new RectF(offX + left, offY + top, offX + right, offY + bottom);
                //path.addArc(left, top, right, bottom, iAngle, 120f);
                //path.addArc(oval, iAngle, 120f);
                path.addOval(oval, (iDirection == Inscription.DIRECTION_CCW) ? Path.Direction.CCW : Path.Direction.CW);

                matrix.reset();
                matrix.postRotate(iAngle, offX + pathRadiusCenterX, offY + pathRadiusCenterY);
                path.transform(matrix);

                Shader shaderCW = new SweepGradient(offX + pathRadiusCenterX, offY + pathRadiusCenterY, colorsCW, positionsCW);
                Shader shaderCCW = new SweepGradient(offX + pathRadiusCenterX, offY + pathRadiusCenterY, colorsCCW, positionsCCW);
                Shader shader = ((iDirection==Inscription.DIRECTION_CCW)? shaderCCW: shaderCW);
                matrix.reset();
                shader.getLocalMatrix(matrix);
                matrix.postRotate(iAngle, offX + pathRadiusCenterX, offY + pathRadiusCenterY);
                shader.setLocalMatrix(matrix);
                paintLegend.setShader(shader);

                break;
        }
        if (showLegend) canvas.drawPath(path, paintLegend);
        paintLegend.setShader(null);


        paintText.setColor(iTextColor);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setTextAlign(Paint.Align.LEFT);
        paintText.setTypeface(Typeface.create(iFontFamily, iFontStyle));
        paintText.setTextSize(scale * ACommon.pixelEquivalent(iTextSize, wfvScreenRadius));
        paintText.setTextScaleX(iTextScaleX);

        if (null != iText) canvas.drawTextOnPath(iText, path, 0f, 0f, paintText);

    } // draw

    private void drawABC(Canvas canvas, Paint paint, float scale, float offX, float offY) {
        if (wfvAuxLoaded) {
            paint.setTextSize(40f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            ACommon.drawHvAlignedText(canvas, offX + scale * wfvAuxAcx, offY + scale * wfvAuxAcy, "A", paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            paint.setColor(Color.BLACK);
            ACommon.drawHvAlignedText(canvas, offX + scale * wfvAuxAcx, offY + scale * wfvAuxAcy, "A", paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);

            paint.setTextSize(40f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            ACommon.drawHvAlignedText(canvas, offX + scale * wfvAuxBcx, offY + scale * wfvAuxBcy, "B", paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            paint.setColor(Color.BLACK);
            ACommon.drawHvAlignedText(canvas, offX + scale * wfvAuxBcx, offY + scale * wfvAuxBcy, "B", paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);

            paint.setTextSize(40f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            ACommon.drawHvAlignedText(canvas, offX + scale * wfvAuxCcx, offY + scale * wfvAuxCcy, "C", paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            paint.setColor(Color.BLACK);
            ACommon.drawHvAlignedText(canvas, offX + scale * wfvAuxCcx, offY + scale * wfvAuxCcy, "C", paint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);

            // draw cross in center of plate
            float centerX = canvas.getWidth() / 2f;
            float centerY = canvas.getHeight() / 2f;
            paint.setColor(legendCircleColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            canvas.drawLine(centerX - 20f, centerY, centerX + 20f, centerY, paint);
            canvas.drawLine(centerX, centerY-20f, centerX, centerY+20f, paint);

        }
    } // drawABC

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

} // class InscriptionAppearance
