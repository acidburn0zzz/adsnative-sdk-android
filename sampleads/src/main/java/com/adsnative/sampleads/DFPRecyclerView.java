package com.adsnative.sampleads;

import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sijojohn on 25/09/18.
 */

public class DFPRecyclerView extends Fragment {

    DFPRecyclerAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.native_recycler_view);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView.
        mRecyclerView.setHasFixedSize(true);
        // Specify a linear layout manager.
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        List<String> items = new ArrayList<String>();
        for (int i = 0; i < 1000; ++i) {
            items.add("Recycler Placement " + i);
        }
        // Specify an adapter.
        adapter = new DFPRecyclerAdapter(getContext(), items);
        mRecyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        // You must call this or the ad adapter may cause a memory leak.
        adapter.destroy();
        super.onDestroyView();
    }
}
