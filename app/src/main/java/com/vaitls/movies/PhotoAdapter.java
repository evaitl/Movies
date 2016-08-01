package com.vaitls.movies;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by evaitl on 7/31/16.
 */
class  PhotoAdapter extends  RecyclerView.Adapter<PhotoHolder>{
    private static final String TAG=PhotoAdapter.class.getSimpleName();
    private MovieDataCache mMovieDataCache;
    private PostersFragment mPostersFragment;

    public PhotoAdapter(PostersFragment postersFragment, MovieDataCache movieDataCache){
        mMovieDataCache=movieDataCache;
        mPostersFragment=postersFragment;
        assert movieDataCache!=null;
        Log.d(TAG,"constr");
    }
    @Override
    public int getItemCount() {
        Log.d(TAG,"gic "+ mMovieDataCache.getPopularTotal());
        return mMovieDataCache.getPopularTotal();
    }

   @Override
    public void onBindViewHolder(PhotoHolder holder, int position) {
       Log.d(TAG,"obvh " +position);
        MovieInfo mi=mMovieDataCache.getPopular(position);
       holder.bindMovieInfo(mMovieDataCache.getPopular(position));
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG,"ocvh");
        TextView textView=new TextView(mPostersFragment.getActivity());
        return new PhotoHolder(textView);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mMovieDataCache.addAdapter(MovieListType.POPULAR,this);
        Log.d(TAG,"oatrv");
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mMovieDataCache.removeAdapter(MovieListType.POPULAR,this);
        Log.d(TAG,"odfrv");
    }
}
