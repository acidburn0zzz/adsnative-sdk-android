package com.adsnative.sampleads;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mopub.nativeads.PMAdRenderer;
import com.mopub.nativeads.PolymorphBidder;
import com.adsnative.util.ANLog;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.nativeads.AdapterHelper;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.ViewBinder;
import com.mopub.nativeads.PMAdRenderer.PolymorphViewBinder;

import java.net.URLEncoder;

public class MoPubNativeAd extends Fragment {

    private MoPubNative moPubNative;
    MoPubNative.MoPubNativeNetworkListener moPubNativeListener;
    private String MOPUB_AD_UNIT_ID = "65b014426e144eb5bce47622d49abf63";
    private String PM_AD_UNIT_ID = "NosADe7KvUy4b326YAeoGdVcIhxIwhKFAlje1GWv";

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

        final PolymorphViewBinder mediaViewBinder = new PolymorphViewBinder.Builder(R.layout.facebook_native_ad)
                .mediaViewId(R.id.fb_media)
                .iconImageViewId(R.id.fb_icon_image)
                .titleId(R.id.fb_title)
                .textId(R.id.fb_text)
                .callToActionId(R.id.fb_call_to_action)
                .adChoicesRelativeLayoutId(R.id.fb_ad_choices)
                .build();

        final ViewBinder staticViewBinder = new ViewBinder.Builder(R.layout.fan_native_layout_backup)
                .mainImageId(R.id.an_main_image)
                .iconImageId(R.id.an_icon_image)
                .titleId(R.id.an_title)
                .textId(R.id.an_summary)
                .callToActionId(R.id.an_call_to_action)
                .privacyInformationIconImageId(R.id.an_ad_choices)
                .build();


        moPubNativeListener = new MoPubNative.MoPubNativeNetworkListener() {
            @Override
            public void onNativeLoad(NativeAd nativeAd) {
                ANLog.e("onNativeLoad");
                nativeAdContainer.addView(renderAd(nativeAd, mediaViewBinder));

            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                // ...
            }
        };
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder("AD_UNIT_ID")
                .build();
        MoPub.initializeSdk(getContext(), sdkConfiguration, new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {

                moPubNative = new MoPubNative(getContext(), MOPUB_AD_UNIT_ID, moPubNativeListener);
                PMAdRenderer pmAdRenderer = new PMAdRenderer(mediaViewBinder);
                MoPubStaticNativeAdRenderer moPubStaticNativeAdRenderer = new MoPubStaticNativeAdRenderer(staticViewBinder);
                moPubNative.registerAdRenderer(pmAdRenderer);
                moPubNative.registerAdRenderer(moPubStaticNativeAdRenderer);
                PolymorphBidder polymorphBidder = new PolymorphBidder(getContext());
                polymorphBidder.loadMopubAd(PM_AD_UNIT_ID, moPubNative, null);
            }
        });

//        private SdkInitializationListener initSdkListener() {
//            return new SdkInitializationListener() {
//                @Override
//                public void onInitializationFinished() {
//           /* MoPub SDK initialized.
//           Check if you should show the consent dialog here, and make your ad requests. */
//                }
//            };
//        }

//        moPubNative.makeRequest();

        return view;
    }

    private View renderAd(NativeAd nativeAd, PolymorphViewBinder viewBinder) {
        AdapterHelper adapterHelper = new AdapterHelper(this.getContext(), 0, 3);
        View view = adapterHelper.getAdView(null, null, nativeAd);
        return view;
    }

    @Override
    public void onDestroyView() {
        // You must call this or the ad adapter may cause a memory leak.
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        // AdsNative recommends loading new ads when the user returns to your activity.
        // mNativeAd.loadAd();
        super.onResume();
    }
}
