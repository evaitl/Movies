package com.vaitls.movies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by evaitl on 7/30/16.
 */
public class PostersFragment extends Fragment{
    private RecyclerView mPostersRecylerView;
    public static PostersFragment newInstance() {
        return new PostersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View v=inflater.inflate(R.layout.fragment_posters, container, false);
        mPostersRecylerView = (RecyclerView) v.findViewById(R.id.fragment_posters_recycler_view);
        mPostersRecylerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
