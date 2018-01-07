package com.adsnative.ads;

import android.content.Context;
import android.support.annotation.NonNull;

import com.adsnative.network.AdRequest;
import com.mopub.nativeads.MoPubAdAdapter;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.RequestParameters;

/**
 * Created by sijojohn on 18/07/17.
 */

public class PolymorphBidder{

    private Context mContext;

    public PolymorphBidder(Context context) {
        this.mContext = context;
    }

    private void loadAds(@NonNull final String adUnitId, @NonNull String pm_ad_unit_id,
                 final ANRequestParameters mRequestParameters, final MoPubAdAdapter mAdAdapter) {
        AdRequest adRequest = new AdRequest(pm_ad_unit_id, mRequestParameters, this.mContext);
        adRequest.setNetworkListener(new NetworkListener() {
            @Override
            public void onLoadSuccess(NativeAdUnit nativeAdUnit) {
                PrefetchAds.setAd(nativeAdUnit);
                Double ecpm = nativeAdUnit.getEcpm();
                if(ecpm != null) {
                    RequestParameters requestParameters = new RequestParameters.Builder().keywords("ecpm:"+ecpm).build();
                    mAdAdapter.loadAds(adUnitId, requestParameters);
                }
                else {
                    mAdAdapter.loadAds(adUnitId);
                }
            }

            @Override
            public void onLoadFailure(FailureMessage failureMessage) {
                mAdAdapter.loadAds(adUnitId);
            }
        });
        adRequest.makeRequest();
    }

    public void loadMopubAd(@NonNull final String pm_ad_unit_id, final MoPubNative moPubNative, final RequestParameters moPubrequestParameters)
    {
        AdRequest adRequest = new AdRequest(pm_ad_unit_id, null, this.mContext);
        adRequest.setNetworkListener(new NetworkListener() {
            @Override
            public void onLoadSuccess(NativeAdUnit nativeAdUnit) {
                if (PrefetchAds.getSize() > 0) {
                    PrefetchAds.getAd(); // clear stale prefetched ad
                }
                PrefetchAds.setAd(nativeAdUnit);
                Double ecpm = nativeAdUnit.getEcpm();
                if(ecpm != null) {
                    RequestParameters requestParameters = null;
                    if (moPubrequestParameters == null) {
                        requestParameters = new RequestParameters.Builder().keywords("ecpm:" + ecpm).build();
                    } else {
                        requestParameters = new RequestParameters.Builder().keywords("ecpm: "+ ecpm + "," + moPubrequestParameters.getKeywords()).build();
                    }
                    moPubNative.makeRequest(requestParameters);
                }
                else {
                    if (moPubrequestParameters == null) {
                        moPubNative.makeRequest();
                    } else {
                        moPubNative.makeRequest(moPubrequestParameters);
                    }
                }
            }

            @Override
            public void onLoadFailure(FailureMessage failureMessage) {

                if (moPubrequestParameters == null) {
                    moPubNative.makeRequest();
                } else {
                    moPubNative.makeRequest(moPubrequestParameters);
                }
            }
        });
        adRequest.makeRequest();
    }
}
