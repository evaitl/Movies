package com.vaitls.movies;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.muller.snappingrecyclerview.SnappingRecyclerView;

/**
 * Created by evaitl on 8/1/16.
 */
public class DetailsFragment extends Fragment {
    private static final String TAG=DetailsFragment.class.getSimpleName();
    private static final String ARG_IDX = "four score and seven";
    private static final String ARG_SO = "years ago our ...";
    private MovieDataCache mDC;
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

    void setSearchOrder(MovieListType searchOrder) {
        if (mSearchOrder != searchOrder) {
            mSearchOrder = searchOrder;
            mDetailsAdapter.setSearchOrder(searchOrder);
        }
    }
    void setIndex(int idx) {
        mLayoutManager.scrollToPosition(idx);
    }
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mDC = MovieDataCache.getInstance();
        View v = inflater.inflate(R.layout.snapping_recycler_view, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.snapping_recycler);
        mLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new DetailsAdapter(mSearchOrder));
       // mRecyclerView.setSnapEnabled(true);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle bundle=getArguments();
        mSearchOrder=(MovieListType)bundle.getSerializable(ARG_SO);
        if(mSearchOrder==null){
            Log.d(TAG,"Default search order");
            mSearchOrder=MovieListType.POPULAR;
        }
        mIndex=bundle.getInt(ARG_IDX,0);
        Log.d(TAG,"df onCreate");
    }

    class DetailsAdapter extends RecyclerView.Adapter<DetailsHolder> {
        MovieListType mSearchOrder;

        DetailsAdapter(MovieListType searchOrder) {
            Log.d(TAG,"new da "+ searchOrder);
            mSearchOrder = searchOrder;
        }

        @Override
        public int getItemCount() {
            return mDC.getTotal(mSearchOrder);
        }

        @Override
        public DetailsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.details_scroll_view,parent,false);
            return new DetailsHolder(v);
        }

        void setSearchOrder(MovieListType searchOrder) {
            if (searchOrder == mSearchOrder) {
                return;
            }
            notifyItemRangeRemoved(0, mDC.getTotal(mSearchOrder));
            mDC.removeAdapter(mSearchOrder, this);
            mDC.addAdapter(searchOrder, this);
            mSearchOrder = searchOrder;
        }

        @Override
        public void onBindViewHolder(DetailsHolder h, int position) {
            Log.d(TAG,"on bindViewHolder " + position);
            MovieInfo mi=mDC.get(mSearchOrder,position);
            h.getTitleTextView().setText(mi.getTitle());
            h.getRatingTextView().setText(String.format("%.2f",mi.getVote_average()));
            h.getPlotTextView().setText(mi.getOverview());
            h.getDateTextView().setText(mi.getRelease_date());
            boolean favorite=mDC.isFavorite(mi.getId());
            h.getFavoriteButton().setImageDrawable(
                    ContextCompat.getDrawable(getContext(),
                    favorite? R.drawable.ic_gold_star: R.drawable.ic_black_star));
            String uri="http://image.tmdb.org/t/p/w185"+
                    mi.getPoster_path();
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
            mFavoriteButton = (ImageButton) v.findViewById(R.id.fragment_details_favorite_image_button);
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
