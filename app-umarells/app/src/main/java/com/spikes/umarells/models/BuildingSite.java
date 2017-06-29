package com.spikes.umarells.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

/**
 * Created by Luca Rossi
 * (luca.rossi@alea.pro) on 29/06/2017.
 */

@IgnoreExtraProperties
public class BuildingSite {
    @PropertyName("name")
    private String mName;
    @PropertyName("lat")
    private Double mLat;
    @PropertyName("lng")
    private Double mLng;

    public BuildingSite() {
    }

    public String getName() {
        return mName;
    }

    public Double getLat() {
        return mLat;
    }

    public Double getLng() {
        return mLng;
    }
}
