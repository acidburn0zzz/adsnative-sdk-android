package com.adsnative.header_bidding.DFP;

import android.content.Context;
import android.os.Bundle;

import com.adsnative.ads.PMBannerAdListener;
import com.adsnative.ads.PMBannerView;
import com.adsnative.ads.PrefetchAds;
import com.adsnative.util.ANLog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

/**
 * Created by sijojohn on 12/06/18.
 */

public class PolymorphBannerAdapter implements CustomEventBanner {

    @Override
    public void requestBannerAd(Context context, CustomEventBannerListener customEventBannerListener, String s, AdSize adSize, MediationAdRequest mediationAdRequest, Bundle bundle) {
        PMBannerView bannerView;
        // check if there's a cached PM banner ad
        if (PrefetchAds.getBannerSize() > 0 && ((bannerView = PrefetchAds.getBannerAd()) != null)) {
            final PolymorphStaticBannerAd polymorphStaticNativeAd = new PolymorphStaticBannerAd(
                    context, bannerView, customEventBannerListener, mediationAdRequest);
            polymorphStaticNativeAd.loadAd();

        } else {
            ANLog.d("Couldn't find Prefetched ads. Making a fresh request to PM");
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }


    static class PolymorphStaticBannerAd implements PMBannerAdListener {

        private Context mContext;
        private PMBannerView mBannerView;
        private CustomEventBannerListener mCustomEventBannerListener;
        private MediationAdRequest mMediationRequest;

        PolymorphStaticBannerAd(final Context context,
                                final PMBannerView bannerView,
                                final CustomEventBannerListener customEventBannerListener, MediationAdRequest mediaitionAdRequest) {
            mContext = context;
            mBannerView = bannerView;
            mCustomEventBannerListener = customEventBannerListener;
            mMediationRequest = mediaitionAdRequest;
        }

        void loadAd() {
            this.onBannerAdLoaded(mBannerView);
        }

        @Override
        public void onBannerAdLoaded(PMBannerView bannerView) {
            mCustomEventBannerListener.onAdLoaded(bannerView);
        }

        @Override
        public void onBannerAdClicked(PMBannerView bannerView) {
            mCustomEventBannerListener.onAdClicked();
            mCustomEventBannerListener.onAdOpened();
            mCustomEventBannerListener.onAdLeftApplication();
        }

        @Override
        public void onBannerAdFailed(String message) {
            ANLog.e(message);
            mCustomEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
        }

    }
}
