package com.nnys.bikeable;


import android.content.SharedPreferences;

import com.google.android.gms.location.places.UserDataType;

public class UserPreferences {
    // todo: make a general preferences abstarct class!!

    SharedPreferences sharedPreferences;

    private boolean userAvoidsUphills;
    private boolean userPreferesBikingRoutes;

    public UserPreferences(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }


    public boolean doesUserAvoidUphills() {
        return userAvoidsUphills;
    }

    public boolean doesUserPrefereBikingRoutes() {
        return userPreferesBikingRoutes;
    }


    public void setUserAvoidsUphills(boolean userAvoidsUphills) {
        this.userAvoidsUphills = userAvoidsUphills;
    }

    public void setUserPreferesBikingRoutes(boolean userPreferesBikingRoutes) {
        this.userPreferesBikingRoutes = userPreferesBikingRoutes;
    }

    public void update(boolean isAvoidUphillsChecked, boolean isPreferBikePathChecked) {
        setUserAvoidsUphills(isAvoidUphillsChecked);
        setUserPreferesBikingRoutes(isPreferBikePathChecked);
    }

    public void loadUserPreferences(){
        userAvoidsUphills = sharedPreferences.getBoolean("userAvoidsUphills", true);
        userPreferesBikingRoutes = sharedPreferences.getBoolean("userPreferesBikingRoutes", true);
    }

    public void saveUserPreferences(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("userAvoidsUphills", userAvoidsUphills);
        editor.putBoolean("userPreferesBikingRoutes", userPreferesBikingRoutes);
        editor.commit();
    }
}
