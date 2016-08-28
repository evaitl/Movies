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

import static com.vaitls.movies.data.Contract.Meta;
import static com.vaitls.movies.data.Contract.TopRated;
import static java.lang.Math.min;


/**
 * Created by evaitl on 8/17/16.
 * <p/>
 * Gets the popular and toprated lists from themoviedb.org using the api described
 * <a href="http://docs.themoviedb.apiary.io/">here</a>.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = MoviesSyncAdapter.class.getSimpleName();
    private static final int PRECACHE_PAGES = 5;
    // TODO 100
    private static final int MAX_PAGE = 50;
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
        Log.d(TAG, "constructor");
        mContentResolver = context.getContentResolver();
    }

    public MoviesSyncAdapter(Context context, boolean autoInitialize,
                             boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Log.d(TAG, "construrcture 2");
        mContentResolver = context.getContentResolver();
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(TAG, "syncImmediately");
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
        ContentResolver.setSyncAutomatically(newAccount,
                                             context.getString(R.string.content_authority), true);

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
        if (meta != null && meta.getCount() > 0) {
            meta.moveToFirst();
            lastPopPage = meta.getInt(Meta.IDX.LAST_POP_PAGE);
            lastTrPage = meta.getInt(Meta.IDX.LAST_TR_PAGE);
            maxPopPage = meta.getInt(Meta.IDX.MAX_POP_PAGE);
            maxTrPage = meta.getInt(Meta.IDX.MAX_TR_PAGE);
        } else {
            maxPopPage = maxTrPage = MAX_PAGE;
        }
        if (meta != null) {
            meta.close();
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

    private void saveBulk(MoviePage mp, long expires, Uri uri) {
        ContentValues[] values = new ContentValues[mp.getTotal_results()];
        int i = 0;
        int rank = 1 + (mp.getPage() - 1) * mp.getTotal_results();
        for (MovieInfo mi : mp.getResults()) {
            if (!mi.isGoodData()) {
                continue;
            }
            values[i++] = Contract.buildBulk()
                .putRank(rank++)
                .putExpires(expires)
                .putMid(mi.getId())
                .putPlot(mi.getOverview())
                .putPosterPath(mi.getPoster_path())
                .putReleaseDate(mi.getRelease_date())
                .putTitle(mi.getTitle())
                .putVoteAverage(mi.getVote_average())
                .putVoteCount(mi.getVote_count())
                .putGenres(Arrays.toString(mi.getGenre_ids()))
                .build();
        }
        mContentResolver.bulkInsert(uri, values);
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
        Log.d(TAG, "prefetching...");
        try {
            int nextPopPage = lastPopPage % maxPopPage + 1;
            for (int i = 0; i < min(PRECACHE_PAGES, MAX_PAGE); ++i) {
                if (nextPopPage == 0) {
                    nextPopPage = 1;
                }
                Log.d(TAG, "pop page " + nextPopPage);
                Response<MoviePage> rp =
                    MovieApi.retrofit.create(MovieApi.class)
                        .getPage(MovieListType.POPULAR.toString(), sApiKey, nextPopPage)
                        .execute();
                if (!rp.isSuccessful()) {
                    Log.i(TAG, "Failed fetching pop page " + nextPopPage);
                    break;
                }
                long expires = getExpires(rp);
                MoviePage moviePage = rp.body();
                maxPopPage = min(moviePage.getTotal_pages(), MAX_PAGE);
                saveBulk(moviePage, expires, Contract.Popular.URI);
                lastPopPage = nextPopPage++;
            }
            int nextTRPage = lastTrPage % maxTrPage + 1;
            for (int i = 0; i < min(PRECACHE_PAGES, MAX_PAGE); ++i) {
                if (nextTRPage == 0) {
                    nextTRPage = 1;
                }
                Log.d(TAG, "tr page " + nextTRPage);
                Response<MoviePage> rp =
                    MovieApi.retrofit.create(MovieApi.class)
                        .getPage(MovieListType.TOPRATED.toString(), sApiKey, nextTRPage)
                        .execute();
                if (!rp.isSuccessful()) {
                    Log.i(TAG, "Failed fetching TR page " + nextTRPage);
                    break;
                }
                long expires = getExpires(rp);
                MoviePage moviePage = rp.body();
                maxTrPage = min(moviePage.getTotal_pages(), MAX_PAGE);
                saveBulk(moviePage, expires, TopRated.URI);
                lastTrPage = nextTRPage++;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error in prefetch", e);
        }
    }
    private long getExpires(Response<MoviePage> rp){
        long expires;
        Date d = rp.headers().getDate("expires");
        if (d == null) {
            // 24 hours from now.
            expires = (new Date()).getTime() / 1000 + 24 * 60 * 60;
        } else {
            expires = d.getTime() / 1000;
        }
        return expires;
    }
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
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
     * <p/>
     * Perhaps use (activeNetworkInfo.getType() == NetworkInfo.TYPE_WIFI).
     *
     * @return true if we can get to server.
     */
    private boolean checkNetwork() {
        ConnectivityManager connectivityManager
            = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            Log.d(TAG, "h3");
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

    /**
     * Created by evaitl on 7/30/16.
     * <p/>
     * The fields are created to match the json information in a themoviedb api call.
     */
    private static class MoviePage {
        private int page;
        private MovieInfo[] results;
        private int total_results;
        private int total_pages;

        /**
         * Private constructor:  Only create these with gson.
         */
        private MoviePage() {
        }

        // Generated getters.
        public int getPage() {
            return page;
        }

        public MovieInfo[] getResults() {
            return results;
        }

        public int getTotal_pages() {
            return total_pages;
        }

        public int getTotal_results() {
            return total_results;
        }
    }

    /**
     * Created by evaitl on 7/30/16.
     * <p/>
     * The fields are created to match the json information in a themoviedb api calls.
     * <p/>
     * Not all fields are returned with every call, so some will have default values.
     */
    private static class MovieInfo {
        private boolean adult;
        private String backdrop_path;
        private int[] genre_ids;
        private int id;
        private String original_language;
        private String original_title;
        private String overview;
        private String release_date;
        private String poster_path;
        private float popularity;
        private String title;
        private boolean video;
        private float vote_average;
        private int vote_count;


        private String imdb_id;
        private ProductionCompanies[] production_companies;

        private ProductionCountries[] production_countries;
        private long revenue;
        private int runtime;
        private SpokenLanguages[] spoken_languages;
        private String status;
        private String tagline;

        /**
         * Private constructor: Only create these with gson.
         */
        private MovieInfo() {
        }

        /**
         * @return Can we put this in the movies db?
         */
        boolean isGoodData() {
            return id > 0 &&
                poster_path != null &&
                title != null &&
                release_date != null &&
                overview != null &&
                vote_average > 0 &&
                vote_count > 0;
        }

        public String getImdb_id() {
            return imdb_id;
        }

        public ProductionCompanies[] getProduction_companies() {
            return production_companies;
        }

        public ProductionCountries[] getProduction_countries() {
            return production_countries;
        }

        public long getRevenue() {
            return revenue;
        }

        public int getRuntime() {
            return runtime;
        }

        public SpokenLanguages[] getSpoken_languages() {
            return spoken_languages;
        }

        public String getStatus() {
            return status;
        }

        public String getTagline() {
            return tagline;
        }

        // Generated getters.
        public boolean isAdult() {
            return adult;
        }

        public String getBackdrop_path() {
            return backdrop_path;
        }

        public int[] getGenre_ids() {
            return genre_ids;
        }

        public int getId() {
            return id;
        }

        public String getOriginal_language() {
            return original_language;
        }

        public String getOriginal_title() {
            return original_title;
        }

        public String getOverview() {
            return overview;
        }

        public float getPopularity() {
            return popularity;
        }

        public String getPoster_path() {
            return poster_path;
        }

        public String getRelease_date() {
            return release_date;
        }

        public String getTitle() {
            return title;
        }

        public boolean isVideo() {
            return video;
        }

        public float getVote_average() {
            return vote_average;
        }

        public int getVote_count() {
            return vote_count;
        }

        class ProductionCompanies {
            String name;
            int id;
        }

        class ProductionCountries {
            String iso_3166_1;
            String name;
        }

        class SpokenLanguages {
            String iso_639_1;
            String name;
        }
    }
}
