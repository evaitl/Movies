package com.vaitls.movies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by evaitl on 8/1/16.
 */
public class DetailsFragment extends Fragment {
    private static final String TAG = DetailsFragment.class.getSimpleName();
    private static final String ARG_IDX = "ARG_IDX";
    private static final String ARG_SO = "ARG_SO";
    private MovieInfo mMovieInfo;
    private MovieDataCache mMovieDataCache;
    private boolean mFavorite;
    private MovieListType mSearchOrder;


    public static DetailsFragment newInstance(MovieListType searchOrder, int idx) {
        Bundle args = new Bundle();
        args.putInt(ARG_IDX, idx);
        args.putSerializable(ARG_SO, searchOrder);
        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_details, container, false);
        TextView titleField = (TextView) v.findViewById(R.id.fragment_details_title_text_view);
        TextView ratingField = (TextView) v.findViewById(R.id.fragment_details_rating_text_view);
        TextView plotField = (TextView) v.findViewById(R.id.fragment_details_plot_text_view);
        TextView dateField = (TextView) v.findViewById(R.id.fragment_details_date_text_view);
        ImageView thumbnail = (ImageView) v.findViewById(R.id.fragment_details_thubnail_image_view);
        final ImageButton favoriteButton = (ImageButton) v.findViewById(R.id.fragment_details_favorite_image_button);

        titleField.setText(mMovieInfo.getTitle());
        ratingField.setText(String.format("%.2f", mMovieInfo.getVote_average()));
        plotField.setText(mMovieInfo.getOverview());
        dateField.setText(mMovieInfo.getRelease_date());
        favoriteButton.setImageDrawable(ContextCompat.getDrawable(getContext(),
                mFavorite ?
                        R.drawable.ic_gold_star : R.drawable.ic_black_star));

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFavorite = !mFavorite;
                favoriteButton.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        mFavorite ?
                                R.drawable.ic_gold_star : R.drawable.ic_black_star));
            }
        });

        String uri = "http://image.tmdb.org/t/p/w185" +
                mMovieInfo.getPoster_path();
        Glide.with(thumbnail.getContext())
                .load(uri)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.drawable.sad_face)
                .fallback(R.drawable.sad_face)
                .crossFade()
                .centerCrop()
                .into(thumbnail);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int idx = getArguments().getInt(ARG_IDX);
        mSearchOrder = (MovieListType) getArguments().getSerializable(ARG_SO);
        Log.d(TAG, "df:oC " + mSearchOrder + " " + idx);
        mMovieDataCache = MovieDataCache.getInstance();
        mMovieInfo = mMovieDataCache.get(mSearchOrder, idx);
        mFavorite = mMovieDataCache.isFavorite(mMovieInfo.getId());
    }
}
