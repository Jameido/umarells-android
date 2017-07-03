package com.spikes.umarells.features.detail;

import android.net.Uri;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.spikes.umarells.R;

/**
 * Created by Luca Rossi
 * (luca.rossi@alea.pro) on 03/07/2017.
 */

public class GalleryAdapter extends FirebaseRecyclerAdapter<String, GalleryAdapter.ImageViewHolder> {

    public GalleryAdapter(Query query) {
        super(String.class, R.layout.list_item_gallery, ImageViewHolder.class, query);
    }

    @Override
    protected void populateViewHolder(ImageViewHolder viewHolder, String model, int position) {
        Glide.with(viewHolder.itemView.getContext())
                .load(Uri.parse(model))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fitCenter()
                .thumbnail(0.4f)
                .dontAnimate()
                .into((AppCompatImageView)viewHolder.itemView);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageViewHolder(View itemView) {
            super(itemView);
        }
    }
}
