package com.adsnative.sampleads;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adsnative.ads.ANAdPositions;
import com.adsnative.ads.ANAdViewBinder;
import com.adsnative.ads.ANRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewFragment extends Fragment {

    private ANRecyclerAdapter mRecyclerAdapter;

    private String AD_UNIT_ID = "2Pwo1otj1C5T8y6Uuz9v-xbY1aB09x8rWKvsJ-HI";

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.native_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<String> items = new ArrayList<String>();
        for (int i = 0; i < 100; ++i) {
            items.add("Recycler Placement " + i);
        }

        final MyRecyclerAdapter originalAdapter = new MyRecyclerAdapter(getActivity(), items);

        /*
        // Create an ad adapter that gets its positioning information from the AdsNative Ad Server.
        // This adapter will be used in place of the original adapter for the ListView.
        ANAdPositions.ServerPositions serverPositions =
                ANAdPositions.serverPositioning();
        */

        // Create an ad adapter that gets its positioning information from client.
        // This adapter will be used in place of the original adapter for the ListView.
        ANAdPositions.ClientPositions clientPositions = ANAdPositions.clientPositioning();
        clientPositions.addFixedPosition(5);
        clientPositions.enableRepeatingPositions(18);

        mRecyclerAdapter = new ANRecyclerAdapter(getActivity(), originalAdapter, AD_UNIT_ID, clientPositions);

        // Set up an renderer that knows how to put ad data in an ad view.
        final ANAdViewBinder anAdViewBinder = new ANAdViewBinder.Builder(R.layout.fan_native_layout)
                        .bindAssetsWithDefaultKeys(getActivity())
                        .build();

        // Register the renderer with the ANListAdapter and then set the adapter on the ListView.
        mRecyclerAdapter.registerViewBinder(anAdViewBinder);
        recyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerAdapter.loadAds();

        return view;
    }

    @Override
    public void onDestroyView() {
        // You must call this or the ad adapter may cause a memory leak.
        mRecyclerAdapter.destroy();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        /*
        // AdsNative recommends loading new ads when the user returns to your activity.
        anListAdapter.loadAds();
        */
        super.onResume();
    }
}
