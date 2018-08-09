package com.adsnative.header_bidding.DFP;

import android.content.Context;

import com.adsnative.ads.ANAdListener;
import com.adsnative.ads.ANRequestParameters;
import com.adsnative.ads.AdHelper;
import com.adsnative.ads.NativeAdUnit;
import com.adsnative.ads.PMBannerAdListener;
import com.adsnative.ads.PMBannerView;
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
    private String PMplacementId;
    private AdLoader adLoader;
    private PublisherAdView pubAdView;
    private PMUnifiedAd PMunifiedAd;
    private String requestType;

    // Native and Banner ads
    public PolymorphBidder(Context context, String pmPlacementId, AdLoader adLoader, AdHelper.AdSize adSize) {
        this.mContext = context;
        this.bannerHeight = adSize.getHeight();
        this.bannerWidth = adSize.getWidth();
        this.PMplacementId = pmPlacementId;
        this.adLoader = adLoader;
        this.PMunifiedAd = new PMUnifiedAd(this.mContext, this.PMplacementId, AdHelper.AdTypes.AD_TYPE_ALL);
        this.PMunifiedAd.setBannerSize(this.bannerWidth, this.bannerHeight);
        requestType = AdHelper.AdTypes.AD_TYPE_ALL.toString();
    }

    // Only native ads
    public PolymorphBidder(Context context, String pmPlacementId, AdLoader adLoader) {
        this.mContext = context;
        this.PMplacementId = pmPlacementId;
        this.adLoader = adLoader;
        this.PMunifiedAd = new PMUnifiedAd(this.mContext, this.PMplacementId, AdHelper.AdTypes.AD_TYPE_NATIVE);
        requestType = AdHelper.AdTypes.AD_TYPE_NATIVE.toString();
    }

    // Only banner ads
    public PolymorphBidder(Context context, String pmPlacementId, PublisherAdView pubAdView) {
        this.mContext = context;
        this.PMplacementId = pmPlacementId;
        this.pubAdView = pubAdView;
        this.PMunifiedAd = new PMUnifiedAd(this.mContext, this.PMplacementId, AdHelper.AdTypes.AD_TYPE_BANNER);
        requestType = AdHelper.AdTypes.AD_TYPE_BANNER.toString();
    }

    public void setBiddingInterval(Double biddingInterval) {
        this.biddingInterval = biddingInterval;
    }

    public void setPubAdRequestBuilder(Builder pubBuilder) {
        this.pubBuilder = pubBuilder;
    }

    public void loadDFPAd() {
        if (requestType.equalsIgnoreCase(AdHelper.AdTypes.AD_TYPE_ALL.toString())) {
            loadDFPUnifiedAd();
        }
        if (requestType.equalsIgnoreCase(AdHelper.AdTypes.AD_TYPE_BANNER.toString())) {
            loadDFPBannerAd();
        }
        if (requestType.equalsIgnoreCase(AdHelper.AdTypes.AD_TYPE_NATIVE.toString())) {
            loadDFPNativeAd();
        }
    }

    // DFP Native Ads
    private void loadDFPNativeAd() {
        if (pubBuilder == null) {
            pubBuilder = new PublisherAdRequest.Builder();
        }
        List<String> keywords = new ArrayList<String>();
        keywords.add("&hb=1");
        ANRequestParameters requestParameters = new ANRequestParameters.Builder().keywords(keywords).build();

        PMunifiedAd.setNativeAdListener(new ANAdListener() {
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
        PMunifiedAd.loadAd(requestParameters);
    }

    // DFP Banner Ads
    private void loadDFPBannerAd() {
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
        PMunifiedAd.setBannerSize(bannerWidth, bannerHeight);
        PMunifiedAd.setBannerAdListener(new PMBannerAdListener() {
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
        PMunifiedAd.loadAd(requestParameters);
    }

    // DFP Native and Banner Ads
    private void loadDFPUnifiedAd() {
        if (pubBuilder == null) {
            pubBuilder = new PublisherAdRequest.Builder();
        }
        List<String> keywords = new ArrayList<String>();
        keywords.add("&hb=1");
        ANRequestParameters requestParameters = new ANRequestParameters.Builder().keywords(keywords).build();

        PMunifiedAd.setBannerSize(bannerWidth, bannerHeight);
        PMunifiedAd.setBannerAdListener(new PMBannerAdListener() {
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
                    adLoader.loadAd(newRequest);
                } else {
                    adLoader.loadAd(pubBuilder.build());
                }
            }

            @Override
            public void onBannerAdClicked(PMBannerView bannerView) {
                ANLog.d("PM banner clicked");
            }

            @Override
            public void onBannerAdFailed(String message) {
                adLoader.loadAd(pubBuilder.build());
            }
        });
        PMunifiedAd.setNativeAdListener(new ANAdListener() {
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

            }

            @Override
            public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
                return false;
            }
        });
        PMunifiedAd.loadAd(requestParameters);

    }

    public void setBannerSize(AdHelper.AdSize bannerSize) {
        this.bannerWidth = bannerSize.getWidth();
        this.bannerHeight = bannerSize.getHeight();
    }
}
