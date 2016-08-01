package com.vaitls.movies;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by evaitl on 7/31/16.
 */
class  PhotoAdapter extends  RecyclerView.Adapter<PhotoHolder>{
    private static final String TAG=PhotoAdapter.class.getSimpleName();
    private MovieDataCache mMovieDataCache;
    private PostersFragment mPostersFragment;
    private MovieListType mSearchOrder;
    public PhotoAdapter(PostersFragment postersFragment,
                        MovieListType searchOrder){
        mSearchOrder=searchOrder;
        mMovieDataCache=MovieDataCache.getInstance(null);
        mPostersFragment=postersFragment;

        Log.d(TAG,"constr");
    }

    void setSearchOrder(MovieListType searchOrder){
        if(searchOrder==mSearchOrder){
            return;
        }

        mMovieDataCache.removeAdapter(mSearchOrder,this);
        mMovieDataCache.addAdapter(searchOrder,this);
        mSearchOrder=searchOrder;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
      //  Log.d(TAG,"gic "+ mMovieDataCache.getPopularTotal());
        return mMovieDataCache.getPopularTotal();
    }

   @Override
    public void onBindViewHolder(PhotoHolder holder, int position) {
       Log.d(TAG,"obvh " +position);
       holder.bindMovieInfo(mSearchOrder, position);
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG,"ocvh");
        LayoutInflater inflater=LayoutInflater.from(mPostersFragment.getActivity());
        View view= inflater.inflate(R.layout.gallery_item,parent,false);
        return new PhotoHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mMovieDataCache.addAdapter(mSearchOrder,this);
        Log.d(TAG,"oatrv");
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mMovieDataCache.removeAdapter(mSearchOrder,this);
        Log.d(TAG,"odfrv");
    }
}
