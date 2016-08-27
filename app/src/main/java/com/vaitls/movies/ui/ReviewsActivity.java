package com.vaitls.movies.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import com.vaitls.movies.MovieListType;
import com.vaitls.movies.R;
import com.vaitls.movies.data.ReviewLoader;
import static com.vaitls.movies.data.Contract.Reviews;
/**
 * Created by evaitl on 8/26/16.
 */
public class ReviewsActivity extends ListActivity implements ReviewLoader.ReviewCallback{
    private static final String TAG=ReviewsActivity.class.getSimpleName();
    private final static String EXTRA_MID="mid or no mid, that is the question";
    private int mMid;
    private ReviewLoader mReviewLoader;
    private SimpleCursorAdapter mAdapter;
    public static Intent newIntent(Context packageContext, int mid) {
        Intent intent = new Intent(packageContext, ReviewsActivity.class);
        intent.putExtra(EXTRA_MID, mid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mMid=getIntent().getIntExtra(EXTRA_MID,0);
        String [] cnames={Reviews.COLS.AUTHOR, Reviews.COLS.CONTENT};
        int [] views={R.id.review_item_author_text_view, R.id.review_item_content_text_view};
        mAdapter=new SimpleCursorAdapter(this, R.layout.review_item, null, cnames, views, 0 );
        setListAdapter(mAdapter);
        mReviewLoader=new ReviewLoader(this,mMid,this);
    }

    @Override
    public void onReviewsLoaded(Cursor cursor) {
        mReviewLoader=null;
        mAdapter.swapCursor(cursor);
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"onStop");
        super.onStop();
        if(mReviewLoader!=null){
            mReviewLoader.cancel();
            mReviewLoader=null;
        }
    }
}
