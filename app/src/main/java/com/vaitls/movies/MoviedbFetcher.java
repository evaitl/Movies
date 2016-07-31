package com.vaitls.movies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by evaitl on 7/30/16.
 */
public class MoviedbFetcher {
    private final static String TAG = MoviedbFetcher.class.getSimpleName();

    ;
    static MoviedbFetcher sMoviedbFetcher;
    private String mApiKey;

    private MoviedbFetcher(String apiKey) {
        mApiKey = apiKey;
    }

    public static MoviedbFetcher getInstance(String apiKey) {
        if (sMoviedbFetcher == null && apiKey != null) {
            sMoviedbFetcher = new MoviedbFetcher(apiKey);
        }
        return sMoviedbFetcher;
    }



    public void fetchItems() {
        try {
            String url = Uri.parse("http://api.themoviedb.org/3/movie")
                    .buildUpon()
                    .appendPath("top_rated")
                    // .appendPath("popular")
                    .appendQueryParameter("api_key", mApiKey)
                    .build().toString();
            Log.i(TAG, "URL: " + url);
            String jsonString = getUrlString(url);
            Log.i(TAG, "JSON: " + jsonString);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch: ", e);
        }
    }

    public MoviePage fetchPage(MovieListType mlt, int pageNo) {
        try {
            String url = Uri.parse("http://api.themoviedb.org/3/movie")
                    .buildUpon()
                    .appendPath(mlt.toString())
                    .appendQueryParameter("api_key", mApiKey)
                    .build().toString();
            String jsonString = getUrlString(url);
            Gson gson = (new GsonBuilder()).create();
            MoviePage mp = gson.fromJson(jsonString, MoviePage.class);
            return mp;
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch: ", e);
        }
        return null;
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1500];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public enum MovieListType {
        POPULAR("popular"),
        TOP_RATED("top_rated");
        private final String mString;

        private MovieListType(String name) {
            mString = name;
        }

        @Override
        public String toString() {
            return mString;
        }
    }
}
