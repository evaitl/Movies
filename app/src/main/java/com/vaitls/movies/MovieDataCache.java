package com.vaitls.movies;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by evaitl on 7/30/16.
 *
 * Central data store for movie data. Singleton using a factory method, getInstance. The
 * MovieDataCache acts as an intermediary between a RecycleView.Adapter and
 * a MoviedbFetcher.
 *
 * The json api returns a page of movies at a time. We fetch those in an AsyncTask in the
 * MoviedbFetcher, which calls either updatePopular() or updateTopRated() from onPostExecute.
 * After updating the internal list here, MovieDataCache then calls the adapters
 * notifyItemRangeInserted() to update the gui.
 *
 */
public class MovieDataCache {
    private static final String TAG = MovieDataCache.class.getSimpleName();
    private static MovieDataCache sCache;
    private List<MovieInfo> mPopularList;
    private int mPopularTotalPages;
    private int mLastPopularPage;
    private List<MovieInfo> mTopRatedList;
    private int mTopRatedTotalPages;
    private int mLastTopRatedPage;
    private int mMaxTRFetched;
    private int mMaxPopFetched;
    private MoviedbFetcher mFetcher;
    private Set<RecyclerView.Adapter> mTRAdapters;
    private Set<RecyclerView.Adapter> mPAdapters;

    private MovieDataCache(String apiKey) {
        assert apiKey != null;
        mPopularList = new ArrayList<>(100);
        mTopRatedList = new ArrayList<>(100);
        mTRAdapters = new HashSet<>();
        mPAdapters = new HashSet<>();
        mFetcher = MoviedbFetcher.getInstance(this,apiKey);
    }
    public static MovieDataCache getInstance(String apiKey) {
        if (sCache == null) {
            sCache = new MovieDataCache(apiKey);
        }
        return sCache;
    }
    void addAdapter(MovieListType movieListType, RecyclerView.Adapter adapter){
        if(movieListType==MovieListType.POPULAR){
            mPAdapters.add(adapter);
        }else {
            mTRAdapters.add(adapter);
        }
    }
    void removeAdapter(MovieListType movieListType,  RecyclerView.Adapter adapter){
        if(movieListType==MovieListType.POPULAR){
            mPAdapters.remove(adapter);
        }else{
            mTRAdapters.remove(adapter);
        }
    }
    private void prefetch(){

            if (mPopularList.size() - mMaxPopFetched < 10){
                getNextPopularPage();
            }

            if(mTopRatedList.size()-mMaxTRFetched <10) {
                getNextTopRatedPage();
            }

    }


    void updatePopular(MoviePage mp) {
        if (mp == null) return;
        assert mp.getPage() == mLastPopularPage + 1;
        mPopularTotalPages = mp.getTotal_pages();
        mLastPopularPage = mp.getPage();
        int oldEnd = mPopularList.size();
        mPopularList.addAll(Arrays.asList(mp.getResults()));
        for(RecyclerView.Adapter adapter: mPAdapters) {
            adapter.notifyItemRangeInserted(oldEnd, mp.getResults().length);
        }
    }

    void updateTopRated(MoviePage mp) {
        if (mp == null) return;
        assert mp.getPage() == mLastTopRatedPage + 1;
        mTopRatedTotalPages = mp.getTotal_pages();
        mLastTopRatedPage = mp.getPage();
        int oldEnd = mTopRatedList.size();
        mTopRatedList.addAll(Arrays.asList(mp.getResults()));
        for(RecyclerView.Adapter adapter: mTRAdapters) {
            adapter.notifyItemRangeInserted(oldEnd, mp.getResults().length);
        }
    }

    public  void invalidate() {
        mPopularList.clear();
        mTopRatedList.clear();
        mTopRatedTotalPages = mLastTopRatedPage = 0;
        mPopularTotalPages = mLastPopularPage = 0;
        mMaxPopFetched = mMaxTRFetched = 0;
    }

    private void getNextPopularPage() {
        if (mLastPopularPage >= mPopularTotalPages &&
                mPopularTotalPages!=0) {
            return;
        }
        mFetcher.fetchPage(MovieListType.POPULAR, mLastPopularPage + 1);
    }

    private void getNextTopRatedPage() {
        if (mLastTopRatedPage >= mTopRatedTotalPages &&
                mTopRatedTotalPages !=0) {
            return;
        }
        mFetcher.fetchPage(MovieListType.TOP_RATED, mLastTopRatedPage + 1);
    }
    public  MovieInfo getPopular(int idx) {
        if (idx > mMaxPopFetched) {
            mMaxPopFetched = idx;
            prefetch();
        }
        return mPopularList.get(idx);
    }
    public  int getPopularTotal() {
        return mPopularList.size();
    }
    public  int getTopRatedTotal() {
        return mTopRatedList.size();
    }
    public  MovieInfo getTopRated(int idx) {
        if (idx > mMaxTRFetched) {
            mMaxTRFetched = idx;
            prefetch();
        }
        return mTopRatedList.get(idx);
    }

}
