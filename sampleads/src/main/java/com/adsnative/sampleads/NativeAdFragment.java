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

import java.net.URLEncoder;

public class NativeAdFragment extends Fragment {

    private ANNativeAd mNativeAd;

    private String AD_UNIT_ID = "ping";

    public void setAdUnitId(String AD_UNIT_ID) {
        if(AD_UNIT_ID != null && !AD_UNIT_ID.isEmpty()) {
            ANLog.e("Placement id: "+AD_UNIT_ID);
            this.AD_UNIT_ID = URLEncoder.encode(AD_UNIT_ID);
        }
    }

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
                ANLog.e("PUBLISHER CALLBACK : onAdLoaded()");
                nativeAdContainer.removeAllViews();
                nativeAdContainer.addView(mNativeAd.renderAdView(nativeAdUnit));
            }

            @Override
            public void onAdFailed(String message) {
                ANLog.e("PUBLISHER CALLBACK : onAdFailed() - " + message);
            }

            @Override
            public void onAdImpressionRecorded() {
                ANLog.e("PUBLISHER CALLBACK : onAdImpressionRecorded()");
            }

            @Override
            public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
                ANLog.e("PUBLISHER CALLBACK : onAdClicked()");
                return false;
            }
        });

        // Set up view binder that knows how to put ad data in an ad view.
        final ANAdViewBinder anAdViewBinder = new ANAdViewBinder.Builder(R.layout.fan_native_layout)
                .bindAssetsWithDefaultKeys(getActivity())
                .bindAdChoices(R.id.an_ad_choices)
                .build();
        mNativeAd.registerViewBinder(anAdViewBinder);
        mNativeAd.loadAd();

        return view;
    }

    @Override
    public void onDestroyView() {
        // You must call this or the ad adapter may cause a memory leak.
        mNativeAd.destroy();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        // AdsNative recommends loading new ads when the user returns to your activity.
        // mNativeAd.loadAd();
        super.onResume();
    }
}
