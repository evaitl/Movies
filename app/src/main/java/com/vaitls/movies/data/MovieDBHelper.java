package com.vaitls.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by evaitl on 8/16/16.
 * <p/>
 * TODO: Clean this up to use a contract.
 */
class MovieDBHelper extends SQLiteOpenHelper {
    private static final String TAG = MovieDBHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String CLEAN_TABLES =
                "drop table if exists movies;" +
                        "drop table if exists popular;" +
                        "drop table if exists toprated;" +
                        //"drop table if exists genres;"+
                        "drop table if exists reviews;"+
                        "drop table if exists videos;"+
                        "drop table if exists favorites;" +
                        "";
        if(oldVersion!=newVersion) {
            db.execSQL(CLEAN_TABLES);
            onCreate(db);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TABLES =
                "create table movies(" +
                        "_id integer primary key autoincrement, " +
                        "mid integer not null unique on conflict replace, " +
                        "title text not null, " +
                        "plot text not null, " +
                        "poster_path text not null,"+
                        "release_date text not null,"+
                        "overview text not null,"+
                        "vote_average real not null);" +

                        "create table popular(" +
                        "_id integer primary key autoincrement, " +
                        "rank integer not null, " +
                        "mid integer not null unique on conflict replace," +
                        "expires integer not null);" +

                        "create table toprated(" +
                        "_id integer primary key autoincrement," +
                        "rank integer not null," +
                        "mid integer not null unique on conflict replace," +
                        "expires integer not null);" +
/*

                        "create table genres("+
                        "_id integer primary key autoincrement,"+
                        "mid integer not null,"+
                        "gid integer not null);"+
*/

                        "create table reviews("+
                        "_id integer primary key autoincrement,"+
                        "mid integer not null,"+
                        "id text,"+
                        "author text,"+
                        "content text not null,"+
                        "url text,"+
                        ");"+

                        "create table videos("+
                        "_id integer primary key autoincrement,"+
                        "mid integer not null,"+
                        "id text not null,"+
                        "iso_639_1 text not null,"+
                        "name text not null,"+
                        "site text not null,"+
                        "key text not null,"+
                        ");"+

                        "create table favorites(" +
                        "_id integer primary key autoincrement," +
                        "mid integer not null unique on conflict ignore);" +

                        "";
        db.execSQL(SQL_CREATE_TABLES);
    }
}
