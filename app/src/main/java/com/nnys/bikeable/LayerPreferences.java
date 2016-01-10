package com.nnys.bikeable;

import android.content.SharedPreferences;

/**
 * Created by Sharon on 10/01/2016.
 */
public class LayerPreferences {
    // todo: make a general preferences abstarct class!!

    SharedPreferences sharedPreferences;

    private boolean showAllBikePaths;
    private boolean showOverlapBikePaths;
    private boolean showUphills;
    private boolean showAllTelOFun;
    private boolean showNearTelOFun;

    public LayerPreferences(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }


    public void setShowAllBikePaths(boolean showAllBikePaths) {
        this.showAllBikePaths = showAllBikePaths;
    }

    public void setShowOverlapBikePaths(boolean showOverlapBikePaths) {
        this.showOverlapBikePaths = showOverlapBikePaths;
    }

    public void setShowUphills(boolean showUphills) {
        this.showUphills = showUphills;
    }

    public void setShowAllTelOFun(boolean showAllTelOFun) {
        this.showAllTelOFun = showAllTelOFun;
    }

    public void setShowNearTelOFun(boolean showNearTelOFun) {
        this.showNearTelOFun = showNearTelOFun;
    }

    public boolean isShowAllBikePaths() {
        return showAllBikePaths;
    }

    public boolean isShowOverlapBikePaths() {
        return showOverlapBikePaths;
    }

    public boolean isShowUphills() {
        return showUphills;
    }

    public boolean isShowAllTelOFun() {
        return showAllTelOFun;
    }

    public boolean isShowNearTelOFun() {
        return showNearTelOFun;
    }

    public void update(
            boolean showAllBikePaths,
            boolean showOverlapBikePaths,
            boolean showUphills,
            boolean showAllTelOFun,
            boolean showNearTelOFun ) {

        setShowAllBikePaths(showAllBikePaths);
        setShowOverlapBikePaths(showOverlapBikePaths);
        setShowUphills(showUphills);
        setShowAllTelOFun(showAllTelOFun);
        setShowNearTelOFun(showNearTelOFun);
    }


    public void loadLayerPreferences(){
        showAllBikePaths = sharedPreferences.getBoolean("showAllBikePaths", false);
        showOverlapBikePaths = sharedPreferences.getBoolean("showOverlapBikePaths", false);
        showUphills = sharedPreferences.getBoolean("showUphills", false);
        showAllTelOFun = sharedPreferences.getBoolean("showAllTelOFun", false);
        showNearTelOFun = sharedPreferences.getBoolean("showNearTelOFun", false);

    }

    public void saveLayerPreferences(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("showAllBikePaths", showAllBikePaths);
        editor.putBoolean("showOverlapBikePaths", showOverlapBikePaths);
        editor.putBoolean("showUphills", showUphills);
        editor.putBoolean("showAllTelOFun", showAllTelOFun);
        editor.putBoolean("showNearTelOFun", showNearTelOFun);
        editor.commit();
    }


}
