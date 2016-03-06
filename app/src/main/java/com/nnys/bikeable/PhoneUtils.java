package com.nnys.bikeable;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Class to store general phone-related functions
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
