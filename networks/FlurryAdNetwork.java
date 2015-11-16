package com.adsnative.mediation;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.adsnative.ads.BaseNativeAd;
import com.adsnative.ads.ErrorCode;
import com.adsnative.network.AdResponse;
import com.adsnative.util.ANLog;

import com.flurry.android.FlurryAgent;
import com.flurry.android.ads.FlurryAdErrorType;
import com.flurry.android.ads.FlurryAdNative;
import com.flurry.android.ads.FlurryAdNativeAsset;
import com.flurry.android.ads.FlurryAdNativeListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sreekanth on 04/11/15.
 */
public class FlurryAdNetwork extends CustomAdNetwork {
    private static final String FLURRY_API_KEY = "flurryApiKey";
    private static final String PLACEMENT_ID_KEY = "placementId";

    private FlurryAdNative adNative = null;

    // CustomAdNetwork implementation
    @Override
    protected void loadNativeAd(final Context context,
                                final CustomEventListener customEventListener,
                                final AdResponse adResponse) {

        String flurryApiKey = null;
        String placementId = null;
        JSONObject customEventClassData = adResponse.getCustomAdNetworkData();
        try {
            flurryApiKey = customEventClassData.getString(FLURRY_API_KEY);
            placementId = customEventClassData.getString(PLACEMENT_ID_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (placementId == null || placementId.length() <= 0) {
            ANLog.e("FlurryAdNetwork: " + ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            customEventListener.onNativeAdFailed(ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setLogLevel(Log.VERBOSE);

        FlurryAgent.init(context, flurryApiKey);

        adNative = new FlurryAdNative(context, placementId);
        final FlurryNativeAd flurryNativeAd =
                new FlurryNativeAd(context, customEventListener);
        adNative.setListener(flurryNativeAd);
        flurryNativeAd.setFlurryNative(adNative);
        flurryNativeAd.loadAd();

    }

    static class FlurryNativeAd extends BaseNativeAd implements FlurryAdNativeListener {
        private final Context mContext;
        private final CustomEventListener mCustomEventListener;
        private FlurryAdNative mFlurryNativeAd;

        FlurryNativeAd(final Context context,
                         final CustomEventListener customEventListener) {
            mContext = context.getApplicationContext();
            mCustomEventListener = customEventListener;
        }

        void setFlurryNative(final FlurryAdNative mFlurryNative) {
            mFlurryNativeAd = mFlurryNative;
        }

        void loadAd() {
            ANLog.e("FlurryAdNetwork#loadAd()");
            mFlurryNativeAd.fetchAd();
        }

        // FlurryAdNativeListener
        @Override
        public void onFetched(FlurryAdNative flurryAdNative) {
            ANLog.e("FlurryAdNetwork#onFetched()");

            FlurryAdNativeAsset title = flurryAdNative.getAsset("headline");
            ANLog.e("FlurryAdNetwork#title: " + title);
            if (title != null) {
                setTitle(title.getValue());
            }
            FlurryAdNativeAsset summary = flurryAdNative.getAsset("summary");
            ANLog.e("FlurryAdNetwork#summary: " + summary);
            if (summary != null) {
                setSummary(summary.getValue());
            }
            FlurryAdNativeAsset iconImage = flurryAdNative.getAsset("secImage");
            ANLog.e("FlurryAdNetwork#iconImage: " + iconImage);
            if (iconImage != null) {
                setIconImage(iconImage.getValue());
            }
            FlurryAdNativeAsset mainImage = flurryAdNative.getAsset("secOrigImg");
            ANLog.e("FlurryAdNetwork#mainImage: " + mainImage);
            if (mainImage != null) {
                setMainImage(mainImage.getValue());
            }
            FlurryAdNativeAsset appCategory = flurryAdNative.getAsset("appCategory");
            ANLog.e("FlurryAdNetwork#appCategory: " + appCategory);
            FlurryAdNativeAsset appRating = flurryAdNative.getAsset("appRating");
            ANLog.e("FlurryAdNetwork#appRating: " + appRating);
            String cta = "Read More";
            if (appCategory != null || appRating != null) {
                cta = "Install Now";
            }
            setCallToAction(cta);
            setPromotedByTag("Sponsored");

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
                        mCustomEventListener.onNativeAdLoaded(FlurryNativeAd.this);
                    }

                    @Override
                    public void onImagesFailedToCache(ErrorCode errorCode) {
                        ANLog.e("FacebookAdNetwork: " + errorCode);
                        mCustomEventListener.onNativeAdFailed(errorCode);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onShowFullscreen(FlurryAdNative flurryAdNative) {
            // do nothing
        }

        @Override
        public void onCloseFullscreen(FlurryAdNative flurryAdNative) {
            // do nothing
        }

        @Override
        public void onAppExit(FlurryAdNative flurryAdNative) {
            // do nothing
        }

        @Override
        public void onClicked(FlurryAdNative flurryAdNative) {
            notifyAdClicked();
        }

        @Override
        public void onImpressionLogged(FlurryAdNative flurryAdNative) {
            notifyAdImpressed();
        }

        @Override
        public void onError(FlurryAdNative flurryAdNative, FlurryAdErrorType flurryAdErrorType, int i) {
            ANLog.e("FlurryAdNetwork#onError: " + flurryAdErrorType.name());
            ANLog.e("Error:"+flurryAdErrorType.toString()+" CODE:"+i);
            mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
        }

        // BaseNativeAd
        @Override
        public void prepare(final View view) {
            mFlurryNativeAd.setTrackingView(view);
            setOverridingClickTracker(true);
            setOverridingImpressionTracker(true);
        }

        @Override
        public void clear(final View view) {
            mFlurryNativeAd.removeTrackingView();
        }

        @Override
        public void destroy() {
            mFlurryNativeAd.destroy();
        }
    }
}
