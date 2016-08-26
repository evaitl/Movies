package com.vaitls.movies.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.vaitls.movies.R;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static com.vaitls.movies.data.Contract.Videos;

/**
 * Created by evaitl on 8/25/16.
 * <p/>
 * This thing will try to fetch and cache any trailers associated with movie mid.
 * If they are already cached, skip the fetch.
 * <p/>
 * When it has something (or nothing), it will call a callback with a cursor (or maybe null).
 * Shit happens.
 */
public final class TrailerLoader {
    private static final String TAG = TrailerLoader.class.getSimpleName();
    private final int mMid;
    private final Context mContext;
    private TrailerCallback mCallback;
    private String mApiKey;

    public TrailerLoader(final Context context, final int mid, final TrailerCallback callback) {
        mCallback = callback;
        mMid = mid;
        mContext = context;
        mApiKey = context.getString(R.string.themoviedb_key);


        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                ContentResolver resolver = context.getContentResolver();
                String[] selectionArgs = new String[1];
                selectionArgs[0] = Integer.toString(mid);
                Cursor c = resolver.query(Videos.URI, Videos.PROJECTION, "mid = ?", selectionArgs,
                                          null);
                if (c != null && c.getCount() != 0) {
                    return c;
                }
                if (c != null) {
                    c.close();
                    ;
                }
                Response<Trailers> rp = null;
                try {
                    rp = TrailersApi.retrofit.create(TrailersApi.class)
                        .getTrailers(mMid, mApiKey).execute();
                } catch (IOException e) {
                    Log.e(TAG, "Error loading trailers: " + mid, e);
                }
                if (rp == null) {
                    return null;
                }
                ContentValues[] cv = new ContentValues[rp.body().getResults().length];
                int i = 0;
                for (Trailer t : rp.body().getResults()) {
                    cv[i++] = Contract.buildVideo()
                        .putMid(mid)
                        .putKey(t.getKey())
                        .putLang(t.getIso_639_1())
                        .putName(t.getName())
                        .putSite(t.getSite())
                        .putSize(t.getSize())
                        .putVid(t.getId())
                        .build();
                }
                resolver.bulkInsert(Videos.URI, cv);
                return resolver.query(Videos.URI, Videos.PROJECTION, "mid = ?", selectionArgs,
                                      null);
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                if (mCallback != null) {
                    mCallback.onTrailersLoaded(cursor);
                }
            }
        }.execute();
    }

    /**
     * We will complete the fetch and cache the data, but just
     * won't do the callback on a cancel.
     */
    public void cancel() {
        mCallback = null;
    }

    public interface TrailerCallback {
        void onTrailersLoaded(Cursor cursor);
    }

    private interface TrailersApi {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        @GET("movie/{id}/videos")
        Call<Trailers> getTrailers(@Path("id") int mid,
                                   @Query("api_key") String apiKey);
    }

    private class Trailer {
        private String id;
        private String iso_639_1;
        private String key;
        private String name;
        private String site;
        private int size;
        private String type;

        private Trailer() {
        }

        String getId() {
            return id;
        }

        String getIso_639_1() {
            return iso_639_1;
        }

        String getKey() {
            return key;
        }

        String getName() {
            return name;
        }

        String getSite() {
            return site;
        }

        int getSize() {
            return size;
        }

        String getType() {
            return type;
        }
    }

    private class Trailers {
        private int id;
        private Trailer[] results;

        private Trailers() {
        }

        int getId() {
            return id;
        }

        Trailer[] getResults() {
            return results;
        }
    }

}
