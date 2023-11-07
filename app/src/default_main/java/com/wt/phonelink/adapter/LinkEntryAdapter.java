package com.wt.phonelink.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wt.phonelink.R;
import com.wt.phonelink.constant.Constants;

public class LinkEntryAdapter extends RecyclerView.Adapter<LinkEntryAdapter.EnterHolder> {
    private OnItemClickListener onItemClickListener;

    @NonNull
    @Override
    public EnterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EnterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_link_enter, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EnterHolder holder, int position) {
        holder.enterIcon.setBackgroundResource(Constants.sLinkEnterIconRes.get(Constants.sLinkTypes.get(position)));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(Constants.sLinkTypes.get(holder.getAdapterPosition()), v);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return Constants.sLinkTypes.size();
    }

    public void setOnItemClickListener(OnItemClickListener icl) {
        this.onItemClickListener = icl;
    }

    static class EnterHolder extends RecyclerView.ViewHolder {
        private ImageView enterIcon;

        public EnterHolder(@NonNull View itemView) {
            super(itemView);
            enterIcon = itemView.findViewById(R.id.iv_item_enter);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String linkType, View view);
    }
}
