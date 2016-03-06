package com.nnys.bikeable;

import android.text.style.CharacterStyle;

import com.google.android.gms.location.places.AutocompletePrediction;

import java.util.List;

/**
 * This class extends the autocomplete prediction so that location taken from the map could
 * be added to the autocomplete text view.
 */
public class CustomAutoCompletePrediction implements AutocompletePrediction {

    String primaryText;
    String secondaryText;
    String placeId;

    public CustomAutoCompletePrediction(String primaryText, String secondaryText) {
        this.primaryText = primaryText;
        this.secondaryText = secondaryText;
    }

    public CustomAutoCompletePrediction(String primaryText, String secondaryText, String placeId) {
        this.primaryText = primaryText;
        this.secondaryText = secondaryText;
        this.placeId = placeId;
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
        return placeId;
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

    public void setSecondaryText(String secondaryText) {
        this.secondaryText = secondaryText;
    }
}
