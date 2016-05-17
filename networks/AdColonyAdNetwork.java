package com.adsnative.mediation;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.adsnative.ads.BaseNativeAd;
import com.adsnative.ads.ErrorCode;
import com.adsnative.network.AdResponse;
import com.adsnative.util.ANLog;

import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyAdAvailabilityListener;
import com.jirbo.adcolony.AdColonyNativeAdView;

import org.json.JSONException;
import org.json.JSONObject;

public class AdColonyAdNetwork extends CustomAdNetwork {
    private static final String PLACEMENT_ID_KEY = "placementId";
    private static final String APP_ID_KEY = "appId";
    private static boolean isConfiguredAdColony = false;

    // CustomAdNetwork implementation
    @Override
    protected void loadNativeAd(final Context context,
                                final CustomEventListener customEventListener,
                                final AdResponse adResponse) {

        String placementId = null;
        String appId = null;
        JSONObject customAdNetworkData = adResponse.getCustomAdNetworkData();
        try {
            placementId = customAdNetworkData.getString(PLACEMENT_ID_KEY);
            appId = customAdNetworkData.getString(APP_ID_KEY);
            ANLog.d("AdColonyAdNetwork: " + placementId);
            ANLog.d("AdColonyAdNetwork: " + appId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (placementId == null || placementId.length() <= 0
                || appId == null || appId.length() <= 0) {
            ANLog.e("AdColonyAdNetwork: " + ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            customEventListener.onNativeAdFailed(ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        if(!isConfiguredAdColony) {
            AdColony.configure((Activity) context, "version:1.0,store:google", appId, placementId);
            isConfiguredAdColony = true;
        }
        final AdColonyNativeAd adColonyNativeAd =
                new AdColonyNativeAd(context,
                        placementId, customEventListener);
        adColonyNativeAd.loadAd();
    }

    static class AdColonyNativeAd extends BaseNativeAd implements AdColonyAdAvailabilityListener {
        private final Context mContext;
        private final String mPlacementId;
        private final CustomEventListener mCustomEventListener;
        private boolean adRequestCompleted = false;

        private AdColonyNativeAdView mAdColonyNativeAd;

        AdColonyNativeAd(final Context context,
                         final String placementId,
                         final CustomEventListener customEventListener) {
            mContext = context;//.getApplicationContext();
            mPlacementId = placementId;
            mCustomEventListener = customEventListener;
            AdColony.addAdAvailabilityListener(this);
        }

        void loadAd() {
            add_native_ad_view();
            new CountDownTimer(5000, 5000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // do nothing
                }
                @Override
                public void onFinish() {
                    // make another fetchAd() call after 5 secs
                    if (!adRequestCompleted) {
                        ANLog.d("AdColonyAdNetwork -> loadAd() -> CountDownTimer -> fetchAd()");
                        AdColonyNativeAd.this.add_native_ad_view();
                        new CountDownTimer(2000, 2000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                // do nothing
                            }
                            @Override
                            public void onFinish() {
                                if (!adRequestCompleted) {
                                    // terminate all ad calls if they ever return after 7 secs
                                    adRequestCompleted = true;
                                    mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
                                }
                            }
                        }.start();
                    }
                }
            }.start();
        }

        private void add_native_ad_view( ){

            DisplayMetrics display_metrics = mContext.getResources().getDisplayMetrics();
            int width = display_metrics.widthPixels > display_metrics.heightPixels ? display_metrics.heightPixels : display_metrics.widthPixels;
            float density = display_metrics.density;;
            mAdColonyNativeAd = new AdColonyNativeAdView((Activity)mContext , mPlacementId, width, (int)(density*200));

            if(mAdColonyNativeAd.isReady()){
                adRequestCompleted = true;
                setProviderName(AdColonyAdNetwork.class.getName());
                setAdView(mAdColonyNativeAd);
                setTitle(mAdColonyNativeAd.getTitle());
                setSummary(mAdColonyNativeAd.getDescription());
                setPromotedByTag("Sponsored");

                ImageView icon = mAdColonyNativeAd.getAdvertiserImage();
                setIconImageDrawable(icon == null ? null : icon.getDrawable());

                //Make sure UI changes are happening on the main thread
                ((Activity)mContext).runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        mCustomEventListener.onNativeAdLoaded(AdColonyNativeAd.this);
                    }
                });
            }
        }

        // AdColonyAdAvailabilityListener
        @Override
        public void onAdColonyAdAvailabilityChange(boolean available, String placementId) {

            if (available) {
                add_native_ad_view();
                return;
            }
        }
    }
}

