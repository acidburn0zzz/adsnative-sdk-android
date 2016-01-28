package com.adsnative.mediation;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.adsnative.ads.ErrorCode;
import com.adsnative.ads.BaseNativeAd;
import com.adsnative.network.AdResponse;
import com.adsnative.util.ANLog;

import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.inmobi.sdk.InMobiSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.adsnative.util.Json.getJsonValue;
import static com.adsnative.util.Numbers.parseDouble;

/**
 * Created by sreekanth on 17/09/15.
 */
public class InMobiAdNetwork extends CustomAdNetwork {
    private static final String PLACEMENT_ID_KEY = "placementId";

    // CustomAdNetwork implementation
    @Override
    protected void loadNativeAd(final Context context,
                                final CustomEventListener customEventListener,
                                final AdResponse adResponse) {

        if (!(context instanceof Activity)) {
            customEventListener.onNativeAdFailed(ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        long placementId = 0L;
        String accountId = null;
        JSONObject customEventClassData = adResponse.getCustomAdNetworkData();
        try {
            String sPlacementId = customEventClassData.getString(PLACEMENT_ID_KEY);
            accountId = customEventClassData.getString(PLACEMENT_ID_KEY);
            placementId = Long.parseLong(sPlacementId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (placementId == 0L) {
            customEventListener.onNativeAdFailed(ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        InMobiSdk.init(context, accountId);

        final InMobiNativeAd inMobiNativeAd =
                new InMobiNativeAd(context, customEventListener);
        inMobiNativeAd.setIMNative(new InMobiNative(placementId, inMobiNativeAd));
        inMobiNativeAd.loadAd();
    }

    static class InMobiNativeAd extends BaseNativeAd implements InMobiNative.NativeAdListener {
        static final int IMPRESSION_MIN_TIME_VIEWED = 1000;

        // Modifiable keys
        static final String TITLE = "title";
        static final String DESCRIPTION = "description";
        static final String MAIN_IMAGE = "screenshots";
        static final String ICON_IMAGE = "icon";
        static final String LANDING_URL = "landingURL";
        static final String CTA = "cta";
        static final String RATING = "rating";

        // Constant keys
        static final String URL = "url";

        private final Context mContext;
        private final CustomEventListener mCustomEventNativeListener;
        private InMobiNative mImNative;
        private View mImView;

        InMobiNativeAd(final Context context,
                       final CustomEventListener customEventListener) {
            mContext = context.getApplicationContext();
            mCustomEventNativeListener = customEventListener;
        }

        void setIMNative(final InMobiNative mImNative) {
            this.mImNative = mImNative;
        }

        void loadAd() {
            mImNative.load();
        }

        // InMobiNative.NativeAdListener implementation
        @Override
        public void onAdLoadSucceeded(InMobiNative inMobiNative) {
            if (inMobiNative == null) {
                mCustomEventNativeListener.onNativeAdFailed(ErrorCode.NETWORK_INVALID_STATE);
                return;
            }

            try {
                parseJson(inMobiNative);
            } catch (JSONException e) {
                mCustomEventNativeListener.onNativeAdFailed(ErrorCode.INVALID_JSON);
                return;
            }

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
                        mCustomEventNativeListener.onNativeAdLoaded(InMobiNativeAd.this);
                    }

                    @Override
                    public void onImagesFailedToCache(ErrorCode errorCode) {
                        ANLog.e("InMobiAdNetwork: " + errorCode);
                        mCustomEventNativeListener.onNativeAdLoaded(InMobiNativeAd.this);
                        // mCustomEventNativeListener.onNativeAdFailed(errorCode);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
            ANLog.e("InMobiAdNetwork: Failed to load - " + inMobiAdRequestStatus.getMessage());
            mCustomEventNativeListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
        }

        @Override
        public void onAdDismissed(InMobiNative inMobiNative) {

        }

        @Override
        public void onAdDisplayed(InMobiNative inMobiNative) {

        }

        @Override
        public void onUserLeftApplication(InMobiNative inMobiNative) {

        }

        void parseJson(final InMobiNative inMobiNative) throws JSONException  {
            final JSONObject jsonObject = new JSONObject((String) inMobiNative.getAdContent());

            ANLog.d("InMobiAdNetwork ad response json: " + jsonObject.toString());

            setProviderName(InMobiAdNetwork.class.getName());

            setTitle((String) getJsonValue(jsonObject, TITLE, String.class));
            setSummary((String) getJsonValue(jsonObject, DESCRIPTION, String.class));

            final JSONObject mainImageJsonObject = (JSONObject) getJsonValue(jsonObject, MAIN_IMAGE, JSONObject.class);
            if (mainImageJsonObject != null) {
                setMainImage((String) getJsonValue(mainImageJsonObject, URL, String.class));
            }

            final JSONObject iconJsonObject = (JSONObject) getJsonValue(jsonObject, ICON_IMAGE, JSONObject.class);
            if (iconJsonObject != null) {
                setIconImage((String) getJsonValue(iconJsonObject, URL, String.class));
            }

            setLandingUrl((String) getJsonValue(jsonObject, LANDING_URL, String.class));
            setCallToAction((String) getJsonValue(jsonObject, CTA, String.class));

            setPromotedByTag("Sponsored");

            try {
                setStarRating(parseDouble(jsonObject.opt(RATING)));
            } catch (ClassCastException e) {
                Log.d("AdsNative", "Unable to set invalid star rating for InMobi Native.");
            }
            setImpressionMinTimeViewed(IMPRESSION_MIN_TIME_VIEWED);
        }

        // BaseNativeAd
        @Override
        public void prepare(final View view) {
            mImView = view;
        }

        @Override
        public void recordImpression() {
            if (mImView != null && mImView instanceof ViewGroup) {
                InMobiNative.bind((ViewGroup) mImView, mImNative);
            } else if (mImView != null && mImView.getParent() instanceof ViewGroup) {
                InMobiNative.bind((ViewGroup) mImView.getParent(), mImNative);
            } else {
                Log.e("AdsNative", "InMobi did not receive ViewGroup to attachToView, unable to record impressions");
            }
        }

        @Override
        public void handleClick(final View view) {
            mImNative.reportAdClick(null);
        }

        @Override
        public void clear(final View view) {
            InMobiNative.unbind(mImView);
        }

        @Override
        public void destroy() {
            InMobiNative.unbind(mImView);
        }
    }
}