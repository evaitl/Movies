package com.vaitls.movies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by evaitl on 7/30/16.
 */
public class PostersFragment extends Fragment {
    private static final String TAG = PostersFragment.class.getSimpleName();
    private PhotoAdapter mPhotoAdapter;
    private MovieListType mSearchOrder;
    private RecyclerView mPostersRecylerView;

    public static PostersFragment newInstance() {
        return new PostersFragment();
    }

    void setSearchOrder(MovieListType searchOrder) {
        if (mSearchOrder != searchOrder) {
            mSearchOrder = searchOrder;
            mPhotoAdapter.setSearchOrder(searchOrder);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_posters, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (!isOnline()) {
            setRetainInstance(false);
            setHasOptionsMenu(false);
            return inflater.inflate(R.layout.no_network, container, false);
        }
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.recycler_view, container, false);
        mPostersRecylerView = (RecyclerView) v.findViewById(R.id.fragment_posters_recycler_view);
        int columns = getResources().getInteger(R.integer.columns);
        mPostersRecylerView.setLayoutManager(new GridLayoutManager(getActivity(), columns));
        mPostersRecylerView.setHasFixedSize(true);
        setupAdapter();


        return v;
    }

    private void setupAdapter() {
        Log.d(TAG, "setupAdapter:" + isAdded());
        if (isAdded()) {
            PhotoAdapter adapter = new PhotoAdapter(this, mSearchOrder);
            mPhotoAdapter = adapter;
            mPostersRecylerView.setAdapter(adapter);
        }
        Log.d(TAG, "setupAdapter: done");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mSearchOrder = MovieListType.POPULAR;
        Log.d(TAG, "onCreate");
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    class  PhotoAdapter extends  RecyclerView.Adapter<PhotoHolder>  {

        private final MovieDataCache mMovieDataCache;
        private final PostersFragment mPostersFragment;
        private MovieListType mSearchOrder;



        public PhotoAdapter(PostersFragment postersFragment,
                            MovieListType searchOrder){
            mSearchOrder=searchOrder;
            mMovieDataCache=MovieDataCache.getInstance();
            mPostersFragment=postersFragment;

            Log.d(TAG,"constr");
        }

        void setSearchOrder(MovieListType searchOrder){
            if(searchOrder==mSearchOrder){
                return;
            }
            notifyItemRangeRemoved(0,mMovieDataCache.getTotal(mSearchOrder));
            mMovieDataCache.removeAdapter(mSearchOrder,this);
            mMovieDataCache.addAdapter(searchOrder,this);
            mSearchOrder=searchOrder;
        }

        @Override
        public int getItemCount() {
            return mMovieDataCache.getTotal(mSearchOrder);
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
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            super.onDetachedFromRecyclerView(recyclerView);
            mMovieDataCache.removeAdapter(mSearchOrder,this);
        }
    }

    class PhotoHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        private final ImageView mImageView;
        private MovieListType mSearchOrder;
        private MovieInfo mMovieInfo;
        private final MovieDataCache mdc;
        private int mIdx;
        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView;
            mdc=MovieDataCache.getInstance();
            mImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG,"li on click: "+ mSearchOrder + " "+mIdx);
            MovieActivity movieActivity=(MovieActivity)getActivity();
            movieActivity.listItemSelected(mSearchOrder, mIdx);
        }

        public void bindMovieInfo(MovieListType searchOrder, int idx) {
            mSearchOrder = searchOrder;
            mIdx=idx;
            mMovieInfo = mdc.get(searchOrder,idx);
            String uri="http://image.tmdb.org/t/p/w185"+
                    mMovieInfo.getPoster_path();
            Log.d(TAG,"getting movie: "+ uri);
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

}
