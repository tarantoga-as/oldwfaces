package com.luna_78.wear.watch.face.raf3078;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luna_78.wear.watch.face.raf3078.common.ACommon;
import com.luna_78.wear.watch.face.raf3078.common.AppPreferences;


// By example from here: https://developer.android.com/training/wearables/ui/lists.html

public class WearConfigActivity extends Activity
        implements WearableListView.ClickListener
{

    private static final String TAG = "WCA";
    // Sample dataset for the list
//    String[] mElements = { "Change layout", "Animations", "Phone battery", "Digits relief", "Dial gradient" };
    //Bitmap mBitmaps[] = new Bitmap[3];
    //IconDrawable[] mIconDrawables = new IconDrawable[3];
    String[] mElements; //= {}; //, "Change layout"

//    int mIconId[] = {
//            0, // R.mipmap.ic_chlayout,     // 4
//            0, // R.mipmap.ic_anim_start,   // 0
//            0, // R.mipmap.ic_anim_stop,    // 1
//            0, // R.mipmap.ic_phbatt_show,  // 2
//            0, // R.mipmap.ic_phbatt_hide,  // 3
//            0, // Digits relief
//            0, // Digits relief
//            0, // Dial gradient
//            0, // Dial gradient
//    };

    private WearableListView mWearListView;
    //private WearConfigListAdapter mWearConfigListAdapter;
    private WearConfigListAdapter2 mWearConfigListAdapter2;
    private TextView mListHeader;

//    public boolean mShowRimAnimation = false;
//    public boolean mShowHrDigitsRelief = true;
//    public boolean mShowDialGradient = true;
    //
    public boolean  mIsSourceUtc = false;
    public boolean  mShowHandheldBattery = false;
    public boolean  mIsSweep = false;
    public boolean  mRespectBurnIn = true;
    public int      mTzHemisphere = AppPreferences.DEFAULT_HEMISPHERE;



    // LocalBroadcastManager; registered in onCreate, unregistered in onDestroy
    private BroadcastReceiver mDataFromWearServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int color;
            //boolean trigger;
            if (!action.equals(ACommon.EVENT_ACTION)) return;
            Bundle bundle = intent.getExtras();
            //Log.i(TAG, "onReceive" );
            if (bundle != null) {
                int event = bundle.getInt(ACommon.BCAST_EXTRA_EVENT_TYPE);
                long time = bundle.getLong(ACommon.BCAST_EXTRA_BATTERY_TIME);
                switch (event) {
                    case ACommon.EVT_REPLY_WEARCFG_PARAMETERTS:
                        Bundle toggles = bundle.getBundle(ACommon.KEY_CFGPAL_CONFIG);
                        mShowHandheldBattery = toggles.getBoolean(ACommon.CFG_SHOW_HANDHELD_BATTERY, false);
                        mIsSweep = toggles.getBoolean(AppPreferences.KEY_SWEEP_SECONDS, false);
                        mRespectBurnIn = toggles.getBoolean(AppPreferences.KEY_RESPECT_BURN_IN, true);
                        mIsSourceUtc = toggles.getBoolean(AppPreferences.KEY_SOURCE, false);
                        mTzHemisphere = toggles.getInt(AppPreferences.KEY_HEMISPHERE, AppPreferences.DEFAULT_HEMISPHERE);
                        //
//                        mShowRimAnimation = toggles.getBoolean(ACommon.CFG_SHOW_RIM_ANIMATION);
//                        mShowHrDigitsRelief = toggles.getBoolean(ACommon.CFG_SHOW_HRDIGITS_RELIEF);
//                        mShowDialGradient = toggles.getBoolean(ACommon.CFG_SHOW_DIAL_GRADIENT);
//                        Log.i(TAG, "^^^^^ EVT_REPLY_WEARCFG_PARAMETERTS, mShowHandheldBattery=" + mShowHandheldBattery + ", mShowRimAnimation=" + mShowRimAnimation);
                        break;
                    default:
                        break;
                }
            }
        } // mDataFromWearServiceReceiver.onReceive
    };







    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(WearConfigActivity.this).unregisterReceiver(mDataFromWearServiceReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mElements = new String[5];
        mElements[0] = this.getResources().getString(R.string.elm_time_source); //, //"Time source",
        mElements[1] = this.getResources().getString(R.string.elm_tz_hemisphere); //, //"TZ hemisphere",
        mElements[2] = this.getResources().getString(R.string.elm_phone_battery); //, //"Phone battery",
        mElements[3] = this.getResources().getString(R.string.elm_sweep_seconds); //, //"Sweep seconds",
        mElements[4] = this.getResources().getString(R.string.elm_burn_in_margin); //, //"Burn in margin",


        LocalBroadcastManager.getInstance(WearConfigActivity.this).registerReceiver(mDataFromWearServiceReceiver,
        new IntentFilter(ACommon.EVENT_ACTION));

        broadcastEmptyToWearFaceService(ACommon.EVT_REQUEST_WEARCFG_PARAMETERTS, System.currentTimeMillis());

        setContentView(R.layout.activity_wear_config);

//        setContentView(R.layout.activity_wear_config_2);
//        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

//        Resources resources = getResources();
//        Drawable drawable;
//        Bitmap bitmap;
//        mBitmaps[0] = null;
//        mBitmaps[1] = null;
//        drawable = resources.getDrawable(R.drawable.ic_collection);
//        bitmap = ((BitmapDrawable) drawable).getBitmap();
//        //mBitmaps[2] = Bitmap.createScaledBitmap(bitmap, 50, 50, true);
//        mBitmaps[2] = bitmap;
//        mIconDrawables[2] = new IconDrawable(mBitmaps[2]);

        // Get the list component from the layout of the activity
        mWearListView = (WearableListView) findViewById(R.id.wearable_list);
        // Assign an adapter to the list
//        mWearConfigListAdapter = new WearConfigListAdapter(this, mElements, mBitmaps, mIconDrawables);
//        mWearListView.setAdapter(mWearConfigListAdapter);
        mWearConfigListAdapter2 = new WearConfigListAdapter2(this, mElements/*, mBitmaps, mIconDrawables*/);
        mWearListView.setAdapter(mWearConfigListAdapter2);
        // Set a click listener
        mWearListView.setClickListener(WearConfigActivity.this);

        mListHeader = (TextView) findViewById(R.id.list_header);
        mWearListView.addOnScrollListener(
                new WearableListView.OnScrollListener() {
                    @Override
                    public void onScroll(int i) {
                        //Log.i(TAG, "_____ onScroll, i=" + i);
                    }

                    @Override
                    public void onAbsoluteScrollChange(int i) {
//                        boolean showListHeader;
//                        //Log.i(TAG, "^^^^^ Scroll=" + i + ", ListHeight=" + mWearListView.getHeight() + ", Y=" + mListHeader.getY());
//                        if (i <= 20) {
//                            showListHeader = true;
//                            mListHeader.setVisibility(View.VISIBLE);
//                        } else {
//                            showListHeader = false;
//                            mListHeader.setVisibility(View.INVISIBLE);
//                        }
                    }

                    @Override
                    public void onScrollStateChanged(int i) {
                        //Log.i(TAG, "_____ onScrollStateChanged, i=" + i);
                    }

                    @Override
                    public void onCentralPositionChanged(int i) {
                        //Log.i(TAG, "_____ onCentralPositionChanged, i=" + i);
                        if (0 == i) {
                            mListHeader.setVisibility(View.VISIBLE);
                        } else {
                            mListHeader.setVisibility(View.INVISIBLE);
                        }
                    }
                }
        );
    }


    @Override // WearableListView.ClickListener
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Integer tag = (Integer) viewHolder.itemView.getTag();

        // use this data to complete some action ...
        //Log.i(TAG, "=== Position " + tag + " selected.");
        int event = -1;
        switch (tag) {
            case 0:
                event = ACommon.EVT_WEARCFG_TOGGLE_TIME_SOURCE; //ACommon.EVT_WEARCFG_TOGGLE_ANIMATION;
                break;
            case 1:
                event = ACommon.EVT_WEARCFG_TOGGLE_TZ_HEMISPHERE;
                break;
            case 2:
                event = ACommon.EVT_WEARCFG_TOGGLE_PHONE_BATTERY;
                break;
            case 3:
                event = ACommon.EVT_WEARCFG_TOGGLE_SWEEP_SECONDS; //ACommon.EVT_WEARCFG_TOGGLE_HRDIGITS_RELIEF;
                break;
            case 4:
                event = ACommon.EVT_WEARCFG_TOGGLE_RESPECT_BURN_IN; //ACommon.EVT_WEARCFG_TOGGLE_DIAL_GRADIENT;
                break;
//            case 5:
//                event = ACommon.EVT_WEARCFG_TOGGLE_LAYOUT;
//                break;
        }
        if (-1 != event) {
            long time = System.currentTimeMillis();
            broadcastEmptyToWearFaceService(event, time);
        }

        this.finish();
    }

    @Override // WearableListView.ClickListener
    public void onTopEmptyRegionClick() {

    }


    private void broadcastEmptyToWearFaceService(int event, long time) {
        Intent intent = new Intent();
        intent.setAction(ACommon.EVENT_ACTION);
        intent.putExtra(ACommon.BCAST_EXTRA_EVENT_TYPE, event);
        intent.putExtra(ACommon.BCAST_EXTRA_BATTERY_TIME, time); //long time = new Date().getTime();
        intent.putExtra(ACommon.KEY_VALUE, 0);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


/*

    private static final class WearConfigListAdapter extends WearableListView.Adapter {

        private String[] mDataset;
        private Bitmap[] mBitmaps;
        IconDrawable[] mIconDrawables;
        private final Context mContext;
        private final LayoutInflater mInflater;

        public WearConfigListAdapter(Context context, String[] dataset, Bitmap[] bitmaps, IconDrawable[] drawables) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mDataset = dataset;
            mBitmaps = bitmaps;
            mIconDrawables = drawables;
        }

        public static class ItemViewHolder extends WearableListView.ViewHolder {

            private TextView textView;
            private ImageView iconView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                textView = (TextView) itemView.findViewById(R.id.option_name);
                iconView = (ImageView) itemView.findViewById(R.id.option_icon);
                Log.i(TAG, "=== iconView height=" + iconView.getHeight() + ", width=" + iconView.getWidth());
                Log.i(TAG, "=== iconView max height=" + iconView.getMaxHeight() + ", max width=" + iconView.getMaxWidth());
            }
        }



        @Override // WearableListView.Adapter
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //return null;
            return new ItemViewHolder(mInflater.inflate(R.layout.wear_config_list_row, null));
        }

        @Override // WearableListView.Adapter
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            // retrieve the text view
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView view = itemHolder.textView;
            // replace text contents
            view.setText(mDataset[position]);
            // replace list item's metadata
            holder.itemView.setTag(position);
            //
            ImageView icon = itemHolder.iconView;
            if (mIconDrawables[position] != null) {

                //icon.setImageBitmap(mBitmaps[position]);
                icon.setBackground(mIconDrawables[position]);
            }
        }

        @Override // WearableListView.Adapter
        public int getItemCount() {
            return mDataset.length;
        }
    } // class WearConfigListAdapter

*/



    class IconDrawable extends Drawable {

        Bitmap mBitmap, mScaledBitmap;

        IconDrawable(Bitmap bmp) {
            mBitmap = bmp;
        }

        @Override
        public void draw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int dimMin = Math.min(width, height);
            //Log.i(TAG, "+++++ draw, dim=" + dimMin);
            if (null != mBitmap) {
                if (null == mScaledBitmap) {
                    mScaledBitmap = Bitmap.createScaledBitmap(mBitmap, dimMin, dimMin, true);
                }
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setDither(true);
                paint.setFilterBitmap(true);
                //paint.setAlpha(mAlpha);
                canvas.drawBitmap(mScaledBitmap, 0, 0, paint);
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
    }





    private /*static final*/ class WearConfigListAdapter2 extends WearableListView.Adapter {

        private String[] mDataset;
        private Bitmap[] mBitmaps;
        IconDrawable[] mIconDrawables;
        private final Context mContext;
        private final LayoutInflater mInflater;

        public WearConfigListAdapter2(Context context, String[] dataset/*, Bitmap[] bitmaps, IconDrawable[] drawables*/) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mDataset = dataset;
//            mBitmaps = bitmaps;
//            mIconDrawables = drawables;
        }

        public /*static*/ class ItemViewHolder extends WearableListView.ViewHolder {

            private TextView textView;
            private CircledImageView iconView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                textView = (TextView) itemView.findViewById(R.id.option_text);
                iconView = (CircledImageView) itemView.findViewById(R.id.option_image);
                //Log.i(TAG, "=== iconView height=" + iconView.getHeight() + ", width=" + iconView.getWidth());
                //Log.i(TAG, "=== iconView max height=" + iconView.getMaxHeight() + ", max width=" + iconView.getMaxWidth());
            }
        }



        @Override // WearableListView.Adapter
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //return null;
            return new ItemViewHolder(mInflater.inflate(R.layout.wear_config_list_row_2, null));
        }

        @Override // WearableListView.Adapter
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            String optname;
            int opticon;

            // retrieve the text and icon view
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView text = itemHolder.textView;
            CircledImageView icon = itemHolder.iconView;

            // replace text contents
            switch (position) {
                case 0:
                    optname = mDataset[position] + ": " +
                            ((true == mIsSourceUtc) ?
                                    mContext.getResources().getString(R.string.watch_tz_source_device) :
                                    mContext.getResources().getString(R.string.watch_tz_source_utc)
                            ); //"device" "UTC"
                    text.setText(optname);
//                    optname = mDataset[position] + ": " + ((true == mShowRimAnimation) ? "stop" : "run");
//                    text.setText(optname);
//                    opticon = ((true == mShowRimAnimation) ? mIconId[2] : mIconId[1]);
//                    icon.setImageResource(opticon);
                    break;
                case 1:
                    String hsph = "";
                    if (AppPreferences.TZ_HEMISPHERE_LOWER == mTzHemisphere) {
                        hsph = mContext.getResources().getString(R.string.watch_tz_hemisphere_upper); //"upper"
                    } else if (AppPreferences.TZ_HEMISPHERE_UPPER == mTzHemisphere) {
                        hsph = mContext.getResources().getString(R.string.watch_tz_hemisphere_lower);
                    }
                    optname = mDataset[position] + ": " + hsph;
                    text.setText(optname);
                    break;
                case 2:
                    optname = mDataset[position] + ": " + ((true == mShowHandheldBattery) ?
                            mContext.getResources().getString(R.string.handheld_battery_hide) :
                            mContext.getResources().getString(R.string.handheld_battery_show));
                    text.setText(optname);
//                    opticon = ((true == mShowHandheldBattery) ? mIconId[4] : mIconId[3]);
//                    icon.setImageResource(opticon);
                    break;
                case 3:
                    optname = mDataset[position] + ": " + ((true == mIsSweep) ?
                            mContext.getResources().getString(R.string.word_off) :
                            mContext.getResources().getString(R.string.word_on));
                    text.setText(optname);
                    //opticon = ((true == mShowHandheldBattery) ? mIconId[4] : mIconId[3]);
                    //icon.setImageResource(opticon);
                   break;
                case 4:
                    optname = mDataset[position] + ": " + ((true == mRespectBurnIn) ?
                            mContext.getResources().getString(R.string.word_off) :
                            mContext.getResources().getString(R.string.word_on));
                    text.setText(optname);
                    //opticon = ((true == mShowHandheldBattery) ? mIconId[4] : mIconId[3]);
                    //icon.setImageResource(opticon);
                    break;
//                case 5:
//                    text.setText(mDataset[position]);
////                    opticon = mIconId[0];
////                    icon.setImageResource(opticon);
//                    break;

                default:
                    break;
            }

            // replace list item's metadata
            holder.itemView.setTag(position);

//            // replace item's icon
//            if (mIconDrawables[position] != null) {
//                //icon.setImageBitmap(mBitmaps[position]);
//                icon.setBackground(mIconDrawables[position]);
//            }
        }

        @Override // WearableListView.Adapter
        public int getItemCount() {
            return mDataset.length;
        }


    } // class WearConfigListAdapter2



} // class WearConfigActivity
