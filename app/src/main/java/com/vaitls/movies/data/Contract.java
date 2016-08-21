package com.vaitls.movies.data;

import android.content.UriMatcher;
import android.net.Uri;

/**
 * Created by evaitl on 8/17/16.
 */

public class Contract {
    static final UriMatcher uriMatcher;
    static final int M_FAVORITES_DIR = 1;
    static final int M_TOPRATED_DIR = 2;
    static final int M_POPULAR_DIR = 3;
    static final int M_MOVIES_DIR = 4;
    static final int M_FAVORITE = 5;
    static final int M_TOPRATED = 6;
    static final int M_POPULAR = 7;
    static final int M_MOVIE = 8;
    static final int M_VIDEOS_DIR = 9;
    static final int M_VIDEO = 10;
    static final int M_REVIEWS_DIR = 11;
    static final int M_REVIEW = 12;
    static final int M_META=13;
    static final String CONTENT_AUTH = "com.vaitls.movies.app";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTH);
    static final String FAVORITES = "favorites";
    public static final Uri FAVORITES_URI =
            BASE_CONTENT_URI.buildUpon()
                    .appendPath(FAVORITES)
                    .build();
    static final String POPULAR = "popular";
    public static final Uri POPULAR_URI =
            BASE_CONTENT_URI.buildUpon()
                    .appendPath(POPULAR)
                    .build();
    static final String TOP_RATED = "top_rated";
    public static final Uri TOP_RATED_URI =
            BASE_CONTENT_URI.buildUpon()
                    .appendPath(TOP_RATED)
                    .build();
    static final String MOVIE = "movies";
    public static final Uri MOVIE_INFO_URI =
            BASE_CONTENT_URI.buildUpon()
                    .appendPath(MOVIE)
                    .build();

    static final String VIDEO = "video";
    public static final Uri VIDEO_URI =
            BASE_CONTENT_URI.buildUpon()
                    .appendPath(VIDEO)
                    .build();
    static final String REVIEW = "review";
    public static final Uri REVIEW_URI =
            BASE_CONTENT_URI.buildUpon()
                    .appendPath(REVIEW)
                    .build();
    static final String META="meta";
    public static final Uri META_URI=
            BASE_CONTENT_URI.buildUpon()
            .appendPath(META)
            .build();
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTH, FAVORITES, M_FAVORITES_DIR);
        uriMatcher.addURI(CONTENT_AUTH, FAVORITES + "/#", M_FAVORITE);
        uriMatcher.addURI(CONTENT_AUTH, TOP_RATED, M_TOPRATED_DIR);
        uriMatcher.addURI(CONTENT_AUTH, TOP_RATED + "/#", M_TOPRATED);
        uriMatcher.addURI(CONTENT_AUTH, POPULAR, M_POPULAR_DIR);
        uriMatcher.addURI(CONTENT_AUTH, POPULAR + "/#", M_POPULAR);
        uriMatcher.addURI(CONTENT_AUTH, MOVIE, M_MOVIES_DIR);
        uriMatcher.addURI(CONTENT_AUTH, MOVIE + "/#", M_MOVIE);
        uriMatcher.addURI(CONTENT_AUTH, VIDEO, M_VIDEOS_DIR);
        uriMatcher.addURI(CONTENT_AUTH, VIDEO + "/#", M_VIDEO);
        uriMatcher.addURI(CONTENT_AUTH, REVIEW, M_REVIEWS_DIR);
        uriMatcher.addURI(CONTENT_AUTH, REVIEW + "/#", M_REVIEW);
        uriMatcher.addURI(CONTENT_AUTH,META,M_META);
    }

    public interface Meta{
        String COL__ID="_id";
        String COL_LAST_POP_PAGE="last_pop_page";
        String COL_LAST_TR_PAGE="last_tr_page";
        String COL_MAX_POP_PAGE="max_pop_page";
        String COL_MAX_TR_PAGE="max_tr_page";
    }
    /**
     * These interfaces just exists to collect the column names. I'd make them
     * final, but java doesn't let you do final interfaces.
     */
    public interface Movies {
        String COL__ID = "_id";
        String COL_MID = "mid";
        String COL_TITLE = "title";
        String COL_PLOT = "plot";
        String COL_POSTER_PATH = "poster_path";
        String COL_RELEASE_DATE = "release_date";
        String COL_VOTE_COUNT   = "vote_count";
        String COL_VOTE_AVERAGE = "vote_average";
    }

    public interface Favorites extends Movies {

    }

    public interface TopRated extends Movies {
        String COL_RANK = "rank";
        String COL_EXPIRES = "expires";
    }

    public interface Popular extends TopRated {

    }

    public interface Reviews extends Movies {
        String COL_ID = "id";
        String COL_AUTHOR = "author";
        String COL_CONTENT = "content";
        String COL_URL = "url";
    }

    public interface Videos extends Movies {
        String COL_ID = "id";
        String COL_LANG = "lang";
        String COL_NAME = "name";
        String COL_SITE = "site";
        String COL_KEY = "key";
    }
}
