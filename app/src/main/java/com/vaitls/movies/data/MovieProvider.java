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

import java.util.HashMap;
import java.util.Map;

import static com.vaitls.movies.data.Contract.CONTENT_AUTH;
import static com.vaitls.movies.data.Contract.Favorites;
import static com.vaitls.movies.data.Contract.TopRated;
import static com.vaitls.movies.data.Contract.Popular;
import static com.vaitls.movies.data.Contract.Reviews;
import static com.vaitls.movies.data.Contract.Videos;
import static com.vaitls.movies.data.Contract.GenreNames;
import static com.vaitls.movies.data.Contract.MatcherIdxs;
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


    /**
     * TODO add delete support for db pruning.
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted=0;
        switch (uriMatcher.match(uri)){
            case MatcherIdxs.FAVORITES:
                rowsDeleted= db.delete(Contract.TableNames.FAVORITES,selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("No deletes supported: "+uri);
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
        long rowID = -1;
        ContentResolver contentResolver=getContext().getContentResolver();
        Uri.Builder retBuilder = Contract.BASE_CONTENT_URI.buildUpon();
        switch (uriMatcher.match(uri)) {
            case Contract.MatcherIdxs.MOVIES :
                rowID=db.insert(Contract.TableNames.MOVIES, null, values);
                contentResolver.notifyChange(Contract.Movies.URI,null);
                // These three are joined to movies on a query
                contentResolver.notifyChange(Contract.Favorites.URI,null);
                contentResolver.notifyChange(Contract.TopRated.URI,null);
                contentResolver.notifyChange(Contract.Popular.URI,null);
                break;
            case Contract.MatcherIdxs.FAVORITES :
                rowID=db.insert(Contract.TableNames.FAVORITES, null, values);
                break;
            case Contract.MatcherIdxs.META :
                rowID=db.insert(Contract.TableNames.META, null, values);
                break;
            case Contract.MatcherIdxs.GENRE_NAMES :
                rowID=db.insert(Contract.TableNames.GENRE_NAMES, null, values);
                break;
            case Contract.MatcherIdxs.TOPRATED :
                rowID=db.insert(Contract.TableNames.TOPRATED, null, values);
                break;
            case Contract.MatcherIdxs.POPULAR :
                rowID=db.insert(Contract.TableNames.POPULAR, null, values);
                break;
            case Contract.MatcherIdxs.VIDEOS :
                rowID=db.insert(Contract.TableNames.VIDEOS, null, values);
                break;
            case Contract.MatcherIdxs.REVIEWS :
                rowID=db.insert(Contract.TableNames.REVIEWS, null, values);
                break;
            default:
                throw new IllegalArgumentException("Insert URI" + uri);
        }
        if (rowID < 0) {
            throw new SQLException("insertion failed: " + uri);
        }
        retBuilder.appendEncodedPath(String.valueOf(rowID));
        Uri rUri = retBuilder.build();
        contentResolver.notifyChange(rUri, null);
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
        favoritesProjectionMap.put(Favorites.COLS.POSTER_PATH,"movies.poster_path");
        favoritesProjectionMap.put(Favorites.COLS.RELEASE_DATE,"movies.release_date");
        favoritesProjectionMap.put(Favorites.COLS.VOTE_AVERAGE, "movies.vote_average");
        favoritesProjectionMap.put(Favorites.COLS.VOTE_COUNT,"movies.vote_count");
        favoritesProjectionMap.put(Favorites.COLS.GENRES,"movies.genres");
        favoritesProjectionMap.put(Favorites.COLS.FAVORITE, "favorites.favorite");

        topRatedProjectionMap = new HashMap<>(favoritesProjectionMap);
        topRatedProjectionMap.put(TopRated.COLS.RANK, "toprated.rank");
        topRatedProjectionMap.put(TopRated.COLS.EXPIRES, "toprated.expires");

        popularProjectionMap = new HashMap<>(favoritesProjectionMap);
        popularProjectionMap.put(Popular.COLS.RANK, "popular.rank");
        popularProjectionMap.put(Popular.COLS.EXPIRES, "popular.expires");

    }
    private Map<String,String> genreNamesProjectionMap;
    @Override
    public boolean onCreate() {
        mMovieDBHelper = new MovieDBHelper(getContext());
        db = mMovieDBHelper.getWritableDatabase();
        uriMatcher = Contract.uriMatcher;
        fillProjectionMaps();
        return db != null;
    }


    /**
     * I'm not sure I made the right choice here. I think there are three ways to go with
     * these queries:
     *
     * <ol>
     *     <li>Raw tables and let callers specify joins.</li>
     *     <li>Do the joins on the query (here)</li>
     *     <li>Create views with joins and have queries specify
     *     views instead of tables.</li>
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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
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
                break;
            case MatcherIdxs.TOPRATED:
                qb.setProjectionMap(topRatedProjectionMap);
                qb.setTables(
                    "movies left join favorites on movies.mid=favorites.mid "+
                    "inner join toprated on movies.mid=toprated.mid");
                break;
            case MatcherIdxs.POPULAR:
                qb.setProjectionMap(popularProjectionMap);
                qb.setTables(
                    "movies left join favorites on movies.mid=favorites.mid "+
                    "inner join popular on movies.mid=popular.mid");
                break;
            default:
                throw new IllegalArgumentException("unhandled URI"+uri);
        }
        Cursor c=qb.query(db,projection,selection,selectionArgs,null,null,sortOrder);
        c.setNotificationUri(getContext().getContentResolver(),uri);
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
