package com.adsnative.mediation;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.adsnative.ads.BaseNativeAd;
import com.adsnative.ads.ErrorCode;
import com.adsnative.network.AdResponse;
import com.adsnative.util.ANLog;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdSettings;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sijojohn on 03/08/18.
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class FacebookNativeAd extends BaseNativeAd implements NativeAdListener {
        private static final String SOCIAL_CONTEXT_FOR_AD = "socialContextForAd";

        private final Context mContext;
        private final NativeAd mFbNativeAd;
        private final CustomEventListener mCustomEventListener;
        private MediaView fbMediaView;

        FacebookNativeAd(final Context context,
                         final NativeAd nativeAd,
                         final CustomEventListener customEventListener) {
            mContext = context.getApplicationContext();
            mFbNativeAd = nativeAd;
            mCustomEventListener = customEventListener;
        }

        void loadAd() {
            // for testing purpose only
//            AdSettings.addTestDevice("0cdf374f83fda2c592ff2fd20109576b");
            if (mFbNativeAd != null) {
                mFbNativeAd.setAdListener(this);
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

            setTitle(mFbNativeAd.getAdHeadline());
            mFbNativeAd.getAdChoicesIcon();
            setSummary(mFbNativeAd.getAdBodyText());
            setCallToAction(mFbNativeAd.getAdCallToAction());

            String adChoices = mFbNativeAd.getAdChoicesImageUrl();
            setAdChoicesIcon(adChoices);
            setAdChoicesClickThroughUrl(mFbNativeAd.getAdChoicesLinkUrl());
            setPromotedByTag("Sponsored");
            setType("facebook");
            fbMediaView = new MediaView(mContext);
            AdChoicesView adChoicesView = new AdChoicesView(mContext, mFbNativeAd, true);
            setAdChoicesView(adChoicesView);
            setMediaView(fbMediaView);

            addCustomField(SOCIAL_CONTEXT_FOR_AD, mFbNativeAd.getAdSocialContext());

            mCustomEventListener.onNativeAdLoaded(FacebookNativeAd.this);
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
                /* IMPORTANT */
                /* Fb mandates icon image for native ads */
                /* publisher needs to add a tag "icon" for icon imageview element */
                final View icon = view.findViewWithTag("icon");
                if (icon != null) {
                    try {
                        final LinearLayout parent = (LinearLayout) icon.getParent();
                        parent.removeView(icon);
                        AdIconView adIconView = new AdIconView(mContext);
                        parent.addView(adIconView, icon.getLayoutParams().width, icon.getLayoutParams().height);
                        parent.requestLayout();
                        mFbNativeAd.registerViewForInteraction(view, fbMediaView, adIconView);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mFbNativeAd.registerViewForInteraction(view, fbMediaView);
                    }
                } else {
                    ANLog.e("FbAdNetwork: couldn't find icon imageview in layout!");
                    mFbNativeAd.registerViewForInteraction(view, fbMediaView);
                }

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

        @Override
        public void onMediaDownloaded(Ad ad) {

        }

    }
}