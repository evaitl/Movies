package com.vaitls.movies.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vaitls.movies.data.MovieListType;
import com.vaitls.movies.R;
import com.vaitls.movies.data.Contract;
import com.vaitls.movies.data.GenreNameMapper;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    private MovieListType mSearchOrder;
    private DetailsAdapter mDetailsAdapter;
    private int mIndex;
    @BindView(R.id.full_page_recycler_view) RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MovieActivity mMovieActivity;

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
        String[] PROJECTION = null;
        if (id == MovieListType.FAVORITE.ordinal()) {
            uri = Favorites.URI;
            PROJECTION = Favorites.PROJECTION;
        } else if (id == MovieListType.POPULAR.ordinal()) {
            uri = Contract.Popular.URI;
            PROJECTION = Contract.Popular.PROJECTION;
        } else if (id == MovieListType.TOPRATED.ordinal()) {
            uri = Contract.TopRated.URI;
            PROJECTION = Contract.TopRated.PROJECTION;
        } else {
            throw new IllegalStateException("unknown loader id " + id);
        }
        return new CursorLoader(getActivity(), uri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "olr");
        mDetailsAdapter.swapCursor(null);
    }

    /**
     * When I'm browsing the favorites list, I hate having the view I'm looking at
     * disappear as soon as I touch the star. Instead, we turn off the star
     * by setting favorite to false and put that in the db.
     * <p/>
     * In onPause or when changing away from the favorite list, I preen out the
     * no-longer favorites from the favorites list.
     * <p/>
     * Do this in an asynctask. No db in UI thread.
     */
    private void preenFavorites() {
        Log.d(TAG, "preening Favorits");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                getContext().getContentResolver().delete(Favorites.URI, "favorite = 0", null);
                return null;
            }
        }.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSearchOrder == MovieListType.FAVORITE) {
            preenFavorites();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "olf");
        mDetailsAdapter.swapCursor(data);
        setIndex(mIndex);
    }

    void setSearchOrder(MovieListType searchOrder) {
        if (mSearchOrder != searchOrder) {
            if (mSearchOrder == MovieListType.FAVORITE) {
                preenFavorites();
            }
            mSearchOrder = searchOrder;
            mIndex = 0;
            Log.d(TAG, "init loader this-- setSearchOrder");
            getLoaderManager().initLoader(mSearchOrder.ordinal(), null, this);
        }
    }

    void setIndex(int idx) {
        mLayoutManager.scrollToPosition(idx);
    }

    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.recycler_view, container, false);
        ButterKnife.bind(this,v);
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
        FragmentActivity fa= getActivity();
        if(fa instanceof  MovieActivity){
            mMovieActivity=(MovieActivity) fa;
        }
        Bundle bundle = getArguments();
        mSearchOrder = (MovieListType) bundle.getSerializable(ARG_SO);
        if (mSearchOrder == null) {
            Log.d(TAG, "Default search order");
            mSearchOrder = MovieListType.POPULAR;
        }
        mIndex = bundle.getInt(ARG_IDX, 0);
        mDetailsAdapter = new DetailsAdapter(getContext(), null);
        Log.d(TAG, "initLoader this -- onCreate");
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
            Log.d(TAG, "ossc: " + newState + "mSettling " + mSettling);
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (!mSettling && newState == RecyclerView.SCROLL_STATE_IDLE) {

                View v = lm.findViewByPosition(lm.findFirstVisibleItemPosition());
                if (-v.getLeft() < v.getWidth() / 2) {
                    mIndex = lm.findFirstVisibleItemPosition();
                } else {
                    mIndex = lm.findLastVisibleItemPosition();
                }
                recyclerView.smoothScrollToPosition(mIndex);
                if(mMovieActivity!=null){
                    mMovieActivity.detailsItemSettled(mIndex);
                }
                mSettling = true;
                /**
                 * <a href="https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html">
                 *     RecyclerView</a>
                 * <blockquote>
                 *     When writing a RecyclerView.LayoutManager you almost always want
                 *     to use layout positions whereas when writing an RecyclerView.Adapter,
                 *     you probably want to use adapter positions.
                 * </blockquote>
                 */
                DetailsHolder h = (DetailsHolder) recyclerView.findViewHolderForLayoutPosition(
                    mIndex);
            }
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                newState == RecyclerView.SCROLL_STATE_SETTLING) {
                mSettling = false;
            }
        }
    }

    class DetailsAdapter extends RecyclerViewCursorAdapter<DetailsHolder> {

        /**
         * Ugly hack. Normally load trailers on a scrollstate settled. That doesn't happen
         * for the first holder that comes up, so...
         */
        boolean bFirstHolder = true;


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
            h.bind(cursor);
        }

        @Override
        public void onViewDetachedFromWindow(DetailsHolder holder) {
            super.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewAttachedToWindow(DetailsHolder holder) {
            super.onViewAttachedToWindow(holder);
            if (bFirstHolder) {
                bFirstHolder = false;
            }
        }
    }


    class DetailsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final int LOADER_FAVORITES = 1;
        private final int LOADER_REVIEWS = 2;
        private final int LOADER_TRAILERS = 3;
        /**
         * I'm not convinced this butterknife thing is a good idea. It moves the binding
         * from the code to the declaration, but the runtime inserts the almost the same code
         * back I would have written into the runtime.
         *
         * The downside is that I no longer see the code because the code that is run is not
         * what I see here in the editor.
         *
         * One of my 'favorite' things about C++ is that you never know without a full code
         * inspection if the code that is running has ANY relationship with what you see
         * in source. Every single operator can be turned into
         * "go spawn xconq (OK - old, minecraft then)" behind the scenes. Why do that to java
         * with annotations?
         *
         */
        @BindView(R.id.fragment_details_title_text_view)  TextView mTitleTextView;
        @BindView(R.id.fragment_details_date_text_view)  TextView mDateTextView;
        @BindView(R.id.fragment_details_rating_text_view)  TextView mRatingTextView;
        @BindView(R.id.fragment_details_favorite_image_button)  ImageButton mFavoriteButton;
        @BindView(R.id.fragment_details_plot_text_view)  TextView mPlotTextView;
        @BindView(R.id.fragment_details_thubnail_image_view)  ImageView mThumbnail;
        @BindView(R.id.fragment_details_genre_text_view)  TextView mGenresTextView;
        @BindView(R.id.fragment_details_reviews_button)  Button mReviewsButton;
        @BindView(R.id.fragment_details_videos_button)  Button mVideosButton;
        private int mMid;
        private boolean mFavorite;

        public DetailsHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
            mFavoriteButton.setOnClickListener(this);
        }

        /**
         * Sets the favorite image and updates the database with an insert.
         *
         * @param mid
         * @param set
         */
        private void setFavoriteImage(final int mid, final boolean set) {
            mFavoriteButton.setImageDrawable(
                ContextCompat.getDrawable(getContext(),
                                          set ? R.drawable.ic_gold_star
                                              : R.drawable.ic_black_star));
            if (mFavorite == set) {
                return;
            }
            mFavorite = set;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    ContentResolver resolver = getContext().getContentResolver();
                    resolver.insert(Favorites.URI, Contract.buildFavorites()
                        .putMid(mid)
                        .putFavorite(set)
                        .build());
                    return null;
                }
            }.execute();
        }

        void bind(Cursor cursor) {
            /**
             Counting on using the contract projections and
             the colmuns are stable between tables (TopRated.IDX.RANK==Popular.IDX.RANK).
             */
            mFavorite = cursor.getInt(Favorites.IDX.FAVORITE) == 1;
            mMid = cursor.getInt(Favorites.IDX.MID);
            mDateTextView.setText(cursor.getString(Favorites.IDX.RELEASE_DATE));
            mPlotTextView.setText(cursor.getString(Favorites.IDX.PLOT));
            mRatingTextView.setText(String.format(new Locale("en"),
                                                  "%.2f",
                                                   cursor.getFloat(Favorites.IDX.VOTE_AVERAGE)));
            mTitleTextView.setText(cursor.getString(Favorites.IDX.TITLE));
            mGenresTextView.setText(GenreNameMapper.map(cursor.getString(Favorites.IDX.GENRES)));
            setFavoriteImage(mMid, mFavorite);
            mReviewsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick Reviews");
                    Intent intent = ReviewsActivity.newIntent(getContext(), mMid);
                    startActivity(intent);
                }
            });
            mVideosButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = TrailersActivity.newIntent(getContext(), mMid);
                    startActivity(intent);
                }
            });

            String uri = "http://image.tmdb.org/t/p/w185" +
                cursor.getString(Favorites.IDX.POSTER_PATH);
            /**
             * Decided to not cache images/videos in the db. My plex server DB is 12GB,
             * which would kill my cell phone. Glide/Picasso do some local caching I think.
             */
            Glide.with(getContext())
                .load(uri)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.sad_face)
                .fallback(R.drawable.sad_face)
                .crossFade()
                .centerCrop()
                .into(mThumbnail);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "clicked");
            setFavoriteImage(mMid, !mFavorite);
        }
    }
}
