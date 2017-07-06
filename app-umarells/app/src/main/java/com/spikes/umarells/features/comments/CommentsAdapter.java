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

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.spikes.umarells.R;
import com.spikes.umarells.models.Comment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Luca Rossi on 05/07/2017.
 */

public class CommentsAdapter extends FirebaseRecyclerAdapter<Comment, CommentsAdapter.CommentViewHolder> {

    public CommentsAdapter(Query query) {
        super(Comment.class, R.layout.list_item_comment, CommentViewHolder.class, query);
    }

    @Override
    protected void populateViewHolder(CommentViewHolder viewHolder, Comment model, int position) {
        viewHolder.mTextAuthor.setText(model.getAuthorName());
        viewHolder.mTextContent.setText(model.getContent());
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_comment_author)
        AppCompatTextView mTextAuthor;
        @BindView(R.id.text_comment_content)
        AppCompatTextView mTextContent;

        public CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}