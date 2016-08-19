package com.vaitls.movies;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.vaitls.movies.sync.MovieInfo;
import com.vaitls.movies.sync.MoviePage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
 * method, getInstance(). The MovieDataCache uses retrofit2 to update from the web site.
 * <p/>
 * When fetching the popular or top_rated lists, we get a page of movies at a time. For the
 * favorites list, we fetch movie information one at a time. The movie information
 * we get back from an individual fetch is slightly expanded over what we get from a full
 * page fetch.
 * <p/>
 *
 * @see <a href="http://docs.themoviedb.apiary.io/">moviedb api</a>
 */
final public class MovieDataCache {

    /**
     * A RecyclerView has a RecyclerView.RecyclerAdapter, but a PagerView has a PagerAdapter.
     * Both take the notify functions, but the two classes aren't related. Subclass both and
     * implement this interface.
     */

    private static final String TAG = MovieDataCache.class.getSimpleName();
    private static final int MAX_ERRORS = 5;
    private static MovieDataCache sCache;
    private static String sApiKey;
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
    private List<RecyclerView.Adapter> mFAdapters;

    private List<MovieInfo> mFavoriteList;
    private Set<Integer> mUnfetchedFavorites;
    private int mFetchErrors;

    private MovieDataCache() {
        if (sApiKey == null) {
            throw new IllegalStateException("API key not set");
        }
        mFavoriteList = new ArrayList<>(20);
        mUnfetchedFavorites = new HashSet<>();
        mPopularList = new ArrayList<>(100);
        mTopRatedList = new ArrayList<>(100);
        mTRAdapters = new LinkedList<>();
        mPAdapters = new LinkedList<>();
        mFAdapters = new LinkedList<>();
    }

    /**
     * Set the API key. You must do this before {@link #getInstance}
     *
     * @param apiKey
     */
    static void setKey(String apiKey) {
        sApiKey = apiKey;
    }

    /**
     * Singleton pattern. This should be the only access to a data cache.
     *
     * @return
     */
    public static MovieDataCache getInstance() {
        if (sCache == null) {
            sCache = new MovieDataCache();
            sCache.prefetch();
        }
        return sCache;
    }

    /**
     * Get the list of favorites for storing.
     *
     * @return
     */
    public int[] getFavorites() {
        int[] fl = new int[mFavoriteList.size()];
        int index = 0;
        for (MovieInfo mi : mFavoriteList) {
            fl[index++] = mi.getId();
        }
        return fl;
    }

    /**
     * Remove one or more favorites.
     *
     * @param f The ids of the favorites to remove.
     */
    public void removeFavorites(int... f) {
        for (Integer i : f) {
            mUnfetchedFavorites.remove(i);
        }
        for (int id : f) {
            for(int idx=0;idx<mFavoriteList.size(); ++idx){
                if(mFavoriteList.get(idx).getId()==id){
                    mFavoriteList.remove(idx);
                    for(RecyclerView.Adapter adapter: mFAdapters){
                        adapter.notifyItemRemoved(idx);
                    }
                    break;
                }
            }

        }
    }

    public void addFavorite(MovieInfo mi){
        updateFavorites(mi);
    }
    /**
     * Add one or mroe favorites.
     *
     * @param f
     */
    public void addFavorites(int... f) {
        Log.d(TAG,"addFavorites: "+ f);
        Set<Integer> currentFavorites = new HashSet<>(mFavoriteList.size() + f.length);
        for (MovieInfo mi : mFavoriteList) {
            currentFavorites.add(mi.getId());
        }
        boolean fetchMore = false;
        id_loop:
        for (Integer id : f) {
            if (currentFavorites.contains(id)) {
                continue;
            }
            /**
             * Very ugly, but we can skip some network traffic by searching the other lists
             * for id and copy the mi to the mFavoriteList.
             */
            for(MovieInfo mi: mPopularList){
                if(mi.getId()==id){
                    addFavorite(mi);
                    break id_loop;
                }
            }
            for(MovieInfo mi: mTopRatedList){
                if(mi.getId()==id){
                    addFavorite(mi);
                    break id_loop;
                }
            }

            fetchMore |= mUnfetchedFavorites.add(id);
        }
        if (!fetchMore) {
            return;
        }
        prefetch();
    }
    public boolean isFavorite(int id){
        for(MovieInfo mi: mFavoriteList){
            if(id==mi.getId()){
                return true;
            }
        }
        for(int i: mUnfetchedFavorites){
            if(id==i){
                return true;
            }
        }
        return false;
    }

    private void fetchPage(MovieListType type, int page) {
        if (mFetching) {
            return;
        }
        mFetching = true;
        MovieApi.retrofit.create(MovieApi.class)
                .getPage(type.toString(), sApiKey, page)
                .enqueue(new CBP(type));
    }

    private void fetchFavorites() {
        if (mUnfetchedFavorites.isEmpty()) {
            return;
        }
        if (mFetching) {
            return;
        }
        mFetching = true;
        int id = mUnfetchedFavorites.iterator().next();
        MovieApi.retrofit.create(MovieApi.class)
                .getInfo(id, sApiKey)
                .enqueue(new CBI());
    }

    /**
     * Adds an adapter of type {@link MovieListType}. Adapter will be notified
     * if the list of that type changes.
     *
     * @param movieListType
     * @param adapter
     */
    public void addAdapter(MovieListType movieListType, RecyclerView.Adapter adapter) {
        if (movieListType == MovieListType.POPULAR) {
            mPAdapters.add(adapter);
        } else if (movieListType == MovieListType.TOP_RATED) {
            mTRAdapters.add(adapter);
        } else {
            mFAdapters.add(adapter);
        }
    }

    /**
     * Stop listening for change events.
     *
     * @param movieListType
     * @param adapter
     */
    public void removeAdapter(MovieListType movieListType, RecyclerView.Adapter adapter) {
        if (movieListType == MovieListType.POPULAR) {
            mPAdapters.remove(adapter);
        } else if (movieListType == MovieListType.TOP_RATED) {
            mTRAdapters.remove(adapter);
        } else {
            mFAdapters.remove(adapter);
        }
    }

    private void prefetch() {
     //   Log.d(TAG, "prefetching");
        if (mPopularList.size() - mMaxPopFetched < 20) {
            getNextPopularPage();
        }
        if (mTopRatedList.size() - mMaxTRFetched < 20) {
            getNextTopRatedPage();
        }
        fetchFavorites();
    }

    private void update(MovieListType type, MoviePage mp) {
        if (mp == null) {
            mFetchErrors++;
        } else {
            mFetchErrors = 0;
            if (type == MovieListType.POPULAR) {
                updatePopular(mp);
            } else {
                updateTopRated(mp);
            }
        }
        mFetching = false;
        schedulePrefetch();
    }

    /**
     * Hacked up error handing. If we get MAX_ERRORS failed
     * calls to the server, we will wait for a second before trying again.
     * <p/>
     * Got a better idea?
     */
    private void schedulePrefetch() {
        if (mFetchErrors < MAX_ERRORS) {
            prefetch();
        } else {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    prefetch();
                }
            }, 1000);
        }
    }

    private void updateFavorites(MovieInfo mi) {
        if (mi == null) {
            mFetchErrors++;
        } else {
            mFetchErrors = 0;
        }
        if (mi != null && mUnfetchedFavorites.remove(mi.getId())) {
            mFavoriteList.add(mi);
            Collections.sort(mFavoriteList, new Comparator<MovieInfo>() {
                @Override
                public int compare(MovieInfo lhs, MovieInfo rhs) {
                    return lhs.getTitle().compareTo(rhs.getTitle());
                }
            });
            for (RecyclerView.Adapter adapter : mFAdapters) {
                // Minor lie to the adapter, but we are adding one and resorting, so this works out.
                adapter.notifyItemInserted(0);
                adapter.notifyDataSetChanged();
            }
        }
        mFetching = false;
        schedulePrefetch();
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
    }

    private void getNextPopularPage() {
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

/*
    public void invalidate() {
        mPopularList.clear();
        mTopRatedList.clear();
        mTopRatedTotalPages = mLastTopRatedPage = 0;
        mPopularTotalPages = mLastPopularPage = 0;
        mMaxPopFetched = mMaxTRFetched = 0;
        prefetch();
    }
*/

    /**
     * Get a {@link MovieInfo}.
     *
     * @param movieListType
     * @param idx
     * @return
     */

    public MovieInfo get(MovieListType movieListType, int idx) {
        if (movieListType == MovieListType.POPULAR) {
            return getPopular(idx);
        } else if (movieListType == MovieListType.TOP_RATED) {
            return getTopRated(idx);
        } else {
            return getFavorite(idx);
        }
    }

    /**
     * Get the total movies of a given type.
     *
     * @param movieListType
     * @return
     */
    public int getTotal(MovieListType movieListType) {
        if (movieListType == MovieListType.POPULAR) {
            return getPopularTotal();
        } else if (movieListType == MovieListType.TOP_RATED) {
            return getTopRatedTotal();
        } else {
            return getFavoriteTotal();
        }
    }

    private MovieInfo getFavorite(int idx) {
        return mFavoriteList.get(idx);
    }

    private MovieInfo getPopular(int idx) {
        if (idx > mMaxPopFetched) {
            mMaxPopFetched = idx;
            prefetch();
        }
        return mPopularList.get(idx);
    }

    private int getFavoriteTotal() {
        return mFavoriteList.size();
    }

    private int getPopularTotal() {
        return mPopularList.size();
    }

    private int getTopRatedTotal() {
        return mTopRatedList.size();
    }

    private MovieInfo getTopRated(int idx) {
        if (idx > mMaxTRFetched) {
            mMaxTRFetched = idx;
            prefetch();
        }
        return mTopRatedList.get(idx);
    }

    private interface MovieApi {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/3/movie/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        @GET("{id}")
        Call<MovieInfo> getInfo(@Path("id") int id,
                                @Query("api_key") String apiKey);

        @GET("{type}")
        Call<MoviePage> getPage(@Path("type") String type,
                                @Query("api_key") String apiKey,
                                @Query("page") int page);
    }

    private class CBP implements Callback<MoviePage> {
        final MovieListType mMovieListType;

        CBP(MovieListType movieListType) {
            mMovieListType = movieListType;
        }

        @Override
        public void onFailure(Call<MoviePage> call, Throwable t) {
            Log.w(TAG, "Page fetch failure", t);
            update(mMovieListType, null);
        }

        @Override
        public void onResponse(Call<MoviePage> call, Response<MoviePage> response) {
            //TODO Schedule an invalidation based on response.headers() "Expires" time.
            update(mMovieListType, response.body());
        }
    }
    private class CBI implements Callback<MovieInfo> {
        @Override
        public void onFailure(Call<MovieInfo> call, Throwable t) {
            Log.w(TAG, "Movieapi  mi failure: ", t);
            updateFavorites(null);
        }

        @Override
        public void onResponse(Call<MovieInfo> call, Response<MovieInfo> response) {
            updateFavorites(response.body());
        }
    }
}
