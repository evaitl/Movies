package com.vaitls.movies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.vaitls.movies.data.Contract.Favorites;
import static com.vaitls.movies.data.Contract.TopRated;
import static com.vaitls.movies.data.Contract.Popular;
import static com.vaitls.movies.data.Contract.Reviews;
import static com.vaitls.movies.data.Contract.Videos;

/**
 * Created by evaitl on 8/16/16.
 *
 * I know there are libraries to automatically create ContentProvieders,
 * but I want to do it by hand once to see what is involved.
 *
 */
public class MovieProvider extends ContentProvider {
    private MovieDBHelper mMovieDBHelper;
    private SQLiteDatabase db;
    private UriMatcher uriMatcher;
    private Map<String, String> favoritesProjectionMap;
    private Map<String, String> topRatedProjectionMap;
    private Map<String, String> popularProjectionMap;
    private Map<String, String> reviewsProjectionMap;
    private Map<String, String> videosProjectionMap;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case Contract.M_FAVORITE:
                count = db.delete(Contract.FAVORITES, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete URI" + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder("vnd.android.cursor.");
        final String ITEM = "item/vnd.com.vaitls.movies.";
        final String DIR = "dir/vnd.com.vaitls.movies.";
        switch (uriMatcher.match(uri)) {
            case Contract.M_FAVORITE:
                stringBuilder.append(ITEM);
                stringBuilder.append(Contract.FAVORITES);
                break;
            case Contract.M_FAVORITES_DIR:
                stringBuilder.append(DIR);
                stringBuilder.append(Contract.FAVORITES);
                break;
            case Contract.M_MOVIE:
                stringBuilder.append(ITEM);
                stringBuilder.append(Contract.MOVIE);
            case Contract.M_MOVIES_DIR:
                stringBuilder.append(DIR);
                stringBuilder.append(Contract.MOVIE);
                break;
            case Contract.M_POPULAR:
                stringBuilder.append(ITEM);
                stringBuilder.append(Contract.POPULAR);
                break;
            case Contract.M_POPULAR_DIR:
                stringBuilder.append(DIR);
                stringBuilder.append(Contract.POPULAR);
                break;
            case Contract.M_TOPRATED:
                stringBuilder.append(ITEM);
                stringBuilder.append(Contract.TOP_RATED);
                break;
            case Contract.M_TOPRATED_DIR:
                stringBuilder.append(DIR);
                stringBuilder.append(Contract.TOP_RATED);
                break;
            default:
                throw new IllegalArgumentException("unknown query " + uri);
        }
        return stringBuilder.toString();
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = -1;
        Uri.Builder retBuilder = Contract.BASE_CONTENT_URI.buildUpon();
        switch (uriMatcher.match(uri)) {
            case Contract.M_FAVORITE:
                rowID = db.insert(Contract.FAVORITES, null, values);
                retBuilder.appendEncodedPath(Contract.FAVORITES);
                break;
            case Contract.M_TOPRATED:
                rowID = db.insert(Contract.TOP_RATED, null, values);
                retBuilder.appendEncodedPath(Contract.TOP_RATED);
                break;
            case Contract.M_MOVIE:
                rowID = db.insert(Contract.MOVIE, null, values);
                retBuilder.appendEncodedPath(Contract.MOVIE);
                break;
            case Contract.M_POPULAR:
                rowID = db.insert(Contract.POPULAR, null, values);
                retBuilder.appendEncodedPath(Contract.POPULAR);
                break;
            case Contract.M_REVIEW:
                rowID = db.insert(Contract.MOVIE, null, values);
                retBuilder.appendEncodedPath(Contract.MOVIE);
                break;
            default:
                throw new IllegalArgumentException("Insert URI" + uri);
        }
        if (rowID < 0) {
            throw new SQLException("insertion failed" + uri);
        }
        retBuilder.appendEncodedPath(String.valueOf(rowID));
        Uri rUri = retBuilder.build();
        getContext().getContentResolver().notifyChange(rUri, null);
        return rUri;
    }

    /**
     * Most of the queries are actually of joins, so we need projection maps
     * to get the right fields.
     */
    private void fillProjectionMaps() {
        favoritesProjectionMap = new HashMap<>();
        favoritesProjectionMap.put(Favorites.COL_MID, "movies.mid");
        favoritesProjectionMap.put(Favorites.COL__ID, "movies._id");
        favoritesProjectionMap.put(Favorites.COL_TITLE, "movies.title");
        favoritesProjectionMap.put(Favorites.COL_PLOT, "movies.plot");
        favoritesProjectionMap.put(Favorites.COL_POSTER_PATH,"movies.poster_path");
        favoritesProjectionMap.put(Favorites.COL_RELEASE_DATE,"moves.release_date");
        favoritesProjectionMap.put(Favorites.COL_VOTE_AVERAGE, "movies.vote_average");

        topRatedProjectionMap = new HashMap<>(favoritesProjectionMap);
        topRatedProjectionMap.put(TopRated.COL_RANK, "toprated.rank");
        topRatedProjectionMap.put(TopRated.COL_EXPIRES, "toprated.expires");

        popularProjectionMap = new HashMap<>(favoritesProjectionMap);
        popularProjectionMap.put(Popular.COL_RANK, "popular.rank");
        popularProjectionMap.put(Popular.COL_EXPIRES, "popular.expires");

        reviewsProjectionMap = new HashMap<>(favoritesProjectionMap);
        reviewsProjectionMap.put(Reviews.COL_ID, "reviews.id");
        reviewsProjectionMap.put(Reviews.COL_AUTHOR, "reviews.author");
        reviewsProjectionMap.put(Reviews.COL_CONTENT, "reviews.content");
        reviewsProjectionMap.put(Reviews.COL_URL, "reviews.url");

        videosProjectionMap = new HashMap<>(favoritesProjectionMap);
        videosProjectionMap.put(Videos.COL_ID, "videos.id");
        videosProjectionMap.put(Videos.COL_LANG, "videos.iso_639_1");
        videosProjectionMap.put(Videos.COL_NAME, "videos.name");
        videosProjectionMap.put(Videos.COL_SITE, "videos.site");
        videosProjectionMap.put(Videos.COL_KEY, "videos.key");
    }

    @Override
    public boolean onCreate() {
        mMovieDBHelper = new MovieDBHelper(getContext());
        db = mMovieDBHelper.getWritableDatabase();
        uriMatcher = Contract.uriMatcher;
        fillProjectionMaps();
        return db != null;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case Contract.M_FAVORITES_DIR:
                qb.setTables("movies inner join favorites on movies.mid=favorites.mid");
                qb.setProjectionMap(favoritesProjectionMap);
                if(sortOrder==null){
                    /*
                    TODO: 1 utf8 nocase collation function
                    TODO: 2 title collation -- skips articles (a, the) in sorting.

                    "Die Hard" in a german locale on xbmc always ends up at H....
                     */
                    sortOrder="title collate nocase";
                }
                break;
            case Contract.M_TOPRATED_DIR:
                qb.setTables("movies inner join toprated on movies.mid=toprated.mid");
                qb.setProjectionMap(topRatedProjectionMap);
                if(sortOrder==null){
                    sortOrder="rank asc";
                }
                break;
            case Contract.M_POPULAR_DIR:
                qb.setTables("movies inner join popular on popular.mid=movies.mid");
                qb.setProjectionMap(popularProjectionMap);
                if(sortOrder==null){
                    sortOrder="rank asc";
                }
                break;
            default:
                throw new IllegalArgumentException("unhandled URI"+uri);
        }
        Cursor c=qb.query(db,projection,selection,selectionArgs,null,null,sortOrder);
        return null;
    }

    /**
     * We aren't doing updates, just inserts. I set up the sql tables to replace on conflicts.
     *
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new IllegalArgumentException("No updates " + uri);
    }
}
