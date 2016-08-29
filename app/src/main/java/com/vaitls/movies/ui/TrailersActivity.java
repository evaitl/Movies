package com.vaitls.movies.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.vaitls.movies.R;
import com.vaitls.movies.data.TrailerLoader;

import static com.vaitls.movies.data.Contract.Videos;

/**
 * Created by evaitl on 8/27/16.
 */
public class TrailersActivity extends ListActivity implements TrailerLoader.TrailerCallback {
    private static final String TAG = TrailersActivity.class.getSimpleName();
    private final static String EXTRA_MID = "mid";
    private int mMid;
    private TrailerLoader mTrailerLoader;
    private SimpleCursorAdapter mAdapter;

    public static Intent newIntent(Context packageContext, int mid) {
        Intent intent = new Intent(packageContext, TrailersActivity.class);
        intent.putExtra(EXTRA_MID, mid);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailers);
        mMid = getIntent().getIntExtra(EXTRA_MID, 0);
        String[] cnames = {Videos.COLS.NAME};
        int[] views = {R.id.trailer_item_name_text_view};
        mAdapter = new SimpleCursorAdapter(this, R.layout.trailers_item, null,
                                           cnames, views, 0);
        setListAdapter(mAdapter);
        mTrailerLoader = new TrailerLoader(this, mMid, this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTrailerLoader != null) {
            mTrailerLoader.cancel();
            mTrailerLoader = null;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = mAdapter.getCursor();
        c.moveToPosition(position);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        /*
        Highly unlikely this works on anything but youtube. The data is
        site and key, but not how they form a full url.
         */
        String data = String.format("http://%s.com/watch?v=%s",
                                    c.getString(Videos.IDX.SITE),
                                    c.getString(Videos.IDX.KEY));
        Log.d(TAG, "olic data= " + data);
        intent.setData(Uri.parse(data));
        startActivity(intent);
    }

    @Override
    public void onTrailersLoaded(Cursor cursor) {
        mTrailerLoader = null;
        mAdapter.swapCursor(cursor);
    }
}
