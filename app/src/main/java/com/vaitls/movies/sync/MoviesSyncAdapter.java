package com.vaitls.movies.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by evaitl on 8/17/16.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {


    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

    }

    public static void syncImmediately(Context context){

    }
    public static void initializeSyncAdapter(Context context){

    }
}
