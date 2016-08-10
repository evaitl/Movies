package com.vaitls.movies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by evaitl on 8/1/16.
 */
public class DetailsPagerActivity extends AppCompatActivity {
    private static final String TAG=DetailsPagerActivity.class.getSimpleName();
    private static final String EXTRA_IDX="com.vaitls.movies.idx";
    private static final String EXTRA_SO="com.vaitls.movies.so";
    private ViewPager mViewPager;
    private MovieDataCache mDC;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_viewpager);
        final int idx=getIntent().getIntExtra(EXTRA_IDX,0);
        final MovieListType searchOrder=(MovieListType)getIntent().getSerializableExtra(EXTRA_SO);
        Log.d(TAG,"dpa:oc:"+searchOrder+" "+idx);
        mViewPager=(ViewPager)findViewById(R.id.activity_details_view_pager);
        mDC=MovieDataCache.getInstance();
        FragmentManager fragmentManager=getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return DetailsFragment.newInstance(searchOrder,position);
            }
            @Override
            public int getCount() {
                return mDC.getTotal(searchOrder);
            }
        });
        mViewPager.setCurrentItem(idx);
    }

    public static Intent newIntent(Context packageContext,MovieListType searchOrder, int idx ){
        Intent intent = new Intent(packageContext,DetailsPagerActivity.class);
        intent.putExtra(EXTRA_IDX,idx);
        intent.putExtra(EXTRA_SO,searchOrder);
        return intent;
    }
}
