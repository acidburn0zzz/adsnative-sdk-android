package com.adsnative.header_bidding.DFP;

import android.content.Context;
import android.support.annotation.NonNull;

import com.adsnative.ads.ANAdListener;
import com.adsnative.ads.PMBannerAdListener;
import com.adsnative.ads.PMBannerView;
import com.adsnative.ads.ANNativeAd;
import com.adsnative.ads.ANRequestParameters;
import com.adsnative.ads.AdHelper;
import com.adsnative.ads.NativeAdUnit;
import com.adsnative.ads.PMUnifiedAd;
import com.adsnative.ads.PrefetchAds;
import com.adsnative.util.ANLog;
import com.adsnative.util.Utils;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest.Builder;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sijojohn on 11/06/18.
 */

public class PolymorphBidder {

    private Context mContext;
    private Double biddingInterval = 0.05;
    private Builder pubBuilder;
    private int bannerWidth = 0;
    private int bannerHeight = 0;

    public PolymorphBidder(Context context) {
        this.mContext = context;
    }

    public void setBiddingInterval(Double biddingInterval) {
        this.biddingInterval = biddingInterval;
    }

    public void setPubAdRequestBuilder(Builder pubBuilder) {
        this.pubBuilder = pubBuilder;
    }

    public void loadDFPAd(@NonNull final String pmPlacementId, @NonNull final AdLoader adLoader) {
        if (pubBuilder == null) {
            pubBuilder = new PublisherAdRequest.Builder();
        }
        List<String> keywords = new ArrayList<String>();
        keywords.add("&hb=1");
        ANRequestParameters requestParameters = new ANRequestParameters.Builder().keywords(keywords).build();
        ANNativeAd anNativeAd = new ANNativeAd(this.mContext, pmPlacementId);

        anNativeAd.setNativeAdListener(new ANAdListener() {
            @Override
            public void onAdLoaded(NativeAdUnit nativeAdUnit) {
                if (PrefetchAds.getSize() > 0) {
                    PrefetchAds.getAd(); // clear cache
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
                    PublisherAdRequest newRequest = pubBuilder
                            .addCustomTargeting("ecpm", bidPrice)
                            .build();
                    adLoader.loadAd(newRequest);
                } else {
                    adLoader.loadAd(pubBuilder.build());
                }
            }

            @Override
            public void onAdFailed(String message) {
                adLoader.loadAd(pubBuilder.build());
            }

            @Override
            public void onAdImpressionRecorded() {
                ANLog.d("PM impression recorded");

            }

            @Override
            public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
                ANLog.d("PM native Ad Clicked");
                return false;
            }
        });
        anNativeAd.loadAd(requestParameters);
    }

    public void loadDFPBannerAd(@NonNull final String pmPlacementId, @NonNull final PublisherAdView pubAdView) {
        if (pubBuilder == null) {
            pubBuilder = new PublisherAdRequest.Builder();
        }
        List<String> keywords = new ArrayList<String>();
        keywords.add("&hb=1");
        ANRequestParameters requestParameters = new ANRequestParameters.Builder().keywords(keywords).build();
        if (pubAdView.getAdSize() == null) {
            ANLog.e("AdSize not set, please use setAdSizes() method");
            return;
        } else {
            bannerWidth = pubAdView.getAdSize().getWidth();
            bannerHeight = pubAdView.getAdSize().getHeight();
        }
        PMUnifiedAd pmUnifiedAd = new PMUnifiedAd(this.mContext, pmPlacementId, AdHelper.AdTypes.AD_TYPE_BANNER);
        pmUnifiedAd.setBannerSize(bannerWidth, bannerHeight);
        pmUnifiedAd.setBannerAdListener(new PMBannerAdListener() {
            @Override
            public void onBannerAdLoaded(PMBannerView bannerView) {
                if (PrefetchAds.getBannerSize() > 0) {
                    PrefetchAds.getBannerAd(); // clear cache
                }
                PrefetchAds.setBannerAd(bannerView);
                Double ecpm = bannerView.getEcpm();

                // get bidding interval from server
                if (bannerView.getBiddingInterval() != null) {
                    biddingInterval = bannerView.getBiddingInterval();
                }
                ANLog.d("biddingInterval: " + biddingInterval);
                Double roundedEcpm = Utils.roundEcpm(ecpm, biddingInterval);

                if (roundedEcpm != null) {
                    String bidPrice = String.format("%.2f", roundedEcpm);
                    PublisherAdRequest newRequest = pubBuilder
                            .addCustomTargeting("ecpm", bidPrice)
                            .build();
                    ANLog.d("passing ecpm of " + bidPrice);
                    pubAdView.loadAd(newRequest);
                } else {
                    pubAdView.loadAd(pubBuilder.build());
                }
            }

            @Override
            public void onBannerAdClicked(PMBannerView bannerView) {
                ANLog.d("PM banner clicked");
            }

            @Override
            public void onBannerAdFailed(String message) {
                pubAdView.loadAd(pubBuilder.build());
            }
        });
        pmUnifiedAd.loadAd(requestParameters);
    }
}
