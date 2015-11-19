package com.adsnative.sampleads;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.adsnative.ads.ANAdListener;
import com.adsnative.ads.ANAdViewBinder;
import com.adsnative.ads.ANNativeAd;
import com.adsnative.ads.NativeAdUnit;
import com.adsnative.util.ANLog;

public class NativeAdFragment extends Fragment {

    private ANNativeAd mNativeAd;
    // private String AD_UNIT_ID = "2bMP97UQpLEiavFiqi7Cnw2BpDmqEau_ZUdDQzug";
    private String AD_UNIT_ID = "I6jzxM3nheJk4RVIstiPKGN7YHOBKag-Q_5b0AnV";

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_native_ad, container, false);
        final RelativeLayout nativeAdContainer = (RelativeLayout) view.findViewById(R.id.native_ad);

        mNativeAd = new ANNativeAd(getActivity(), AD_UNIT_ID);
        mNativeAd.setNativeAdListener(new ANAdListener() {
            @Override
            public void onAdLoaded(NativeAdUnit nativeAdUnit) {
                ANLog.d("PUBLISHER CALLBACK : onAdLoaded()");
                nativeAdContainer.removeAllViews();
                nativeAdContainer.addView(mNativeAd.renderAdView(nativeAdUnit));
            }

            @Override
            public void onAdFailed(String message) {
                ANLog.d("PUBLISHER CALLBACK : onAdFailed()");
            }

            @Override
            public void onAdImpressionRecorded() {
                ANLog.d("PUBLISHER CALLBACK : onAdImpressionRecorded()");
            }

            @Override
            public void onAdClicked() {
                ANLog.d("PUBLISHER CALLBACK : onAdClicked()");
            }
        });

        // Set up view binder that knows how to put ad data in an ad view.
        final ANAdViewBinder anAdViewBinder = new ANAdViewBinder.Builder(R.layout.list_view_ad_unit)
                .bindAssetsWithDefaultKeys(getActivity())
                .build();
        mNativeAd.registerViewBinder(anAdViewBinder);

        return view;
    }

    @Override
    public void onDestroyView() {
        // You must call this or the ad adapter may cause a memory leak.
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        // AdsNative recommends loading new ads when the user returns to your activity.
        mNativeAd.loadAd();
        super.onResume();
    }
}
