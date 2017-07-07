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

package com.spikes.umarells.features.reviews;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spikes.umarells.R;
import com.spikes.umarells.models.Review;
import com.spikes.umarells.shared.AppCompatActivityExt;

import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;

public class ReviewsActivity extends AppCompatActivityExt {

    private static final String EXTRA_ID = "EXTRA_ID";
    private static final String TAG = ReviewsActivity.class.getSimpleName();

    private ReviewsAdapter mReviewsAdapter;
    private DatabaseReference mReviewsReference;
    private String mBuildingSiteId;

    @BindView(R.id.recycler_reviews)
    RecyclerView mRecyclerReviews;

    public static Intent getStartIntent(Context context, String buildingSiteId) {
        Intent startIntent = new Intent(context, ReviewsActivity.class);
        startIntent.putExtra(EXTRA_ID, buildingSiteId);
        return startIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        Bundle extras = getIntent().getExtras();
        if (null != extras && extras.containsKey(EXTRA_ID)) {

            initDataSource(extras.getString(EXTRA_ID, ""));
        }
    }

    @OnClick(R.id.fab_add_review)
    void addReview() {
        if (null == getUser()) {
            startAuthentication();
        } else {
            mReviewsReference.push()
                    .setValue(
                            getDummyReview().toMap(),
                            (databaseError, databaseReference) -> {
                                if (null != databaseError) {
                                    //TODO show error
                                }
                            }
                    );
        }

    }

    private void initDataSource(String buildingSiteId) {
        mBuildingSiteId = buildingSiteId;

        mReviewsReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("building_reviews")
                .child(buildingSiteId);

        mReviewsAdapter = new ReviewsAdapter(mReviewsReference);
        mRecyclerReviews.setAdapter(mReviewsAdapter);
    }

    private Review getDummyReview() {
        Random rnd = new Random();
        return new Review(
                getUser().getUid(),
                getUser().getDisplayName(),
                getRandomReviewTitle(rnd),
                getRandomReviewContent(rnd),
                rnd.nextInt(5),
                System.currentTimeMillis() / 1000
        );
    }

    private String getRandomReviewTitle(Random rnd) {
        switch (rnd.nextInt(5)) {
            case 0:
                return "Tee, non si fa mica così";
            case 1:
                return "Eeeh, ai miei tempi.";
            case 2:
                return "Una volta qui erano tutti campi";
            case 3:
                return "Non si fa mica così quel lavoro li";
            case 4:
                return "Ma che lavoro fatto male!";
            default:
                return "Ehhh, ai miei tempi queste cose non si facevano così";
        }
    }

    private String getRandomReviewContent(Random rnd) {
        switch (rnd.nextInt(3)) {
            case 0:
                return "Ma guarda tu se se si può fare una roba del genere... Mah";
            case 1:
                return "Tutto coperto, non si può neanche guardare dentro, che shifo";
            case 2:
                return "Ehhh, ai miei tempi sì che sapevamo lavorare come Dio comanda!";
            case 3:
                return "Cosa mi guardi, drogato? Ah che io lo so... delinquente...";
            default:
                return "";
        }
    }
}
