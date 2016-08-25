package com.vaitls.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by evaitl on 8/16/16.
 * <p/>
 * TODO: Clean this up to use a contract.
 */
class MovieDBHelper extends SQLiteOpenHelper {
    private static final String TAG = MovieDBHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 856;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "cleaning tables");

        if (oldVersion != newVersion) {
            db.execSQL("drop table if exists movies;");
            db.execSQL("drop table if exists favorites;");
            db.execSQL("drop table if exists genres;");
            db.execSQL("drop table if exists toprated;");
            db.execSQL("drop table if exists popular;");
            db.execSQL("drop table if exists videos;");
            db.execSQL("drop table if exists reviews;");
            db.execSQL("drop table if exists genre_names;");
            db.execSQL("drop table if exists meta;");
            onCreate(db);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "creating tables");
        // Get this as a side effect of fetching the popular and top_rated lists.
        // Place in a separate table.
        db.execSQL(
            "create table movies(" +
                "_id integer primary key autoincrement, " +
                "mid integer not null unique on conflict replace, " +
                "title text not null, " +
                "plot text not null, " +
                "poster_path text not null," +
                "release_date text not null," +
                "vote_count integer not null," +
                "vote_average real not null,"+

                "genres text not null"+
                ");");

        db.execSQL(
            "create table favorites(" +
                "_id integer primary key autoincrement," +
                "mid integer not null unique on conflict replace," +
                "favorite integer not null default 0" +
                ");");

/**
I feel bad about this. By all SQL theory, we should have a genres table and do a join,
but it is just too much work for the payback. Instead I'm putting the genre ids
in a text column in the movies table. When we look at a movies row,
 we'll convert the ids to strings from the genre_names table.

        // genre/movie/list
        db.execSQL("create table genres(" +
                       "_id integer primary key autoincrement," +
                       "mid integer not null," +
                       "gid integer not null);");
*/

        // movie/top_rated?page=XXX
        db.execSQL(
            "create table toprated(" +
                "_id integer primary key autoincrement," +
                "rank integer not null," +
                "mid integer not null unique on conflict replace," +
                "expires integer not null);");

        /*
I'm not setting the ranks in the lists as unique because data will be fetched
periodically and we won't ever fetch the whole database in a single swell foop.

Sometimes we will have more than one movie in the local cache with the same rank
until we get around to updating our local data to fix it. IMHO is better to have a
popular/toprated movie off a few places on the list than knocked off the list.
NBD.
 */
        // movie/popular?page=XXX
        db.execSQL(
            "create table popular(" +
                "_id integer primary key autoincrement, " +
                "rank integer not null, " +
                "mid integer not null unique on conflict replace," +
                "expires integer not null);");

        // movie/<mid>/videos
        db.execSQL("create table videos(" +
                       "_id integer primary key autoincrement," +
                       "mid integer not null," +
                       "id text not null," +
                       "iso_639_1 text not null," +
                       "name text not null," +
                       "site text not null," +
                       "key text not null," +
                       "size integer not null" +
                       ");");
        // movie/<mid>/reviews
        db.execSQL("create table reviews(" +
                       "_id integer primary key autoincrement," +
                       "mid integer not null," +
                       "id text not null," +
                       "author text not null," +
                       "content text not null," +
                       "url text not null" +
                       ");");

        // genre/movie/list?api_key=XXX
        db.execSQL("create table genre_names(" +
                       "_id integer primary key autoincrement, " +
                       "gid integer unique not null on conflict replace, " +
                       "name text not null" +
                       ");"
        );

    /*
     * Creating a single row of meta information about the db to
     * keep track of what needs to be fetched next.
     *
     * The 'single' column is a hack to make sure we just have one row.
     */
        db.execSQL(
            "create table meta(" +
                "_id integer primary key autoincrement, " +
                "single integer default 0 unique on conflict replace check (single=0), " +
                "last_tr_page integer not null, " +
                "max_tr_page integer not null, " +
                "last_pop_page integer not null, " +
                "max_pop_page integer not null" +
                ");");



    }
}
