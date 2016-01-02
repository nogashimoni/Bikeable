package com.nnys.bikeable;

import android.text.style.CharacterStyle;

import com.google.android.gms.location.places.AutocompletePrediction;

import java.util.List;

/**
 * Created by Sharon on 02/01/2016.
 */
public class CustomAutoCompletePrediction implements AutocompletePrediction {

    String primaryText;
    String secondaryText;

    public CustomAutoCompletePrediction(String primaryText, String secondaryText) {
        this.primaryText = primaryText;
        this.secondaryText = secondaryText;
    }

    @Override
    public CharSequence getFullText(CharacterStyle characterStyle) {
        return String.format("%s, %s", this.primaryText, this.secondaryText);
    }

    @Override
    public CharSequence getPrimaryText(CharacterStyle characterStyle) {
        return this.primaryText;
    }

    @Override
    public CharSequence getSecondaryText(CharacterStyle characterStyle) {
        return this.secondaryText;
    }

    @Override
    public String getDescription() {
        return this.primaryText;
    }

    @Override
    public List<? extends Substring> getMatchedSubstrings() {
        return null;
    }

    @Override
    public String getPlaceId() {
        return null;
    }

    @Override
    public List<Integer> getPlaceTypes() {
        return null;
    }

    @Override
    public AutocompletePrediction freeze() {
        return this;
    }

    @Override
    public boolean isDataValid() {
        return false;
    }
}
