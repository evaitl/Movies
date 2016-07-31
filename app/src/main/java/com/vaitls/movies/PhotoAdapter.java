package com.vaitls.movies;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by evaitl on 7/31/16.
 */
class  PhotoAdapter extends  RecyclerView.Adapter<PhotoHolder>{
    private MovieDataCache mMovieDataCache;
    private PostersFragment mPostersFragment;

    public PhotoAdapter(PostersFragment postersFragment, MovieDataCache movieDataCache){
        mMovieDataCache=movieDataCache;
        mPostersFragment=postersFragment;
        assert movieDataCache!=null;
        movieDataCache.addAdapter(MovieListType.POPULAR,this);
    }
    @Override
    public int getItemCount() {
        return mMovieDataCache.getPopularTotal();
    }

   @Override
    public void onBindViewHolder(PhotoHolder holder, int position) {
        MovieInfo mi=mMovieDataCache.getPopular(position);
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView=new TextView(mPostersFragment.getActivity());
        return new PhotoHolder(textView);
    }
}
