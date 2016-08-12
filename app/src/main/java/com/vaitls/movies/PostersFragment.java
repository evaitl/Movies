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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by evaitl on 7/30/16.
 */
public class PostersFragment extends Fragment{
    private static final String TAG=PostersFragment.class.getSimpleName();
    private PhotoAdapter mPhotoAdapter;
    private MovieListType mSearchOrder;
    private RecyclerView mPostersRecylerView;
    public static PostersFragment newInstance() {
        return new PostersFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_posters,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item_popularity_order:
                mSearchOrder=MovieListType.POPULAR;
                Log.d(TAG,"mipo: "+mSearchOrder);
                mPhotoAdapter.setSearchOrder(mSearchOrder);
                return true;

            case R.id.menu_item_ratings_order:
                mSearchOrder=MovieListType.TOP_RATED;
                Log.d(TAG,"miro: "+mSearchOrder);
                mPhotoAdapter.setSearchOrder(mSearchOrder);
                return true;
            case R.id.menu_item_favorites:
                mSearchOrder=MovieListType.FAVORITE;
                Log.d(TAG,"mirf:");
                mPhotoAdapter.setSearchOrder(mSearchOrder);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(!isOnline()){
            setRetainInstance(false);
            setHasOptionsMenu(false);
            return inflater.inflate(R.layout.no_network,container,false);
        }
        Log.d(TAG,"onCreateView");
        View v=inflater.inflate(R.layout.fragment_posters, container, false);
        mPostersRecylerView = (RecyclerView) v.findViewById(R.id.fragment_posters_recycler_view);
        int columns= getResources().getInteger(R.integer.columns);
        mPostersRecylerView.setLayoutManager(new GridLayoutManager(getActivity(),columns));
        mPostersRecylerView.setHasFixedSize(true);
        setupAdapter();
        return v;
    }
    private void setupAdapter(){
        Log.d(TAG,"setupAdapter:"+isAdded());
        if(isAdded()){
            PhotoAdapter adapter= new PhotoAdapter(this,mSearchOrder);
            mPhotoAdapter = adapter;
            mPostersRecylerView.setAdapter(adapter);
        }
        Log.d(TAG,"setupAdapter: done");
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mSearchOrder=MovieListType.POPULAR;


        Log.d(TAG,"onCreate");
    }
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
