package com.vaitls.movies.data;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.net.Uri;

/**
 * Created by evaitl on 8/17/16.
 * <p/>
 * Contract description for accessing the MovieProvider database.
 * For the interfaces within the Contract (Meta, Movies, Favorites, ...), the URI
 * field is the Uri for the table/view. The COLS nested interface has the column names,
 * the PROJECTION member is the default projection, and IDX nested interface is
 * the column indexes of the default projection.
 * <p/>
 * The table names and matcher indices aren't public because they aren't needed by clients,
 * just by the MovieProvider and MovieDBHelper, which are in this package.
 * <p/>
 * Most of the URIs return joins on queries, so the fields that are settable
 * are a subset of the fields in the COLS classes. The buildXXX functions return fluent
 * interfaces for ContentValues builders appropriate for the various tables.
 */
public class Contract {
    static final UriMatcher uriMatcher;
    static final String CONTENT_AUTH = "com.vaitls.movies.app";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTH);

    /**
     *
     */
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTH, TableNames.FAVORITES, MatcherIdxs.FAVORITES);
        uriMatcher.addURI(CONTENT_AUTH, TableNames.MOVIES, MatcherIdxs.MOVIES);
        uriMatcher.addURI(CONTENT_AUTH, TableNames.GENRES, MatcherIdxs.GENRES);
        uriMatcher.addURI(CONTENT_AUTH, TableNames.TOPRATED, MatcherIdxs.TOPRATED);
        uriMatcher.addURI(CONTENT_AUTH, TableNames.POPULAR, MatcherIdxs.POPULAR);
        uriMatcher.addURI(CONTENT_AUTH, TableNames.VIDEOS, MatcherIdxs.VIDEOS);
        uriMatcher.addURI(CONTENT_AUTH, TableNames.REVIEWS, MatcherIdxs.REVIEWS);
        uriMatcher.addURI(CONTENT_AUTH, TableNames.GENRE_NAMES, MatcherIdxs.GENRE_NAMES);
        uriMatcher.addURI(CONTENT_AUTH, TableNames.META, MatcherIdxs.META);
    }

    public static PopularBuilder buildPopular() {
        return new PopularBuilder();
    }

    public static TopRatedBuilder buildTopRated() {
        return new TopRatedBuilder();
    }

    public static GenresBuilder buildGenres() {
        return new GenresBuilder();
    }

    public static MoviesBuilder buildMovies() {
        return new MoviesBuilder();
    }

    public static FavoritesBuilder buildFavorites() {
        return new FavoritesBuilder();
    }

    public static GenreNamesBuilder buildGenreNames() {
        return new GenreNamesBuilder();
    }

    public interface Meta {
        Uri URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TableNames.META).build();
        String[] PROJECTION = {
            COLS._ID, COLS.LAST_POP_PAGE, COLS.LAST_TR_PAGE,
            COLS.MAX_POP_PAGE, COLS.MAX_TR_PAGE,
        };

        interface COLS {
            String _ID = "_id";
            String LAST_POP_PAGE = "last_pop_page";
            String LAST_TR_PAGE = "last_tr_page";
            String MAX_POP_PAGE = "max_pop_page";
            String MAX_TR_PAGE = "max_tr_page";
        }

        interface IDX {
            int _ID = 0;
            int LAST_POP_PAGE = 1;
            int LAST_TR_PAGE = 2;
            int MAX_POP_PAGE = 3;
            int MAX_TR_PAGE = 4;
        }

    }

    /**
     * These interfaces just exists to collect the column names. I'd make them
     * final, but java doesn't let you do final interfaces.
     */
    public interface Movies {
        Uri URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TableNames.MOVIES).build();
        String[] PROJECTION = {
            COLS._ID, COLS.MID, COLS.TITLE, COLS.PLOT, COLS.POSTER_PATH,
            COLS.RELEASE_DATE, COLS.VOTE_COUNT, COLS.VOTE_AVERAGE,
        };

        interface COLS {
            String _ID = "_id";
            String MID = "mid";
            String TITLE = "title";
            String PLOT = "plot";
            String POSTER_PATH = "poster_path";
            String RELEASE_DATE = "release_date";
            String VOTE_COUNT = "vote_count";
            String VOTE_AVERAGE = "vote_average";
        }

        interface IDX {
            int _ID = 0;
            int MID = 1;
            int TITLE = 2;
            int PLOT = 3;
            int POSTER_PATH = 4;
            int RELEASE_DATE = 5;
            int VOTE_COUNT = 6;
            int VOTE_AVERAGE = 7;
        }
    }

    public interface Favorites extends Movies {
        Uri URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TableNames.FAVORITES).build();
        String[] PROJECTION = {
            COLS._ID, COLS.MID, COLS.TITLE, COLS.PLOT,
            COLS.POSTER_PATH, COLS.RELEASE_DATE,
            COLS.VOTE_COUNT, COLS.VOTE_AVERAGE, COLS.FAVORITE
        };

        interface COLS extends Movies.COLS {
            String FAVORITE = "favorite";
        }

        interface IDX extends Movies.IDX {
            int FAVORITE = 8;
        }
    }

    public interface TopRated extends Favorites {
        Uri URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TableNames.TOPRATED).build();
        String[] PROJECTION = {
            COLS._ID, COLS.MID, COLS.TITLE, COLS.PLOT,
            COLS.POSTER_PATH, COLS.RELEASE_DATE,
            COLS.VOTE_COUNT, COLS.VOTE_AVERAGE, COLS.FAVORITE,
            COLS.RANK, COLS.EXPIRES,
        };

        interface COLS extends Favorites.COLS {
            String RANK = "rank";
            String EXPIRES = "expires";
        }

        interface IDX extends Favorites.IDX {
            int RANK = 9;
            int EXPIRES = 10;
        }
    }

    public interface Popular extends TopRated {
        Uri URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TableNames.POPULAR).build();

        interface COLS extends TopRated.COLS {
        }

        interface IDX extends TopRated.IDX {
        }
    }

    public interface Reviews {
        Uri URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TableNames.REVIEWS).build();
        String[] PROJECTION = {
            COLS._ID,
            COLS.MID,
            COLS.ID,
            COLS.AUTHOR,
            COLS.CONTENT,
            COLS.URL,
        };

        interface COLS {
            String _ID = "_id";
            String MID = "mid";
            String ID = "id";
            String AUTHOR = "author";
            String CONTENT = "content";
            String URL = "url";
        }

        interface IDX {
            int _ID = 0;
            int MID = 1;
            int ID = 2;
            int AUTHOR = 3;
            int CONTENT = 4;
            int URL = 5;
        }

    }

    public interface Videos {
        Uri URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TableNames.VIDEOS).build();
        String[] PROJECTION = {
            COLS._ID, COLS.MID, COLS.ID, COLS.LANG,
            COLS.NAME, COLS.SITE, COLS.KEY, COLS.SIZE,
        };

        interface COLS {
            String _ID = "_id";
            String MID = "mid";
            String ID = "id";
            String LANG = "iso_639_1";
            String NAME = "name";
            String SITE = "site";
            String KEY = "key";
            String SIZE = "size";
        }

        interface IDX {
            int _ID = 0;
            int MID = 1;
            int ID = 2;
            int LANG = 3;
            int NAME = 4;
            int SITE = 5;
            int KEY = 6;
            int SIZE = 7;
        }
    }

    public interface Genres {
        Uri URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TableNames.GENRES).build();
        String[] PROJECTION = {
            COLS._ID, COLS.MID, COLS.GID,
        };

        interface COLS {
            String _ID = "_id";
            String MID = "mid";
            String GID = "gid";
        }

        interface IDX {
            int _ID = 0;
            int MID = 1;
            int GID = 2;
        }
    }

    public interface GenreNames {
        Uri URI =
            BASE_CONTENT_URI.buildUpon().appendPath(TableNames.GENRE_NAMES).build();
        String[] PROJECTION = {
            COLS._ID, COLS.GID, COLS.NAME,
        };

        interface COLS {
            String _ID = "_id";
            String GID = "gid";
            String NAME = "name";
        }

        interface IDX {
            int _ID = 0;
            int GID = 1;
            int NAME = 2;
        }
    }

    /**
     * Integers for matches returned by uriMatcher.
     */
    interface MatcherIdxs {
        int MOVIES = 1;
        int FAVORITES = 2;
        int GENRES = 3;
        int TOPRATED = 4;
        int POPULAR = 5;
        int VIDEOS = 6;
        int REVIEWS = 7;
        int GENRE_NAMES = 8;
        int META = 9;
    }

    /**
     * List of table names in the db.
     */
    interface TableNames {
        String MOVIES = "movies";
        String FAVORITES = "favorites";
        String GENRES = "genres";
        String TOPRATED = "toprated";
        String POPULAR = "popular";
        String VIDEOS = "videos";
        String REVIEWS = "reviews";
        String GENRE_NAMES = "genre_names";
        String META = "meta";
    }

    static class Builder {
        ContentValues mValues;

        Builder() {
            mValues = new ContentValues();
        }

        public ContentValues build() {
            return mValues;
        }
    }

    public static class MoviesBuilder extends Builder {
        public MoviesBuilder putMid(int mid) {
            mValues.put(Movies.COLS.MID, mid);
            return this;
        }

        public MoviesBuilder putTitle(String title) {
            mValues.put(Movies.COLS.TITLE, title);
            return this;
        }

        public MoviesBuilder putPlot(String plot) {
            mValues.put(Movies.COLS.PLOT, plot);
            return this;
        }

        public MoviesBuilder putPosterPath(String posterPath) {
            mValues.put(Movies.COLS.POSTER_PATH, posterPath);
            return this;
        }

        public MoviesBuilder putReleaseDate(String releaseDate) {
            mValues.put(Movies.COLS.RELEASE_DATE, releaseDate);
            return this;
        }

        public MoviesBuilder putVoteCount(int voteCount) {
            mValues.put(Movies.COLS.VOTE_COUNT, voteCount);
            return this;
        }

        public MoviesBuilder putVoteAverage(float voteAverage) {
            mValues.put(Movies.COLS.VOTE_AVERAGE, voteAverage);
            return this;
        }
    }

    public static class FavoritesBuilder extends Builder {
        public FavoritesBuilder putMid(int mid) {
            mValues.put(Favorites.COLS.MID, mid);
            return this;
        }
    }

    public static class GenresBuilder extends Builder {
        public GenresBuilder putMid(int mid) {
            mValues.put(Genres.COLS.MID, mid);
            return this;
        }

        public GenresBuilder putGid(int gid) {
            mValues.put(Genres.COLS.GID, gid);
            return this;
        }

    }

    public static class TopRatedBuilder extends Builder {
        public TopRatedBuilder putMid(int mid) {
            mValues.put(TopRated.COLS.MID, mid);
            return this;
        }

        public TopRatedBuilder putRank(int rank) {
            mValues.put(TopRated.COLS.RANK, rank);
            return this;
        }

        public TopRatedBuilder putExpires(long expires) {
            mValues.put(TopRated.COLS.EXPIRES, expires);
            return this;
        }
    }

    public static class PopularBuilder extends TopRatedBuilder {

    }

    public static class GenreNamesBuilder extends Builder {
        public GenreNamesBuilder putGid(int gid) {
            mValues.put(GenreNames.COLS.GID, gid);
            return this;
        }

        public GenreNamesBuilder putName(String name) {
            mValues.put(GenreNames.COLS.NAME, name);
            return this;
        }
    }
}
