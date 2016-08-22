package com.vaitls.movies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vaitls.movies.data.Contract;

import static com.vaitls.movies.data.Contract.Favorites;

/**
 * Created by evaitl on 8/1/16.
 * <p/>
 * A v4 PagerAdapter doesn't deal well with changes in the underlying lists.
 * There is no notifyRangeChanged() etcetera calls. Even though we are paging,
 * I went with a v7 RecyclerView so I can use the better adapter.
 * <p/>
 * RecyclerViews don't snap into place they way a ViewPager does, so I added
 * an RecyclerView.OnScrollListener to scroll into position whenever the scrolling
 * goes idle.
 */
public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = DetailsFragment.class.getSimpleName();
    private static final String ARG_IDX = "four score and seven";
    private static final String ARG_SO = "years ago our ...";
    private static final String[] PROJECTION = {
            Favorites.COL__ID,
            Favorites.COL_TITLE,
            Favorites.COL_RELEASE_DATE,
            Favorites.COL_PLOT,
            Favorites.COL_POSTER_PATH,
            Favorites.COL_FAVORITE,
            Favorites.COL_VOTE_AVERAGE,
            Favorites.COL_MID,
    };
    private static final int COL__ID = 0;
    private static final int COL_TITLE = 1;
    private static final int COL_RELEASE_DATE = 2;
    private static final int COL_PLOT = 3;
    private static final int COL_POSTER_PATH = 4;
    private static final int COL_FAVORITE = 5;
    private static final int COL_VOTE_AVERAGE = 6;
    private static final int COL_MID = 7;
    private MovieListType mSearchOrder;
    private DetailsAdapter mDetailsAdapter;
    private int mIndex;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    public static DetailsFragment newInstance(MovieListType searchOrder, int idx) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_IDX, idx);
        bundle.putSerializable(ARG_SO, searchOrder);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        if (id == MovieListType.FAVORITE.ordinal()) {
            uri = Contract.FAVORITES_URI;
        } else if (id == MovieListType.POPULAR.ordinal()) {
            uri = Contract.POPULAR_URI;
        } else if (id == MovieListType.TOP_RATED.ordinal()) {
            uri = Contract.TOP_RATED_URI;
        } else {
            throw new IllegalStateException("unknown loader id " + id);
        }
        return new CursorLoader(getActivity(), uri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDetailsAdapter.swapCursor(null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDetailsAdapter.swapCursor(data);
        setIndex(mIndex);
    }

    void setSearchOrder(MovieListType searchOrder) {
        if (mSearchOrder != searchOrder) {
            mSearchOrder = searchOrder;
            mIndex=0;
            getLoaderManager().initLoader(mSearchOrder.ordinal(),null,this);
        }
    }

    void setIndex(int idx) {
        mLayoutManager.scrollToPosition(idx);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.recycler_view, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.full_page_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity(),
                                                 LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mDetailsAdapter);
        mRecyclerView.addOnScrollListener(new PagingRecyclerLock());
        return v;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle bundle = getArguments();
        mSearchOrder = (MovieListType) bundle.getSerializable(ARG_SO);
        if (mSearchOrder == null) {
            Log.d(TAG, "Default search order");
            mSearchOrder = MovieListType.POPULAR;
        }
        mIndex = bundle.getInt(ARG_IDX, 0);
        mDetailsAdapter = new DetailsAdapter(getContext(), null);
        getLoaderManager().initLoader(mSearchOrder.ordinal(), null, this);
        Log.d(TAG, "df onCreate");
    }

    /* This is not general purpose.
     * Assumes we end up with two views contesting for the full screen.
     * Whomever is closest to the middle gets it.
     */
    class PagingRecyclerLock extends RecyclerView.OnScrollListener {
        boolean mSettling;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (!mSettling && newState == RecyclerView.SCROLL_STATE_IDLE) {

                View v = lm.findViewByPosition(lm.findFirstVisibleItemPosition());
                if (-v.getLeft() < v.getWidth() / 2) {
                    mIndex=lm.findFirstVisibleItemPosition();
                } else {
                    mIndex=lm.findLastVisibleItemPosition();
                }
                recyclerView.smoothScrollToPosition(mIndex);
                mSettling = true;
            }
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                    newState == RecyclerView.SCROLL_STATE_SETTLING) {
                mSettling = false;
            }
        }
    }

    class DetailsAdapter extends RecyclerViewCursorAdapter<DetailsHolder> {

        public DetailsAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }


        @Override
        public DetailsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.details_scroll_view, parent, false);
            return new DetailsHolder(v);
        }

            @Override
        public void onBindViewHolder(DetailsHolder h, Cursor cursor) {
            h.getDateTextView().setText(cursor.getString(COL_RELEASE_DATE));
            h.getPlotTextView().setText(cursor.getString(COL_PLOT));
            h.getRatingTextView().setText(String.format("%.2f", cursor.getFloat(COL_VOTE_AVERAGE)));
            h.getTitleTextView().setText(cursor.getString(COL_TITLE));
            h.getFavoriteButton().setImageDrawable(
                    ContextCompat.getDrawable(getContext(),
                                              cursor.getInt(
                                                      COL_FAVORITE) == 1 ? R.drawable
                                                      .ic_gold_star : R.drawable.ic_black_star));
            String uri = "http://image.tmdb.org/t/p/w185" +
                    cursor.getString(COL_POSTER_PATH);
            Glide.with(getContext())
                    .load(uri)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.drawable.sad_face)
                    .fallback(R.drawable.sad_face)
                    .crossFade()
                    .centerCrop()
                    .into(h.getThumbnail());
        }
    }

    class DetailsHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private TextView mRatingTextView;
        private ImageButton mFavoriteButton;
        private TextView mPlotTextView;
        private ImageView mThumbnail;

        public DetailsHolder(View v) {
            super(v);
            mTitleTextView = (TextView) v.findViewById(R.id.fragment_details_title_text_view);
            mDateTextView = (TextView) v.findViewById(R.id.fragment_details_date_text_view);
            mRatingTextView = (TextView) v.findViewById(R.id.fragment_details_rating_text_view);
            mFavoriteButton = (ImageButton) v.findViewById(
                    R.id.fragment_details_favorite_image_button);
            mPlotTextView = (TextView) v.findViewById(R.id.fragment_details_plot_text_view);
            mThumbnail = (ImageView) v.findViewById(R.id.fragment_details_thubnail_image_view);
        }

        public TextView getDateTextView() {
            return mDateTextView;
        }

        public ImageButton getFavoriteButton() {
            return mFavoriteButton;
        }

        public TextView getPlotTextView() {
            return mPlotTextView;
        }

        public TextView getRatingTextView() {
            return mRatingTextView;
        }

        public ImageView getThumbnail() {
            return mThumbnail;
        }

        public TextView getTitleTextView() {
            return mTitleTextView;
        }
    }
}
