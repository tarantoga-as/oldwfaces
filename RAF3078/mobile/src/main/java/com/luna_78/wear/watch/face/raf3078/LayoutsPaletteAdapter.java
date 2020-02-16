package com.luna_78.wear.watch.face.raf3078;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.luna_78.wear.watch.face.raf3078.common.ACommon;
import com.luna_78.wear.watch.face.raf3078.common.Layout;
import com.luna_78.wear.watch.face.raf3078.common.LayoutsPalette;
import com.luna_78.wear.watch.face.raf3078.common.SerializerXML;


/*
java.lang.IllegalStateException: The content of the adapter has changed but ListView did not receive a notification.
Make sure the content of your adapter is not modified from a background thread, but only
from the UI thread. [in ListView(2131296374, class android.widget.ListView) with
Adapter(class com.luna_78.airforceru.ConfigPaletteAdapter)]
*/

/**
 * Created by buba on 27/05/15.
 */
public class LayoutsPaletteAdapter extends BaseAdapter {


    private static final String TAG = "LPA";
    private static final int CROSS_OFFSET = 20;

    HandheldCompanionConfigActivity mActivity;
//    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    int mIconSide = ACommon.LAYOUT_PALETTE_ICON_SIDE_DIMENSION; //210
    long mTimeOut = 5000;
    Path mCrossIcon;

    LayoutsPalette gLayoutsPalette = null;

    public EditText mEditElementName;
    private ListView mListView;
    //public ImageButton mButtonSetElementName;

    boolean mLocked = false;
    int savedFirstVisiblePosition = 0;
    //
    public void lock() {
        mLocked = true;
        if (null != mListView) savedFirstVisiblePosition = mListView.getFirstVisiblePosition();
    }
    public void unlock() {
        mLocked = false;
        if (null != mListView) mListView.setSelectionFromTop(savedFirstVisiblePosition, 0);
    }

    public void setEditElementNameTuple(EditText editText/*, ImageButton btn*/) {
        mEditElementName = editText;
        //mButtonSetElementName = btn;
    }
    public void setListView(ListView view) { mListView = view; }

    Handler mCommonHandler;
    public void setCommonHandler(Handler handler) { mCommonHandler = handler; }


    LayoutsPaletteAdapter(Context context) {
        mActivity = (HandheldCompanionConfigActivity) context;
        mCrossIcon = new Path();
        mCrossIcon.moveTo(CROSS_OFFSET, CROSS_OFFSET); mCrossIcon.lineTo(mIconSide-CROSS_OFFSET, mIconSide-CROSS_OFFSET);
        mCrossIcon.moveTo(CROSS_OFFSET, mIconSide-CROSS_OFFSET); mCrossIcon.lineTo(mIconSide-CROSS_OFFSET, CROSS_OFFSET);
//        for (int i = 0; i < mImporterActivity.mLayoutsPalette.size(); i++) {
//            mIdMap.put(mImporterActivity.mLayoutsPalette.get(i), i);
//        }
    }

    public void setLayoutsPalette(LayoutsPalette palette) {
        gLayoutsPalette = palette;
        if (gLayoutsPalette==null) System.gc();
    }

    @Override
    public int getCount() {
        int result = 0;
        if (!mLocked) {
            if (gLayoutsPalette!=null) result = gLayoutsPalette.size();
        }
        //Log.i(TAG, "((( getCount, return=" + result);
        return result;
    }

    @Override
    public Object getItem(int position) {
        Object result = null;
        if (!mLocked) {
            if (gLayoutsPalette!=null) result = gLayoutsPalette.get(position);
        }
        //Log.i(TAG, "((( getItem, position=" + position + ", result=" + result.getClass().getCanonicalName());
        return result;
    }

    @Override
    public long getItemId(int position) {
        //Log.i(TAG, "((( getItemId, position=" + position);
//        String item = (String) getItem(position);
//        return mIdMap.get(item);
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        boolean result = true;
        if (!mLocked) {
            if (gLayoutsPalette!=null) result = (gLayoutsPalette.size() == 0);
        }
        //Log.i(TAG, "((( isEmpty, result=" + result);
        return result;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (gLayoutsPalette==null) {
            System.gc();
            //Log.i(TAG, "((( getView, gLayoutsPalette = " + gLayoutsPalette);
            return null;
        }

        //Log.i(TAG, "((( getView, position = " + position + ", parent=" + parent);

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.config_palette_row, parent, false);

        Integer positionTag = position;
        Layout layoutElement = gLayoutsPalette.get(position);

        if (null == layoutElement) return null;

        TextView txtFirstLine = (TextView) rowView.findViewById(R.id.txtFirstLine);
        txtFirstLine.setTag(positionTag);
        String cpName = layoutElement.name;
        String cpNameGen = String.valueOf(position + 1) + ". " + cpName;
        txtFirstLine.setText(cpNameGen);

        // lazy image loader: http://stackoverflow.com/a/3068012/423868
        // https://github.com/thest1/LazyList/tree/master/src/com/fedorvlasov/lazylist

        ImageButton btnIcon = (ImageButton) rowView.findViewById(R.id.btnIcon);
        //Bitmap bmp = layoutElement.iconDense;
        btnIcon.setMaxHeight(mIconSide);
        btnIcon.setMinimumHeight(mIconSide);
        btnIcon.setMaxWidth(mIconSide);
        btnIcon.setMinimumWidth(mIconSide);
        WatchFaceIconDrawable btnDraw = new WatchFaceIconDrawable(position, false);
        btnIcon.setBackground(btnDraw);
        btnIcon.setTag(positionTag);
        btnIcon.setOnClickListener(mIconOnClick);
        btnIcon.setOnLongClickListener(mIconOnLongClick);

        ImageButton btnIconAmbient = (ImageButton) rowView.findViewById(R.id.btnIconAmbient);
        btnIconAmbient.setMaxHeight(mIconSide);
        btnIconAmbient.setMinimumHeight(mIconSide);
        btnIconAmbient.setMaxWidth(mIconSide);
        btnIconAmbient.setMinimumWidth(mIconSide);
        //bmp = layoutElement.iconAmbient;
        WatchFaceIconDrawable btnDrawAmbient = new WatchFaceIconDrawable(position, true); // get ambient bitmap
        btnIconAmbient.setBackground(btnDrawAmbient);
        btnIconAmbient.setTag(positionTag);
        btnIconAmbient.setOnClickListener(mIconOnClick);
        btnIconAmbient.setOnLongClickListener(mIconOnLongClick);

//        TextView textView = (TextView) rowView.findViewById(R.id.txtFirstLine);
//        textView.setTag(positionTag);
//        textView.setOnClickListener(mTextOnClick);

        return rowView;
    }


//    View.OnClickListener mTextOnClick = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            Integer position = (Integer) v.getTag();
//            Layout layoutElement = gLayoutsPalette.get(position);
//            if (null != mEditElementName) {
//                mEditElementName.setEnabled(true);
//                mEditElementName.setText(layoutElement.name);
//                mEditElementName.setTag(R.id.tag_element_name, position);
//                mEditElementName.requestFocus();
//            }
//        }
//    };

    View.OnClickListener mIconOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag();
            Layout layoutElement = gLayoutsPalette.get(position);
            mActivity.mService.sendFullConfigToSet(layoutElement.config);
        }
    };

    View.OnLongClickListener mIconOnLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final RelativeLayout rowView = (RelativeLayout) v.getParent();
            final ListView listView = (ListView) rowView.getParent();
            //Log.i(TAG, "((( onLongClick, listView=" + listView);
            final Integer position = (Integer) v.getTag();

            long deletionRequestMs = gLayoutsPalette.getDeletionRequestMs(position);
            if (deletionRequestMs != -1) {
                long currentMs = System.currentTimeMillis();
                if (0 == deletionRequestMs || mTimeOut < (currentMs - deletionRequestMs)) {
                    gLayoutsPalette.setDeletionRequestMs(position);
                    mActivity.mVibrator.vibrate(20);
                    rowView.animate().setDuration(300).alpha(0.5f)
                            .withEndAction(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            rowView.setAlpha(1);
                                        }
                                    }
                            );
                    rowView.getChildAt(0).postInvalidate();
                    rowView.getChildAt(1).postInvalidate();
                    if (null != mCommonHandler) mCommonHandler.removeMessages(ACommon.MSG_DELETION_TIMEOUT);
                    if (null != mCommonHandler) mCommonHandler.sendEmptyMessageDelayed(ACommon.MSG_DELETION_TIMEOUT, mTimeOut);
                    return true;
                } else {
                    rowView.animate().setDuration(1000).alpha(0)
                            .withEndAction(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            mActivity.removeConfigPaletteElement(position);
                                            clearEditElementNameTuple(listView);
                                            rowView.setAlpha(1);
                                        }
                                    }
                            );
                }
            }
            return true;
        }
    };

    private void clearEditElementNameTuple(View listView) {
        if (listView != null) listView.requestFocus();
        if (mEditElementName != null) {
            mEditElementName.setTag(R.id.tag_element_name, null);
            mEditElementName.setText("");
            //mEditElementName.clearFocus();
            mEditElementName.setEnabled(false);
        }
    }







    class WatchFaceIconDrawable extends Drawable {

        //Bitmap mIcon;
        Paint mPaint;
        int mPosition;
        boolean mAmbient;

//        WatchFaceIconDrawable(Bitmap bmp) {
//            mIcon = bmp;
//            mPaint = new Paint();
//            mPaint.setColor(0xff7f7f7f);
//            mPaint.setAntiAlias(true);
//        }

        WatchFaceIconDrawable(int position, boolean ambient) {
            mPosition = position;
            mAmbient = ambient;
            mPaint = new Paint();
            mPaint.setColor(0xff7f7f7f);
            mPaint.setAntiAlias(true);
        }

        @Override
        public void draw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            float centerX = width / 2f;
            float centerY = height / 2f;
            Bitmap bmp = null;

            if (gLayoutsPalette!=null) {
//            if (mIcon != null) {
//                canvas.drawBitmap(mIcon, 0, 0, null);
                Layout layoutElement = gLayoutsPalette.get(mPosition);
                if (layoutElement != null) {
                    //bmp = (mAmbient) ? layoutElement.iconAmbient : layoutElement.iconDense;

                    //Log.i(TAG, "((( draw, pos=" + mPosition + ", iconDense=" + layoutElement.iconDense + ", iconAmb=" + layoutElement.iconAmbient);
                    bmp = (mAmbient) ?
                            SerializerXML.bmpFromFile(mActivity, layoutElement.iconAmbientFileName) :
                            SerializerXML.bmpFromFile(mActivity, layoutElement.iconDenseFileName);
                }
            }

            mPaint.setColor(0xff7f7f7f);

            if (bmp!=null) {
                //Bitmap sb = Bitmap.createScaledBitmap(bmp, mIconSide, mIconSide, true);
                canvas.drawBitmap(bmp, 0, 0, null);
            } else {
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(2f);
                canvas.drawCircle(centerX, centerY, Math.min(centerX, centerY) - 5f, mPaint);
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                mPaint.setStrokeWidth(2f);
                mPaint.setTextSize(Math.min(centerX, centerY) / 2f);
                ACommon.drawHvAlignedText(canvas, centerX, centerY, "?", mPaint, Paint.Align.CENTER, ACommon.TextVertAlign.Middle);
            }

            if (gLayoutsPalette!=null) {
                long deletionRequestMs = gLayoutsPalette.getDeletionRequestMs(mPosition);
                if (0 != deletionRequestMs) {
                    long currentMs = System.currentTimeMillis();
                    if (mTimeOut < (currentMs - deletionRequestMs)) {
                        gLayoutsPalette.clearDeletionRequestMs(mPosition);
                        return;
                    }
                    mPaint.setStrokeWidth(5f);
                    mPaint.setColor(0xFF33B5E5);
                    mPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawPath(mCrossIcon, mPaint);
                }
            }
        } // draw

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

    } // class WatchFaceIconDrawable

} // class LayoutsPaletteAdapter
