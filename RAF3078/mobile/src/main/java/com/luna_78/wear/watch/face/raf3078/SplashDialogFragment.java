package com.luna_78.wear.watch.face.raf3078;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;

import com.luna_78.wear.watch.face.raf3078.common.ACommon;

//import android.app.DialogFragment;
//import android.app.FragmentManager;


// see: http://developer.android.com/guide/topics/ui/dialogs.html#ShowingADialog


/**
 * Created by buba on 31/05/15.
 */
public class SplashDialogFragment extends DialogFragment {

    private static final String TAG = "SDF";

    public static final String SPLASH_TAG = "splash";

    enum FragmentType {INITIAL, ORDINAL}

    FragmentType mType = FragmentType.INITIAL;
    //
    public void setType(FragmentType type) { mType = type; }
    public FragmentType getType() { return mType;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.i(TAG, "((( onCreate, type=" + mType);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        //Log.i(TAG, "((( onStart, type=" + mType);
        super.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //Log.i(TAG, "((( onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        //Log.i(TAG, "((( onResume, type=" + mType);
        super.onResume();
    }

    @Override
    public void onPause() {
        //Log.i(TAG, "((( onPause");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        //Log.i(TAG, "((( onDestroyView");
        super.onDestroyView();
        //mView = null;
    }

    @Override
    public void onDestroy() {
        //Log.i(TAG, "((( onDestroy, type=" + mType);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        //Log.i(TAG, "((( onDetach");
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        //Log.i(TAG, "((( onCreateView");
        View view = inflater.inflate(R.layout.circle_progress_splash, container, false);

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setBackground(new ProgressBackground());

        view.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        //return false;
                        return true;
                    }
                }
        );
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        //Log.i(TAG, "((( onCreateDialog");
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.requestWindowFeature(Window.)
        //dialog.
        return dialog;
    }

    public void showSplash(FragmentManager fragmentManager, boolean wide) {
        //Log.i(TAG, "((( showSplash, fragment = " + this + ", type=" + mType);

        SplashDialogFragment fragment;
//        fragment = (SplashDialogFragment) fragmentManager.findFragmentByTag(SPLASH_TAG);
//        if (null == fragment) fragment = this;
        fragment = this;

        if (/*wide*/true) {
            // show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, fragment, SPLASH_TAG).commit(); //.show(fragment) .show(this) disallowAddToBackStack(). , SPLASH_TAG
        } else {
            // show the fragment as a dialog
            fragment.show(fragmentManager, SPLASH_TAG);
        }
    } // showSplash

    public void dismissSplash(FragmentManager fragmentManager, boolean wide) {
        //Log.i(TAG, "((( dismissSplash, fragment = " + this + ", type=" + mType);

        SplashDialogFragment fragment;
//        fragment = (SplashDialogFragment) fragmentManager.findFragmentByTag(SPLASH_TAG);
//        if (null == fragment) fragment = this;
        fragment = this;

        Activity activity = fragment.getActivity();
        //Log.i(TAG, "((( dismissSplash, activity = " + activity);
        //Log.i(TAG, "((( dismissSplash, activity alive = " + ACommon.isActivityAlive(activity));
        if (!ACommon.isActivityAlive(activity)) return;

        if (/*wide*/true) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragment).commit(); // .hide(this) .disallowAddToBackStack()
            //transaction.
        } else {
            fragment.dismiss();
        }
    } // dismissSplash

    public void dismissSplashUnconditionally(FragmentManager fragmentManager) {
        //Log.i(TAG, "((( dismissSplashUnconditionally, fragment = " + this + ", type=" + mType);

        SplashDialogFragment fragment;
//        fragment = (SplashDialogFragment) fragmentManager.findFragmentByTag(SPLASH_TAG);
//        if (null == fragment) fragment = this;
        fragment = this;

//        Activity activity = fragment.getActivity();
//        Log.i(TAG, "((( dismissSplash, activity = " + activity);
//        Log.i(TAG, "((( dismissSplash, activity alive = " + ACommon.isActivityAlive(activity));
//        if (!ACommon.isActivityAlive(activity)) return;

        if (/*wide*/true) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragment).commit(); // .hide(this) .disallowAddToBackStack()
            //transaction.
        } else {
            fragment.dismiss();
        }
    } // dismissSplashUnconditionally



    class ProgressBackground extends Drawable {

        @Override
        public void draw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            //dim = (float) Math.min(width, height);
            float centerX = width / 2f;
            float centerY = height / 2f;
            float radiusMax = Math.min(centerX, centerY);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            paint.setColor(Color.WHITE);
            //paint.setStrokeWidth(2f);
            paint.setStyle(Paint.Style.FILL);

            canvas.drawCircle(centerX, centerY, radiusMax, paint);
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
    } // class ProgressBackground


} // class SplashDialogFragment
