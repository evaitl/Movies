package com.vaitls.movies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

public class MovieActivity extends SingleFragmentActivity {
    private static final String TAG=MovieActivity.class.getSimpleName();
    @Override
    protected Fragment createFragment() {
        Log.d(TAG,"Creating posters fragment");
        return PostersFragment.newInstance();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
    }
}
