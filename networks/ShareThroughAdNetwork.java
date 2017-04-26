package com.adsnative.mediation;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;

import com.adsnative.ads.BaseNativeAd;
import com.adsnative.network.AdResponse;
import com.adsnative.ads.ErrorCode;
import com.adsnative.util.ANLog;
import com.adsnative.sampleads.R;

import org.json.JSONException;
import org.json.JSONObject;

import com.sharethrough.sdk.Sharethrough;
import com.sharethrough.sdk.Sharethrough.AdListener;
import com.sharethrough.sdk.Sharethrough.OnStatusChangeListener;
import com.sharethrough.sdk.STRSdkConfig;
import com.sharethrough.sdk.BasicAdView;
import com.sharethrough.sdk.mediation.ICreative;

/**
 * Created by sijojohn on 28/12/16.
 */

public class ShareThroughAdNetwork extends CustomAdNetwork {

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
//              placementId = "155c3656";
                ANLog.d("ShareThroughAdNetwork: " + placementId);
            } else {
                ANLog.e("Attempted to invoke getString on null customAdNetworkData");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (placementId == null || placementId.length() <= 0) {
            ANLog.e("ShareThroughAdNetwork: " + ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            customEventListener.onNativeAdFailed(ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }
        // handle any exception that might come from 3rd party class
        try {
            final ShareThroughNativeAd sharethrougNativeAd =
                    new ShareThroughNativeAd(context, placementId, customEventListener);
            if (sharethrougNativeAd != null) {
                sharethrougNativeAd.loadAd();
            } else {
                ANLog.e("Attempted to invoke loadAd on null sharethrougNativeAd");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ShareThroughNativeAd extends BaseNativeAd implements OnStatusChangeListener, AdListener {

        private final Context mContext;
        private final String mPlacementId;
        private Sharethrough sharethrough;
        private final CustomEventListener mCustomEventListener;
        private BasicAdView adView;
        private boolean adRequestCompleted = false;

        ShareThroughNativeAd(final Context context,
                             final String placementId, final CustomEventListener customEventListener) {
            mContext = context;
            mPlacementId = placementId;
            mCustomEventListener = customEventListener;
        }

        void loadAd() {
            // Instantiate Sharethrough with an Activity context
            sharethrough = new Sharethrough(new STRSdkConfig(mContext, mPlacementId));
            if (sharethrough != null) {
                sharethrough.setOnStatusChangeListener(this);
                sharethrough.setAdListener(this);
                sharethrough.fetchAds();
            } else {
                ANLog.e("sharethrough is null");
            }

        }

        public BasicAdView getBasicAdView() {
            BasicAdView adView = new BasicAdView(mContext);
            if (adView != null) {
                adView.prepareWithResourceIds(R.layout.sharethrough_ad_view, R.id.title, R.id.description, R.id.advertiser, R.id.thumbnail, R.id.optout, R.id.brand_logo, R.id.slug);
            } else {
                ANLog.e("Attempted to invoke prepareWithResourceIds on null adView");
            }
            return adView;
        }

        @Override
        public void newAdsToShow() {
            ANLog.e("ShareThough newAdsToShow");
            if (!adRequestCompleted) {
                adView = getBasicAdView();
                if (sharethrough != null) {
                    sharethrough.putCreativeIntoAdView(adView, 0);
                    adRequestCompleted = true;
                } else {
                    ANLog.e("Attempted to invoke putCreativeIntoAdView on null sharethrough");
                }
            }
        }

        @Override
        public void noAdsToShow() {
            ANLog.e("ShareThroughAdNetwork: " + ErrorCode.NETWORK_NO_FILL);
            mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
        }

        @Override
        public void onAdClicked(ICreative iCreative) {
            ANLog.e("ShareThrough Ad Clicked");
            notifyAdClicked();
        }

        @Override
        public void onAdRendered(ICreative iCreative) {
            ANLog.e("ShareThrough Ads rendered");
            if (adView != null) {
                setProviderName(ShareThroughAdNetwork.class.getName());
                if (adView.getTitle() != null && adView.getTitle().getText() != null) {
                    setTitle((String) adView.getTitle().getText());
                }
                if (adView.getDescription() != null && adView.getDescription().getText() != null) {
                    setSummary((String) adView.getDescription().getText());
                }
                if (adView.getBrandLogo() != null && adView.getBrandLogo().getDrawable() != null) {
                    setIconImageDrawable(adView.getBrandLogo().getDrawable());
                }
                if (adView.getOptout() != null && adView.getOptout().getDrawable() != null) {
                    setAdChoicesDrawable(adView.getOptout().getDrawable());
                }
                if (adView.getAdvertiser() != null && adView.getAdvertiser().getText() != null) {
                    setPromotedByTag((String) adView.getAdvertiser().getText());
                }

                // removing extra details from ShareThrough Adview
                try {
                    if (adView.getAdView() != null) {
                        for (int adViewItemIndex = 0; adViewItemIndex < ((ViewGroup) adView.getAdView()).getChildCount(); ++adViewItemIndex) {
                            View adViewItem = ((ViewGroup) adView.getAdView()).getChildAt(adViewItemIndex);
                            if (adViewItem instanceof RelativeLayout) {
                                for (int mediaViewItemIndex = 0; mediaViewItemIndex < ((ViewGroup) adViewItem).getChildCount(); ++mediaViewItemIndex) {
                                    View mediaViewItem = ((ViewGroup) adViewItem).getChildAt(mediaViewItemIndex);
                                    if (mediaViewItem instanceof TextView)
                                        ((ViewGroup) adViewItem).removeViewAt(mediaViewItemIndex);
                                }
                            }
                        }
                        setMediaView(adView.getAdView());
                        mCustomEventListener.onNativeAdLoaded(ShareThroughNativeAd.this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }

}

