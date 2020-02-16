package com.luna_78.wear.watch.face.raf3078;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Locale;

/**
 * Created by buba on 15/05/15.
 */

// com.luna_78.airforceru.SpinnedEditText


interface OnSpinnedEditValueChanged {
    void onValueChanged(float value);
}


public class SpinnedEditText extends LinearLayout {

    private static final String TAG = "SET";

    Button btnUp, btnDown;
    EditText editValue;

    float floatValue, minValue = 0.0f, maxValue = 360.0f;
    float upIncrement = 0.1f, downDecrement = 0.1f, mIncrement = 0.1f;
    //int intValue;

    private OnSpinnedEditValueChanged valueChangedCallBack;
    public void addOnValueChangedListener(OnSpinnedEditValueChanged callback) {
        valueChangedCallBack = callback;
    }

    public void setRange(float min, float max) {
        minValue = min;
        maxValue = max;
    }

    public SpinnedEditText(Context context) {
        super(context);
        initializeViews(context);
    }

    public SpinnedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public SpinnedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.spinned_edit_text_view, this);
    } // initializeViews


    // todo: DecimalFormatSymbols
    // 0.0 = ۰٫۹۰


    private OnLongClickListener mLongClickSetIncrement = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mIncrement == 0.1f) {
                mIncrement = 1f;
                btnDown.setText("<<");
                btnUp.setText(">>");
            } else {
                mIncrement = 0.1f;
                btnDown.setText("<");
                btnUp.setText(">");
            }
            return true;
        }
    };


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        btnUp = (Button) this.findViewById(R.id.btnUp);
        btnDown = (Button) this.findViewById(R.id.btnDown);
        editValue = (EditText) this.findViewById(R.id.editValue);


        btnUp.setOnTouchListener(
                new RepeatListener(400, 100, new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // the code to execute repeatedly
                        String txt = editValue.getText().toString();
                        String stringVal = String.format(Locale.US, "%.2f", (floatValue + mIncrement));
                        //Log.i(TAG, "((( onClick UP, txt=" + txt + ", stringVal=" + stringVal);
                        try {
                            float floatVal = Float.parseFloat(stringVal);
                            setValue(cleanFloatValue(floatVal));
                        } catch (NumberFormatException e) {
                            //e.printStackTrace();
                        }
                    }
                })
        );

        btnDown.setOnTouchListener(
                new RepeatListener(400, 100, new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // the code to execute repeatedly
                        String txt = editValue.getText().toString();
                        String stringVal = String.format(Locale.US, "%.2f", (floatValue - mIncrement));
                        //Log.i(TAG, "((( onClick DOWN, txt=" + txt + ", stringVal=" + stringVal);
                        try {
                            float floatVal = Float.parseFloat(stringVal);
                            setValue(cleanFloatValue(floatVal));
                        } catch (NumberFormatException e) {
                            //e.printStackTrace();
                        }
                    }
                })
        );

//        btnDown.setOnLongClickListener(mLongClickSetIncrement);
//        btnUp.setOnLongClickListener(mLongClickSetIncrement);

        editValue.setOnLongClickListener(mLongClickSetIncrement);
        editValue.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        // editable.toString().replace(',', '.')
                        String e = editValue.getText().toString();
                        //Log.i(TAG, "((( afterTextChanged, e=" + e);
                        float f;
                        try {
                            f = Float.parseFloat(e);
                            String txt = String.format(Locale.US, "%.2f", f);
                            f = Float.parseFloat(txt);
                            setFloatValue(f);
                        } catch (NumberFormatException e1) {
                            //e1.printStackTrace();
                            setFloatValue(minValue);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }
                }
        );
    }

    public void setValue(float val) {
        //Log.i(TAG, "((( SpinnedEditText.setValue, val=" + val); //  + ", editValue=" + editValue.getText().toString()
//        setFloatValue(val);
//        Log.i(TAG, "((( SpinnedEditText.setValue, floatValue=" + floatValue); //  + ", editValue=" + editValue.getText().toString()
        editValue.setText(String.valueOf(val));
    } // setValue

    public void setFloatValue(float val) {
        floatValue = cleanFloatValue(val);
        valueChangedCallBack.onValueChanged(floatValue);
    } // setFloatValue

    public float cleanFloatValue(float val) {
        if (val < minValue) return maxValue;
        if (val > maxValue) return minValue;
        return val;
    }

} // SpinnedEditText
