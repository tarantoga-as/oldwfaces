package com.luna_78.wear.watch.face.raf3078;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;

/**
 * Created by buba on 20/07/15.
 */
public class PathPoint {

    private static final String TAG = "PP";
    public static final float NEAR_MONO_ANGLE = 0.05f;
    public static final float NEAR_MONO_SPACE = 1.0f;
    public static final int SEGMENT_DIVIDER = 2;
    final static float RAD2DEG = (float) (180f/Math.PI);


    float pathDistance;
    float pointX, pointY, pointTangent;
    boolean isFull;

    public boolean testFull() { return isFull; }

    public float getPathDistance() { return pathDistance; }

    public float getPointX() { return pointX; }

    public float getPointY() { return pointY; }

    public float getPointTangent() { return pointTangent; }

    public PathPoint(float dist, float x, float y, float tan) {
        pathDistance = dist;
        pointX = x;
        pointY = y;
        pointTangent = tan;
        isFull = true;
    }

    public PathPoint(float dist) {
        pathDistance = dist;
        pointX = 0;
        pointY = 0;
        pointTangent = 0;
        isFull = false;
    }

    public PathPoint(PathPoint base) {
        pathDistance = base.getPathDistance();
        pointX = base.getPointX();
        pointY = base.getPointY();
        pointTangent = base.getPointTangent();
        isFull = base.testFull();
    }

    public void receiveValues(PathMeasure pathMeasure) {
        float[] coord = new float[2], tangent = new float[2];
        //float tangentAngle;

        pathMeasure.getPosTan(pathDistance, coord, tangent);
        //tangentAngle = (float) (Math.atan2(tangent[1], tangent[0]) * RAD2DEG);
        //currPP.pathDistance = pathDistance; currPP.pointX = coord[0]; currPP.pointY = coord[1]; currPP.pointTangent = tangentAngle;
        pointX = coord[0]; pointY = coord[1]; pointTangent = (float) (Math.atan2(tangent[1], tangent[0]) * RAD2DEG);
        isFull = true;
    }




//    public static void traverseSegment(int pathIndex, PathPoint start, PathPoint end, PathMeasure pathMeasure,
//                                       AWearFaceService.MonoSegmentProcessor segmCallBack) {
    public static void traverseSegment(Boolean emboss, PathPoint start, PathPoint end, PathMeasure pathMeasure,
                                       AWearFaceService.MonoSegmentProcessor segmCallBack) {
        float segmentLength = end.pathDistance - start.pathDistance;
        float speed = segmentLength / SEGMENT_DIVIDER;
        //float segmentDistance;
//        float[] coord = new float[2], tangent = new float[2];
//        float tangentAngle;

        if (!start.testFull()) start.receiveValues(pathMeasure);
        if (!end.testFull()) end.receiveValues(pathMeasure);

        float tanDiff = Math.abs(end.pointTangent - start.pointTangent);
        float spaceDist = (float) Math.hypot(Math.abs(end.pointX - start.pointX), Math.abs(end.pointY - start.pointY));

        if (tanDiff <= NEAR_MONO_ANGLE || spaceDist <= NEAR_MONO_SPACE) {
            // bingo !!!
            //segmCallBack.onSegment(pathIndex, start, end, pathMeasure);
            segmCallBack.onSegment(emboss, start, end, pathMeasure);
            return;
        }

        for (int i=0; i< SEGMENT_DIVIDER; i++) {
            PathPoint newStart, newEnd;

            if (0 == i) newStart = new PathPoint(start);
            else newStart = new PathPoint(start.pathDistance + speed * i);

            if (SEGMENT_DIVIDER -1 == i) newEnd = new PathPoint(end);
            else newEnd = new PathPoint(start.pathDistance + speed * (i + 1));

            //traverseSegment(pathIndex, newStart, newEnd, pathMeasure, segmCallBack);
            traverseSegment(emboss, newStart, newEnd, pathMeasure, segmCallBack);
        }

    } // traverseSegment


    //
    //Matrix matrix = new Matrix();
    //float[] matrixVal = new float[9];
    //pathMeasure.getMatrix(pathDistance, matrix, PathMeasure.POSITION_MATRIX_FLAG + PathMeasure.TANGENT_MATRIX_FLAG);
//                logMsg = String.format("Distance: %s. Position (x,y): %s. Tangent (cos,sin): %s",
//                        distance, Arrays.toString(coord), Arrays.toString(tangent));
//                Log.i(TAG, "(((( Contour; " + logMsg);
//                matrix.getValues(matrixVal);
//                logMsg = String.format("Matrix: %s", Arrays.toString(matrixVal));
//                Log.i(TAG, "(((( Contour; " + logMsg);
//                logMsg = String.format("Dist: %s. (X,Y): %s. Cos: %s. Sin: %s",
//                        distance, Arrays.toString(coord), (float)(Math.acos(tangent[0]) * RAD2DEG),
//                        (float)(Math.asin(tangent[1]) * RAD2DEG));
//

//    public static void traverseContour(int pathIndex, int pathType, int contourIndex,
//                                       PathMeasure pathMeasure,
//                                       AWearFaceService.MonoSegmentProcessor segmCallBack) {
    public static void traverseContour(Boolean emboss, int contourIndex,
                                       PathMeasure pathMeasure,
                                       AWearFaceService.MonoSegmentProcessor segmCallBack) {
        float pathLength = pathMeasure.getLength();
        float pathDistance;
        float speed = pathLength / 20;
        float[] coord = new float[2], tangent = new float[2];
        float tangentAngle;
        String logMsg;
        PathPoint prevPP = null, currPP = null;
        PathPoint nearMonoStart = null, nearMonoEnd = null;

        //Log.i(TAG, "((( Contour, type=" + pathType + ", pathI=" + pathIndex + ", contourN=" + contourIndex + ", length=" + pathLength);

        for (pathDistance = 0; pathDistance < pathLength; pathDistance += speed) { // && (counter < 20)
            pathMeasure.getPosTan(pathDistance, coord, tangent);
            tangentAngle = (float) (Math.atan2(tangent[1], tangent[0]) * RAD2DEG);
            //currPP.pathDistance = pathDistance; currPP.pointX = coord[0]; currPP.pointY = coord[1]; currPP.pointTangent = tangentAngle;
            currPP = new PathPoint(pathDistance, coord[0], coord[1], tangentAngle);
            if (null == prevPP) { prevPP = currPP; continue; }
//                logMsg = String.format("Dist: %s. (X,Y): %s. Tan: %s.", pathDistance, Arrays.toString(coord), tangentAngle);
//                Log.i(TAG, "(((( Contour; " + logMsg);

            //traverseSegment(pathIndex, prevPP, currPP, pathMeasure, segmCallBack);
            traverseSegment(emboss, prevPP, currPP, pathMeasure, segmCallBack);

            prevPP = currPP;
        }

        pathMeasure.getPosTan(pathLength, coord, tangent);
        tangentAngle = (float) (Math.atan2(tangent[1], tangent[0]) * RAD2DEG);
        //currPP.pathDistance = pathLength; currPP.pointX = coord[0]; currPP.pointY = coord[1]; currPP.pointTangent = tangentAngle;
        currPP = new PathPoint(pathLength, coord[0], coord[1], tangentAngle);
        //traverseSegment(pathIndex, prevPP, currPP, pathMeasure, segmCallBack);
        traverseSegment(emboss, prevPP, currPP, pathMeasure, segmCallBack);
    } // traverseContour


    //public static void traversePath(int pathIndex, int pathType, Path path, AWearFaceService.MonoSegmentProcessor segmCallBack) {
    public static void traversePath(Path path, Boolean emboss, AWearFaceService.MonoSegmentProcessor segmCallBack) {
        PathMeasure pathMeasure = new PathMeasure(path, false);
        int contourIndex = 0;
        do {
            contourIndex++;
            //traverseContour(pathIndex, pathType, contourIndex, pathMeasure, segmCallBack);
            traverseContour(emboss, contourIndex, pathMeasure, segmCallBack);
        } while (pathMeasure.nextContour());
    } // traversePath



    private static final float LIGHT_ROTATION = 0f; // при LIGHT_ROTATION = 0f свет падает от 12-ти часов
    private static final float EMBOSS = -90f;       // -90f == выпукло, 90f == впукло
    private static final float DEBOSS = 90f;        // -90f == выпукло, 90f == впукло
    //
    public static int tangentColor(boolean emboss, float tanAngle, int colorSide, int colorLightness, int colorDarkness, float lightSourceAngle) {
        // похоже, tanAngle - это НЕ угол наклона касательной, а угол наклона НОРМАЛИ в точке пути;
        // поэтому нужна коррекция normal_correction.
        // при LIGHT_ROTATION = 0f свет падает от 12-ти часов
//        float normal_correction;
//        if (emboss) normal_correction = EMBOSS;
//        else normal_correction = DEBOSS;
        float angle = tanAngle + (emboss ? EMBOSS : DEBOSS) + lightSourceAngle;

        //normalize angle !!!
        angle = (angle + 360) % 360;    // force to 0 <= angle <= 360
        if (angle > 180) angle -= 360;  // force into the minimum absolute value residue class, so that -180 < angle <= 180

//        int colorSide = Color.argb(255, 127, 127, 127); //denseAppearance.mMainTickColor;
//        int colorLight = Color.WHITE;
//        int colorDark = Color.BLACK;
        //int colorSide = denseAppearance.mMainTickColor;
        int sideR = Color.red(colorSide);
        int sideG = Color.green(colorSide);
        int sideB = Color.blue(colorSide);
        int sideA = Color.alpha(colorSide);
        int lightA = Color.alpha(colorLightness);
        int darkA = Color.alpha(colorDarkness);
        int r=0, g=0, b=0, a=0;
        //float result = 0f;

        float k;
        if (angle >= 0 && angle <= 90f) { // angle >= 0 && angle <= 90f
            // цвет меняется линейно от colorSide при angle=0 до 000000 при angle=90
            k = angle / 90f;
            //result = colorSide * (1f - k);
            r = (int) (sideR * (1f - k));
            g = (int) (sideG * (1f - k));
            b = (int) (sideB * (1f - k));
            a = (int) (sideA + (darkA - sideA) * k);
            //Log.i(TAG, "((((( Contour Q1 angle=" + tanAngle + ", k=" + k + ", R="+r+" G="+g+" B="+b);

        } else if (angle > 90f && angle <= 180f) {
            // цвет меняется линейно от 000000 при angle=90 до colorSide при angle=180
            k = (angle - 90f) / 90f;
            //result = colorSide * k;
            r = (int) (sideR * k);
            g = (int) (sideG * k);
            b = (int) (sideB * k);
            a = (int) (darkA - (darkA - sideA) * k);
            //Log.i(TAG, "((((( Contour Q2 angle=" + angle + ", k=" + k + ", R="+r+" G="+g+" B="+b);

        } else if (angle < 0 && angle >= -90f) {
            // цвет меняется линейно от colorSide при angle=0 до colorLight при angle=-90
            k = angle / -90f;
            //result = colorSide + (colorWhite - colorSide) * k;
            r = (int) (sideR + (Color.red(colorLightness) - sideR) * k);
            g = (int) (sideG + (Color.green(colorLightness) - sideG) * k);
            b = (int) (sideB + (Color.blue(colorLightness) - sideB) * k);
            a = (int) (sideA + (lightA - sideA) * k);
            //Log.i(TAG, "((((( Contour Q3 angle=" + tanAngle + ", k=" + k + ", R="+r+" G="+g+" B="+b);

        } else if (angle < -90f && angle >= -180f) {
            // цвет меняется линейно от colorLight при angle=-90 до colorSide при angle=180
            k = (angle + 90f) / -90f;
            //result = colorSide + (colorWhite - colorSide) * (1f - k);
            r = (int) (sideR + (Color.red(colorLightness) - sideR) * (1f - k));
            g = (int) (sideG + (Color.green(colorLightness) - sideG) * (1f - k));
            b = (int) (sideB + (Color.blue(colorLightness) - sideB) * (1f - k));
            a = (int) (lightA - (lightA - sideA) * k);
            //Log.i(TAG, "((((( Contour Q4 angle=" + tanAngle + ", k=" + k + ", R="+r+" G="+g+" B="+b);

        }

        r = Math.min(Math.abs(r), 255);
        g = Math.min(Math.abs(g), 255);
        b = Math.min(Math.abs(b), 255);
        a = Math.min(Math.abs(a), 255);

        return Color.argb(a, r, g, b);
    }



} // class PathPoint
