package com.vaitls.movies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vaitls.movies.data.Contract;

/**
 * Created by evaitl on 8/19/16.
 */
public class MoviesSyncService  extends Service{
    private final String TAG=MoviesSyncService.class.getSimpleName();
    private static final Object sLock=new Object();
    private static MoviesSyncAdapter sMoviesSyncAdapter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sMoviesSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"sync service onCreate");
        synchronized (sLock){
            if(sMoviesSyncAdapter==null){
                sMoviesSyncAdapter=new MoviesSyncAdapter(getApplicationContext(),true);
            }
        }

    }
}
