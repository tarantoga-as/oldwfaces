package com.luna_78.wear.watch.face.raf3078;

import android.os.AsyncTask;

import com.luna_78.wear.watch.face.raf3078.common.ACommon;


// see here for asynktask: http://stackoverflow.com/questions/28866234/how-to-run-binded-service-method-in-asynctask-android
//  http://stackoverflow.com/questions/9814821/show-progressdialog-android
//  http://www.google.com/design/spec/components/progress-activity.html#
//  http://developer.android.com/guide/topics/ui/dialogs.html#ShowingADialog
//  http://www.google.com/design/spec/components/dialogs.html#dialogs-full-screen-dialogs


/**
 * Created by buba on 29/05/15.
 */
public class AsyncFileSerialization extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "AFS";

    public static final String SAVE = "save";
    public static final String LOAD = "load";

    public APhoneService boundService;
    private HandheldCompanionConfigActivity mActivity;
    private SplashDialogFragment mSplash;

    public AsyncFileSerialization(APhoneService service, HandheldCompanionConfigActivity activity) {
        boundService = service;
        mActivity = activity;
        mSplash = new SplashDialogFragment();
        mSplash.setType(SplashDialogFragment.FragmentType.ORDINAL);
        mSplash.showSplash(mActivity.getSupportFragmentManager(), mActivity.mScreenWideEnough);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Boolean result = null;

        int pid = android.os.Process.myPid();
        int tid = android.os.Process.myTid();
        //Log.i(TAG, "((( doInBackground, PID=" + pid + ", TID=" + tid);

        if (null == params) return null;
        if (2 != params.length) return null;

        switch (params[0]) {
            case SAVE:
                //Log.i(TAG, "((( doInBackground, save = " + params[1]);
                result = boundService.gLayoutsPalette.saveToXmlFile(params[1]);
                break;
            case LOAD:
                //Log.i(TAG, "((( doInBackground, load = " + params[1]);
                break;
        }

        //result = boundService.testServiceMethod(params[1]);
        return result;
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        //LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mTabCollectionView = (TabCollectionView) inflater.inflate(R.layout.single_tab_view, null);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        //super.onPostExecute(aBoolean);
        //Log.i(TAG, "((( onPostExecute, result=" + aBoolean);
        mSplash.dismissSplash(mActivity.getSupportFragmentManager(), mActivity.mScreenWideEnough);
        mSplash = null;
        //if (mActivity.isFinishing() || mActivity.isDestroyed()) return;
        if (!ACommon.isActivityAlive(mActivity)) return;
        mActivity.mAdapter.unlock();
        if (null != aBoolean && aBoolean == true) {
            mActivity.mAdapter.notifyDataSetChanged();
        }
    }
} // class AsyncFileSerialization
