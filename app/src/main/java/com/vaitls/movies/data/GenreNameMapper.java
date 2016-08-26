package com.vaitls.movies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.vaitls.movies.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by evaitl on 8/24/16.
 * <p/>
 * <p/>
 * We aren't supposed to do networking or grab a cursor in the UI thread,
 * so this does both in the background. Try to get the genre names
 * from the db. If there aren't any, fetch them from web (should only
 * happen once per install). If we fail, try MAX_TRIES times before
 * giving up with a little bit of a backoff.
 * <p/>
 * After the db is initalized, this just loads the db and is called to
 * map from genre IDs to genre names.
 * <p/>
 * TODO: refresh the genre names table based on the Expires header.
 */
public final class GenreNameMapper {
    private static final String TAG = GenreNameMapper.class.getSimpleName();
    private static Map<Integer, String> sNames = new HashMap<>();

    // Don't make these.
    private GenreNameMapper() {
    }

    public static void loadGenres(Context context) {
        new GenreLoader(context).execute();
    }

    public static String lookup(Integer a) {
        String value = sNames.get(a);
        if (value == null) {
            value = a.toString();
        }
        return value;
    }

    public static String map(String ids) {
        String[] tokens = ids.split("[^0-9]");
        StringBuilder sb = new StringBuilder(35);
        boolean first = true;
        for (String s : tokens) {
            if (s.equals("")) {
                continue;
            }
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(lookup(Integer.parseInt(s)));
        }
        return sb.toString();
    }

    private interface GenreApi {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        @GET("genre/movie/list")
        Call<GenreNames> getGenreNames(@Query("api_key") String sApiKey);
    }

    private static class GenreNames {
        GenreName[] genres;

        private GenreNames() {
        }

        public GenreName[] getGenres() {
            return genres;
        }
    }

    private static class GenreName {
        private int id;
        private String name;

        private GenreName() {
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static class GenreLoader extends AsyncTask<Void, Void, Cursor> {
        private static final int MAX_TRIES = 4;
        private static int tries = 0;
        private Context mContext;
        private String sApiKey;

        GenreLoader(Context context) {
            mContext = context;
            sApiKey = context.getString(R.string.themoviedb_key);
        }

        /**
         * Returns null if either the cursor is null or has no data.
         *
         * @return a genreNames cursor or null
         */
        Cursor getGenreNamesCursor() {
            Cursor cursor = mContext.getContentResolver().query(Contract.GenreNames.URI,
                                                                Contract.GenreNames.PROJECTION,
                                                                null, null, null);
            if (cursor == null) {
                return null;
            }
            if (cursor.getCount() == 0) {
                cursor.close();
                return null;
            }
            return cursor;
        }

        /**
         * Cache the names in the local db.
         *
         * @param names set of names/gids from the web
         */
        void saveData(GenreName[] names) {
            ContentValues[] contentValues = new ContentValues[names.length];
            int i = 0;
            for (GenreName name : names) {
                contentValues[i++] =
                    Contract.buildGenreNames()
                        .putName(name.getName())
                        .putGid(name.getId())
                        .build();
            }
            mContext.getContentResolver().bulkInsert(Contract.GenreNames.URI, contentValues);
        }

        /**
         * @return A cursor with data in the database or null
         */
        @Override
        protected Cursor doInBackground(Void... params) {
            Cursor cursor = getGenreNamesCursor();
            if (cursor != null && cursor.getCount() != 0) {
                return cursor;
            }
            Response<GenreNames> rp = null;
            try {
                rp =
                    GenreApi.retrofit.create(GenreApi.class)
                        .getGenreNames(sApiKey).execute();
            } catch (IOException e) {
                Log.e(TAG, "IOException in retrofit");
            }
            if (rp != null && rp.isSuccessful()) {
                saveData(rp.body().getGenres());
                return getGenreNamesCursor();
            }
            if (tries++ < MAX_TRIES) {
                new Timer("fetch genre names").schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            loadGenres(mContext);
                        }
                    }
                    , 1000 * tries);
            }
            return null;
        }

        /**
         * We are back in teh UI thread and either have null or a cursor with data.
         * Save the gid/name in the local map for lookups.
         *
         * @param cursor
         */
        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor == null) {
                Log.e(TAG, "Genre names load failed");
                return;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Integer gid = cursor.getInt(Contract.GenreNames.IDX.GID);
                String name = cursor.getString(Contract.GenreNames.IDX.NAME);
                sNames.put(gid, name);
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

}
