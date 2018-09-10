package com.adsnative.sampleads;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.adsnative.ads.ANAdPositions;
import com.adsnative.ads.ANAdViewBinder;
import com.adsnative.ads.ANListAdapter;

public class ListViewFragment extends Fragment {

    private ANListAdapter mListAdapter;

    private String AD_UNIT_ID = "2Pwo1otj1C5T8y6Uuz9v-xbY1aB09x8rWKvsJ-HI";

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        final ListView listView = (ListView) view.findViewById(R.id.native_list_view);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1);
        for (int i = 0; i < 100; ++i) {
            adapter.add("Placement " + i);
        }

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
        clientPositions.enableRepeatingPositions(10);

        mListAdapter = new ANListAdapter(getActivity(), adapter, AD_UNIT_ID, clientPositions);

        // Set up an renderer that knows how to put ad data in an ad view.
        final ANAdViewBinder anAdViewBinder = new ANAdViewBinder.Builder(R.layout.fan_native_layout)
                        .bindAssetsWithDefaultKeys(getActivity())
                        .build();

        // Register the renderer with the ANListAdapter and then set the adapter on the ListView.
        mListAdapter.registerViewBinder(anAdViewBinder);
        listView.setAdapter(mListAdapter);
        mListAdapter.loadAds();

        return view;
    }

    @Override
    public void onDestroyView() {
        // You must call this or the ad adapter may cause a memory leak.
        mListAdapter.destroy();
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
