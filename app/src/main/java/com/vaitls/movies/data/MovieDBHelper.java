package com.vaitls.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by evaitl on 8/16/16.
 * <p>
 * TODO: Clean this up to use a contract.
 */
class MovieDBHelper extends SQLiteOpenHelper {
    private static final String TAG = MovieDBHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 2;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "cleaning tables");
        if (oldVersion != newVersion) {
            db.execSQL("drop table if exists movies;");
            db.execSQL("drop table if exists toprated;");
            db.execSQL("drop table if exists reviews;");
            db.execSQL("drop table if exists videos;");
            db.execSQL("drop table if exists favorites;");
            onCreate(db);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "creating tables");
        db.execSQL(
                "create table movies(" +
                        "_id integer primary key autoincrement, " +
                        "mid integer not null unique on conflict replace, " +
                        "title text not null, " +
                        "plot text not null, " +
                        "poster_path text not null," +
                        "release_date text not null," +
                        "vote_count integer not null," +
                        "vote_average real not null);");
/*
I'm not setting the ranks in the lists as unique because data will be fetched
periodically and we won't ever fetch the whole database in a single swell foop.

Sometimes we will have more than one movie in the local cache with the same rank
until we get around to updating our local data to fix it. NBD.
 */
        db.execSQL(
                "create table popular(" +
                        "_id integer primary key autoincrement, " +
                        "rank integer not null, " +
                        "mid integer not null unique on conflict replace," +
                        "expires integer not null);");
        db.execSQL(
                "create table toprated(" +
                        "_id integer primary key autoincrement," +
                        "rank integer not null," +
                        "mid integer not null unique on conflict replace," +
                        "expires integer not null);");

/*  Mehhh....We aren't displaying these, so why save them?

                        "create table genres("+
                        "_id integer primary key autoincrement,"+
                        "mid integer not null,"+
                        "gid integer not null);"+
*/

/*
Let's not cache these.
                        "create table reviews("+
                        "_id integer primary key autoincrement,"+
                        "mid integer not null,"+
                        "id text,"+
                        "author text,"+
                        "content text not null,"+
                        "url text"+
                        ");"+

                        "create table videos("+
                        "_id integer primary key autoincrement,"+
                        "mid integer not null,"+
                        "id text not null,"+
                        "iso_639_1 text not null,"+
                        "name text not null,"+
                        "site text not null,"+
                        "key text not null"+
                        ");"+
*/
/*
 * Creating a single row of meta information about the db to
  * keep track of what needs to be fetch next;
 */
        db.execSQL(
                "create table meta(" +
                        "_id integer primary key autoincrement, " +
                        "single integer default 0 unique on conflict replace check (single=0), " +
                        "last_tr_page integer default 0, " +
                        "max_tr_page integer, " +
                        "last_pop_page integer default 0, " +
                        "max_pop_page integer " +
                        ");");
        db.execSQL("create table genre_names("+
                "_id integer primary key autoincrement, "+
                "id integer unique not null, "+
                "name text not null"+
                ");"
        );
        db.execSQL(
                "create table favorites(" +
                        "_id integer primary key autoincrement," +
                        "mid integer not null unique on conflict ignore," +
// OK. This is stupid, but it makes my life easier.
                        "favorite integer not null default 1 check (favorite=1)" +
                        ");");

    }
}
