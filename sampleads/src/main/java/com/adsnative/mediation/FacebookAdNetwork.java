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
import com.facebook.ads.MediaView;
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
        JSONObject customAdNetworkData = null;
        if (adResponse != null) {
            customAdNetworkData = adResponse.getCustomAdNetworkData();
        } else {
            ANLog.e("Attempted to invoke getCustomAdNetworkData on null adResponse");
        }
        try {
            if (customAdNetworkData != null) {
                placementId = customAdNetworkData.getString(PLACEMENT_ID_KEY);
//            placementId = "202991423187328_724667197686412";
                ANLog.d("FacebookAdNetwork: " + placementId);
            } else {
                ANLog.e("Attempted to invoke getString on null customAdNetworkData");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (placementId == null || placementId.length() <= 0) {
            ANLog.e("FacebookAdNetwork: " + ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            if (customEventListener != null) {
                customEventListener.onNativeAdFailed(ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            } else {
                ANLog.e("Attempted to invoke onNativeAdFailed on null customEventListener");
            }
            return;
        }
        // handle any exception that might come from 3rd party class
        try {
            final FacebookNativeAd facebookNativeAd =
                    new FacebookNativeAd(context,
                            new NativeAd(context, placementId), customEventListener);
            if (facebookNativeAd != null) {
                facebookNativeAd.loadAd();
            } else {
                ANLog.e("Attempted to invoke loadAd on null facebookNativeAd");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
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
//            AdSettings.addTestDevice("8d98c3d2ec70e7d185403ed007386f12");
            // for testing purpose only
            if (mFbNativeAd != null) {
                mFbNativeAd.setAdListener(this);
                mFbNativeAd.setImpressionListener(this);
                mFbNativeAd.loadAd();
            } else {
                ANLog.e("mFbNativeAd is null");
            }
        }

        // AdListener
        @Override
        public void onAdLoaded(final Ad ad) {
            ANLog.d("FacebookAdNetwork#onAdLoaded");
            // This identity check is from Facebook's Native API sample code:
            // https://developers.facebook.com/docs/audience-network/android/native-api

            if (mFbNativeAd != null && !mFbNativeAd.equals(ad) || !mFbNativeAd.isAdLoaded()) {
                ANLog.e("FacebookAdNetwork: " + ErrorCode.NETWORK_INVALID_STATE);
                if (mCustomEventListener != null) {
                    mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_INVALID_STATE);
                }
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

            setType("facebook");
            MediaView fbMediaView = new MediaView(mContext);
            fbMediaView.setNativeAd(mFbNativeAd);
            setMediaView(fbMediaView);

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
            if (mFbNativeAd != null) {
                mFbNativeAd.registerViewForInteraction(view);
                setOverridingClickTracker(true);
                setOverridingImpressionTracker(true);
            } else {
                ANLog.e("Attempted to invoke registerViewForInteraction on null mFbNativeAd");
            }
        }

        @Override
        public void clear(final View view) {
            if (mFbNativeAd != null) {
                mFbNativeAd.unregisterView();
            } else {
                ANLog.e("Attempted to invoke unregisterView on null mFbNativeAd");
            }
        }

        @Override
        public void destroy() {
            if (mFbNativeAd != null) {
                mFbNativeAd.destroy();
            } else {
                ANLog.e("Attempted to invoke destroy on null mFbNativeAd");
            }
        }

        private Double getDoubleRating(final Rating rating) {
            if (rating == null) {
                return null;
            }

            return MAX_STAR_RATING * rating.getValue() / rating.getScale();
        }
    }
}