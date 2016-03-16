package com.adsnative.mediation;

import android.content.Context;
import android.view.View;

import com.adsnative.ads.BaseNativeAd;
import com.adsnative.ads.ErrorCode;
import com.adsnative.network.AdResponse;
import com.adsnative.util.ANLog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.formats.NativeAd.Image;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeCustomTemplateAd;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sreekanth on 17/09/15.
 */
public class DFPAdNetwork extends CustomAdNetwork {
    private static final String PLACEMENT_ID_KEY = "placementId";
    private static final String TEMPLATE_ID_KEY = "customTemplateId";
    private static final String AD_TYPE_KEY = "adType";

    // CustomAdNetwork implementation
    @Override
    protected void loadNativeAd(final Context context,
                                final CustomEventListener customEventListener,
                                final AdResponse adResponse) {

        String placementId = null;
        String customTemplateId = null;
        String adType = null;
        JSONObject customAdNetworkData = adResponse.getCustomAdNetworkData();
        try {
            placementId = customAdNetworkData.getString(PLACEMENT_ID_KEY);
            customTemplateId = customAdNetworkData.getString(TEMPLATE_ID_KEY);
            adType = customAdNetworkData.getString(AD_TYPE_KEY);
            ANLog.d("DFPAdNetwork: " + placementId);
            ANLog.d("DFPAdNetwork: " + customTemplateId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (placementId == null || placementId.length() <= 0
                || customTemplateId == null || customTemplateId.length() <= 0) {
            ANLog.e("DFPAdNetwork: " + ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            customEventListener.onNativeAdFailed(ErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        final DFPNativeAd dfpNativeAd =
                new DFPNativeAd(context, placementId, customTemplateId, adType, customEventListener);
        dfpNativeAd.loadAd();
    }

    static class DFPNativeAd extends BaseNativeAd implements
            NativeAppInstallAd.OnAppInstallAdLoadedListener,
            NativeContentAd.OnContentAdLoadedListener,
            NativeCustomTemplateAd.OnCustomTemplateAdLoadedListener {

        private final Context mContext;
        private final String mPlacementId;
        private final CustomEventListener mCustomEventListener;

        private String mCustomTemplateId;
        private String mAdType;

        private AdLoader mDFPAdLoader;
        private NativeCustomTemplateAd mNativeCustomTemplateAd;

        DFPNativeAd(final Context context,
                    final String placementId,
                    final String customTemplateId,
                    final String adType,
                    final CustomEventListener customEventListener) {
            mContext = context.getApplicationContext();
            mPlacementId = placementId;
            mCustomTemplateId = customTemplateId;
            mAdType = adType;
            mCustomEventListener = customEventListener;
        }

        void loadAd() {
            boolean appInstallOrContent = false;

            if (mCustomTemplateId != null && mCustomTemplateId.length() > 0) { //custom template ad
                mDFPAdLoader = new AdLoader.Builder(mContext, mPlacementId)
                        // .forCustomTemplateAd("10063170", this, null)
                        .forCustomTemplateAd(mCustomTemplateId, this, null)
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(int errorCode) {
                                super.onAdFailedToLoad(errorCode);
                                ANLog.e("DFPAdNetwork: " + errorCode);
                                mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
                            }
                        })
                        .withNativeAdOptions(new NativeAdOptions.Builder()
                                .setReturnUrlsForImageAssets(true)
                                .build())
                        .build();
            } else if (mAdType != null && mAdType.length() > 0) {
                if ("app-install".equalsIgnoreCase(mAdType)) { //app-install ad
                    mDFPAdLoader = new AdLoader.Builder(mContext, mPlacementId)
                            .forAppInstallAd(this)
                            .withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(int errorCode) {
                                    super.onAdFailedToLoad(errorCode);
                                    ANLog.e("DFPAdNetwork: " + errorCode);
                                    mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
                                }
                            })
                            .withNativeAdOptions(new NativeAdOptions.Builder()
                                    .setReturnUrlsForImageAssets(true)
                                    .build())
                            .build();
                } else if ("content".equalsIgnoreCase(mAdType)) { //content ad
                    mDFPAdLoader = new AdLoader.Builder(mContext, mPlacementId)
                            .forContentAd(this)
                            .withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(int errorCode) {
                                    super.onAdFailedToLoad(errorCode);
                                    ANLog.e("DFPAdNetwork: " + errorCode);
                                    mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
                                }
                            })
                            .withNativeAdOptions(new NativeAdOptions.Builder()
                                    .setReturnUrlsForImageAssets(true)
                                    .build())
                            .build();
                } else {
                    appInstallOrContent = true;
                }
            } else {
                appInstallOrContent = true;
            }

            //if mAdType is empty or contains invalid adtype, load either app-install or content ad
            if (appInstallOrContent) {
                mDFPAdLoader = new AdLoader.Builder(mContext, mPlacementId)
                        .forAppInstallAd(this)
                        .forContentAd(this)
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(int errorCode) {
                                super.onAdFailedToLoad(errorCode);
                                ANLog.e("DFPAdNetwork: " + errorCode);
                                mCustomEventListener.onNativeAdFailed(ErrorCode.NETWORK_NO_FILL);
                            }
                        })
                        .withNativeAdOptions(new NativeAdOptions.Builder()
                                .setReturnUrlsForImageAssets(true)
                                .build())
                        .build();
            }

            if (mDFPAdLoader != null){
                mDFPAdLoader.loadAd(new PublisherAdRequest.Builder().build());
            }
        }

        // NativeAppInstallAd.OnAppInstallAdLoadedListener
        @Override
        public void onAppInstallAdLoaded(NativeAppInstallAd nativeAppInstallAd) {
            setProviderName(DFPAdNetwork.class.getName());

            setTitle((String) nativeAppInstallAd.getHeadline());
            setSummary((String) nativeAppInstallAd.getBody());

            Image iconImage = nativeAppInstallAd.getIcon();
            if (iconImage != null) {
                setIconImage(iconImage.getUri().toString());
            }

            List<Image> images = nativeAppInstallAd.getImages();
            if (images != null && images.size() > 0) {
                setMainImage(images.get(0).getUri().toString());
            }

            setCallToAction((String) nativeAppInstallAd.getCallToAction());
            setStarRating(nativeAppInstallAd.getStarRating());

            setPromotedByTag("Sponsored");

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
                        mCustomEventListener.onNativeAdLoaded(DFPNativeAd.this);
                    }

                    @Override
                    public void onImagesFailedToCache(ErrorCode errorCode) {
                        ANLog.e("DFPAdNetwork: " + errorCode);
                        mCustomEventListener.onNativeAdLoaded(DFPNativeAd.this);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // NativeContentAd.OnContentAdLoadedListener
        @Override
        public void onContentAdLoaded(NativeContentAd nativeContentAd) {
            setProviderName(DFPAdNetwork.class.getName());

            setTitle((String) nativeContentAd.getHeadline());
            setSummary((String) nativeContentAd.getBody());

            Image iconImage = nativeContentAd.getLogo();
            if (iconImage != null) {
                setIconImage(iconImage.getUri().toString());
            }

            List<Image> images = nativeContentAd.getImages();
            if (images != null && images.size() > 0) {
                setMainImage(images.get(0).getUri().toString());
            }

            setCallToAction((String) nativeContentAd.getCallToAction());

            setPromotedByTag("Sponsored");

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
                        mCustomEventListener.onNativeAdLoaded(DFPNativeAd.this);
                    }

                    @Override
                    public void onImagesFailedToCache(ErrorCode errorCode) {
                        ANLog.e("DFPAdNetwork: " + errorCode);
                        mCustomEventListener.onNativeAdLoaded(DFPNativeAd.this);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // NativeCustomTemplateAd.OnCustomTemplateAdLoadedListener
        @Override
        public void onCustomTemplateAdLoaded(NativeCustomTemplateAd nativeCustomTemplateAd) {
            // store the ad object for impression and click tracking later
            mNativeCustomTemplateAd = nativeCustomTemplateAd;

            ANLog.e("DFPAdNetwork : Available Assets : " + nativeCustomTemplateAd.getAvailableAssetNames());

            setProviderName(DFPAdNetwork.class.getName());

            setTitle((String) nativeCustomTemplateAd.getText("Headline"));
            setSummary((String) nativeCustomTemplateAd.getText("Body"));

            Image iconImage = nativeCustomTemplateAd.getImage("IconImage");
            if (iconImage != null) {
                setIconImage(iconImage.getUri().toString());
            }

            Image mainImage = nativeCustomTemplateAd.getImage("MainImage");
            if (mainImage != null) {
                setMainImage(mainImage.getUri().toString());
            }

            setCallToAction((String) nativeCustomTemplateAd.getText("CallToAction"));

            setPromotedByTag("Sponsored");

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
                        mCustomEventListener.onNativeAdLoaded(DFPNativeAd.this);
                    }

                    @Override
                    public void onImagesFailedToCache(ErrorCode errorCode) {
                        ANLog.e("DFPAdNetwork: " + errorCode);
                        mCustomEventListener.onNativeAdLoaded(DFPNativeAd.this);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // BaseNativeAd
        @Override
        public void prepare(final View view) {
            // nothing to do here...
        }

        @Override
        public void recordImpression() {
            if (mNativeCustomTemplateAd != null) {
                mNativeCustomTemplateAd.recordImpression();
            }
        }

        @Override
        public void handleClick(final View view) {
            if (mNativeCustomTemplateAd != null) {
                mNativeCustomTemplateAd.performClick("MainImage");
            }
        }
    }
}