package com.adsnative.mediation;

import android.content.Context;
import android.os.CountDownTimer;

import com.adsnative.ads.BaseNativeAd;
import com.adsnative.ads.ErrorCode;
import com.adsnative.network.AdResponse;
import com.adsnative.util.ANLog;
import com.adsnative.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sreekanth on 26/01/16.
 */
public class ANS2SAdNetwork extends CustomAdNetwork {
    @Override
    protected void loadNativeAd(final Context context,
                                final CustomEventListener customEventListener,
                                final AdResponse adResponse) {

        final int networkPriorityIndex = adResponse.getNetworkPriorityIndex();
        final S2SNativeAd s2SNativeAd = new S2SNativeAd(context, customEventListener);

        if (adResponse.isS2SAdReady()) {
            int s2sNetworkIndex = adResponse.getS2SNetworkIndex();
            /**
             * Check if S2S Network is ready to be shown.
             * The index must be ranked higher or equal to waterfall (network priority) index.
             */
            if (s2sNetworkIndex <= networkPriorityIndex) {
                s2SNativeAd.loadAd(adResponse.getS2SAdData());
            }
        } else {
            new CountDownTimer(3000, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (adResponse.isS2SAdReady()) {
                        int s2sNetworkIndex = adResponse.getS2SNetworkIndex();
                        /**
                         * Check if S2S Network is ready to be shown.
                         * The index must be ranked higher or equal to waterfall (network priority) index.
                         */
                        if (s2sNetworkIndex <= networkPriorityIndex) {
                            s2SNativeAd.loadAd(adResponse.getS2SAdData());
                        }
                        cancel();
                    }
                }

                @Override
                public void onFinish() {
                    if (adResponse.isS2SAdReady()) {
                        int s2sNetworkIndex = adResponse.getS2SNetworkIndex();
                        /**
                         * Check if S2S Network is ready to be shown.
                         * The index must be ranked higher or equal to waterfall (network priority) index.
                         */
                        if (s2sNetworkIndex <= networkPriorityIndex) {
                            s2SNativeAd.loadAd(adResponse.getS2SAdData());
                        }
                    } else {
                        ANLog.e("S2S Network ad failed to load within 3 sec timeout, trying fallback networks");
                        customEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
                    }
                }
            }.start();
        }
    }

    static class S2SNativeAd extends BaseNativeAd {
        private final Context mContext;
        private final CustomEventListener mCustomEventListener;

        S2SNativeAd(final Context context,
                    final CustomEventListener customEventListener) {
            mContext = context.getApplicationContext();
            mCustomEventListener = customEventListener;
        }

        public void loadAd(JSONObject s2sJSON) {

            if (s2sJSON == null || s2sJSON.length() <= 0) {
                mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
                return;
            }

            setProviderName(ANS2SAdNetwork.class.getName());

            setTitle(s2sJSON.optString(Constants.S2S_TITLE));
            setSummary(s2sJSON.optString(Constants.S2S_SUMMARY));
            JSONObject iconImage = s2sJSON.optJSONObject(Constants.S2S_ICON_IMAGE);
            if (iconImage != null) {
                setIconImage(iconImage.optString("url"));
            }
            JSONObject mainImage = s2sJSON.optJSONObject(Constants.S2S_MAIN_IMAGE);
            if (mainImage != null) {
                setMainImage(mainImage.optString("url"));
            }
            String starRating = s2sJSON.optString(Constants.S2S_STAR_RATING);
            try {
                setStarRating(Double.parseDouble(starRating));
            } catch (NumberFormatException nfe) {
                ANLog.e("NumberFormatException parsing star rating : " + nfe.getMessage());
            }
            setCallToAction(s2sJSON.optString(Constants.S2S_CALL_TO_ACTION));
            setLandingUrl(s2sJSON.optString(Constants.S2S_LANDING_URL));
            setPromotedBy(s2sJSON.optString(Constants.S2S_PROMOTED_BY));
            if (getPromotedBy() != null && getPromotedBy().length() > 0) {
                setPromotedByTag("Promoted By");
            } else {
                setPromotedByTag("Sponsored");
            }

            JSONObject trackers = s2sJSON.optJSONObject(Constants.S2S_TRACKERS);
            if (trackers != null && trackers.length() > 0) {
                JSONArray impressionTrackers = trackers.optJSONArray(Constants.S2S_IMPRESSION);
                if (impressionTrackers != null && impressionTrackers.length() > 0) {
                    for (int i = 0; i < impressionTrackers.length(); i++) {
                        try {
                            addImpressionTracker(impressionTrackers.getString(i));
                        } catch (JSONException e) {
                            ANLog.e("JSONException adding impression tracker for ANS2SAdNetwork : " +
                                    e.getMessage());
                        }
                    }
                }
                JSONArray clickTrackers = trackers.optJSONArray(Constants.S2S_CLICK);
                if (clickTrackers != null && clickTrackers.length() > 0) {
                    for (int i = 0; i < clickTrackers.length(); i++) {
                        try {
                            addClickTracker(clickTrackers.getString(i));
                        } catch (JSONException e) {
                            ANLog.e("JSONException adding click tracker for ANS2SAdNetwork : " +
                                    e.getMessage());
                        }
                    }
                }
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
                        mCustomEventListener.onNativeAdLoaded(S2SNativeAd.this);
                    }

                    @Override
                    public void onImagesFailedToCache(ErrorCode errorCode) {
                        ANLog.e("ANS2SAdNetwork: " + errorCode);
                        mCustomEventListener.onNativeAdLoaded(S2SNativeAd.this);
                        // mCustomEventListener.onNativeAdFailed(errorCode);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
