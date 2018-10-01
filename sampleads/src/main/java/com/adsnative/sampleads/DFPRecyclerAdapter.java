package com.adsnative.sampleads;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adsnative.header_bidding.DFP.PolymorphBidder;
import com.adsnative.util.ANLog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

/**
 * Created by sreekanth on 06/02/16.
 */
public class DFPRecyclerAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String DFP_AD_UNIT_ID = "/21666124832/pm_test";
    private static String PM_AD_UNIT_ID = "2Pwo1otj1C5T8y6Uuz9v-xbY1aB09x8rWKvsJ-HI";
    private List<String> mItemList;
    private Context mContext;
    private List<NativeContentAd> mAdsCache;
    private Handler mRestoreCacheHandler;
    private Runnable mRestoreCacheRunnable;
    private Boolean mIsLoading = false;
    private PolymorphBidder pm_bidder;
    public DFPRecyclerAdapter(Context context, List<String> itemList) {
        this.mItemList = itemList;
        this.mContext = context;
        this.mAdsCache = new ArrayList<NativeContentAd>();
        mRestoreCacheHandler = new Handler();
        mRestoreCacheRunnable = new Runnable() {
            @Override
            public void run() {
                refreshCache();
            }
        };
        AdLoader.Builder builder = new AdLoader.Builder(mContext, DFP_AD_UNIT_ID);
        builder.forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
            @Override
            public void onContentAdLoaded(NativeContentAd nativeContentAd) {
                ANLog.e("onAdLoaded");
                mAdsCache.add(nativeContentAd);
                mIsLoading = false;
            }
        });
        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                ANLog.e("onAdFailedToLoad: "+errorCode);
                mIsLoading = false;
            }
        });
        AdLoader adLoader = builder.build();
        pm_bidder = new PolymorphBidder(mContext, PM_AD_UNIT_ID, adLoader);

        refreshCache();

    }

    private void refreshCache() {
        if (mAdsCache != null && mAdsCache.size() < 5) {
            ANLog.e("Requesting DFP HB ads: " +mAdsCache.size());
            if (!mIsLoading) {
                pm_bidder.loadDFPAd();
                mIsLoading = true;
            }
        }
        if (mAdsCache.size() == 5)
            mRestoreCacheHandler.removeCallbacks(mRestoreCacheRunnable);
        else
            mRestoreCacheHandler.postDelayed(mRestoreCacheRunnable, 2000);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row, null);

            CustomViewHolder viewHolder = new CustomViewHolder(view);
            return viewHolder;
        } else {

            View contentAdLayoutView = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.ad_content_ad_view,
                    parent, false);
            return new UnifiedNativeAdViewHolder(contentAdLayoutView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == 0) {
            CustomViewHolder customHolder = (CustomViewHolder) holder;
            customHolder.textView.setText(mItemList.get(position));
        } else {
            ANLog.e("size: "+ mAdsCache.size());
            if (mAdsCache!=null && mAdsCache.size()>0) {
                NativeContentAd nativeAd = mAdsCache.remove(0);
                populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holder).getAdView());}

        }
    }

    @Override
    public int getItemViewType(int position) {
        if ((position>20 && position%10 == 0))
            return 1;
        else return 0;
    }

    @Override
    public int getItemCount() {
        return (mItemList != null ? mItemList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView textView;

        public CustomViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.rv_title);
        }
    }
    private void populateNativeAdView(NativeContentAd nativeContentAd, NativeContentAdView adView) {
        ((TextView) adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeContentAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeContentAd.getCallToAction());
        ((TextView) adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());

        if (nativeContentAd.getImages().size() > 0) {
            ((ImageView) adView.getImageView()).setImageDrawable(nativeContentAd.getImages().get(0).getDrawable());
            ANLog.e("nativeContentAd.getImages().get(0): " + nativeContentAd.getImages().get(0).getUri());
            ANLog.e("nativeContentAd.getImages().get(0): " + nativeContentAd.getImages().get(0).getScale());
            ANLog.e("nativeContentAd.getImages().get(0): " + nativeContentAd.getImages().get(0).getDrawable());
            adView.getMediaView().setVisibility(View.GONE);

        }
        if (nativeContentAd.getLogo() != null) {
            ANLog.e("nativeContentAd.getLogo().getUri(): " + nativeContentAd.getLogo().getUri());
            ((ImageView) adView.getLogoView()).setImageDrawable(nativeContentAd.getLogo().getDrawable());
        }
        adView.setNativeAd(nativeContentAd);
    }
    public class UnifiedNativeAdViewHolder extends RecyclerView.ViewHolder {

        private NativeContentAdView adView;

        public NativeContentAdView getAdView() {
            return adView;
        }

        UnifiedNativeAdViewHolder(View view) {
            super(view);
            adView = (NativeContentAdView) view;

            // The MediaView will display a video asset if one is present in the ad, and the
            // first image asset otherwise.
            adView.setHeadlineView(adView.findViewById(R.id.contentad_headline));
            adView.setBodyView(adView.findViewById(R.id.contentad_body));
            adView.setCallToActionView(adView.findViewById(R.id.contentad_call_to_action));
            adView.setImageView(adView.findViewById(R.id.contentad_image));
            adView.setLogoView(adView.findViewById(R.id.contentad_logo));
            adView.setAdvertiserView(adView.findViewById(R.id.contentad_advertiser));

            MediaView mediaView = (MediaView) adView.findViewById(R.id.contentad_media);
            adView.setMediaView(mediaView);
        }
    }

    public void destroy() {
        if (mAdsCache!=null && mAdsCache.size() >0)
            mAdsCache.clear();
        if (mRestoreCacheHandler.hasMessages(0))
            mRestoreCacheHandler.removeCallbacks(mRestoreCacheRunnable);
    }
}
