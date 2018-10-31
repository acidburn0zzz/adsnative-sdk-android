package com.mopub.nativeads;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
     * A view binder containing the layout resource and views to be rendered by the renderer.
     */
    private final PolymorphViewBinder mViewBinder;

    /**
     * A weak hash map used to keep track of view holder so that the views can be properly recycled.
     */
    private final WeakHashMap<View, PolymorphViewHolder> mViewHolderMap;

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
        PolymorphViewHolder viewHolder = mViewHolderMap.get(view);
        if (viewHolder == null) {
            viewHolder = PolymorphViewHolder.fromViewBinder(view, mViewBinder);
            mViewHolderMap.put(view, viewHolder);
        }
        update(nativeAd, viewHolder);

    }


    /**
     * This method will render the given native ad view using the native ad and set the views to
     * Google's native ad view.
     *  @param polymorphAd         a static native ad object containing the required assets to
     *                               set to the native ad view.
     * @param polymorphViewHolder a static native view holder object containing the mapped
     */


    private void update(PolymorphVideoEnabledAd polymorphAd,
                        PolymorphViewHolder polymorphViewHolder) {
        NativeRendererHelper.addTextView(
                polymorphViewHolder.mTitleView, polymorphAd.getTitle());
        NativeRendererHelper.addTextView(
                polymorphViewHolder.mTextView, polymorphAd.getText());
        if (polymorphViewHolder.mMediaView != null && polymorphAd.getMediaView() != null) {
            polymorphViewHolder.mMediaView.removeAllViews();
            polymorphViewHolder.mMediaView.addView(polymorphAd.getMediaView());

        }
        NativeRendererHelper.addTextView(polymorphViewHolder.mCallToActionView,
                polymorphAd.getCallToAction());
        if (polymorphViewHolder.mIconImageView != null && polymorphAd.getIconImageUrl() != null) {
            NativeImageHelper.loadImageView(polymorphAd.getIconImageUrl(),
                    polymorphViewHolder.mIconImageView);
        }

        if (polymorphViewHolder.mMainImageView != null && polymorphAd.getMainImageUrl() != null) {
            NativeImageHelper.loadImageView(polymorphAd.getMainImageUrl(), polymorphViewHolder.mMainImageView);
        }

        // Add the AdChoices icon to the container if one is provided by the publisher.
        if (polymorphViewHolder.mAdChoicesIconContainer != null) {
            ANLog.e("adding adchoices");
            polymorphViewHolder.mAdChoicesIconContainer.removeAllViews();
            final AdChoicesView adChoicesView = (AdChoicesView) polymorphAd.getAdChoicesView();
            ViewGroup.LayoutParams layoutParams = adChoicesView.getLayoutParams();
            if (layoutParams instanceof RelativeLayout.LayoutParams) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.ALIGN_PARENT_END);
                } else {
                    ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                }
            }
            polymorphViewHolder.mAdChoicesIconContainer.addView(adChoicesView);
        }
    }

    @Override
    public boolean supports(@NonNull BaseNativeAd nativeAd) {
        return nativeAd instanceof PolymorphVideoEnabledAd;
    }

    private static class PolymorphViewHolder {
        @Nullable
        View mMainView;
        @Nullable
        TextView mTitleView;
        @Nullable
        TextView mTextView;
        @Nullable
        TextView mCallToActionView;
        @Nullable
        ImageView mIconImageView;
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
        LinearLayout mAdChoicesIconContainer;
        @Nullable
        LinearLayout mMediaView;

        private static final PolymorphViewHolder EMPTY_VIEW_HOLDER =
                new PolymorphViewHolder();

        @NonNull
        public static PolymorphViewHolder fromViewBinder(@NonNull View view,
                                                         @NonNull PolymorphViewBinder
                                                                          viewBinder) {
            final PolymorphViewHolder viewHolder = new PolymorphViewHolder();
            viewHolder.mMainView = view;
            try {
                viewHolder.mTitleView = (TextView) view.findViewById(viewBinder.titleId);
                viewHolder.mTextView = (TextView) view.findViewById(viewBinder.textId);
                viewHolder.mCallToActionView =
                        (TextView) view.findViewById(viewBinder.callToActionId);

                viewHolder.mIconImageView =
                        (ImageView) view.findViewById(viewBinder.iconImageViewId);
                viewHolder.mMainImageView =
                        (ImageView) view.findViewById(viewBinder.mainImageViewId);
                viewHolder.mAdChoicesIconContainer =
                        (LinearLayout) view.findViewById(viewBinder.adChoicesRelativeLayoutId);
                viewHolder.mMediaView =
                        (LinearLayout) view.findViewById(viewBinder.mediaViewId);

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
