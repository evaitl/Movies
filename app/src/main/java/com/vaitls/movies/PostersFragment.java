package com.vaitls.movies;

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
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.vaitls.movies.data.Contract;
import static com.vaitls.movies.data.Contract.Popular;
import static com.vaitls.movies.data.Contract.TopRated;
import static com.vaitls.movies.data.Contract.Favorites;
import static com.vaitls.movies.data.Contract.Movies;
/**
 * Created by evaitl on 7/30/16.
 */
public class PostersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = PostersFragment.class.getSimpleName();
    private PhotoAdapter mPhotoAdapter;
    private MovieListType mSearchOrder;
    private RecyclerView mPostersRecylerView;

    public static PostersFragment newInstance() {
        return new PostersFragment();
    }

    void setSearchOrder(MovieListType searchOrder) {
        if (mSearchOrder != searchOrder) {
            mSearchOrder = searchOrder;
            // TODO initLoader?
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_posters, menu);
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
        mPostersRecylerView = (RecyclerView) v.findViewById(R.id.full_page_recycler_view);
        int columns = getResources().getInteger(R.integer.columns);
        mPostersRecylerView.setLayoutManager(new GridLayoutManager(getActivity(), columns));
        mPostersRecylerView.setHasFixedSize(true);
        return v;
    }


    private final static String []PROJECTION={
            Movies.COL__ID,
            Movies.COL_MID,
            Movies.COL_POSTER_PATH,
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri=null;
        if(id==MovieListType.FAVORITE.ordinal()){
            uri=Contract.FAVORITES_URI;
        }else if(id==MovieListType.POPULAR.ordinal()){
            uri=Contract.POPULAR_URI;
        }else if (id==MovieListType.TOP_RATED.ordinal()){
            uri=Contract.TOP_RATED_URI;
        }else{
            throw new IllegalStateException("unknown loader id "+id);
        }
        return new CursorLoader(getActivity(),uri,PROJECTION, null,null, null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPhotoAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPhotoAdapter.swapCursor(data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mSearchOrder = MovieListType.POPULAR;
        mPhotoAdapter=new PhotoAdapter(getContext(),null);
        getLoaderManager().initLoader(mSearchOrder.ordinal(),null,this);
        Log.d(TAG, "onCreate");
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    class PhotoAdapter extends RecyclerViewCursorAdapter<PhotoHolder>{
        int mid_col;
        int poster_col;
        public PhotoAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            mid_col=cursor.getColumnIndexOrThrow(Movies.COL_MID);
            poster_col=cursor.getColumnIndexOrThrow(Movies.COL_POSTER_PATH);
        }
        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG,"ocvh");
            LayoutInflater inflater=LayoutInflater.from(getActivity());
            View view= inflater.inflate(R.layout.gallery_item,parent,false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder viewHolder, Cursor cursor) {
            viewHolder.bindImageInfo(cursor.getInt(mid_col),cursor.getString(poster_col));
        }
    }

    class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private int mId;
        private ImageView mImageView;
        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView=(ImageView)itemView;
            mImageView.setOnClickListener(this);
        }
        void bindImageInfo(int mid, String posterPath){
            mId=mid;
            String uri="http://image.tmdb.org/t/p/w185"+posterPath;
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
            Log.d(TAG,"li on click: "+ mSearchOrder + " ");
            MovieActivity movieActivity=(MovieActivity)getActivity();
            movieActivity.listItemSelected(mSearchOrder, mId);
        }
    }
}
