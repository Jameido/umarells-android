/*
 * Copyright 2017.  Luca Rossi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package com.spikes.umarells.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Luca Rossi on 05/07/2017.
 */

@IgnoreExtraProperties
public class BuildingSite {
    private static final String PROP_NAME = "name";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_ADDRESS = "address";
    private static final String PROP_LAT = "lat";
    private static final String PROP_LNG = "lng";
    private static final String PROP_START = "start";
    private static final String PROP_END = "end";


    @PropertyName(PROP_NAME)
    private String mName;
    @PropertyName(PROP_DESCRIPTION)
    private String mDescription;
    @PropertyName(PROP_LAT)
    private Double mLat;
    @PropertyName(PROP_LNG)
    private Double mLng;
    @PropertyName(PROP_ADDRESS)
    private String mAddress;
    @PropertyName(PROP_START)
    private Long mStart;
    @PropertyName(PROP_END)
    private Long mEnd;

    public BuildingSite() {
    }

    public BuildingSite(String name, String description, Double lat, Double lng, String address, Long start, Long end) {
        mName = name;
        mDescription = description;
        mLat = lat;
        mLng = lng;
        mAddress = address;
        mStart = start;
        mEnd = end;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public Double getLat() {
        return mLat;
    }

    public Double getLng() {
        return mLng;
    }

    public Long getStart() {
        return mStart;
    }

    public Long getEnd() {
        return mEnd;
    }

    public String getAddress() {
        return mAddress;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(PROP_NAME, mName);
        map.put(PROP_DESCRIPTION, mDescription);
        map.put(PROP_LAT, mLat);
        map.put(PROP_LNG, mLng);
        map.put(PROP_ADDRESS, mAddress);
        map.put(PROP_START, mStart);
        map.put(PROP_END, mEnd);

        return map;
    }
}
