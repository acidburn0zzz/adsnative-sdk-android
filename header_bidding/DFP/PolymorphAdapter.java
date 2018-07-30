package com.adsnative.header_bidding.DFP;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.adsnative.ads.ANAdListener;
import com.adsnative.ads.NativeAdUnit;
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
                    context, nativeAdUnit, customEventNativeListener, nativeMediationAdRequest);
            polymorphStaticNativeAd.loadAd();

        } else {
            ANLog.e("Couldn't find Prefetched ads");
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

    static class PolymorphStaticNativeAd implements ANAdListener {

        private Context mContext;
        private NativeAdUnit mNativeAd;
        private CustomEventNativeListener mCustomEventNativeListener;
        private NativeMediationAdRequest mNativeMediationRequest;

        PolymorphStaticNativeAd(final Context context,
                                final NativeAdUnit nativeAd,
                                final CustomEventNativeListener customEventNativeListener, NativeMediationAdRequest nativeMediationAdRequest) {
            mContext = context;
            mNativeAd = nativeAd;
            mCustomEventNativeListener = customEventNativeListener;
            mNativeMediationRequest = nativeMediationAdRequest;
        }

        void loadAd() {
            this.onAdLoaded(mNativeAd);
        }

        @Override
        public void onAdLoaded(NativeAdUnit nativeAdUnit) {
            if (mNativeMediationRequest.isContentAdRequested()) {
                mCustomEventNativeListener.onAdLoaded(new PMNativeContentAdMapper(nativeAdUnit));
            } else {
                if (nativeAdUnit.getCallToAction() != null) {
                    mCustomEventNativeListener.onAdLoaded(new PMNativeAppInstallAdMapper(nativeAdUnit));
                } else {
                    this.onAdFailed("Couldn't find app install ad");
                }
            }
        }

        @Override
        public void onAdFailed(String message) {
            ANLog.e(message);
            mCustomEventNativeListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
        }

        @Override
        public void onAdImpressionRecorded() {
        }

        @Override
        public boolean onAdClicked(NativeAdUnit nativeAdUnit) {
            return false;
        }
    }

    static class PMNativeContentAdMapper extends NativeContentAdMapper {
        private NativeAdUnit mNativeAdUnit;

        // Mapping PM ad assets to DFP
        PMNativeContentAdMapper(NativeAdUnit nativeAdUnit) {
            mNativeAdUnit = nativeAdUnit;
            setHeadline(mNativeAdUnit.getTitle());
            setBody(mNativeAdUnit.getSummary());
            setAdvertiser(mNativeAdUnit.getPromotedBy());
            setCallToAction(mNativeAdUnit.getCallToAction());
            // mapping image assets using NativeMappedImage
            setLogo(new NativeMappedImage(mNativeAdUnit.getIconImageDrawable(), Uri.parse(mNativeAdUnit.getIconImage()), 1.0));
            List imagesList = new ArrayList();
            imagesList.add(new NativeMappedImage(mNativeAdUnit.getMainImageDrawable(), Uri.parse(mNativeAdUnit.getMainImage()), 1.0));
            setImages(imagesList);
        }

        @Override
        public void recordImpression() {
            ANLog.d("Firing PM impression tracker");
            super.recordImpression();
            mNativeAdUnit.recordImpression(null);
        }

        @Override
        public void handleClick(View view) {
            ANLog.d("Handling PM click");
            super.handleClick(view);
            mNativeAdUnit.handleClick(view);
        }
    }

    static class PMNativeAppInstallAdMapper extends NativeAppInstallAdMapper {
        private NativeAdUnit mNativeAdUnit;

        // Mapping PM ad assets to DFP
        PMNativeAppInstallAdMapper(NativeAdUnit nativeAdUnit) {
            mNativeAdUnit = nativeAdUnit;
            setHeadline(mNativeAdUnit.getTitle());
            setBody(mNativeAdUnit.getSummary());
            setCallToAction(mNativeAdUnit.getCallToAction());
            // mapping image assets using NativeMappedImage
            setIcon(new NativeMappedImage(mNativeAdUnit.getIconImageDrawable(), Uri.parse(mNativeAdUnit.getIconImage()), 1.0));
            List imagesList = new ArrayList();
            imagesList.add(new NativeMappedImage(mNativeAdUnit.getMainImageDrawable(), Uri.parse(mNativeAdUnit.getMainImage()), 1.0));
            setImages(imagesList);
        }

        @Override
        public void recordImpression() {
            ANLog.d("Firing PM impression tracker");
            super.recordImpression();
            mNativeAdUnit.recordImpression(null);
        }

        @Override
        public void handleClick(View view) {
            ANLog.d("Handling PM click");
            super.handleClick(view);
            mNativeAdUnit.handleClick(view);
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
