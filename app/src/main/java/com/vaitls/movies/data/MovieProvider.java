package com.vaitls.movies.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import static com.vaitls.movies.data.Contract.Favorites;
import static com.vaitls.movies.data.Contract.GenreNames;
import static com.vaitls.movies.data.Contract.MatcherIdxs;
import static com.vaitls.movies.data.Contract.Popular;
import static com.vaitls.movies.data.Contract.Reviews;
import static com.vaitls.movies.data.Contract.TopRated;
import static com.vaitls.movies.data.Contract.Videos;

/**
 * Created by evaitl on 8/16/16.
 * <p/>
 * I know there are libraries to automatically create ContentProvieders,
 * but I want to do it by hand once to see what is involved.
 */
public class MovieProvider extends ContentProvider {
    private static final String TAG = MovieProvider.class.getSimpleName();
    private MovieDBHelper mMovieDBHelper;
    private SQLiteDatabase db;
    private UriMatcher uriMatcher;
    private Map<String, String> favoritesProjectionMap;
    private Map<String, String> topRatedProjectionMap;
    private Map<String, String> popularProjectionMap;
    private Map<String, String> reviewsProjectionMap;
    private Map<String, String> videosProjectionMap;
    private ContentResolver mContentResolver;
    private Map<String, String> genreNamesProjectionMap;

    /**
     * @param tableName
     * @param values
     * @return
     */
    private int listInsertHelper(String tableName, Uri uri, ContentValues[] values) {
        ContentValues listValues;
        for (ContentValues v : values) {
            if (v == null) {
                continue;
            }
            listValues = Contract.buildTopRated()
                .putExpires(v.getAsLong(TopRated.COLS.EXPIRES))
                .putMid(v.getAsInteger(TopRated.COLS.MID))
                .putRank(v.getAsInteger(TopRated.COLS.RANK))
                .build();
            v.remove(TopRated.COLS.EXPIRES);
            v.remove(TopRated.COLS.RANK);
            db.insert(Contract.TableNames.MOVIES, null, v);
            db.insert(tableName, null, listValues);
        }
        mContentResolver.notifyChange(Contract.Movies.URI, null);
        mContentResolver.notifyChange(uri, null);
        return values.length;
    }

    private int bulkHelper(String tableName, Uri uri, ContentValues[] values) {
        for (ContentValues v : values) {
            if (v == null) {
                continue;
            }
            db.insert(tableName, null, v);
        }
        mContentResolver.notifyChange(uri, null);
        return values.length;
    }

    /**
     * It was taking way too long to get an initial screen. Do an optimization here
     * to get jsut one notify change per page of movies.
     *
     * @param uri
     * @param values
     * @return
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.d(TAG, "bulkInsert " + uri);
        int cnt;
        switch (uriMatcher.match(uri)) {
            case MatcherIdxs.POPULAR:
                return listInsertHelper(Contract.TableNames.POPULAR, Popular.URI, values);
            case MatcherIdxs.TOPRATED:
                return listInsertHelper(Contract.TableNames.TOPRATED, TopRated.URI, values);
            case MatcherIdxs.REVIEWS:
                return bulkHelper(Contract.TableNames.REVIEWS, Reviews.URI, values);
            case MatcherIdxs.VIDEOS:
                return bulkHelper(Contract.TableNames.VIDEOS, Videos.URI, values);
            case MatcherIdxs.GENRE_NAMES:
                return bulkHelper(Contract.TableNames.GENRE_NAMES, GenreNames.URI,values);
        }
        return super.bulkInsert(uri, values);
    }

    /**
     * TODO add delete support for db pruning.
     *
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete " + uri);
        int rowsDeleted = 0;
        switch (uriMatcher.match(uri)) {
            case MatcherIdxs.FAVORITES:
                rowsDeleted = db.delete(Contract.TableNames.FAVORITES, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(Contract.Favorites.URI, null);
                break;
            default:
                throw new UnsupportedOperationException("No deletes supported: " + uri);
        }
        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        StringBuilder stringBuilder =
            new StringBuilder("vnd.android.cursor.dir/vnd.com.vaitls.movies.");
        /**
         * We just directories. Use a 'where' clause to get
         * an individual item.
         */
        switch (uriMatcher.match(uri)) {
            case Contract.MatcherIdxs.MOVIES:
                stringBuilder.append(Contract.TableNames.MOVIES);
                break;
            case Contract.MatcherIdxs.FAVORITES:
                stringBuilder.append(Contract.TableNames.FAVORITES);
                break;
            case Contract.MatcherIdxs.GENRE_NAMES:
                stringBuilder.append(Contract.TableNames.GENRE_NAMES);
                break;
            case Contract.MatcherIdxs.TOPRATED:
                stringBuilder.append(Contract.TableNames.TOPRATED);
                break;
            case Contract.MatcherIdxs.META:
                stringBuilder.append(Contract.TableNames.META);
                break;
            case Contract.MatcherIdxs.POPULAR:
                stringBuilder.append(Contract.TableNames.POPULAR);
                break;
            case Contract.MatcherIdxs.VIDEOS:
                stringBuilder.append(Contract.TableNames.VIDEOS);
                break;
            case Contract.MatcherIdxs.REVIEWS:
                stringBuilder.append(Contract.TableNames.REVIEWS);
                break;
            default:
                throw new IllegalArgumentException("unknown query " + uri);
        }
        return stringBuilder.toString();
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert " + uri);
        long rowID = -1;
        Uri.Builder retBuilder = Contract.BASE_CONTENT_URI.buildUpon();
        switch (uriMatcher.match(uri)) {
            case Contract.MatcherIdxs.MOVIES:
                rowID = db.insert(Contract.TableNames.MOVIES, null, values);
                mContentResolver.notifyChange(Contract.Movies.URI, null);
                // These three are joined to movies on a query
                mContentResolver.notifyChange(Favorites.URI, null);
                mContentResolver.notifyChange(TopRated.URI, null);
                mContentResolver.notifyChange(Popular.URI, null);
                break;
            case Contract.MatcherIdxs.FAVORITES:
                rowID = db.insert(Contract.TableNames.FAVORITES, null, values);
                mContentResolver.notifyChange(Favorites.URI, null);
                // Really should notify TR and Pop here, but they can't see in this app.
                // Don't trigger more work than necessary.
                break;
            case Contract.MatcherIdxs.META:
                rowID = db.insert(Contract.TableNames.META, null, values);
                break;
            case Contract.MatcherIdxs.GENRE_NAMES:
                rowID = db.insert(Contract.TableNames.GENRE_NAMES, null, values);
                mContentResolver.notifyChange(GenreNames.URI, null);
                break;
            case Contract.MatcherIdxs.TOPRATED:
                rowID = db.insert(Contract.TableNames.TOPRATED, null, values);
                mContentResolver.notifyChange(TopRated.URI, null);
                break;
            case Contract.MatcherIdxs.POPULAR:
                rowID = db.insert(Contract.TableNames.POPULAR, null, values);
                mContentResolver.notifyChange(Popular.URI, null);
                break;
            case Contract.MatcherIdxs.VIDEOS:
                rowID = db.insert(Contract.TableNames.VIDEOS, null, values);
                mContentResolver.notifyChange(Videos.URI, null);
                break;
            case Contract.MatcherIdxs.REVIEWS:
                rowID = db.insert(Contract.TableNames.REVIEWS, null, values);
                mContentResolver.notifyChange(Reviews.URI, null);
                break;
            default:
                throw new IllegalArgumentException("Insert URI" + uri);
        }
        if (rowID < 0) {
            throw new SQLException("insertion failed: " + uri);
        }
        retBuilder.appendEncodedPath(String.valueOf(rowID));
        Uri rUri = retBuilder.build();
        mContentResolver.notifyChange(rUri, null);
        return rUri;
    }

    /**
     * Several of the queries are actually of joins instead of tables,
     * so we need projection maps to get the right fields.
     */
    private void fillProjectionMaps() {
        favoritesProjectionMap = new HashMap<>();
        favoritesProjectionMap.put(Favorites.COLS.MID, "movies.mid");
        favoritesProjectionMap.put(Favorites.COLS._ID, "movies._id");
        favoritesProjectionMap.put(Favorites.COLS.TITLE, "movies.title");
        favoritesProjectionMap.put(Favorites.COLS.PLOT, "movies.plot");
        favoritesProjectionMap.put(Favorites.COLS.POSTER_PATH, "movies.poster_path");
        favoritesProjectionMap.put(Favorites.COLS.RELEASE_DATE, "movies.release_date");
        favoritesProjectionMap.put(Favorites.COLS.VOTE_AVERAGE, "movies.vote_average");
        favoritesProjectionMap.put(Favorites.COLS.VOTE_COUNT, "movies.vote_count");
        favoritesProjectionMap.put(Favorites.COLS.GENRES, "movies.genres");
        favoritesProjectionMap.put(Favorites.COLS.FAVORITE, "favorites.favorite");

        topRatedProjectionMap = new HashMap<>(favoritesProjectionMap);
        topRatedProjectionMap.put(TopRated.COLS.RANK, "toprated.rank");
        topRatedProjectionMap.put(TopRated.COLS.EXPIRES, "toprated.expires");

        popularProjectionMap = new HashMap<>(favoritesProjectionMap);
        popularProjectionMap.put(Popular.COLS.RANK, "popular.rank");
        popularProjectionMap.put(Popular.COLS.EXPIRES, "popular.expires");

    }

    @Override
    public boolean onCreate() {
        mMovieDBHelper = new MovieDBHelper(getContext());
        db = mMovieDBHelper.getWritableDatabase();
        uriMatcher = Contract.uriMatcher;
        mContentResolver = getContext().getContentResolver();
        fillProjectionMaps();
        return db != null;
    }


    /**
     * I'm not sure I made the right choice here. I think there are three ways to go with
     * these queries:
     * <p/>
     * <ol>
     * <li>Raw tables and let callers specify joins.</li>
     * <li>Do the joins on the query (here)</li>
     * <li>Create views with joins and have queries specify
     * views instead of tables.</li>
     * </ol>
     * I guess I decided that the first is two hard and the third is too abstract. A good
     * argument could be made though that I should have gone with the views. I'll probably
     * do that next time.
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case MatcherIdxs.GENRE_NAMES:
                qb.setTables(Contract.TableNames.GENRE_NAMES);
                break;
            case MatcherIdxs.META:
                qb.setTables(Contract.TableNames.META);
                break;
            case MatcherIdxs.REVIEWS:
                qb.setTables(Contract.TableNames.REVIEWS);
                break;
            case MatcherIdxs.VIDEOS:
                qb.setTables(Contract.TableNames.VIDEOS);
                break;
            case MatcherIdxs.FAVORITES:
                qb.setProjectionMap(favoritesProjectionMap);
                qb.setTables("movies inner join favorites on movies.mid=favorites.mid");
                if (sortOrder == null) {
                    sortOrder = "title collate nocase asc";
                }
                break;
            case MatcherIdxs.TOPRATED:
                qb.setProjectionMap(topRatedProjectionMap);
                qb.setTables(
                    "movies left join favorites on movies.mid=favorites.mid " +
                        "inner join toprated on movies.mid=toprated.mid");
                if (sortOrder == null) {
                    sortOrder = "rank asc";
                }
                break;
            case MatcherIdxs.POPULAR:
                qb.setProjectionMap(popularProjectionMap);
                qb.setTables(
                    "movies left join favorites on movies.mid=favorites.mid " +
                        "inner join popular on movies.mid=popular.mid");
                if (sortOrder == null) {
                    sortOrder = "rank asc";
                }
                break;
            default:
                throw new IllegalArgumentException("unhandled URI" + uri);
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(mContentResolver, uri);
        return c;
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
