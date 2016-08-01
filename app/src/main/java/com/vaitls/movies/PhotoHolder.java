package com.vaitls.movies;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by evaitl on 7/31/16.
 */

class PhotoHolder extends RecyclerView.ViewHolder {
    private static final String TAG=PhotoHolder.class.getSimpleName();
    private ImageView mImageView;
    private MovieInfo mMovieInfo;
    public PhotoHolder(View itemView) {
        super(itemView);
        mImageView = (ImageView) itemView;
    }

    public void bindMovieInfo(MovieInfo mi) {
        mMovieInfo = mi;
        String uri="http://image.tmdb.org/t/p/w185"+
                mi.getPoster_path();
       /* Uri uri = Uri.parse("http://image.tmdb.org/t/p/")
                .buildUpon()
                .appendPath("w185")
                .appendPath(mi.getPoster_path())
                .build();*/
        Log.d(TAG,"getting movie: "+ uri.toString());
        Picasso.with(mImageView.getContext())
                .load(uri)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.sad_face)
                //.centerInside()
                .fit()
                .into(mImageView);
    }
}