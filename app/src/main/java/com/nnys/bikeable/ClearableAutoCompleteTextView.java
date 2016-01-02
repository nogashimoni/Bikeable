package com.nnys.bikeable;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.location.places.AutocompletePrediction;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * sub class of {@link android.widget.AutoCompleteTextView} that includes a clear (dismiss / close) button with
 * a OnClearListener to handle the event of clicking the button
 * based on code from https://gist.github.com/mderazon/6700044
 * @author Michael Derazon
 *
 */
public class ClearableAutoCompleteTextView extends AutoCompleteTextView {
    // was the text just cleared?
    boolean doClear = false;
    AutocompletePrediction prediction;
    ClearableAutoCompleteTextView currView = this;

    // if not set otherwise, the default clear listener clears the text in the
    // text view
    private OnClearListener defaultClearListener = new OnClearListener() {

        @Override
        public void onClear() {
            ClearableAutoCompleteTextView view = ClearableAutoCompleteTextView.this;
            view.clearListSelection();
            view.dismissDropDown();
            view.hideClearButton();
            prediction = null;
        }
    };

    public OnClearListener onClearListener = defaultClearListener;

    /* The image we defined for the clear button */
    public Drawable imgClearButton = ContextCompat.getDrawable(getContext(),
            R.drawable.abc_ic_clear_mtrl_alpha);


    public interface OnClearListener {
        void onClear();
    }

    /* Required methods, not used in this implementation */
    public ClearableAutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    /* Required methods, not used in this implementation */
    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /* Required methods, not used in this implementation */
    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        // Set the bounds of the button
//        this.setCompoundDrawablesWithIntrinsicBounds(null, null,
//                imgClearButton, null);
        this.setText("");
        this.hideClearButton();

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0){
                    currView.onClearListener.onClear();
                    doClear = false;
                }
                else {
                    currView.showClearButton();
                    if(s.length() < getResources().getInteger(R.integer.auto_complete_thresh)){
                        showOnlyFixedResults();
                    }
                }
            }
        });
//        this.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ClearableAutoCompleteTextView view = (ClearableAutoCompleteTextView)v;
//                if (view.getText().length() == 0) {
//                    view.onClearListener.onClear();
//                } else {
//                    view.showClearButton();
//                }
//                return false;
//            }
//        });

        this.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
            }
        });


        // if the clear button is pressed, fire up the handler. Otherwise do nothing
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                ClearableAutoCompleteTextView et = ClearableAutoCompleteTextView.this;

                if (et.getCompoundDrawables()[2] == null) {
                    showOnlyFixedResults();
                    return false;
                }

                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }

                if (event.getX() > et.getWidth() - et.getPaddingRight() - imgClearButton.getIntrinsicWidth()) {
                    doClear = true;
                    currView.setText("");
                }
                return false;
            }
        });
    }

    private void showOnlyFixedResults() {
        PlaceAutocompleteAdapter currAdapter = (PlaceAutocompleteAdapter) currView.getAdapter();
        if (currAdapter.getFixedResults().size() == 0) {
            Log.i("INFO:", "no fixed results run");
            // no fixed results
            return;
        }
        currAdapter.setResultsList(new ArrayList<AutocompletePrediction>(currAdapter.getFixedResults()));
        currView.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                currView.showDropDown();
            }
        }, 500);
    }

    public void setImgClearButton(Drawable imgClearButton) {
        this.imgClearButton = imgClearButton;
    }

    public void setImgClearButtonColor(int color){
        this.imgClearButton.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public void setOnClearListener(final OnClearListener clearListener) {
        this.onClearListener = clearListener;
    }

    public void hideClearButton() {
        this.setCompoundDrawables(null, null, null, null);
    }

    public void showClearButton() {
        this.setCompoundDrawablesWithIntrinsicBounds(null, null, imgClearButton, null);
    }

    public AutocompletePrediction getPrediction() {
        return prediction;
    }

    public void setPrediction(AutocompletePrediction prediction, boolean setText) {
        this.prediction = prediction;
        if (setText)
            this.setText(prediction.getDescription(), false);
    }
}