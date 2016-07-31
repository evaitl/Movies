package com.vaitls.movies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Created by evaitl on 7/30/16.
 */
public class PostersFragment extends Fragment{
    private static final String TAG=PostersFragment.class.getSimpleName();
    private RecyclerView mPostersRecylerView;
    public static PostersFragment newInstance() {
        return new PostersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(!isOnline()){
            // XXX Put up the sad face fragment and return.
        }
        //return super.onCreateView(inflater, container, savedInstanceState);
        View v=inflater.inflate(R.layout.fragment_posters, container, false);
        mPostersRecylerView = (RecyclerView) v.findViewById(R.id.fragment_posters_recycler_view);
        mPostersRecylerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        String key=getResources().getString(R.string.themoviedb_key);
        MoviedbFetcher.getInstance(key);

        new FetchItemsTask().execute();
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    class FetchItemsTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            String key=getResources().getString(R.string.themoviedb_key);
            new MoviedbFetcher(key).fetchItems();
            return null;
        }
    }
}