package com.vaitls.movies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by evaitl on 8/1/16.
 */
public class DetailsActivity extends AppCompatActivity {
    private static final String TAG=DetailsActivity.class.getSimpleName();
    private static final String EXTRA_IDX="com.vaitls.movies.idx";
    private static final String EXTRA_SO="com.vaitls.movies.so";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        MovieListType searchOrder =(MovieListType) intent.getSerializableExtra(EXTRA_SO);
        if(searchOrder ==null) searchOrder =MovieListType.POPULAR;
        int idx=intent.getIntExtra(EXTRA_IDX,0);

        setContentView(R.layout.activity_single_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = DetailsFragment.newInstance(searchOrder,idx);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public static Intent newIntent(Context packageContext, MovieListType searchOrder,
                                   int idx ){
        Intent intent = new Intent(packageContext,DetailsActivity.class);
        intent.putExtra(EXTRA_IDX,idx);
        intent.putExtra(EXTRA_SO,searchOrder);
        return intent;
    }
}
