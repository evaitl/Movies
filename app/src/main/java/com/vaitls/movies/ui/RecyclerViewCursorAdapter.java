package com.vaitls.movies.ui;


import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by evaitl on 8/19/16.
 *
 * @see <a href="http://goo.gl/7E3A08">here</a>
 * @see <a href="https://goo.gl/yAlH3R">this too</a>
 */
public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder> extends
    RecyclerView.Adapter<VH> {
    private static final String TAG = RecyclerViewCursorAdapter.class.getSimpleName();
    private Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;

    public RecyclerViewCursorAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
        super.setHasStableIds(mRowIdColumn != -1);
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        CursorAdapter ca;
        return 0;
    }


    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        onBindViewHolder(viewHolder, mCursor);
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    public void changeCursor(Cursor cursor) {
        Log.d(TAG, "changeCursor");
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }


    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    public Cursor swapCursor(Cursor newCursor) {
        Log.d(TAG, "swapCursor");
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            int oldRowIdColmun = mRowIdColumn;
            mRowIdColumn = newCursor.getColumnIndex("_id");


            mDataValid = true;
            Log.d(TAG, "calling ndsc");
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            Log.d(TAG, "calling ndsc mdv=false;");
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mDataValid = true;
            Log.d(TAG, "NDSO onchanged");
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            Log.d(TAG, "NDSO oninvalidated");
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}