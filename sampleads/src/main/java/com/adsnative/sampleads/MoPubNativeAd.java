package com.adsnative.sampleads;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.adsnative.header_bidding.mopub.PolymorphBidder;
import com.adsnative.util.ANLog;
import com.mopub.nativeads.AdapterHelper;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.ViewBinder;

import java.net.URLEncoder;

public class MoPubNativeAd extends Fragment {

    private MoPubNative moPubNative;

    private String MOPUB_AD_UNIT_ID = "65b014426e144eb5bce47622d49abf63";
    private String PM_AD_UNIT_ID = "ping";

    public void setAdUnitId(String AD_UNIT_ID) {
        if(AD_UNIT_ID != null && !AD_UNIT_ID.isEmpty()) {
            ANLog.e("Placement id: "+AD_UNIT_ID);
            this.PM_AD_UNIT_ID = URLEncoder.encode(AD_UNIT_ID);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_native_ad, container, false);
        final RelativeLayout nativeAdContainer = (RelativeLayout) view.findViewById(R.id.native_ad);

        final ViewBinder viewBinder = new ViewBinder.Builder(R.layout.fan_native_layout_backup)
                .mainImageId(R.id.an_main_image)
                .iconImageId(R.id.an_icon_image)
                .titleId(R.id.an_title)
                .textId(R.id.an_summary)
                .callToActionId(R.id.an_call_to_action)
                .privacyInformationIconImageId(R.id.an_ad_choices)
                .build();

        MoPubNative.MoPubNativeNetworkListener moPubNativeListener = new MoPubNative.MoPubNativeNetworkListener() {
            @Override
            public void onNativeLoad(NativeAd nativeAd) {
                ANLog.e("onNativeLoad");
                nativeAdContainer.addView(renderAd(nativeAd, viewBinder));

            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                // ...
            }
        };

        moPubNative = new MoPubNative(this.getContext(), MOPUB_AD_UNIT_ID, moPubNativeListener);


        MoPubStaticNativeAdRenderer moPubStaticNativeAdRenderer = new MoPubStaticNativeAdRenderer(viewBinder);
        moPubNative.registerAdRenderer(moPubStaticNativeAdRenderer);
        PolymorphBidder polymorphBidder = new PolymorphBidder(this.getContext());
        polymorphBidder.loadMopubAd(PM_AD_UNIT_ID, moPubNative, null);
//        moPubNative.makeRequest();

        return view;
    }

    private View renderAd(NativeAd nativeAd, ViewBinder viewBinder) {
        AdapterHelper adapterHelper = new AdapterHelper(this.getContext(), 0, 3);
        View view = adapterHelper.getAdView(null, null, nativeAd, viewBinder);
        return view;
    }

    @Override
    public void onDestroyView() {
        // You must call this or the ad adapter may cause a memory leak.
        moPubNative.destroy();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        // AdsNative recommends loading new ads when the user returns to your activity.
        // mNativeAd.loadAd();
        super.onResume();
    }
}
