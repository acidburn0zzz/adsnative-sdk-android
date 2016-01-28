package com.adsnative.mediation;

import android.content.Context;
import android.view.View;

import com.adsnative.ads.BaseNativeAd;
import com.adsnative.ads.ErrorCode;
import com.adsnative.network.AdResponse;
import com.adsnative.util.ANLog;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.ImpressionListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAd.Rating;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sreekanth on 17/09/15.
 */
public class FacebookAdNetwork extends CustomAdNetwork {
    private static final String PLACEMENT_ID_KEY = "placementId";

    // CustomAdNetwork implementation
    @Override
    protected void loadNativeAd(final Context context,
                                final CustomEventListener customEventListener,
                                final AdResponse adResponse) {

        String placementId = null;
        JSONObject customAdNetworkData = adResponse.getCustomAdNetworkData();
        try {
            placementId = customAdNetworkData.getString(PLACEMENT_ID_KEY);
            ANLog.d("FacebookAdNetwork: " + placementId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (placementId == null || placementId.length() <= 0) {
            ANLog.e("FacebookAdNetwork: " + ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            customEventListener.onNativeAdFailed(ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        final FacebookNativeAd facebookNativeAd =
                new FacebookNativeAd(context,
                        new NativeAd(context, placementId), customEventListener);
        facebookNativeAd.loadAd();
    }

    static class FacebookNativeAd extends BaseNativeAd implements AdListener, ImpressionListener {
        private static final String SOCIAL_CONTEXT_FOR_AD = "socialContextForAd";

        private final Context mContext;
        private final NativeAd mFbNativeAd;
        private final CustomEventListener mCustomEventListener;

        FacebookNativeAd(final Context context,
                         final NativeAd nativeAd,
                         final CustomEventListener customEventListener) {
            mContext = context.getApplicationContext();
            mFbNativeAd = nativeAd;
            mCustomEventListener = customEventListener;
        }

        void loadAd() {
            // for testing purpose only
            AdSettings.addTestDevice("8409a7303438f26bbe3dd223c381d3d1");
            // for testing purpose only
            mFbNativeAd.setAdListener(this);
            mFbNativeAd.setImpressionListener(this);
            mFbNativeAd.loadAd();
        }

        // AdListener
        @Override
        public void onAdLoaded(final Ad ad) {
            ANLog.d("FacebookAdNetwork#onAdLoaded");
            // This identity check is from Facebook's Native API sample code:
            // https://developers.facebook.com/docs/audience-network/android/native-api
            if (!mFbNativeAd.equals(ad) || !mFbNativeAd.isAdLoaded()) {
                ANLog.e("FacebookAdNetwork: " + ErrorCode.NETWORK_INVALID_STATE);
                mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_INVALID_STATE);
                return;
            }

            setProviderName(FacebookAdNetwork.class.getName());

            setTitle(mFbNativeAd.getAdTitle());
            setSummary(mFbNativeAd.getAdBody());

            NativeAd.Image coverImage = mFbNativeAd.getAdCoverImage();
            setMainImage(coverImage == null ? null : coverImage.getUrl());

            NativeAd.Image icon = mFbNativeAd.getAdIcon();
            setIconImage(icon == null ? null : icon.getUrl());

            setCallToAction(mFbNativeAd.getAdCallToAction());
            setStarRating(getDoubleRating(mFbNativeAd.getAdStarRating()));

            NativeAd.Image adChoices = mFbNativeAd.getAdChoicesIcon();
            setAdChoicesIcon(adChoices == null ? null : adChoices.getUrl());
            setAdChoicesClickThroughUrl(mFbNativeAd.getAdChoicesLinkUrl());

            setPromotedByTag("Sponsored");

            addCustomField(SOCIAL_CONTEXT_FOR_AD, mFbNativeAd.getAdSocialContext());

            final List<String> imageUrls = new ArrayList<String>();
            final String mainImageUrl = getMainImage();
            if (mainImageUrl != null) {
                imageUrls.add(mainImageUrl);
            }
            final String iconUrl = getIconImage();
            if (iconUrl != null) {
                imageUrls.add(iconUrl);
            }

            try {
                preCacheImages(mContext, imageUrls, new ImageListener() {
                    @Override
                    public void onImagesCached() {
                        mCustomEventListener.onNativeAdLoaded(FacebookNativeAd.this);
                    }

                    @Override
                    public void onImagesFailedToCache(ErrorCode errorCode) {
                        ANLog.e("FacebookAdNetwork: " + errorCode);
                        mCustomEventListener.onNativeAdLoaded(FacebookNativeAd.this);
                        // mCustomEventListener.onNativeAdFailed(errorCode);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(final Ad ad, final AdError adError) {
            ANLog.e("FacebookAdNetwork#onError");
            if (adError == null) {
                ANLog.e("FacebookAdNetwork: " + ErrorCode.UNSPECIFIED);
                mCustomEventListener.onNativeAdFailed(ErrorCode.UNSPECIFIED);
            } else if (adError.getErrorCode() == AdError.NO_FILL.getErrorCode()) {
                ANLog.e("FacebookAdNetwork: " + ErrorCode.NETWORK_NO_FILL);
                mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
            } else if (adError.getErrorCode() == AdError.INTERNAL_ERROR.getErrorCode()) {
                ANLog.e("FacebookAdNetwork: " + ErrorCode.NETWORK_INVALID_STATE + " : " +
                        AdError.INTERNAL_ERROR.getErrorCode());
                mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_INVALID_STATE);
            } else {
                ANLog.e("FacebookAdNetwork: " + adError.getErrorMessage());
                mCustomEventListener.onNativeAdFailed(ErrorCode.UNSPECIFIED);
            }
        }

        @Override
        public void onAdClicked(final Ad ad) {
            notifyAdClicked();
        }

        // ImpressionListener
        @Override
        public void onLoggingImpression(final Ad ad) {
            notifyAdImpressed();
        }

        // BaseNativeAd
        @Override
        public void prepare(final View view) {
            mFbNativeAd.registerViewForInteraction(view);
            setOverridingClickTracker(true);
            setOverridingImpressionTracker(true);
        }

        @Override
        public void clear(final View view) {
            mFbNativeAd.unregisterView();
        }

        @Override
        public void destroy() {
            mFbNativeAd.destroy();
        }

        private Double getDoubleRating(final Rating rating) {
            if (rating == null) {
                return null;
            }

            return MAX_STAR_RATING * rating.getValue() / rating.getScale();
        }
    }
}