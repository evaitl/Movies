package com.vaitls.movies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


/**
 * Created by evaitl on 7/31/16.
 */

class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private static final String TAG=PhotoHolder.class.getSimpleName();
    private ImageView mImageView;
    private MovieListType mSearchOrder;
    private MovieInfo mMovieInfo;
    private MovieDataCache mdc;
    private int idx;
    public PhotoHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        mImageView = (ImageView) itemView;
        mdc=MovieDataCache.getInstance(null);
    }

    @Override
    public void onClick(View view) {
        Context context=mImageView.getContext();
        Log.d(TAG,"oc "+mSearchOrder+" "+idx);
        Intent intent = DetailsPagerActivity.newIntent(context, mSearchOrder, idx);
        context.startActivity(intent);
    }

    public void bindMovieInfo(MovieListType searchOrder, int idx) {
        mSearchOrder = searchOrder;
        mMovieInfo = mdc.get(searchOrder,idx);
        this.idx=idx;
        String uri="http://image.tmdb.org/t/p/w185"+
                mMovieInfo.getPoster_path();
        Log.d(TAG,"getting movie: "+ uri.toString());
        Glide.with(mImageView.getContext())
                .load(uri)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.sad_face)
                .fallback(R.drawable.sad_face)
                .crossFade()
                .centerCrop()
                .into(mImageView);
    }
}