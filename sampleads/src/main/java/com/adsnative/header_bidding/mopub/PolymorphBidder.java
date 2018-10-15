package com.adsnative.header_bidding.mopub;

import android.content.Context;
import android.support.annotation.NonNull;

import com.adsnative.ads.ANAdListener;
import com.adsnative.ads.ANNativeAd;
import com.adsnative.ads.ANRequestParameters;
import com.adsnative.ads.NativeAdUnit;
import com.adsnative.ads.PrefetchAds;
import com.adsnative.util.ANLog;
import com.adsnative.util.Utils;
import com.mopub.nativeads.MoPubAdAdapter;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.RequestParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sijojohn on 18/07/17.
 */

public class PolymorphBidder {

    private Context mContext;
    private Double biddingInterval = 0.05;

    public PolymorphBidder(Context context) {
        this.mContext = context;
    }

    public void loadMopubAd(@NonNull final String pm_ad_unit_id, final MoPubNative moPubNative, final RequestParameters moPubrequestParameters) {
        List<String> keywords = new ArrayList<String>();
        keywords.add("&hb=1");
        ANRequestParameters requestParameters = new ANRequestParameters.Builder().keywords(keywords).build();
        ANNativeAd anNativeAd = new ANNativeAd(this.mContext, pm_ad_unit_id);
        anNativeAd.setNativeAdListener(new ANAdListener() {
            @Override
            public void onAdLoaded(NativeAdUnit nativeAdUnit) {
                if (PrefetchAds.getSize() > 0) {
                    PrefetchAds.getAd(); // clear stale prefetched ad
                }
                PrefetchAds.setAd(nativeAdUnit);
                Double ecpm = nativeAdUnit.getEcpm();

                // get bidding interval from server
                if (nativeAdUnit.getBiddingInterval() != null) {
                    biddingInterval = nativeAdUnit.getBiddingInterval();
                }
                ANLog.d("biddingInterval: " + biddingInterval);
                Double roundedEcpm = Utils.roundEcpm(ecpm, biddingInterval);

                if (roundedEcpm != null) {
                    String bidPrice = String.format("%.2f", roundedEcpm);
                    ANLog.d("passing ecpm of" + " " + bidPrice);
                    RequestParameters requestParameters;
                    if (moPubrequestParameters == null) {
                        requestParameters = new RequestParameters.Builder().keywords("ecpm:" + bidPrice).build();
                    } else {
                        requestParameters = new RequestParameters.Builder().keywords("ecpm: " + bidPrice + "," + moPubrequestParameters.getKeywords()).build();
                    }
                    moPubNative.makeRequest(requestParameters);
                } else {
                    if (moPubrequestParameters == null) {
                        moPubNative.makeRequest();
                    } else {
                        moPubNative.makeRequest(moPubrequestParameters);
                    }
                }
            }

            @Override
            public void onAdFailed(String message) {

                if (moPubrequestParameters == null) {
                    moPubNative.makeRequest();
                } else {
                    moPubNative.makeRequest(moPubrequestParameters);
                }
            }

            @Override
            public void onAdImpressionRecorded() {

            }

            @Override
            public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
                return false;
            }
        });
        anNativeAd.loadAd(requestParameters);
    }
}
