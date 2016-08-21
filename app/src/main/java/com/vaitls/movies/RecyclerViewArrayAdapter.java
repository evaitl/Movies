package com.vaitls.movies;

import android.support.v7.widget.RecyclerView;
import java.util.List;

/**
 * Created by evaitl on 8/19/16.
 * <p>
 * From <a href="http://goo.gl/jjfvTT">here</a>
 */
public abstract class RecyclerViewArrayAdapter<T, VH extends RecyclerView.ViewHolder> extends
        RecyclerView.Adapter<VH> {
    private List<T> items;

    public void bindData(final List<T> items) {
        this.items = items;
        this.notifyDataSetChanged();
    }

    public final T getItem(final int position) {
        return this.items.get(position);
    }

    public List<T> getItems() {
        return items;
    }

    @Override
    public int getItemCount() {
        return this.items != null
                ? this.items.size()
                : 0;
    }

    @Override
    public final void onBindViewHolder(final VH holder, final int position) {
        final T item = this.getItem(position);
        this.onBindViewHolder(holder, item);
    }

    public abstract void onBindViewHolder(final VH holder, final T item);
}