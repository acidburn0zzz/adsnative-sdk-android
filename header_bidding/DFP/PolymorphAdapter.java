package com.adsnative.header_bidding.DFP;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.adsnative.ads.ANAdListener;
import com.adsnative.ads.NativeAdUnit;
import com.adsnative.ads.PMUnifiedAd;
import com.adsnative.ads.PrefetchAds;
import com.adsnative.util.ANLog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.mediation.NativeAppInstallAdMapper;
import com.google.android.gms.ads.mediation.NativeContentAdMapper;
import com.google.android.gms.ads.mediation.NativeMediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventNative;
import com.google.android.gms.ads.mediation.customevent.CustomEventNativeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sijojohn on 12/06/18.
 */

public class PolymorphAdapter implements CustomEventNative {

    @Override
    public void requestNativeAd(Context context, CustomEventNativeListener customEventNativeListener, String parameters, NativeMediationAdRequest nativeMediationAdRequest, Bundle bundle) {
        NativeAdUnit nativeAdUnit;
        // check if there's a cached PM ad
        if (PrefetchAds.getSize() > 0 && ((nativeAdUnit = PrefetchAds.getAd()) != null)) {
            final PolymorphStaticNativeAd polymorphStaticNativeAd = new PolymorphStaticNativeAd(
                    context, nativeAdUnit, customEventNativeListener, nativeMediationAdRequest, PrefetchAds.getAdRequest());
            polymorphStaticNativeAd.loadAd();

        } else {
            ANLog.d("Couldn't find Prefetched Native ad");
            customEventNativeListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    static class PolymorphStaticNativeAd {

        private Context mContext;
        private NativeAdUnit mNativeAd;
        private CustomEventNativeListener mCustomEventNativeListener;
        private NativeMediationAdRequest mNativeMediationRequest;
        private PMUnifiedAd mPMUnifiedAd;

        PolymorphStaticNativeAd(final Context context,
                                final NativeAdUnit nativeAd,
                                final CustomEventNativeListener customEventNativeListener, NativeMediationAdRequest nativeMediationAdRequest, PMUnifiedAd adRequest) {
            mContext = context;
            mNativeAd = nativeAd;
            mCustomEventNativeListener = customEventNativeListener;
            mNativeMediationRequest = nativeMediationAdRequest;
            mPMUnifiedAd = adRequest;
        }

        void loadAd() {
            this.onAdLoaded(mNativeAd);
        }

        public void onAdLoaded(NativeAdUnit nativeAdUnit) {
            if (nativeAdUnit.getCallToAction() != null && !nativeAdUnit.getCallToAction().isEmpty()
                    && (nativeAdUnit.getIconImage() != null || nativeAdUnit.getType().equalsIgnoreCase("facebook"))) {
                mCustomEventNativeListener.onAdLoaded(new PMNativeAppInstallAdMapper(nativeAdUnit, mCustomEventNativeListener, mPMUnifiedAd, mContext));
            } else {
                mCustomEventNativeListener.onAdLoaded(new PMNativeContentAdMapper(nativeAdUnit, mCustomEventNativeListener, mPMUnifiedAd, mContext));
            }
        }

        public void onAdFailed(String message) {
            ANLog.e(message);
            mCustomEventNativeListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
        }

    }

    static class PMNativeContentAdMapper extends NativeContentAdMapper {
        private NativeAdUnit mNativeAdUnit;
        private PMUnifiedAd mPMUnifiedAd;

        PMNativeContentAdMapper(NativeAdUnit nativeAdUnit, final CustomEventNativeListener mCustomEventNativeListener, PMUnifiedAd adRequest, Context mContext) {
            nativeAdUnit.setPubCallbacksListener(new ANAdListener() {
                @Override
                public void onAdLoaded(NativeAdUnit nativeAdUnit) {

                }

                @Override
                public void onAdFailed(String message) {

                }

                @Override
                public void onAdImpressionRecorded() {
                    if (getOverrideImpressionRecording()) {
                        ANLog.d("Firing DFP impression tracker");
                        mCustomEventNativeListener.onAdImpression();
                    }
                }

                @Override
                public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
                    if (getOverrideClickHandling()) {
                        ANLog.d("Firing DFP click tracker");
                        mCustomEventNativeListener.onAdClicked();
                    }
                    return false;
                }
            });
            // Mapping PM ad assets to DFP
            mNativeAdUnit = nativeAdUnit;
            mPMUnifiedAd = adRequest;
            setHeadline(mNativeAdUnit.getTitle());
            setBody(mNativeAdUnit.getSummary());
            setAdvertiser(mNativeAdUnit.getPromotedBy());
            setCallToAction(mNativeAdUnit.getCallToAction());

            // mapping image assets using NativeMappedImage
            if (mNativeAdUnit.getIconImage() != null)
                setLogo(new NativeMappedImage(mNativeAdUnit.getIconImageDrawable(), Uri.parse(mNativeAdUnit.getIconImage()), 1.0));
            List imagesList = new ArrayList();

            if (mNativeAdUnit.getMainImage() != null)
                imagesList.add(new NativeMappedImage(mNativeAdUnit.getMainImageDrawable(), Uri.parse(mNativeAdUnit.getMainImage()), 1.0));
            if (imagesList.size() > 0)
                setImages(imagesList);
            if (mNativeAdUnit.getMediaView() != null) {
                setMediaView(mNativeAdUnit.getMediaView());
                setHasVideoContent(true);
            }
            if (mNativeAdUnit.getAdChoicesView() != null)
                setAdChoicesContent(mNativeAdUnit.getAdChoicesView());
            else if (mNativeAdUnit.getAdChoicesIcon() != null && mNativeAdUnit.getAdChoicesClickThroughUrl() != null) {
                ImageView adchoicesView = new ImageView(mContext);
                nativeAdUnit.loadAdChoicesImage(adchoicesView);
                setAdChoicesContent(adchoicesView);
            }
            // both clicks and impressions will be tracked by PM
            // and forwarded to DFP
            setOverrideClickHandling(true);
            setOverrideImpressionRecording(true);
        }

        @Override
        public void trackViews(View view, Map<String, View> map, Map<String, View> map1) {
            if (getOverrideClickHandling()) {
                mNativeAdUnit.prepare(view);

                // attaching view for tracking clicks and imps
                if (mPMUnifiedAd != null)
                    mPMUnifiedAd.attachViewForInteraction(mNativeAdUnit, view);
            }
        }

        @Override
        public void recordImpression() {
            if (!getOverrideImpressionRecording()) {
                ANLog.d("Firing PM impression tracker");
                mNativeAdUnit.recordImpression(null);
            }
        }

        @Override
        public void handleClick(View view) {
            if (!getOverrideClickHandling()) {
                ANLog.d("Handling PM click");
                mNativeAdUnit.handleClick(view);
            }
        }
    }

    static class PMNativeAppInstallAdMapper extends NativeAppInstallAdMapper {
        private NativeAdUnit mNativeAdUnit;
        private PMUnifiedAd mPMUnifiedAd;

        PMNativeAppInstallAdMapper(NativeAdUnit nativeAdUnit, final CustomEventNativeListener mCustomEventNativeListener, PMUnifiedAd adRequest, Context mContext) {
            nativeAdUnit.setPubCallbacksListener(new ANAdListener() {
                @Override
                public void onAdLoaded(NativeAdUnit nativeAdUnit) {

                }

                @Override
                public void onAdFailed(String message) {

                }

                @Override
                public void onAdImpressionRecorded() {
                    if (getOverrideImpressionRecording()) {
                        ANLog.d("Firing DFP impression tracker");
                        mCustomEventNativeListener.onAdImpression();
                    }
                }

                @Override
                public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
                    if (getOverrideClickHandling()) {
                        ANLog.d("Firing DFP click tracker");
                        mCustomEventNativeListener.onAdClicked();
                    }
                    return false;
                }
            });
            // Mapping PM ad assets to DFP
            mNativeAdUnit = nativeAdUnit;
            mPMUnifiedAd = adRequest;
            setHeadline(mNativeAdUnit.getTitle());
            setBody(mNativeAdUnit.getSummary());
            setCallToAction(mNativeAdUnit.getCallToAction());

            // mapping image assets using NativeMappedImage
            if (mNativeAdUnit.getIconImage() != null)
                setIcon(new NativeMappedImage(mNativeAdUnit.getIconImageDrawable(), Uri.parse(mNativeAdUnit.getIconImage()), 1.0));
            List imagesList = new ArrayList();
            if (mNativeAdUnit.getMainImage() != null)
                imagesList.add(new NativeMappedImage(mNativeAdUnit.getMainImageDrawable(), Uri.parse(mNativeAdUnit.getMainImage()), 1.0));
            if (imagesList.size() > 0)
                setImages(imagesList);
            if (mNativeAdUnit.getMediaView() != null) {

                setMediaView(mNativeAdUnit.getMediaView());
                setHasVideoContent(true);
            }
            if (mNativeAdUnit.getAdChoicesView() != null)
                setAdChoicesContent(mNativeAdUnit.getAdChoicesView());
            else if (mNativeAdUnit.getAdChoicesIcon() != null && mNativeAdUnit.getAdChoicesClickThroughUrl() != null) {
                ImageView adchoicesView = new ImageView(mContext);
                nativeAdUnit.loadAdChoicesImage(adchoicesView);
                setAdChoicesContent(adchoicesView);
            }
            // both clicks and impressions will be tracked by PM
            // and forwarded to DFP
            setOverrideClickHandling(true);
            setOverrideImpressionRecording(true);
        }

        @Override
        public void trackViews(View view, Map<String, View> map, Map<String, View> map1) {
            if (getOverrideClickHandling()) {
                mNativeAdUnit.prepare(view);

                // attaching view for tracking clicks and imps
                if (mPMUnifiedAd != null)
                    mPMUnifiedAd.attachViewForInteraction(mNativeAdUnit, view);
            }
        }

        @Override
        public void recordImpression() {
            if (!getOverrideImpressionRecording()) {
                ANLog.d("Firing PM impression tracker");
                mNativeAdUnit.recordImpression(null);
            }
        }

        @Override
        public void handleClick(View view) {
            if (!getOverrideClickHandling()) {
                ANLog.d("Handling PM click");
                mNativeAdUnit.handleClick(view);
            }
        }
    }

    static class NativeMappedImage extends NativeAd.Image {

        private Drawable drawable;
        private Uri imageUri;
        private double scale;

        public NativeMappedImage(Drawable drawable, Uri imageUri, double scale) {
            this.drawable = drawable;
            this.imageUri = imageUri;
            this.scale = scale;
        }

        @Override
        public Drawable getDrawable() {
            return drawable;
        }

        @Override
        public Uri getUri() {
            return imageUri;
        }

        @Override
        public double getScale() {
            return scale;
        }
    }
}
