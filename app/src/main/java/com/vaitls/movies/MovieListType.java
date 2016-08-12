package com.vaitls.movies;

/**
 * Created by evaitl on 7/31/16.
 */
public enum MovieListType {
    POPULAR("popular"),
    TOP_RATED("top_rated"),
    FAVORITE("favorite");

    private final String mName;
    MovieListType(String str){
        mName=str;
    }

    @Override
    public String toString() {
        return mName;
    }
}
