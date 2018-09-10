package com.adsnative.sampleads;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.adsnative.ads.ANAdListener;
import com.adsnative.ads.ANAdViewBinder;
import com.adsnative.ads.PMBannerAdListener;
import com.adsnative.ads.PMBannerView;
import com.adsnative.ads.AdHelper;
import com.adsnative.ads.AdHelper.AdTypes;
import com.adsnative.ads.NativeAdUnit;
import com.adsnative.ads.PMUnifiedAd;
import com.adsnative.util.ANLog;

import java.net.URLEncoder;

public class BannerAdFragment extends Fragment {

    int width;
    int height;
    private PMUnifiedAd pmUnifiedAd;
    private String AD_UNIT_ID = "ydxcoGD-SPr_w7AItL5VRHVXwZle6TD2S6I4MKf7";

    public void setAdUnitId(String AD_UNIT_ID) {
        if (AD_UNIT_ID != null && !AD_UNIT_ID.isEmpty()) {
            ANLog.e("Placement id: " + AD_UNIT_ID);
            if (AD_UNIT_ID.contains(",")) {
                String a[] = AD_UNIT_ID.split(",");
                width = Integer.parseInt(a[0]);
                height = Integer.parseInt(a[1]);
            } else {
                this.AD_UNIT_ID = URLEncoder.encode(AD_UNIT_ID);
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View nativeView = inflater.inflate(R.layout.fragment_native_ad, container, false);
        final RelativeLayout nativeAdContainer = (RelativeLayout) nativeView.findViewById(R.id.native_ad);
        pmUnifiedAd = new PMUnifiedAd(getActivity(), AD_UNIT_ID, AdTypes.AD_TYPE_ALL);
        pmUnifiedAd.setBannerSize(AdHelper.AdSize.BANNER_300x250);
        final ANAdViewBinder anAdViewBinder = new ANAdViewBinder.Builder(R.layout.fan_native_layout)
                .bindAssetsWithDefaultKeys(getActivity())
                .bindAdChoices(R.id.an_ad_choices)
                .build();
        pmUnifiedAd.registerViewBinder(anAdViewBinder);
        pmUnifiedAd.setBannerAdListener(new PMBannerAdListener() {
            @Override
            public void onBannerAdLoaded(PMBannerView bannerView) {
                ANLog.e("onBannerAdLoaded");
                nativeAdContainer.removeAllViews();
                nativeAdContainer.addView(bannerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            }

            @Override
            public void onBannerReceived(PMBannerView bannerView) {

            }

            @Override
            public void onBannerAdClicked(PMBannerView bannerView) {
                ANLog.e("onBannerAdClicked");
            }

            @Override
            public void onBannerAdFailed(String message) {
                ANLog.e(message);
            }

        });
        pmUnifiedAd.setNativeAdListener(new ANAdListener() {
            @Override
            public void onAdLoaded(NativeAdUnit nativeAdUnit) {
                ANLog.e("onNativeLoaded");
                nativeAdContainer.removeAllViews();
                nativeAdContainer.addView(pmUnifiedAd.renderAdView(nativeAdUnit));
            }

            @Override
            public void onAdFailed(String message) {

            }

            @Override
            public void onAdImpressionRecorded() {

            }

            @Override
            public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
                return false;
            }
        });
        pmUnifiedAd.loadAd();

        return nativeView;

    }

    @Override
    public void onDestroyView() {
        // You must call this or the ad adapter may cause a memory leak.
        pmUnifiedAd.destroy();
        pmUnifiedAd.destroyBanner();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        // AdsNative recommends loading new ads when the user returns to your activity.
        // pmUnifiedAd.loadAd();
        super.onResume();
    }
}
