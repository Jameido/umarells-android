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
public class Review {
    private static final String PROP_AUTHOR_ID = "authorId";
    private static final String PROP_AUTHOR_NAME = "authorName";
    private static final String PROP_TITLE = "title";
    private static final String PROP_CONTENT = "content";
    private static final String PROP_RATING = "rating";
    private static final String PROP_TIMESTAMP = "timestamp";


    @PropertyName(PROP_AUTHOR_ID)
    private String mAuthorId;
    @PropertyName(PROP_AUTHOR_NAME)
    private String mAuthorName;
    @PropertyName(PROP_TITLE)
    private String mTitle;
    @PropertyName(PROP_CONTENT)
    private String mContent;
    @PropertyName(PROP_RATING)
    private Integer mRating;
    @PropertyName(PROP_TIMESTAMP)
    private Long mTimestamp;

    public Review() {
    }

    public Review(String authorId, String authorName, String title, String content, Integer rating, Long timestamp) {
        mAuthorId = authorId;
        mAuthorName = authorName;
        mTitle = title;
        mContent = content;
        mRating = rating;
        mTimestamp = timestamp;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public String getContent() {
        return mContent;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(PROP_AUTHOR_ID, mAuthorId);
        map.put(PROP_AUTHOR_NAME, mAuthorName);
        map.put(PROP_TITLE, mTitle);
        map.put(PROP_CONTENT, mContent);
        map.put(PROP_RATING, mRating);
        map.put(PROP_TIMESTAMP, mTimestamp);

        return map;
    }
}
