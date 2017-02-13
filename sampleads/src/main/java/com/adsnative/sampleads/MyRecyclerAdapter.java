package com.adsnative.sampleads;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sreekanth on 06/02/16.
 */
public class MyRecyclerAdapter
        extends RecyclerView.Adapter<MyRecyclerAdapter.CustomViewHolder> {

    private List<String> mItemList;
    private Context mContext;

    public MyRecyclerAdapter(Context context, List<String> itemList) {
        this.mItemList = itemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        //Setting text view title
        holder.textView.setText(mItemList.get(position));
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
}
