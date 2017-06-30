package com.spikes.umarells.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Luca Rossi
 * (luca.rossi@alea.pro) on 29/06/2017.
 */

@IgnoreExtraProperties
public class BuildingSite {
    private static final String PROP_NAME = "name";
    private static final String PROP_LAT = "lat";
    private static final String PROP_LNG = "lng";


    @PropertyName(PROP_NAME)
    private String mName;
    @PropertyName(PROP_LAT)
    private Double mLat;
    @PropertyName(PROP_LNG)
    private Double mLng;

    public BuildingSite() {
    }

    public BuildingSite(String name, Double lat, Double lng) {
        mName = name;
        mLat = lat;
        mLng = lng;
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

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(PROP_NAME, mName);
        map.put(PROP_LAT, mLat);
        map.put(PROP_LNG, mLng);

        return map;
    }
}
