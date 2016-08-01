package com.vaitls.movies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by evaitl on 8/1/16.
 */
public class DetailsPagerActivity extends FragmentActivity {
    private static final String TAG=DetailsPagerActivity.class.getSimpleName();
    private static final String EXTRA_IDX="com.vaitls.movies.idx";
    private ViewPager mViewPager;
    private MovieDataCache mDC;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_viewpager);
        int idx=(int)getIntent().getSerializableExtra(EXTRA_IDX);
        mViewPager=(ViewPager)findViewById(R.id.activity_details_view_pager);
        mDC=MovieDataCache.getInstance(getString(R.string.themoviedb_key));
        FragmentManager fragmentManager=getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentPagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return DetailsFragment.newInstance(position);
            }
            @Override
            public int getCount() {
                return mDC.getPopularTotal();
            }
        });
        mViewPager.setCurrentItem(idx);
    }

    public static Intent newIntent(Context packageContext,int idx ){
        Intent intent = new Intent(packageContext,DetailsPagerActivity.class);
        intent.putExtra(EXTRA_IDX,idx);
        return intent;
    }
}
