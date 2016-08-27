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

import static com.vaitls.movies.data.Contract.Reviews;

/**
 * Created by evaitl on 8/26/16.
 * <p/>
 * Just like the trailers loader. Register a callback when creating.
 */
public final class ReviewLoader {
    static private final String TAG = ReviewLoader.class.getSimpleName();
    private ReviewCallback mCallback;
    private String mApiKey;

    public ReviewLoader(final Context context, final int mid, final ReviewCallback callback) {
        mApiKey = context.getString(R.string.themoviedb_key);
        mCallback = callback;
        Log.d(TAG, "creating review loader");
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                Log.d(TAG, "dib");
                ContentResolver resolver = context.getContentResolver();
                String[] selectionArgs = new String[1];
                selectionArgs[0] = Integer.toString(mid);
                Cursor c = resolver.query(Reviews.URI, Reviews.PROJECTION, "mid=?", selectionArgs,
                                          null);
                if (c != null && c.getCount() != 0) {
                    return c;
                }
                if (c != null) {
                    c.close();
                }
                Response<TMDReviews> rp = null;
                try {
                    rp = ReviewsApi.retrofit.create(ReviewsApi.class)
                        .getReviews(mid, mApiKey).execute();
                } catch (IOException e) {
                    Log.e(TAG, "Error loading reviews " + mid, e);
                }

                if (rp == null) {
                    Log.e(TAG, "reviews are null");
                    return null;
                }
                ContentValues[] cv = new ContentValues[rp.body().getResults().length];
                if(cv.length==0){
                    Log.e(TAG,"no reviews of "+mid);
                    return null;
                }
                int i = 0;
                for (TMDReview r : rp.body().getResults()) {
                    cv[i++] = Contract.buildReview()
                        .putMid(mid)
                        .putAuthor(r.getAuthor())
                        .putContent(r.getContent())
                        .putRid(r.getId())
                        .putUrl(r.getURL())
                        .build();
                }
                resolver.bulkInsert(Reviews.URI, cv);
                return resolver.query(Reviews.URI, Reviews.PROJECTION,
                                      "mid=?", selectionArgs, null);
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                Log.d(TAG, "ope");
                if (mCallback != null) {
                    mCallback.onReviewsLoaded(cursor);
                }
            }
        }.execute();
    }

    public void cancel() {
        mCallback = null;
    }

    public interface ReviewCallback {
        void onReviewsLoaded(Cursor cursor);
    }

    private interface ReviewsApi {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        @GET("movie/{id}/reviews")
        Call<TMDReviews> getReviews(@Path("id") int mid,
                                    @Query("api_key") String apiKey);
    }

    private class TMDReview {
        private String id;
        private String author;
        private String content;
        private String URL;

        private TMDReview() {
        }

        public String getAuthor() {
            return author;
        }

        public String getContent() {
            return content;
        }

        public String getId() {
            return id;
        }

        public String getURL() {
            if (URL == null) {
                Log.i(TAG, "null URL in review " + id);
                return "";
            }
            return URL;
        }
    }

    private class TMDReviews {
        private int id;
        private int page;
        private TMDReview[] results;

        private TMDReviews() {
        }

        public int getId() {
            return id;
        }

        public int getPage() {
            return page;
        }

        public TMDReview[] getResults() {
            return results;
        }
    }
}
