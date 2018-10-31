package com.mopub.nativeads;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adsnative.util.ANLog;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdIconView;
import com.mopub.common.logging.MoPubLog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import com.mopub.nativeads.PolymorphNativeAdNetwork.PolymorphVideoEnabledAd;

public class PMAdRenderer implements MoPubAdRenderer<PolymorphVideoEnabledAd> {

    /**
     * Key to set and get star rating text view as an extra in the view binder.
     */
    public static final String VIEW_BINDER_KEY_STAR_RATING = "key_star_rating";

    /**
     * Key to set and get advertiser text view as an extra in the view binder.
     */
    public static final String VIEW_BINDER_KEY_ADVERTISER = "key_advertiser";

    /**
     * Key to set and get store text view as an extra in the view binder.
     */
    public static final String VIEW_BINDER_KEY_STORE = "key_store";

    /**
     * Key to set and get price text view as an extra in the view binder.
     */
    public static final String VIEW_BINDER_KEY_PRICE = "key_price";

    /**
     * Key to set and get the AdChoices icon view as an extra in the view binder.
     */
    public static final String VIEW_BINDER_KEY_AD_CHOICES_ICON_CONTAINER = "ad_choices_container";

    /**
     * ID for the frame layout that wraps the Google ad view.
     */
    @IdRes
    private static final int ID_WRAPPING_FRAME = 1001;

    /**
     * ID for the Google native ad view.
     */
    @IdRes
    private static final int ID_GOOGLE_NATIVE_VIEW = 1002;

    /**
     * A view binder containing the layout resource and views to be rendered by the renderer.
     */
    private final PolymorphViewBinder mViewBinder;

    /**
     * A weak hash map used to keep track of view holder so that the views can be properly recycled.
     */
    private final WeakHashMap<View, GoogleStaticNativeViewHolder> mViewHolderMap;

    public PMAdRenderer(PolymorphViewBinder viewBinder) {
        this.mViewBinder = viewBinder;
        this.mViewHolderMap = new WeakHashMap<>();
    }

    @NonNull
    @Override
    public View createAdView(@NonNull Context context, @Nullable ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(mViewBinder.layoutId, parent, false);
    }

    @Override
    public void renderAdView(@NonNull View view,
                             @NonNull PolymorphVideoEnabledAd nativeAd) {
        GoogleStaticNativeViewHolder viewHolder = mViewHolderMap.get(view);
        if (viewHolder == null) {
            viewHolder = GoogleStaticNativeViewHolder.fromViewBinder(view, mViewBinder);
            mViewHolderMap.put(view, viewHolder);
        }
        update(nativeAd, viewHolder);

    }


    /**
     * This method will render the given native ad view using the native ad and set the views to
     * Google's native ad view.
     *  @param staticNativeAd         a static native ad object containing the required assets to
     *                               set to the native ad view.
     * @param staticNativeViewHolder a static native view holder object containing the mapped
     */


    private void update(PolymorphVideoEnabledAd staticNativeAd,
                        GoogleStaticNativeViewHolder staticNativeViewHolder) {
        NativeRendererHelper.addTextView(
                staticNativeViewHolder.mTitleView, staticNativeAd.getTitle());
        NativeRendererHelper.addTextView(
                staticNativeViewHolder.mTextView, staticNativeAd.getText());
        if (staticNativeViewHolder.mMediaView != null && staticNativeAd.getMediaView() != null) {
            staticNativeViewHolder.mMediaView.removeAllViews();
            ANLog.e("staticNativeAd.getMediaView(): "+ staticNativeAd.getMediaView());
            staticNativeViewHolder.mMediaView.addView(staticNativeAd.getMediaView());

        }
        NativeRendererHelper.addTextView(staticNativeViewHolder.mCallToActionView,
                staticNativeAd.getCallToAction());
//        unifiedAdView.setCallToActionView(staticNativeViewHolder.mCallToActionView);
//        NativeImageHelper.loadImageView(staticNativeAd.getIconImageUrl(),
//                staticNativeViewHolder.mIconImageView);

        if (staticNativeViewHolder.mMainImageView != null && staticNativeAd.getMainImageUrl() != null) {
            NativeImageHelper.loadImageView(staticNativeAd.getMainImageUrl(), staticNativeViewHolder.mMainImageView);
        }
//        if (staticNativeAd.getAdvertiser() != null) {
//            NativeRendererHelper.addTextView(
//                    staticNativeViewHolder.mAdvertiserTextView, staticNativeAd.getAdvertiser());
//            unifiedAdView.setAdvertiserView(staticNativeViewHolder.mAdvertiserTextView);
//        }
//         Add the AdChoices icon to the container if one is provided by the publisher.
        if (staticNativeViewHolder.mAdChoicesIconContainer != null) {
            ANLog.e("adding adchoices");
            staticNativeViewHolder.mAdChoicesIconContainer.removeAllViews();
            final AdChoicesView adChoicesView = (AdChoicesView) staticNativeAd.getAdChoicesView();
            ViewGroup.LayoutParams layoutParams = adChoicesView.getLayoutParams();
            if (layoutParams instanceof RelativeLayout.LayoutParams) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.ALIGN_PARENT_END);
                } else {
                    ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                }
            }
            staticNativeViewHolder.mAdChoicesIconContainer.addView(adChoicesView);
        }

        // Set the privacy information icon to null as the Google Mobile Ads SDK automatically
        // renders the AdChoices icon.
//        NativeRendererHelper.addPrivacyInformationIcon(
//                staticNativeViewHolder.mPrivacyInformationIconImageView, null, null);

    }

    @Override
    public boolean supports(@NonNull BaseNativeAd nativeAd) {
        return nativeAd instanceof PolymorphVideoEnabledAd;
    }

    private static class GoogleStaticNativeViewHolder {
        @Nullable
        View mMainView;
        @Nullable
        TextView mTitleView;
        @Nullable
        TextView mTextView;
        @Nullable
        TextView mCallToActionView;
        @Nullable
        AdIconView mIconImageView;
        @Nullable
        ImageView mMainImageView;
        @Nullable
        RelativeLayout adChoicesContainer;
        @Nullable
        TextView mStarRatingTextView;
        @Nullable
        TextView mAdvertiserTextView;
        @Nullable
        TextView mStoreTextView;
        @Nullable
        TextView mPriceTextView;
        @Nullable
        RelativeLayout mAdChoicesIconContainer;
        @Nullable
        LinearLayout mMediaView;

        private static final GoogleStaticNativeViewHolder EMPTY_VIEW_HOLDER =
                new GoogleStaticNativeViewHolder();

        @NonNull
        public static GoogleStaticNativeViewHolder fromViewBinder(@NonNull View view,
                                                                  @NonNull PolymorphViewBinder
                                                                          viewBinder) {
            final GoogleStaticNativeViewHolder viewHolder = new GoogleStaticNativeViewHolder();
            viewHolder.mMainView = view;
            try {
                viewHolder.mTitleView = (TextView) view.findViewById(viewBinder.titleId);
                viewHolder.mTextView = (TextView) view.findViewById(viewBinder.textId);
                viewHolder.mCallToActionView =
                        (TextView) view.findViewById(viewBinder.callToActionId);

                viewHolder.mIconImageView =
                        (AdIconView) view.findViewById(viewBinder.iconImageViewId);
                viewHolder.mMainImageView =
                        (ImageView) view.findViewById(viewBinder.mainImageViewId);
                viewHolder.mAdChoicesIconContainer =
                        (RelativeLayout) view.findViewById(viewBinder.adChoicesRelativeLayoutId);
                viewHolder.mMediaView =
                        (LinearLayout) view.findViewById(viewBinder.mediaViewId);
                Map<String, Integer> extraViews = viewBinder.extras;
                Integer starRatingTextViewId = extraViews.get(VIEW_BINDER_KEY_STAR_RATING);
                if (starRatingTextViewId != null) {
                    viewHolder.mStarRatingTextView =
                            (TextView) view.findViewById(starRatingTextViewId);
                }
                Integer advertiserTextViewId = extraViews.get(VIEW_BINDER_KEY_ADVERTISER);
                if (advertiserTextViewId != null) {
                    viewHolder.mAdvertiserTextView =
                            (TextView) view.findViewById(advertiserTextViewId);
                }
                Integer storeTextViewId = extraViews.get(VIEW_BINDER_KEY_STORE);
                if (storeTextViewId != null) {
                    viewHolder.mStoreTextView = (TextView) view.findViewById(storeTextViewId);
                }
                Integer priceTextViewId = extraViews.get(VIEW_BINDER_KEY_PRICE);
                if (priceTextViewId != null) {
                    viewHolder.mPriceTextView = (TextView) view.findViewById(priceTextViewId);
                }
                Integer adChoicesIconViewId =
                        extraViews.get(VIEW_BINDER_KEY_AD_CHOICES_ICON_CONTAINER);

                return viewHolder;
            } catch (ClassCastException exception) {
                MoPubLog.w("Could not cast from id in ViewBinder to expected View type", exception);
                return EMPTY_VIEW_HOLDER;
            }
        }
    }

    public static class PolymorphViewBinder {

        final int layoutId;
        final int titleId;
        final int textId;
        final int callToActionId;
        final int adChoicesRelativeLayoutId;
        @NonNull
        final Map<String, Integer> extras;
        final int mediaViewId;
        final int iconImageViewId;
        final int mainImageViewId;
        final int advertiserNameId;

        private PolymorphViewBinder(@NonNull final Builder builder) {
            this.layoutId = builder.layoutId;
            this.titleId = builder.titleId;
            this.textId = builder.textId;
            this.callToActionId = builder.callToActionId;
            this.adChoicesRelativeLayoutId = builder.adChoicesRelativeLayoutId;
            this.extras = builder.extras;
            this.mediaViewId = builder.mediaViewId;
            this.iconImageViewId = builder.iconImageViewId;
            this.mainImageViewId = builder.mainImageViewId;
            this.advertiserNameId = builder.advertiserNameId;
        }

        public static class Builder {

            private final int layoutId;
            private int titleId;
            private int textId;
            private int callToActionId;
            private int adChoicesRelativeLayoutId;

            @NonNull
            private Map<String, Integer> extras = Collections.emptyMap();
            private int mediaViewId;
            private int iconImageViewId;
            private int mainImageViewId;
            private int advertiserNameId;

            public Builder(final int layoutId) {
                this.layoutId = layoutId;
                this.extras = new HashMap<>();
            }

            @NonNull
            public final Builder titleId(final int titleId) {
                this.titleId = titleId;
                return this;
            }

            @NonNull
            public final Builder textId(final int textId) {
                this.textId = textId;
                return this;
            }

            @NonNull
            public final Builder callToActionId(final int callToActionId) {
                this.callToActionId = callToActionId;
                return this;
            }

            @NonNull
            public final Builder adChoicesRelativeLayoutId(final int adChoicesRelativeLayoutId) {
                this.adChoicesRelativeLayoutId = adChoicesRelativeLayoutId;
                return this;
            }

            @NonNull
            public final Builder extras(final Map<String, Integer> resourceIds) {
                this.extras = new HashMap<String, Integer>(resourceIds);
                return this;
            }

            @NonNull
            public final Builder addExtra(final String key, final int resourceId) {
                this.extras.put(key, resourceId);
                return this;
            }

            @NonNull
            public Builder mediaViewId(final int mediaViewId) {
                this.mediaViewId = mediaViewId;
                return this;
            }

            @NonNull
            public Builder iconImageViewId(final int adIconViewId) {
                this.iconImageViewId = adIconViewId;
                return this;
            }

            @NonNull
            public Builder mainImageViewId(final int mainImageViewId) {
                this.mainImageViewId = mainImageViewId;
                return this;
            }

            @NonNull
            public Builder advertiserNameId(final int advertiserNameId) {
                this.advertiserNameId = advertiserNameId;
                return this;
            }

            @NonNull
            public PolymorphViewBinder build() {
                return new PolymorphViewBinder(this);
            }
        }
    }

}
