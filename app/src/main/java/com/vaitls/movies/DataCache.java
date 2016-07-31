package com.vaitls.movies;

import java.util.Arrays;
import java.util.List;

/**
 * Created by evaitl on 7/30/16.
 */
public class DataCache {
    static DataCache sDataCache;
    private List<MovieInfo> mPopularList;
    private int mPopularTotalPages;
    private int mLastPopularPage;
    private List<MovieInfo> mTopRatedList;
    private int mTopRatedTotalPages;
    private int mLastTopRatedPage;
    private MoviedbFetcher mFetcher;

    private DataCache() {
        mFetcher = MoviedbFetcher.getInstance(null);
        assert mFetcher != null;
        preLoad();
    }

    public static DataCache getInstance() {
        if (sDataCache == null) {
            sDataCache = new DataCache();
        }
        return sDataCache;
    }

    private void preLoad() {
        while (mPopularList.size() < 50) {
            MoviePage mp = getNextPopularPage();
            if (mp == null) {
                break;
            }
            mPopularTotalPages = mp.getTotal_pages();
            mLastPopularPage = mp.getPage();
            mPopularList.addAll(Arrays.asList(mp.getResults()));
        }
        while (mTopRatedList.size() < 50) {
            MoviePage mp = getNextTopRatedPage();
            if (mp == null) {
                break;
            }
            mTopRatedTotalPages = mp.getTotal_pages();
            mLastTopRatedPage = mp.getPage();
            mTopRatedList.addAll(Arrays.asList(mp.getResults()));
        }
    }

    public void invalidate() {
        mPopularList = mTopRatedList = null;
        mTopRatedTotalPages = mLastTopRatedPage = 0;
        mPopularTotalPages = mLastPopularPage = 0;
        preLoad();
    }

    private MoviePage getNextPopularPage() {
        if (mLastPopularPage >= mPopularTotalPages) {
            return null;
        }
        return mFetcher.fetchPage(MoviedbFetcher.MovieListType.POPULAR, mLastPopularPage + 1);
    }

    private MoviePage getNextTopRatedPage() {
        if (mLastTopRatedPage >= mTopRatedTotalPages) {
            return null;
        }
        return mFetcher.fetchPage(MoviedbFetcher.MovieListType.TOP_RATED, mLastTopRatedPage + 1);
    }

    public MovieInfo getPopular(int idx) {
        return null;
    }

    public int getPopularTotal() {
        return mPopularList.size();
    }

    public int getTopRatedTotal() {
        return mTopRatedList.size();
    }

    public MovieInfo getTopRated(int idx) {
        return null;
    }

}
