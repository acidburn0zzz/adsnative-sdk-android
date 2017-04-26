package com.mopub.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.adsnative.ads.BaseNativeAd;
import com.adsnative.ads.ErrorCode;
import com.adsnative.mediation.CustomAdNetwork;
import com.adsnative.network.AdResponse;
import com.adsnative.util.ANLog;
import com.mopub.common.util.Drawables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sreekanth on 17/09/15.
 */
public class MoPubAdNetwork extends CustomAdNetwork {
    private static final String PLACEMENT_ID_KEY = "placementId";

    // CustomEventNative implementation
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
                ANLog.d("MoPubAdNetwork: " + placementId);
            } else {
                ANLog.e("Attempted to invoke getString on null customAdNetworkData");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (placementId == null || placementId.length() <= 0) {
            ANLog.e("MoPubAdNetwork: " + ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            customEventListener.onNativeAdFailed(ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }
        // handle any exception that might come from 3rd party class
        try {
            final MoPubNativeAd mopubNativeAd =
                    new MoPubNativeAd(context, placementId, customEventListener);
            if (mopubNativeAd != null) {
                mopubNativeAd.loadAd();
            } else {
                ANLog.e("mopubNativeAd is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class MoPubNativeAd extends BaseNativeAd {
        private static final String MOPUB_AD_CHOICES_CLICKTHROUGH_URL =
                "http://www.mopub.com/optout";

        private final Context mContext;
        private final String mAdUnitId;
        private final CustomEventListener mCustomEventListener;

        private String mLandingURL;
        com.mopub.nativeads.NativeClickHandler mMopubClickHandler;

        MoPubNativeAd(final Context context,
                      final String adUnitId,
                      final CustomEventListener customEventListener) {
            mContext = context.getApplicationContext();
            mAdUnitId = adUnitId;
            mCustomEventListener = customEventListener;
        }

        void loadAd() {
            // mMoPubNative.makeRequest();
            // implement MoPub adrequest code here to get native assets
            final NativeUrlGenerator generator = new NativeUrlGenerator(mContext)
                    .withAdUnitId(mAdUnitId)
                    .withRequest(null);
            if (generator != null) {
                final String endpointUrl = generator.generateUrlString("ads.mopub.com");
                com.mopub.network.AdRequest.Listener mVolleyListener = new com.mopub.network.AdRequest.Listener() {
                    @Override
                    public void onSuccess(com.mopub.network.AdResponse adResponse) {
                        onNativeLoad(adResponse);
                    }

                    @Override
                    public void onErrorResponse(@NonNull final com.mopub.volley.VolleyError volleyError) {
                        onNativeFail(volleyError);
                    }
                };
                com.mopub.network.AdRequest mNativeRequest =
                        new com.mopub.network.AdRequest(endpointUrl,
                                com.mopub.common.AdFormat.NATIVE, mAdUnitId,
                                mContext, mVolleyListener);
                com.mopub.volley.RequestQueue requestQueue = com.mopub.network.Networking.getRequestQueue(mContext);
                if (requestQueue != null) {
                    requestQueue.add(mNativeRequest);
                }
            }
        }

        public void onNativeLoad(final com.mopub.network.AdResponse adResponse) {
            if (adResponse == null && mCustomEventListener != null) {
                mCustomEventListener.onNativeAdFailed(ErrorCode.EMPTY_AD_RESPONSE);
                return;
            }
            JSONObject adJSON = adResponse.getJsonBody();
            ANLog.d(adResponse.getStringBody());

            if (adJSON == null && mCustomEventListener!= null) {
                mCustomEventListener.onNativeAdFailed(ErrorCode.EMPTY_AD_RESPONSE);
                return;
            }

            setProviderName(MoPubAdNetwork.class.getName());

            String title = (String) adJSON.opt("title");
            if (title != null) {
                setTitle(title);
            }
            String summary = (String) adJSON.opt("text");
            if (summary != null) {
                setSummary(summary);
            }
            String iconImage = (String) adJSON.opt("iconimage");
            if (iconImage != null) {
                setIconImage(iconImage);
            }
            String mainImage = (String) adJSON.opt("mainimage");
            if (mainImage != null) {
                setMainImage(mainImage);
            }
            String cta = (String) adJSON.opt("ctatext");
            if (cta != null) {
                setCallToAction(cta);
            }
            Double starRating = (Double) adJSON.opt("starrating");
            if (starRating != null) {
                setStarRating(starRating);
            }
            setPromotedByTag("Sponsored");

            setAdChoicesDrawable(Drawables.NATIVE_PRIVACY_INFORMATION_ICON.createDrawable(mContext));
            setAdChoicesClickThroughUrl(MOPUB_AD_CHOICES_CLICKTHROUGH_URL);

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
                        mCustomEventListener.onNativeAdLoaded(MoPubNativeAd.this);
                    }

                    @Override
                    public void onImagesFailedToCache(ErrorCode errorCode) {
                        ANLog.e("MoPubAdNetwork: " + errorCode);
                        if (mCustomEventListener != null) {
                            mCustomEventListener.onNativeAdLoaded(MoPubNativeAd.this);
                        }
                        // mCustomEventListener.onNativeAdFailed(errorCode);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            Object impTrackers = adJSON.opt("imptracker");
            if (impTrackers instanceof JSONArray) {
                final JSONArray trackers = (JSONArray) impTrackers;
                for (int i = 0; i < trackers.length(); i++) {
                    try {
                        addImpressionTracker(trackers.getString(i));
                    } catch (JSONException e) {
                        // This will only occur if we access a non-existent index in JSONArray.
                        ANLog.e("Unable to parse impression trackers from MoPubAdNetwork");
                    }
                }
            }

            Object clkTrackers = adJSON.opt("clktracker");
            if (clkTrackers instanceof JSONArray) {
                final JSONArray trackers = (JSONArray) clkTrackers;
                for (int i = 0; i < trackers.length(); i++) {
                    try {
                        addClickTracker(trackers.getString(i));
                    } catch (JSONException e) {
                        // This will only occur if we access a non-existent index in JSONArray.
                        ANLog.e("Unable to parse click trackers from MoPubAdNetwork");
                    }
                }
            } else if (clkTrackers instanceof String) {
                addClickTracker((String) clkTrackers);
            }

            mLandingURL = (String) adJSON.opt("clk");
        }

        public void onNativeFail(com.mopub.volley.VolleyError volleyError) {
            ANLog.e("MoPubAdNetwork: " + volleyError.getMessage());
            if (mCustomEventListener != null) {
                mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
            }
        }

        // BaseNativeAd
        @Override
        public void prepare(final View view) {
            mMopubClickHandler = new com.mopub.nativeads.NativeClickHandler(mContext);
        }

        @Override
        public void handleClick(final View view) {
            mMopubClickHandler.openClickDestinationUrl(mLandingURL, view);
        }
    }
}