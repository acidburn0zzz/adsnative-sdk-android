package com.adsnative.sampleads;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adsnative.ads.AdHelper;
import com.adsnative.header_bidding.DFP.PolymorphBidder;
import com.adsnative.util.ANLog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.google.android.gms.ads.formats.OnPublisherAdViewLoadedListener;

import java.net.URLEncoder;

public class DFPNativeBannerAd extends Fragment {


    private static final String SIMPLE_TEMPLATE_ID = "10104090";
    private static final String DFP_AD_UNIT_ID = "/6499/example/native";
    private static String PM_AD_UNIT_ID = "2Pwo1otj1C5T8y6Uuz9v-xbY1aB09x8rWKvsJ-HI";

    public void setAdUnitId(String AD_UNIT_ID) {
        if (AD_UNIT_ID != null && !AD_UNIT_ID.isEmpty()) {
            ANLog.e("Placement id: " + AD_UNIT_ID);
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

        ANLog.e("DFP_AD_UNIT_ID: " + DFP_AD_UNIT_ID);
        AdLoader.Builder builder = new AdLoader.Builder(getContext(), DFP_AD_UNIT_ID);

        builder.forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
            @Override
            public void onAppInstallAdLoaded(final NativeAppInstallAd nativeAppInstallAd) {
                NativeAppInstallAdView adView = (NativeAppInstallAdView) inflater
                        .inflate(R.layout.ad_app_install, null);
                adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
                adView.setBodyView(adView.findViewById(R.id.appinstall_body));
                adView.setCallToActionView(adView.findViewById(R.id.appinstall_call_to_action));
                adView.setIconView(adView.findViewById(R.id.appinstall_logo));
                adView.setImageView(adView.findViewById(R.id.appinstall_image));
                MediaView mediaView = (MediaView) adView.findViewById(R.id.appinstall_media);
                adView.setMediaView(mediaView);
                ((TextView) adView.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());
                ((TextView) adView.getBodyView()).setText(nativeAppInstallAd.getBody());
                ((Button) adView.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());
                if (nativeAppInstallAd.getIcon() != null) {
                    ANLog.e("nativeAppInstallAd.getIcon(): "+nativeAppInstallAd.getIcon().getUri());
                    ((ImageView) adView.getIconView()).setImageDrawable(nativeAppInstallAd.getIcon()
                            .getDrawable());
                }
                if (nativeAppInstallAd.getImages().size() > 0) {
                    ((ImageView) adView.getImageView()).setImageDrawable(nativeAppInstallAd.getImages().get(0).getDrawable());
                    mediaView.setVisibility(View.GONE);
                }
                adView.setNativeAd(nativeAppInstallAd);
                nativeAdContainer.addView(adView);

            }
        });

        builder.forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
            @Override
            public void onContentAdLoaded(NativeContentAd nativeContentAd) {
                NativeContentAdView adView = (NativeContentAdView) inflater
                        .inflate(R.layout.ad_content_ad_view, null);
                adView.setHeadlineView(adView.findViewById(R.id.contentad_headline));
                adView.setBodyView(adView.findViewById(R.id.contentad_body));
                adView.setCallToActionView(adView.findViewById(R.id.contentad_call_to_action));
                adView.setAdvertiserView(adView.findViewById(R.id.contentad_advertiser));
                MediaView mediaView = (MediaView) adView.findViewById(R.id.contentad_media);
                adView.setMediaView(mediaView);
                adView.setImageView(adView.findViewById(R.id.contentad_image));
                adView.setLogoView(adView.findViewById(R.id.contentad_logo));
                ((TextView) adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
                ((TextView) adView.getBodyView()).setText(nativeContentAd.getBody());
                ((TextView) adView.getCallToActionView()).setText(nativeContentAd.getCallToAction());
                ((TextView) adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());
                if (nativeContentAd.getImages().size() > 0) {
                    ((ImageView) adView.getImageView()).setImageDrawable(nativeContentAd.getImages().get(0).getDrawable());
                    mediaView.setVisibility(View.GONE);

                }
                if (nativeContentAd.getLogo() != null) {
                    ((ImageView) adView.getLogoView()).setImageDrawable(nativeContentAd.getLogo().getDrawable());
                }
                adView.setNativeAd(nativeContentAd);
                nativeAdContainer.addView(adView);
            }
        });
        builder.withNativeAdOptions(new NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build());
        builder.forPublisherAdView(new OnPublisherAdViewLoadedListener() {
            @Override
            public void onPublisherAdViewLoaded(PublisherAdView publisherAdView) {
                nativeAdContainer.removeAllViews();
                nativeAdContainer.addView(publisherAdView);
            }
        }, AdSize.BANNER);
        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(getContext(), "Failed to load native ad: "
                        + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
        AdLoader adLoader = builder.build();
        PublisherAdRequest.Builder pubBuilder = new PublisherAdRequest.Builder();
        pubBuilder.addCustomTargeting("key", "value").
                setContentUrl("https://www.example.com");
        PolymorphBidder pm_bidder = new PolymorphBidder(getContext(), PM_AD_UNIT_ID, adLoader, AdHelper.AdSize.BANNER_300x250);
        pm_bidder.setPubAdRequestBuilder(pubBuilder);
        pm_bidder.loadDFPAd();
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
        super.onResume();
    }
}
