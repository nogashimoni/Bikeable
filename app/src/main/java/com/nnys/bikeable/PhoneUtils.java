package com.nnys.bikeable;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Sharon on 05/01/2016.
 */
public class PhoneUtils {

    private static DisplayMetrics getDisplayMetrics(){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return metrics;
    }

    public static int getScreenHeight(){
        return getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth(){
        return getDisplayMetrics().widthPixels;
    }
}
