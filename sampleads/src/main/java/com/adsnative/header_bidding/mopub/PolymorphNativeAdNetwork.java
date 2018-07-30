package com.adsnative.header_bidding.mopub;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.adsnative.ads.ANAdListener;
import com.adsnative.ads.ANNativeAd;
import com.adsnative.ads.NativeAdUnit;
import com.adsnative.ads.PrefetchAds;
import com.mopub.nativeads.CustomEventNative;
import com.mopub.nativeads.ImpressionTracker;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.NativeImageHelper;
import com.mopub.nativeads.StaticNativeAd;

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
        NativeAdUnit nativeAdUnit;

        if (PrefetchAds.getSize() > 0 && ((nativeAdUnit = PrefetchAds.getAd()) != null)) {
            final PolymorphStaticNativeAd polymorphStaticNativeAd = new PolymorphStaticNativeAd(
                    context, new ANNativeAd(context, placementId), nativeAdUnit, customEventNativeListener);
            polymorphStaticNativeAd.loadAd();

        } else {
            final PolymorphStaticNativeAd polymorphStaticNativeAd = new PolymorphStaticNativeAd(
                    context, new ANNativeAd(context, placementId), customEventNativeListener);
            polymorphStaticNativeAd.loadAd();

        }

    }

    private boolean extrasAreValid(final Map<String, String> serverExtras) {
        final String placementId = serverExtras.get(PLACEMENT_ID_KEY);
        return (placementId != null && placementId.length() > 0);
    }

    static class PolymorphStaticNativeAd extends StaticNativeAd implements ANAdListener {

        NativeAdUnit mNativeAdUnit;
        com.mopub.nativeads.NativeClickHandler mMopubClickHandler;
        private Context mContext;
        private ANNativeAd mNativeAd;
        private CustomEventNativeListener mCustomEventNativeListener;
        private String mLandingURL;
        private ImpressionTracker mMopubImpressionTracker;

        PolymorphStaticNativeAd(final Context context,
                                final ANNativeAd nativeAd,
                                final CustomEventNativeListener customEventNativeListener) {
            mContext = context.getApplicationContext();
            mNativeAd = nativeAd;
            mCustomEventNativeListener = customEventNativeListener;
        }

        public PolymorphStaticNativeAd(final Context context, final ANNativeAd anNativeAd, final NativeAdUnit nativeAdUnit, final CustomEventNativeListener customEventNativeListener) {
            mContext = context.getApplicationContext();
            mNativeAd = anNativeAd;
            mNativeAdUnit = nativeAdUnit;
            mCustomEventNativeListener = customEventNativeListener;
        }

        void loadAd() {
            if (this.mNativeAdUnit == null) {
                mNativeAd.setNativeAdListener(this);
                mNativeAd.loadAd();
            } else {
                this.onAdLoaded(mNativeAdUnit);
            }
        }

        @Override
        public void prepare(final View view) {
            mMopubClickHandler = new com.mopub.nativeads.NativeClickHandler(mContext);
            mMopubImpressionTracker = new ImpressionTracker(mContext);
            mMopubImpressionTracker.addView(view, this);
            mMopubClickHandler.setOnClickListener(view, this);
        }

        @Override
        public void clear(final View view) {
            if (mMopubImpressionTracker != null) {
                mMopubImpressionTracker.removeView(view);
            }
            if (mMopubClickHandler != null) {
                mMopubClickHandler.clearOnClickListener(view);
            }
        }

        @Override
        public void destroy() {
            if (mMopubImpressionTracker != null) {
                mMopubImpressionTracker.destroy();
            }
            if (mMopubClickHandler != null) {
                mNativeAd.destroy();
            }
        }

        @Override
        public void recordImpression(@NonNull View view) {
            notifyAdImpressed();
        }

        @Override
        public void handleClick(@NonNull View view) {
            notifyAdClicked();
            if (mMopubClickHandler != null) {
                mMopubClickHandler.openClickDestinationUrl(mLandingURL, view);
            }
        }

        @Override
        public void onAdLoaded(NativeAdUnit nativeAdUnit) {
            this.mNativeAdUnit = nativeAdUnit;
            setTitle(nativeAdUnit.getTitle());
            setText(nativeAdUnit.getSummary());
            setIconImageUrl(nativeAdUnit.getIconImage());
            setMainImageUrl(nativeAdUnit.getMainImage());
            setCallToAction(nativeAdUnit.getCallToAction());
            setStarRating(nativeAdUnit.getStarRating());
            setClickDestinationUrl(nativeAdUnit.getLandingUrl());
            setPrivacyInformationIconClickThroughUrl(nativeAdUnit.getAdChoicesClickThroughUrl());
            setPrivacyInformationIconImageUrl(nativeAdUnit.getAdChoicesIcon());
            mLandingURL = nativeAdUnit.getLandingUrl();
            List<String> impTrackers = nativeAdUnit.getImpressionTrackers();
            for (String tracker : impTrackers) {
                addImpressionTracker(tracker);
            }
            List<String> clkTrackers = nativeAdUnit.getClickTrackers();
            for (String tracker : clkTrackers) {
                addClickTracker(tracker);
            }
            final List<String> imageUrls = new ArrayList<String>();
            final String mainImageUrl = nativeAdUnit.getMainImage();
            if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
                imageUrls.add(mainImageUrl);
            }
            final String iconUrl = nativeAdUnit.getIconImage();
            if (iconUrl != null && !iconUrl.isEmpty()) {
                imageUrls.add(iconUrl);
            }
            final String privacyIconUrl = nativeAdUnit.getAdChoicesIcon();
            if (privacyIconUrl != null && !privacyIconUrl.isEmpty()) {
                imageUrls.add(privacyIconUrl);
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

        }

        @Override
        public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
            return false;
        }

    }


}
