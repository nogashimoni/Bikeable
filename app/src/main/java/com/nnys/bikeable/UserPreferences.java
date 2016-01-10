package com.nnys.bikeable;


public class UserPreferences {
    private boolean userAvoidsUphills;
    private boolean userPreferesBikingRoutes;


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
}
