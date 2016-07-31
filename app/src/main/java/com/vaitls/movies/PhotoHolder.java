package com.vaitls.movies;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by evaitl on 7/31/16.
 */

class  PhotoHolder extends RecyclerView.ViewHolder{
    private TextView mTitleTextView;

    public PhotoHolder(View itemView) {
        super(itemView);
        mTitleTextView=(TextView) itemView;
    }
    public void bindMovieInfo(MovieInfo mi){
        mTitleTextView.setText(mi.getTitle());
    }
}