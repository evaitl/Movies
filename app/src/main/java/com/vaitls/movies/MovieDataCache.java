package com.vaitls.movies;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by evaitl on 7/30/16.
 * <p/>
 * MovieDataCache is the central data store for movie data. It is a singleton using a factory
 * method, getInstance(). The
 * MovieDataCache acts as an intermediary between a RecycleView.Adapter and
 * a MoviedbFetcher.
 * <p/>
 * The json api returns a page of movies at a time. We fetch those in an AsyncTask in the
 * MoviedbFetcher, which calls either updatePopular() or updateTopRated() from onPostExecute.
 * After updating the internal list here, MovieDataCache then calls the adapters
 * notifyItemRangeInserted() to update the gui.
 */
public class MovieDataCache {
    private static final String TAG = MovieDataCache.class.getSimpleName();
    private static MovieDataCache sCache;
    private boolean mFetching;
    private List<MovieInfo> mPopularList;
    private int mPopularTotalPages;
    private int mLastPopularPage;
    private List<MovieInfo> mTopRatedList;
    private int mTopRatedTotalPages;
    private int mLastTopRatedPage;
    private int mMaxTRFetched;
    private int mMaxPopFetched;
    private List<RecyclerView.Adapter> mTRAdapters;
    private List<RecyclerView.Adapter> mPAdapters;
    private static String sApiKey;

    private class CB implements Callback<MoviePage> {
        MovieListType mMovieListType;
        CB(MovieListType movieListType){
            mMovieListType=movieListType;
        }
        @Override
        public void onFailure(Call<MoviePage> call, Throwable t) {
            Log.e(TAG,"Movieapi failure: ", t);
        }
        @Override
        public void onResponse(Call<MoviePage> call, Response<MoviePage> response) {
            update(mMovieListType,response.body());
        }
    }

    private interface MovieApi {
        @GET("{type}")
        Call<MoviePage> getPage( @Path("type") String type, @Query("api_key") String apiKey, @Query("page") int page);
        static final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/3/movie/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    static void setKey(String apiKey){
        sApiKey=apiKey;
    }

    private void fetchPage(MovieListType type, int page){
        CB cb=new CB(type);
        MovieApi.retrofit.create(MovieApi.class)
                .getPage(type.toString(),sApiKey,page)
                .enqueue(cb);
    }
    private MovieDataCache() {
        if(sApiKey==null) {
            throw new RuntimeException("API key not set");
        }
        mPopularList = new ArrayList<>(100);
        mTopRatedList = new ArrayList<>(100);
        mTRAdapters = new LinkedList<>();
        mPAdapters = new LinkedList<>();
    }

    public static MovieDataCache getInstance() {
        if (sCache == null) {
            sCache = new MovieDataCache();
            sCache.prefetch();
        }
        return sCache;
    }

    void addAdapter(MovieListType movieListType, RecyclerView.Adapter adapter) {
        if (movieListType == MovieListType.POPULAR) {
            mPAdapters.add(adapter);
        } else {
            mTRAdapters.add(adapter);
        }
    }

    void removeAdapter(MovieListType movieListType, RecyclerView.Adapter adapter) {
        if (movieListType == MovieListType.POPULAR) {
            mPAdapters.remove(adapter);
        } else {
            mTRAdapters.remove(adapter);
        }
    }

    private void prefetch() {
        Log.d(TAG, "prefetching");
        if (mFetching) {
            return;
        }
        if (mPopularList.size() - mMaxPopFetched < 10) {
            mFetching = true;
            getNextPopularPage();
        }

        if (mTopRatedList.size() - mMaxTRFetched < 10) {
            mFetching = true;
            getNextTopRatedPage();
        }

    }
    private void update(MovieListType type, MoviePage mp){
        mFetching = false;
        if (mp == null) return;
        if(type==MovieListType.POPULAR){
            updatePopular(mp);
        }else{
            updateTopRated(mp);
        }
    }
    private void updatePopular(MoviePage mp) {
       // Log.d(TAG, "updating popular " + mp.getPage());

        //assert mp.getPage() == mLastPopularPage + 1;
        mPopularTotalPages = mp.getTotal_pages();
        mLastPopularPage = mp.getPage();
        int oldEnd = mPopularList.size();
        mPopularList.addAll(Arrays.asList(mp.getResults()));
        for (RecyclerView.Adapter adapter : mPAdapters) {
            Log.d(TAG, "Notifying adapter " + oldEnd + " " + mp.getResults().length);
            adapter.notifyItemRangeInserted(oldEnd, mp.getResults().length);
        }
        prefetch();
    }

    private void updateTopRated(MoviePage mp) {
        //assert mp.getPage() == mLastTopRatedPage + 1;
        mTopRatedTotalPages = mp.getTotal_pages();
        mLastTopRatedPage = mp.getPage();
        int oldEnd = mTopRatedList.size();
        mTopRatedList.addAll(Arrays.asList(mp.getResults()));
        for (RecyclerView.Adapter adapter : mTRAdapters) {
            adapter.notifyItemRangeInserted(oldEnd, mp.getResults().length);
        }
        prefetch();
    }

/*
TODO We should invalidate based on  connection.getExpiration().

    public void invalidate() {
        mPopularList.clear();
        mTopRatedList.clear();
        mTopRatedTotalPages = mLastTopRatedPage = 0;
        mPopularTotalPages = mLastPopularPage = 0;
        mMaxPopFetched = mMaxTRFetched = 0;
        prefetch();
    }
*/

    private void getNextPopularPage() {
        Log.d(TAG, "getNextPopularPage");
        if (mLastPopularPage >= mPopularTotalPages &&
                mPopularTotalPages != 0) {
            return;
        }
        fetchPage(MovieListType.POPULAR, mLastPopularPage + 1);
    }

    private void getNextTopRatedPage() {
        if (mLastTopRatedPage >= mTopRatedTotalPages &&
                mTopRatedTotalPages != 0) {
            return;
        }
        fetchPage(MovieListType.TOP_RATED, mLastTopRatedPage + 1);
    }

    public MovieInfo get(MovieListType movieListType, int idx){
        if(movieListType==MovieListType.POPULAR){
            return getPopular(idx);
        }
        return getTopRated(idx);
    }
    public int getTotal(MovieListType movieListType){
        Log.d(TAG,"gt " + movieListType + " " +getPopularTotal() + " "+ getTopRatedTotal());
        if(movieListType==MovieListType.POPULAR){
            return getPopularTotal();
        }
        return getTopRatedTotal();
    }

    public MovieInfo getPopular(int idx) {
        Log.d(TAG, "getPopular " + idx);
        if (idx > mMaxPopFetched) {
            mMaxPopFetched = idx;
            prefetch();
        }
        // TODO better fix for this.
        if(idx>=mPopularList.size()){
            Log.e(TAG, "Attempted index OOB P");
            idx=mPopularList.size()-1;
        }

        return mPopularList.get(idx);
    }

    public int getPopularTotal() {
        return mPopularList.size();
    }

    public int getTopRatedTotal() {
        return mTopRatedList.size();
    }

    public MovieInfo getTopRated(int idx) {
        if (idx > mMaxTRFetched) {
            mMaxTRFetched = idx;
            prefetch();
        }
        // TODO better fix for this.
        if(idx>=mTopRatedList.size()){
            Log.e(TAG, "Attempted index OOB TR");
            idx=mTopRatedList.size()-1;
        }
        return mTopRatedList.get(idx);
    }

}
