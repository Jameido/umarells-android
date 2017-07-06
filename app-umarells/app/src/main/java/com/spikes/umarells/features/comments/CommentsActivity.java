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

package com.spikes.umarells.features.comments;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.spikes.umarells.R;
import com.spikes.umarells.features.detail.BuildingSiteDetailActivity;
import com.spikes.umarells.models.Comment;
import com.spikes.umarells.shared.AppCompatActivityExt;

import butterknife.BindView;
import butterknife.OnClick;

public class CommentsActivity extends AppCompatActivityExt {

    private static final String EXTRA_ID = "EXTRA_ID";
    private static final String TAG = CommentsActivity.class.getSimpleName();

    private CommentsAdapter mCommentsAdapter;
    private DatabaseReference mCommentsReference;
    private String mBuildingSiteId;

    @BindView(R.id.recycler_comments)
    RecyclerView mRecyclerComments;

    public static Intent getStartIntent(Context context, String buildingSiteId) {
        Intent startIntent = new Intent(context, CommentsActivity.class);
        startIntent.putExtra(EXTRA_ID, buildingSiteId);
        return startIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Bundle extras = getIntent().getExtras();
        if (null != extras && extras.containsKey(EXTRA_ID)) {

            initDataSource(extras.getString(EXTRA_ID, ""));
        }
    }

    @OnClick(R.id.fab_add_comment)
    void addComment(){
        if(null == getUser()){
            startAuthentication();
        }else {
            mCommentsReference.push()
                    .setValue(
                            getDummyComment().toMap(),
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

        mCommentsReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("building_comments")
                .child(buildingSiteId);

        mCommentsAdapter = new CommentsAdapter(mCommentsReference);
        mRecyclerComments.setAdapter(mCommentsAdapter);
    }

    private Comment getDummyComment() {
        return new Comment(
                getUser().getUid(),
                getUser().getDisplayName(),
                getString(R.string.ph_description),
                System.currentTimeMillis()/1000
        );


    }
}
