package com.vaitls.movies;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class MovieActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return PostersFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
    }
}
