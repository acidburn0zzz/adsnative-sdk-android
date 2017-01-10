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

import com.sharethrough.sdk.Sharethrough;
import com.sharethrough.sdk.Sharethrough.AdListener;
import com.sharethrough.sdk.Sharethrough.OnStatusChangeListener;
import com.sharethrough.sdk.STRSdkConfig;
import com.sharethrough.sdk.BasicAdView;
import com.sharethrough.sdk.mediation.ICreative;

import org.json.JSONException;
import org.json.JSONObject;

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
        JSONObject customAdNetworkData = adResponse.getCustomAdNetworkData();
        try {
            placementId = customAdNetworkData.getString(PLACEMENT_ID_KEY);
            ANLog.d("ShareThroughAdNetwork: " + placementId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (placementId == null || placementId.length() <= 0) {
            ANLog.e("ShareThroughAdNetwork: " + ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            customEventListener.onNativeAdFailed(ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        final ShareThroughNativeAd sharethrougNativeAd =
                new ShareThroughNativeAd(context,placementId,customEventListener);
        sharethrougNativeAd.loadAd();
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
            sharethrough = new Sharethrough(new STRSdkConfig(mContext,mPlacementId));
            sharethrough.setOnStatusChangeListener(this);
            sharethrough.setAdListener(this);
            sharethrough.fetchAds();

        }

        public BasicAdView getBasicAdView() {
            BasicAdView adView = new BasicAdView(mContext);
            adView.prepareWithResourceIds(R.layout.sharethrough_ad_view, R.id.title, R.id.description, R.id.advertiser, R.id.thumbnail, R.id.optout, R.id.brand_logo, R.id.slug);
            return adView;
        }

        @Override
        public void newAdsToShow() {
            ANLog.e("ShareThough newAdsToShow");
            if(!adRequestCompleted) {
                adView = getBasicAdView();
                sharethrough.putCreativeIntoAdView(adView, 0);
                adRequestCompleted = true;
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
                setTitle((String) adView.getTitle().getText());
                setSummary((String) adView.getDescription().getText());
                setIconImageDrawable(adView.getBrandLogo().getDrawable());
                setAdChoicesDrawable(adView.getOptout().getDrawable());
                setPromotedByTag((String) adView.getAdvertiser().getText());

                // removing extra details from ShareThrough Adview
                for (int adViewItemIndex = 0; adViewItemIndex < ((ViewGroup) adView.getAdView()).getChildCount(); ++adViewItemIndex) {
                    View adViewItem = ((ViewGroup) adView.getAdView()).getChildAt(adViewItemIndex);
                    if (adViewItem instanceof RelativeLayout) {
                        for (int mediaViewItemIndex = 0; mediaViewItemIndex < ((ViewGroup) adViewItem).getChildCount(); ++mediaViewItemIndex) {
                            View mediaViewItem = ((ViewGroup) adViewItem).getChildAt(mediaViewItemIndex);
                            if(mediaViewItem instanceof TextView)
                                ((ViewGroup) adViewItem).removeViewAt(mediaViewItemIndex);
                        }
                    }
                }
                setMediaView(adView.getAdView());
                mCustomEventListener.onNativeAdLoaded(ShareThroughNativeAd.this);
            }
        }


    }

  }

