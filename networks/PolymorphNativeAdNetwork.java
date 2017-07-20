package com.mopub.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.adsnative.ads.ANAdListener;
import com.adsnative.ads.ANNativeAd;
import com.adsnative.ads.NativeAdUnit;
import com.adsnative.util.ANLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mopub.nativeads.NativeImageHelper.preCacheImages;

public class PolymorphNativeAdNetwork extends CustomEventNative {
    private static final String PLACEMENT_ID_KEY = "placement_id";

    // CustomEventNative implementation
    @Override
    protected void loadNativeAd(final Context context,
                                final CustomEventNativeListener customEventNativeListener,
                                final Map<String, Object> localExtras,
                                final Map<String, String> serverExtras) {

        final String placementId;
        if (extrasAreValid(serverExtras)) {
            placementId = serverExtras.get(PLACEMENT_ID_KEY);
        } else {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }
        final PolymorphStaticNativeAd polymorphStaticNativeAd = new PolymorphStaticNativeAd(
                context, new ANNativeAd(context, placementId), customEventNativeListener);
        polymorphStaticNativeAd.loadAd();

    }

    private boolean extrasAreValid(final Map<String, String> serverExtras) {
        final String placementId = serverExtras.get(PLACEMENT_ID_KEY);
        return (placementId != null && placementId.length() > 0);
    }

    static class PolymorphStaticNativeAd extends StaticNativeAd implements ANAdListener {

        private final Context mContext;
        private final ANNativeAd mNativeAd;
        private final CustomEventNativeListener mCustomEventNativeListener;
        View AdView;
        NativeAdUnit nativeAdUnit;
        private String mLandingURL;
        com.mopub.nativeads.NativeClickHandler mMopubClickHandler;

        PolymorphStaticNativeAd(final Context context,
                               final ANNativeAd nativeAd,
                               final CustomEventNativeListener customEventNativeListener) {
            mContext = context.getApplicationContext();
            mNativeAd = nativeAd;
            mCustomEventNativeListener = customEventNativeListener;
        }

        void loadAd() {
            mNativeAd.setNativeAdListener(this);
            mNativeAd.loadAd();
        }

        @Override
        public void prepare(final View view) {
            mNativeAd.attachViewForInteraction(nativeAdUnit,view);
            mMopubClickHandler = new com.mopub.nativeads.NativeClickHandler(mContext);
            mMopubClickHandler.setOnClickListener(view, this);
        }

        @Override
        public void clear(final View view) {
            mMopubClickHandler.clearOnClickListener(view);
        }

        @Override
        public void destroy() {
            mNativeAd.destroy();
        }

        @Override
        public void handleClick(@NonNull View view) {
            notifyAdClicked();
            mMopubClickHandler.openClickDestinationUrl(mLandingURL, view);
        }

        @Override
        public void onAdLoaded(NativeAdUnit nativeAdUnit) {
            this.nativeAdUnit = nativeAdUnit;
            setTitle(nativeAdUnit.getTitle());
            setText(nativeAdUnit.getSummary());
            setIconImageUrl(nativeAdUnit.getIconImage());
            setMainImageUrl(nativeAdUnit.getMainImage());
            setCallToAction(nativeAdUnit.getCallToAction());
            setStarRating(nativeAdUnit.getStarRating());
            setClickDestinationUrl(nativeAdUnit.getLandingUrl());
            mLandingURL = nativeAdUnit.getLandingUrl();
            List<String> impTrackers = nativeAdUnit.getImpressionTrackers();
            for(String tracker : impTrackers) {
                ANLog.e(tracker);
                addImpressionTracker(tracker);
            }
            List<String> clkTrackers = nativeAdUnit.getClickTrackers();
            for(String tracker : clkTrackers) {
                ANLog.e(tracker);
                addClickTracker(tracker);
            }
            final List<String> imageUrls = new ArrayList<String>();
            final String mainImageUrl = nativeAdUnit.getMainImage();
            if (mainImageUrl != null) {
                imageUrls.add(mainImageUrl);
            }
            final String iconUrl = nativeAdUnit.getIconImage();
            if (iconUrl != null) {
                imageUrls.add(iconUrl);
            }

            preCacheImages(mContext, imageUrls, new NativeImageHelper.ImageListener() {
                @Override
                public void onImagesCached() {
                    mCustomEventNativeListener.onNativeAdLoaded(PolymorphStaticNativeAd.this);
                }

                @Override
                public void onImagesFailedToCache(final NativeErrorCode errorCode) {
                    mCustomEventNativeListener.onNativeAdFailed(errorCode);
                }
            });

        }

        @Override
        public void onAdFailed(String message) {
            mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.SERVER_ERROR_RESPONSE_CODE);
        }

        @Override
        public void onAdImpressionRecorded() {
            notifyAdImpressed();
        }

        @Override
        public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
            return false;
        }
    }



}
