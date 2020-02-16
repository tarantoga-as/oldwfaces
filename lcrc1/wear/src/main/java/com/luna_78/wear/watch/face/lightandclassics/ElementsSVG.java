package com.luna_78.wear.watch.face.lightandclassics;

import android.graphics.Matrix;
import android.graphics.Path;

/**
 * Created by buba on 26/10/15.
 */
public class ElementsSVG {
    
    float baseDialDim = 640.0f; // 640x640
    float baseDialWidth = baseDialDim;
    float baseDialHeight = baseDialDim;
    //
    float baseHandWidth = baseDialDim; // 640x80
    float baseHandHeight = 80.0f;

    String mRobotHeadSVG = "M407.782,354.498c0,0.773-0.012,1.547-0.032,2.314h-175.5" +
            "c-0.02-0.768-0.033-1.541-0.033-2.314c0-29.152,16.672-54.704,41.697-68.978l-13.871-25.025" +
            "c-1.684-3.022-0.585-6.844,2.444-8.521c3.029-1.677,6.845-0.585,8.521,2.444l14.222,25.655c10.66-4.257,22.412-6.61,34.769-6.61" +
            "c12.356,0,24.108,2.353,34.768,6.61l14.223-25.655c1.677-3.029,5.492-4.121,8.527-2.444c3.029,1.677,4.121,5.499,2.438,8.521" +
            "l-13.871,25.025C391.11,299.794,407.782,325.346,407.782,354.498z";
    String mRobotEyesSVG =
            //eye left
            "M365.061,314.94c0,2.52-2.03,4.56-4.55,4.56" +
            "c-2.511,0-4.54-2.04-4.54-4.56c0-2.511,2.029-4.551,4.54-4.551C363.03,310.39,365.061,312.43,365.061,314.94z " +
            //eye right
            "M284.04,314.94c0,2.52-2.04,4.56-4.55,4.56" +
            "c-2.52,0-4.55-2.04-4.55-4.56c0-2.511,2.03-4.551,4.55-4.551C282,310.39,284.04,312.43,284.04,314.94z";
    String mGearSVG = "M443,344c0-20.82,15.9-37.92,36.22-39.82" +
            "c-0.45-4.68-1.109-9.29-1.96-13.84C474.9,290.77,472.48,291,470,291c-22.09,0-40-17.91-40-40c0-12.56,5.79-23.78,14.86-31.11" +
            "c-2.7-3.36-5.53-6.61-8.49-9.74C429.22,216.88,419.59,221,409,221c-22.09,0-40-17.91-40-40c0-4.44,0.72-8.71,2.06-12.7" +
            "c-3.88-1.3-7.819-2.46-11.829-3.46C355.59,183.18,339.41,197,320,197s-35.59-13.82-39.23-32.16c-4.01,1-7.95,2.16-11.83,3.46" +
            "c1.34,3.99,2.06,8.26,2.06,12.7c0,22.09-17.91,40-40,40c-10.59,0-20.22-4.12-27.37-10.85c-2.96,3.13-5.79,6.38-8.49,9.74" +
            "C204.21,227.22,210,238.44,210,251c0,22.09-17.91,40-40,40c-2.48,0-4.9-0.23-7.26-0.66c-0.79,4.23-1.42,8.51-1.86,12.85" +
            "C181.15,305.14,197,322.22,197,343c0,16.9-10.48,31.35-25.3,37.21c1.71,4.23,3.6,8.36,5.66,12.4c5.57-2.95,11.91-4.61,18.64-4.61" +
            "c22.09,0,40,17.91,40,40c0,8.7-2.78,16.75-7.5,23.31c3.48,2.431,7.07,4.73,10.75,6.87C246.14,446.12,259.12,438,274,438" +
            "c22.09,0,40,17.91,40,40c0,0.63-0.01,1.26-0.04,1.88c2,0.08,4.02,0.12,6.04,0.12c2.36,0,4.71-0.05,7.04-0.16" +
            "c-0.03-0.609-0.04-1.22-0.04-1.84c0-22.09,17.91-40,40-40c14.7,0,27.55,7.93,34.5,19.75c3.65-2.16,7.21-4.47,10.66-6.91" +
            "C407.65,444.36,405,436.49,405,428c0-22.09,17.91-40,40-40c6.41,0,12.47,1.51,17.85,4.2c1.841-3.63,3.54-7.351,5.101-11.141" +
            "C453.31,375.12,443,360.76,443,344z M320,430c-60.75,0-110-49.25-110-110s49.25-110,110-110s110,49.25,110,110S380.75,430,320,430z";
    String mRobotEraserSVG = "M495,320" +
            "c0,48.324-19.588,92.074-51.257,123.743S368.324,495,320,495c-96.65,0-175-78.351-175-175c0-96.65,78.35-175,175-175" +
            "C416.649,145,495,223.35,495,320z";
    
    Path[] mRobotPath = new Path[] {
            new Path(PathFromPathDataSVG.doPath(mRobotHeadSVG)),
            new Path(PathFromPathDataSVG.doPath(mRobotEyesSVG)),
            new Path(PathFromPathDataSVG.doPath(mGearSVG)),
            new Path(PathFromPathDataSVG.doPath(mRobotEraserSVG)),
    };
    public Path[] mRobotPathScaled;
    //
    public static final int ROBOT_HEAD = 0;
    public static final int ROBOT_EYES = 1;
    public static final int ROBOT_GEAR = 2;
    public static final int ROBOT_ERASER = 3;


    private Path[] scalePathArray(Path[] srcArr, Matrix matrix, Path.FillType fillType) {
        if (null == srcArr) return null;

        int arrSize = srcArr.length;
        Path[] resultArr = new Path[arrSize];

        for (int i=0; i < arrSize; i++) {
            Path row = null;
            if (null != srcArr[i]) {
                row = new Path();
                srcArr[i].transform(matrix, row);
            }
            resultArr[i] = row;
            if (null != fillType) {
                resultArr[i].setFillType(fillType);
            }
        }

        return resultArr;
    } // scalePathArray


    public void scaleRobotPath(float targetWidth, int boundsWidth) {
        float bitmapRatio = targetWidth / baseDialWidth;
        Matrix matrix = new Matrix();
        float bitmapCenterX = (baseDialWidth / 2.0f) * bitmapRatio;
        float bitmapCenterY = (baseDialHeight / 2.0f) * bitmapRatio;
        float screenRatio = /*mVars.width*/ boundsWidth / baseDialWidth;
        float pivotX = (baseDialWidth / 2.0f) * screenRatio;
        float pivotY = (baseDialHeight / 2.0f) * screenRatio;
        matrix.postScale(bitmapRatio, bitmapRatio);
        //matrix.postTranslate(mVars.mBurnInMargin, mVars.mBurnInMargin);
        matrix.postTranslate((pivotX - bitmapCenterX), (pivotY - bitmapCenterY));

        mRobotPathScaled = scalePathArray(mRobotPath, matrix, null);
    }
}
