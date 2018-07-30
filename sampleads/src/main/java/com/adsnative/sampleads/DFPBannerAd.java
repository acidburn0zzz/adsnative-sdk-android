package com.adsnative.sampleads;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.adsnative.header_bidding.DFP.PolymorphBidder;
import com.adsnative.util.ANLog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.net.URLEncoder;

/**
 * Created by sijojohn on 25/07/18.
 */

public class DFPBannerAd extends Fragment {
    private PublisherAdView adView;

    private static String DFP_AD_UNIT_ID = "/6499/example/banner";
    private String PM_AD_UNIT_ID = "4U2khh1YfnOtZRTlQgk1ir_Il_JBY5ljLKk3pKnI";

    public void setAdUnitId(String AD_UNIT_ID) {
        if (AD_UNIT_ID != null && !AD_UNIT_ID.isEmpty()) {
            ANLog.e("Placement id: " + AD_UNIT_ID);
            this.PM_AD_UNIT_ID = URLEncoder.encode(AD_UNIT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_native_ad, container, false);
        final RelativeLayout nativeAdContainer = (RelativeLayout) view.findViewById(R.id.native_ad);

        adView = new PublisherAdView(this.getContext());
        adView.setAdSizes(AdSize.BANNER);
        adView.setAdUnitId(DFP_AD_UNIT_ID);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                nativeAdContainer.removeAllViews();
                nativeAdContainer.addView(adView);

            }
        });
        PolymorphBidder pm_bidder = new PolymorphBidder(getContext());
        pm_bidder.loadDFPBannerAd(PM_AD_UNIT_ID, adView);
//        adView.loadAd(new PublisherAdRequest.Builder().build());
        return view;
    }
}
