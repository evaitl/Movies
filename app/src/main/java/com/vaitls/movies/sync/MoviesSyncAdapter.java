package com.vaitls.movies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.vaitls.movies.MovieListType;
import com.vaitls.movies.R;
import com.vaitls.movies.data.Contract;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static com.vaitls.movies.data.Contract.Movies;
import static com.vaitls.movies.data.Contract.TopRated;
import static com.vaitls.movies.data.Contract.Meta;
import static java.lang.Math.min;


/**
 * Created by evaitl on 8/17/16.
 * <p/>
 * Gets the popular and toprated lists from themoviedb.org using the api described
 * <a href="http://docs.themoviedb.apiary.io/">here</a>.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = MoviesSyncAdapter.class.getSimpleName();
    private static final int PRECACHE_PAGES = 10;
    // TODO 100
    private static final int MAX_PAGE = 10;
    // Every 6 hours.
    private static final int SYNC_INTERVAL = 60 * 60 * 6;
    // More or less....
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 2;
    private static String sApiKey;
    private ContentResolver mContentResolver;
    private int lastPopPage;
    private int lastTrPage;
    private int maxPopPage;
    private int maxTrPage;

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.d(TAG,"constructor");
        mContentResolver = context.getContentResolver();
    }

    public MoviesSyncAdapter(Context context, boolean autoInitialize,
                             boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Log.d(TAG,"construrcture 2");
        mContentResolver = context.getContentResolver();
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(TAG,"syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    private static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    private static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        SyncRequest request = new SyncRequest.Builder().
                syncPeriodic(syncInterval, flexTime).
                setSyncAdapter(account, authority).
                setExtras(new Bundle()).build();
        ContentResolver.requestSync(request);
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        sApiKey = context.getString(R.string.themoviedb_key);
        getSyncAccount(context);
    }

    private void getDbMeta() {
        Cursor meta = mContentResolver.query(Meta.URI,
                                             Meta.PROJECTION, null, null, null);
        if (meta != null && meta.getCount()>0){
            meta.moveToFirst();
            lastPopPage = meta.getInt(Meta.IDX.LAST_POP_PAGE);
            lastTrPage = meta.getInt(Meta.IDX.LAST_TR_PAGE);
            maxPopPage = meta.getInt(Meta.IDX.MAX_POP_PAGE);
            maxTrPage = meta.getInt(Meta.IDX.MAX_TR_PAGE);
        }else{
            maxPopPage=maxTrPage=MAX_PAGE;
        }
        /*
        TODO: put max cached in settings. These are about 2000.
         */
        maxTrPage = min(maxPopPage, MAX_PAGE);
        maxPopPage = min(maxPopPage, MAX_PAGE);
    }

    private void writeDbMeta() {
        ContentValues metaValues = Contract.buildMeta()
            .putLastPopPage(lastPopPage)
            .putLastTRPage(lastTrPage)
            .putMaxPopPage(maxPopPage)
            .putMaxTRPage(maxTrPage)
            .build();
        mContentResolver.insert(Contract.Meta.URI, metaValues);
    }

    private void saveMi(MovieInfo mi, MovieListType type, long expires, int rank) {
        if(!mi.isGoodData()){
            /*
            Apparently some of the movie info in the lists is
            incomplete. This causes SQLiteConstraintExceptions.
             */
            Log.i(TAG,"skipping movie store: "+mi.getTitle());
            return;
        }

        ContentValues movieValues=Contract.buildMovies()
            .putMid(mi.getId())
            .putPlot(mi.getOverview())
            .putPosterPath(mi.getPoster_path())
            .putReleaseDate(mi.getRelease_date())
            .putTitle(mi.getTitle())
            .putVoteAverage(mi.getVote_average())
            .putVoteCount(mi.getVote_count())
            .putGenres(Arrays.toString(mi.getGenre_ids()))
            .build();

        mContentResolver.insert(Movies.URI,movieValues);

        // Counts on TR and Pop lists being the same in Contract.
        ContentValues listValues =  Contract.buildTopRated()
            .putExpires(expires)
            .putMid(mi.getId())
            .putRank(rank)
            .build();

        Uri uri;
        if (type == MovieListType.POPULAR) {
            uri = Contract.Popular.URI;
        } else {
            uri = TopRated.URI;
        }
        mContentResolver.insert(uri, listValues);
    }

    /**
     * For now, we are prefetching from the top-rated and popular lists.
     * I don't want to over-do it, so we will prefetch 20 pages or so from each
     * list (about 400) total movies from each list.
     * <p/>
     * If this happens every 6 hours, we will have 2000+ (overlaps between the two lists)
     * cached in a day or so.
     * <p/>
     * Some design choices. I've decided to not cache images to save space. Glide will do
     * some caching on the fly. I've also decided to not cache reviews and trailers. These
     * require api calls for each movie, so would hammer the server. Unless we
     * download the data, the URL's won't help if we are off line.
     * <p/>
     * I'm using http expires for the db to (eventually) decide what to prune from the db.
     * Technically, I should have max-age override expires, .... Not getting into that.
     * Our db will be a bit behind anyhow, so no reason to get picky.
     */
    private void prefetch() {
        Log.d(TAG,"prefetching...");
        try {
            int nextPopPage = lastPopPage % maxPopPage + 1;
            for (int i = 0; i < PRECACHE_PAGES; ++i) {

                if (nextPopPage == 0) {
                    nextPopPage = 1;
                }
                Log.d(TAG,"pop page "+nextPopPage);
                Response<MoviePage> rp =
                        MovieApi.retrofit.create(MovieApi.class)
                                .getPage(MovieListType.POPULAR.toString(), sApiKey, nextPopPage)
                                .execute();
                if (!rp.isSuccessful()) {
                    Log.i(TAG, "Failed fetching pop page " + nextPopPage);
                    break;
                }
                long expires = 0;
                Date d = rp.headers().getDate("expires");
                if (d == null) {
                    expires = (new Date()).getTime() / 1000 + 24 * 60 * 60;
                } else {
                    expires = d.getTime() / 1000;
                }
                MoviePage moviePage = rp.body();
                maxPopPage = min(moviePage.getTotal_pages(), MAX_PAGE);
                // Assumes each page has the same number of movies.
                int rank = 1 + (moviePage.getTotal_results() * (nextPopPage - 1));
                for (MovieInfo mi : moviePage.getResults()) {
                    saveMi(mi, MovieListType.POPULAR, expires, rank++);
                }
                lastPopPage = nextPopPage++;
            }
            int nextTRPage = lastTrPage % maxTrPage + 1;
            for (int i = 0; i < PRECACHE_PAGES; ++i) {
                if (nextTRPage == 0) {
                    nextTRPage = 1;
                }
                Log.d(TAG,"tr page "+ nextTRPage);
                Response<MoviePage> rp =
                        MovieApi.retrofit.create(MovieApi.class)
                                .getPage(MovieListType.TOPRATED.toString(), sApiKey, nextTRPage)
                                .execute();
                if (!rp.isSuccessful()) {
                    Log.i(TAG, "Failed fetching TR page " + nextTRPage);
                    break;
                }
                long expires = 0;
                Date d = rp.headers().getDate("expires");
                if (d == null) {
                    expires = (new Date()).getTime() / 1000 + 24 * 60 * 60;
                } else {
                    expires = d.getTime() / 1000;
                }
                MoviePage moviePage = rp.body();
                maxTrPage = min(moviePage.getTotal_pages(), MAX_PAGE);
                // Assumes each page has the same number of movies.
                int rank = 1 + (moviePage.getTotal_results() * (nextTRPage - 1));
                for (MovieInfo mi : moviePage.getResults()) {
                    saveMi(mi, MovieListType.TOPRATED, expires, rank++);
                }
                lastTrPage = nextTRPage++;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error in prefetch", e);
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Starting network sync...");
        if (!checkNetwork()) {
            Log.e(TAG, "Can't connect. Bailing on sync");
        }
        getDbMeta();
        prefetch();
        writeDbMeta();
        Log.i(TAG, "Done with sync.");
    }

    /**
     * I think in a real product, we would skip fetching if we are just
     * on the cell network for periodic updates.
     *
     * Perhaps use (activeNetworkInfo.getType() == NetworkInfo.TYPE_WIFI).
     * @return true if we can get to server.
     */
    private boolean checkNetwork() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            Log.d(TAG,"h3");
            return false;
        }
     /*   Log.d(TAG,"h1");
        try {
            return InetAddress.getByName("themoviedb.org").isReachable(1000);
        } catch (IOException e) {
            Log.e(TAG, "server not reachable", e);
        }
        Log.d(TAG,"h2");*/
        return true;
    }

    /**
     * Using retrofit for the actual data transfers and json decoding.
     */
    private interface MovieApi {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/3/movie/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        @GET("{type}")
        Call<MoviePage> getPage(@Path("type") String type,
                                @Query("api_key") String apiKey,
                                @Query("page") int page);
    }
}
