package com.mopub.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.adsnative.ads.ANAdListener;
import com.adsnative.ads.ANNativeAd;
import com.adsnative.ads.NativeAdUnit;
import com.adsnative.ads.PMUnifiedAd;
import com.adsnative.ads.PrefetchAds;
import com.adsnative.util.ANLog;
import com.mopub.nativeads.BaseNativeAd;
import com.mopub.nativeads.CustomEventNative;
import com.mopub.nativeads.ImpressionTracker;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.NativeImageHelper;

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

        NativeAdUnit nativeAdUnit;

        if (PrefetchAds.getSize() > 0 && ((nativeAdUnit = PrefetchAds.getAd()) != null)) {
            if (nativeAdUnit.getMediaView() != null) {
                final PolymorphVideoEnabledAd polymorphStaticNativeAd = new PolymorphVideoEnabledAd(
                        context, nativeAdUnit, PrefetchAds.getAdRequest(), customEventNativeListener);
                polymorphStaticNativeAd.loadAd();
            } else {
                final PolymorphStaticNativeAd polymorphStaticNativeAd = new PolymorphStaticNativeAd(
                        context, nativeAdUnit, PrefetchAds.getAdRequest(), customEventNativeListener);
                polymorphStaticNativeAd.loadAd();
            }

        }

    }

    static class PolymorphVideoEnabledAd extends BaseNativeAd implements ANAdListener {

        NativeAdUnit mNativeAdUnit;
        com.mopub.nativeads.NativeClickHandler mMopubClickHandler;
        private Context mContext;
        private ANNativeAd mNativeAd;
        private CustomEventNativeListener mCustomEventNativeListener;
        private String mLandingURL;
        private ImpressionTracker mMopubImpressionTracker;
        private PMUnifiedAd mAdRequest;

        // Native ad assets.
        private String mTitle;
        private String mText;
        private String mMainImageUrl;
        private String mIconImageUrl;
        private String mCallToAction;
        private Double mStarRating;
        private String mAdvertiser;
        private String mStore;
        private String mPrice;
        private View mMediaView;

        public View getAdChoicesView() {
            return mAdChoicesView;
        }

        public void setAdChoicesView(View mAdChoicesView) {
            this.mAdChoicesView = mAdChoicesView;
        }

        private View mAdChoicesView;

        public String getLandingURL() {
            return mLandingURL;
        }

        public void setLandingURL(String mLandingURL) {
            this.mLandingURL = mLandingURL;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String mTitle) {
            this.mTitle = mTitle;
        }

        public String getText() {
            return mText;
        }

        public void setText(String mText) {
            this.mText = mText;
        }

        public String getMainImageUrl() {
            return mMainImageUrl;
        }

        public void setMainImageUrl(String mMainImageUrl) {
            this.mMainImageUrl = mMainImageUrl;
        }

        public String getIconImageUrl() {
            return mIconImageUrl;
        }

        public void setIconImageUrl(String mIconImageUrl) {
            this.mIconImageUrl = mIconImageUrl;
        }

        public String getCallToAction() {
            return mCallToAction;
        }

        public void setCallToAction(String mCallToAction) {
            this.mCallToAction = mCallToAction;
        }

        public View getMediaView() {
            return mMediaView;
        }

        public void setMediaView(View mMediaView) {
            this.mMediaView = mMediaView;
        }

        public PolymorphVideoEnabledAd(final Context context, final NativeAdUnit nativeAdUnit, PMUnifiedAd adRequest, final CustomEventNativeListener customEventNativeListener) {
            mContext = context.getApplicationContext();
            mNativeAdUnit = nativeAdUnit;
            mAdRequest = adRequest;
            mCustomEventNativeListener = customEventNativeListener;
        }

        void loadAd() {
            mNativeAdUnit.setPubCallbacksListener(this);
            if (this.mNativeAdUnit == null) {
                mNativeAd.loadAd();
            } else {
                this.onAdLoaded(mNativeAdUnit);
            }
        }

        @Override
        public void prepare(final View view) {
            mNativeAdUnit.prepare(view);
            mAdRequest.attachViewForInteraction(mNativeAdUnit, view);
        }

        @Override
        public void clear(final View view) {
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
        public void onAdLoaded(NativeAdUnit nativeAdUnit) {
            this.mNativeAdUnit = nativeAdUnit;
            setTitle(nativeAdUnit.getTitle());
            setText(nativeAdUnit.getSummary());
            setIconImageUrl(nativeAdUnit.getIconImage());
            setMainImageUrl(nativeAdUnit.getMainImage());
            setCallToAction(nativeAdUnit.getCallToAction());
            setLandingURL(nativeAdUnit.getLandingUrl());
            setMediaView(nativeAdUnit.getMediaView());
            setAdChoicesView(nativeAdUnit.getAdChoicesView());
            mLandingURL = nativeAdUnit.getLandingUrl();
            List<String> impTrackers = nativeAdUnit.getImpressionTrackers();

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
                    mCustomEventNativeListener.onNativeAdLoaded(PolymorphVideoEnabledAd.this);
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
            ANLog.e("ad impressed");
            notifyAdImpressed();
        }

        @Override
        public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
            ANLog.e("ad clicked");
            notifyAdClicked();
            return false;
        }

    }

    static class PolymorphStaticNativeAd extends StaticNativeAd implements ANAdListener {

        NativeAdUnit mNativeAdUnit;
        com.mopub.nativeads.NativeClickHandler mMopubClickHandler;
        private Context mContext;
        private ANNativeAd mNativeAd;
        private CustomEventNativeListener mCustomEventNativeListener;
        private String mLandingURL;
        private ImpressionTracker mMopubImpressionTracker;
        private PMUnifiedAd mAdRequest;

        public PolymorphStaticNativeAd(final Context context, final NativeAdUnit anNativeAd, final PMUnifiedAd adRequest, final CustomEventNativeListener customEventNativeListener) {
            mContext = context.getApplicationContext();
            mNativeAdUnit = anNativeAd;
            mAdRequest = adRequest;
            mCustomEventNativeListener = customEventNativeListener;
        }

        void loadAd() {
            mNativeAdUnit.setPubCallbacksListener(this);
            if (this.mNativeAdUnit == null) {
                mNativeAd.loadAd();
            } else {
                this.onAdLoaded(mNativeAdUnit);
            }
        }

        @Override
        public void prepare(final View view) {
            mAdRequest.attachViewForInteraction(mNativeAdUnit, view);
        }

        @Override
        public void clear(final View view) {
        }

        @Override
        public void destroy() {
        }

        @Override
        public void recordImpression(@NonNull View view) {
            notifyAdImpressed();
        }

        @Override
        public void handleClick(@NonNull View view) {
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
            ANLog.e("ad impressed");
            notifyAdImpressed();

        }

        @Override
        public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
            ANLog.e("ad clicked");
            notifyAdClicked();
            return false;
        }

    }


}
