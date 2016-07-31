package com.vaitls.movies;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.os.AsyncTask;
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
    static MoviedbFetcher sMoviedbFetcher;
    private String mApiKey;
    private MovieDataCache mMovieDataCache;

    private MoviedbFetcher(MovieDataCache mdc,String apiKey) {
        mMovieDataCache = mdc;
        mApiKey =apiKey;
    }

    public static MoviedbFetcher getInstance(MovieDataCache mdc, String apiKey) {
        if (sMoviedbFetcher == null) {
            assert mdc != null;
            sMoviedbFetcher = new MoviedbFetcher(mdc,apiKey);
        }
        return sMoviedbFetcher;
    }

    private String getUrlString(String urlString) throws IOException {
        return new String(getUrlBytes(urlString));
    }

    private byte[] getUrlBytes(String urlSpec) throws IOException {
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

    void fetchPage(MovieListType t, int pageNumber) {
        String url =
                Uri.parse("http://api.themoviedb.org/3/movie")
                        .buildUpon()
                        .appendPath(t.toString())
                        .appendQueryParameter("page", pageNumber + "")
                        .appendQueryParameter("api_key", mApiKey)
                        .build().toString();
        PageConsumer c;
        if (t == MovieListType.POPULAR) {
            c = new PageConsumer() {
                @Override
                public void accept(MoviePage mp) {
                    mMovieDataCache.updatePopular(mp);
                }
            };
        } else {
            c = new PageConsumer() {
                @Override
                public void accept(MoviePage mp) {
                    mMovieDataCache.updateTopRated(mp);
                }
            };
        }
        new DownloadPageTask().execute(new DownloadInfo(url, c));
    }

    class DownloadInfo {
        String url;
        PageConsumer c;
        DownloadInfo(String url, PageConsumer c) {
            this.url = url;
            this.c = c;
        }
    }

    private class DownloadPageTask extends AsyncTask<DownloadInfo, Void, MoviePage> {
        DownloadInfo mDownloadInfo;

        @Override
        protected MoviePage doInBackground(DownloadInfo... downloadInfos) {
            mDownloadInfo = downloadInfos[0];
            try {
                String jsonString = getUrlString(mDownloadInfo.url);
                Gson gson = (new GsonBuilder()).create();
                MoviePage mp = gson.fromJson(jsonString, MoviePage.class);
                return mp;
            } catch (IOException e) {
                Log.e(TAG, "Failed to fetch: ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(MoviePage moviePage) {
                mDownloadInfo.c.accept(moviePage);

        }
    }

}
