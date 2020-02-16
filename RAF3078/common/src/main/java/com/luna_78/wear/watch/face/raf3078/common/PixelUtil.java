package com.luna_78.wear.watch.face.raf3078.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.renderscript.ScriptIntrinsicConvolve5x5;
import android.util.Log;

/**
 * Created by buba on 25/07/15.
 */
public class PixelUtil {

/*

    private void changeBackgroundColor(WatchAppearance appearance) {

        int color = appearance.mMainBackgroundColor;

        createBackgroundGradient(appearance);

        Log.i(TAG, "((((( changeBackgroundColor, color=" + color);

        for (int i=0; i<ACommon.NUM_BACKGROUNDS; i++) {
            if (watchBackgroundsBmp[i].mBackgroundColorized == null) continue;
            int bmpwidth = watchBackgroundsBmp[i].mBackgroundColorized.getWidth();
            int bmpheight = watchBackgroundsBmp[i].mBackgroundColorized.getHeight();
            int[] watchBmpRow = new int[bmpwidth];
            int[] gradientRow = new int[bmpwidth];
            int[] fxRow = new int[bmpwidth];
            int[] opaqueRow = new int[bmpwidth];
            int pixR = Color.red(color);
            int pixG = Color.green(color);
            int pixB = Color.blue(color);
//                Log.i(TAG, "§§§§§ R=" + pixR + ", G=" + pixG + ", B=" + pixB);
            float coeff;
            float r, g, b;
            int newcolor;
            for (int y=0; y<bmpheight; y++) {
                mCircleGradientS.getPixels(gradientRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                //watchBackgroundsBmp[i].mBackgroundColorized.getPixels(watchBmpRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                watchBackgroundsBmp[i].mBackgroundBlack.getPixels(watchBmpRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                mFX.getPixels(fxRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                for (int x=0; x<bmpwidth; x++) {
                    int grdA = Color.alpha(gradientRow[x]);
                    if ((color & 0x00FFFFFF) != 0) {
//                            if (appearance.mShowDialGradient == true) {
//                                int grdA = Color.alpha(gradientRow[x]);
//                                coeff = (float)(grdA) / 255f;
//                            } else {
//                                coeff = 1.0f;
//                            }
                        if (appearance.mShowDialGradient == true) {
                            coeff = (float)(grdA) / 255f;
                        } else {
//                                if (grdA == 0) coeff = 0.0f;
//                                else
                            coeff = 1.0f;
                        }
                        r = pixR * coeff * 0.7f + (float)(Color.red(fxRow[x])) * coeff * 0.3f;
                        g = pixG * coeff * 0.7f + (float)(Color.green(fxRow[x])) * coeff * 0.3f;
                        b = pixB * coeff * 0.7f + (float)(Color.blue(fxRow[x])) * coeff * 0.3f;
                        //
                        newcolor = Color.rgb((int)(r), (int)(g), (int)(b));
                        watchBmpRow[x] = (watchBmpRow[x] & 0xFF000000) | (newcolor & 0x00FFFFFF);
                        if (i == 0) opaqueRow[x] = (0xFF000000) | (newcolor & 0x00FFFFFF);
                    } else {
                        watchBmpRow[x] = (watchBmpRow[x] & 0xFF000000) | (color & 0x00FFFFFF);
                        if (i == 0) opaqueRow[x] = (0xFF000000) | (color & 0x00FFFFFF);
                    }
//                        if (y == 160 && x > 210) {
//                            Log.i(TAG, "§§§§§ x: " + x + ", grdA=" + grdA + ", coeff=" + coeff + ", r=" + r + ", g=" + g + ", b=" + b);
//                        }
                }
                watchBackgroundsBmp[i].mBackgroundColorized.setPixels(watchBmpRow, 0, bmpwidth, 0, y, bmpwidth, 1);
                if (i == 0) mCircleGradientNoTransparent.setPixels(opaqueRow, 0, bmpwidth, 0, y, bmpwidth, 1);
            }
        }

        denseAppearance.mDialPlateReady = false;
    } // changeBackgroundColor

    private void changeBigAuxDialColor(WatchAppearance appearance) {
        createBigAuxDialGradient(appearance);
        int color = appearance.mMainCalendarDialBackgroundColor;
        Bitmap destBmp = mBigAuxDialGradientNoTransparent;
        //
        int bmpwidth = destBmp.getWidth();
        int bmpheight = destBmp.getHeight();
        int[] gradientRow = new int[bmpwidth];
        int[] auxRow = new int[bmpwidth];
        int[] fxRow = new int[bmpwidth];
        //
        float coeff;
        for (int y=0; y<bmpheight; y++) {
            mBigAuxDialGradient.getPixels(gradientRow, 0, bmpwidth, 0, y, bmpwidth, 1);
            mFX.getPixels(fxRow, 0, bmpwidth, 0, y, bmpwidth, 1);
            for (int x=0; x<bmpwidth; x++) {
                if ((color & 0x00FFFFFF) != 0) {
                    if (appearance.mShowDialGradient == true) {
                        int grdA = Color.alpha(gradientRow[x]);
                        coeff = (float)(grdA) / 255f;
                    } else coeff = 1.0f;
                    //float coeff = ((float)(grdA) * 0.3f + rgbToY(color) * 0.7f) / 255f;
                    float r, g, b;
                    r = Color.red(color) * coeff * 0.7f + (float)(Color.red(fxRow[x])) * coeff * 0.3f;
                    g = Color.green(color) * coeff * 0.7f + (float)(Color.green(fxRow[x])) * coeff * 0.3f;
                    b = Color.blue(color) * coeff * 0.7f + (float)(Color.blue(fxRow[x])) * coeff * 0.3f;
                    int newcolor = Color.rgb((int)(r), (int)(g), (int)(b));
                    auxRow[x] = (0xFF000000) | (newcolor & 0x00FFFFFF);
                } else {
                    auxRow[x] = (0xFF000000) | (color & 0x00FFFFFF);
                }
            }
            destBmp.setPixels(auxRow, 0, bmpwidth, 0, y, bmpwidth, 1);
        }

        denseAppearance.mDialPlateReady = false;
    } // changeBigAuxDialColor

    // new float[] {1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f}; // /9: box blur
    // new float[] {0, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f}; // sharpen
    // new float[] {-1f, -1f, -1f, -1f, 8f, -1f, -1f, -1f, -1f}; // edge detection
    // new float[] {0f, 20f, 0f, 20f, -59f, 20f, 1f, 13f, 0f}; // /7: fuzzy glass
    // new float[] {2f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, -1f}; // emboss, divisor 2, offset 127
    // new float[] {1.0f, 2.0f, 1.0f, 2.0f, 4.0f, 2.0f, 1.0f, 2.0f, 1.0f}; // gaussian, divisor 16, offset 0
    //sharpen: 0.0,    -1.0,    0.0, -1.0,    5.0,    -1.0, 0.0,    -1.0,    0.0
    //edge: 0.0,    1.0,    0.0, 1.0,    -4.0,    1.0, 0.0,    1.0,    0.0
    //find edges: -1.0,    -1.0,    -1.0, -2.0,    8.0,    -1.0, -1.0,    -1.0,    -1.0
    // new float[] {0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f}; // identity
    private Bitmap convolve3(Bitmap original, float[] coefficients) {
        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(AirForceRuWearFaceService.this);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicConvolve3x3 convolution = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
        convolution.setInput(allocIn);
        convolution.setCoefficients(coefficients);
        convolution.forEach(allocOut);

        allocOut.copyTo(bitmap);
        rs.destroy();
        return bitmap;
    } // convolve3
    //
    private Bitmap convolve5(Bitmap original, float[] coefficients) {
        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(AirForceRuWearFaceService.this);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicConvolve5x5 convolution = ScriptIntrinsicConvolve5x5.create(rs, Element.U8_4(rs));
        convolution.setInput(allocIn);
        convolution.setCoefficients(coefficients);
        //float test[] = new float[] {0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f};
        //convolution.setCoefficients(test);
        convolution.forEach(allocOut);

        allocOut.copyTo(bitmap);
        rs.destroy();
        return bitmap;
    } // convolve5
    //








*/


    public static Bitmap blur(Bitmap original, float radius, Context context) {
        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        blur.setInput(allocIn);
        blur.setRadius(radius);
        blur.forEach(allocOut);

        allocOut.copyTo(bitmap);
        rs.destroy();
        return bitmap;
    }




    public static float rgbToY(int color) {
        return (float) (0.21 * Color.red(color) + 0.72 * Color.green(color) + 0.07 * Color.blue(color));
    }


    public static Bitmap alphaChannel(Bitmap sourceBitmap) {
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        int[] sourceRow = new int[width], destRow = new int[width];
        Bitmap destBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y=0; y<height; y++) {
            int srcA, srcR, srcG, srcB;
            sourceBitmap.getPixels(sourceRow, 0, width, 0, y, width, 1);
            for (int x=0; x<width; x++) {
                srcA = Color.alpha(sourceRow[x]);
                destRow[x] = Color.argb(Color.alpha(sourceRow[x]), 0, 0, 0);
            }
            destBitmap.setPixels(destRow, 0, width, 0, y, width, 1);
        }

        return destBitmap;
    }

} // class PixelUtil
