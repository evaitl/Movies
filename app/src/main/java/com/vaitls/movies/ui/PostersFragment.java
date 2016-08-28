package com.vaitls.movies.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.vaitls.movies.data.MovieListType;
import com.vaitls.movies.R;
import com.vaitls.movies.data.Contract;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.vaitls.movies.data.Contract.Movies;

/**
 * Created by evaitl on 7/30/16.
 */
public class PostersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = PostersFragment.class.getSimpleName();
    private final static String[] PROJECTION = {
        Movies.COLS._ID,
        Movies.COLS.POSTER_PATH,
    };
    private static final int COL__ID = 0;
    private static final int COL_POSTER_PATH = 1;
    private PhotoAdapter mPhotoAdapter;
    private MovieListType mSearchOrder;
    @BindView(R.id.full_page_recycler_view) RecyclerView mPostersRecyclerView;

    public static PostersFragment newInstance() {
        return new PostersFragment();
    }

    void setSearchOrder(MovieListType searchOrder) {
        if (mSearchOrder != searchOrder) {
            mSearchOrder = searchOrder;
            getLoaderManager().initLoader(mSearchOrder.ordinal(), null, this);
        }
    }
    void scrollTo(int idx){
        if(mPostersRecyclerView !=null){
            mPostersRecyclerView.smoothScrollToPosition(idx);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (!isOnline()) {
            setRetainInstance(false);
            setHasOptionsMenu(false);
            return inflater.inflate(R.layout.no_network, container, false);
        }
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.recycler_view, container, false);
        ButterKnife.bind(this,v);
        Log.d(TAG, "setting adapter to " + mPhotoAdapter);
        mPostersRecyclerView.setAdapter(mPhotoAdapter);
        int columnWidthPx=(int)(getResources().getDimension(R.dimen.posters_col_width));
        mPostersRecyclerView.setLayoutManager(new GridAutofitLayoutManager(getActivity(), columnWidthPx));
        mPostersRecyclerView.setHasFixedSize(true);
        return v;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "ocl id " + id);
        Uri uri = null;
        if (id == MovieListType.FAVORITE.ordinal()) {
            uri = Contract.Favorites.URI;

        } else if (id == MovieListType.POPULAR.ordinal()) {
            uri = Contract.Popular.URI;
        } else if (id == MovieListType.TOPRATED.ordinal()) {
            uri = Contract.TopRated.URI;
        } else {
            throw new IllegalStateException("unknown loader id " + id);
        }
        return new CursorLoader(getActivity(), uri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "oLR");
        mPhotoAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "olf");
        mPhotoAdapter.swapCursor(data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mSearchOrder = MovieListType.POPULAR;
        mPhotoAdapter = new PhotoAdapter(getContext(), null);
        mPhotoAdapter.setHasStableIds(true);
        getLoaderManager().initLoader(mSearchOrder.ordinal(), null, this);
        Log.d(TAG, "onCreate");
    }

    private boolean isOnline() {
        ConnectivityManager cm =
            (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    class PhotoAdapter extends RecyclerViewCursorAdapter<PhotoHolder> {


        public PhotoAdapter(Context context, Cursor cursor) {
            super(context, cursor);

        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder viewHolder, Cursor cursor) {
            viewHolder.bindImageInfo(cursor.getString(COL_POSTER_PATH));
        }
    }

    class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView;
            mImageView.setOnClickListener(this);
        }

        void bindImageInfo(String posterPath) {
            String uri = "http://image.tmdb.org/t/p/w185" + posterPath;
            Log.d(TAG,"poster path: "+posterPath);
            Glide.with(getContext())
                .load(uri)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.sad_face)
                .fallback(R.drawable.sad_face)
                .crossFade()
                .centerCrop()
                .into(mImageView);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "li on click: " + mSearchOrder + " ");
            MovieActivity movieActivity = (MovieActivity) getActivity();
            movieActivity.listItemSelected(mSearchOrder, getAdapterPosition());
        }
    }
}
