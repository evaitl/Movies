package com.vaitls.movies.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.vaitls.movies.MovieListType;
import com.vaitls.movies.R;
import com.vaitls.movies.data.GenreNameMapper;
import com.vaitls.movies.sync.MoviesSyncAdapter;

public class MovieActivity extends AppCompatActivity {
    private static final String FAVORITES_FILE_NAME = "movies_favorites.objs";
    private static final String TAG = MovieActivity.class.getSimpleName();
    private PostersFragment mPostersFragment;
    private DetailsFragment mDetailsFragment;


    void listItemSelected(MovieListType searchOrder, int idx){
        if(mDetailsFragment==null) {
            Intent intent = DetailsActivity.newIntent(getApplicationContext(), searchOrder, idx);
            startActivity(intent);
        }else{
            mDetailsFragment.setIndex(idx);
        }
    }
    void detailsItemSelected(int idx){
        /*
        if(mPostersFragment){
            TODO  mPostersFragment.setIndex(idx);
        }
        */
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_posters, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MovieListType searchOrder = null;
        switch (item.getItemId()) {
            case R.id.menu_item_favorites:
                searchOrder = MovieListType.FAVORITE;
                break;
            case R.id.menu_item_popularity_order:
                searchOrder = MovieListType.POPULAR;
                break;
            case R.id.menu_item_ratings_order:
                searchOrder = MovieListType.TOPRATED;
                break;
        }
        if (searchOrder != null) {
            if (mPostersFragment != null) {
                mPostersFragment.setSearchOrder(searchOrder);
            }
            if (mDetailsFragment != null) {
                mDetailsFragment.setSearchOrder(searchOrder);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this,R.xml.settings,false);
        MoviesSyncAdapter.initializeSyncAdapter(this);
        // main is a resource value based on screen size. It points at
        // either single or two fragment layout.
        setContentView(R.layout.main);
        FragmentManager fm = getSupportFragmentManager();
        Log.d(TAG,"main "+R.layout.main+" asf "+R.layout.activity_single_fragment+ " atf "+R.layout.activity_two_fragment);
        Log.d(TAG, "fc: "+findViewById(R.id.fragment_container));
        Log.d(TAG, "lfc: "+findViewById(R.id.left_fragment_container));
        if (findViewById(R.id.fragment_container)!=null) { // single fragment
            Log.d(TAG,"one frag");
            mPostersFragment = (PostersFragment) fm.findFragmentById(R.id.fragment_container);
            if (mPostersFragment == null) {
                mPostersFragment = PostersFragment.newInstance();
                fm.beginTransaction()
                        .add(R.id.fragment_container, mPostersFragment)
                        .commit();
            }
        } else {
            Log.d(TAG,"two frags");
            mPostersFragment = (PostersFragment) fm.findFragmentById(R.id.left_fragment_container);
            mDetailsFragment = (DetailsFragment) fm.findFragmentById(R.id.right_fragment_container);
            if (mPostersFragment == null) {
                mPostersFragment = PostersFragment.newInstance();
                fm.beginTransaction()
                        .add(R.id.left_fragment_container, mPostersFragment)
                        .commit();
            }
            if (mDetailsFragment == null) {
                mDetailsFragment = DetailsFragment.newInstance(MovieListType.POPULAR, 0);
                fm.beginTransaction()
                        .add(R.id.right_fragment_container, mDetailsFragment)
                        .commit();
            }
        }
        GenreNameMapper.loadGenres(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
