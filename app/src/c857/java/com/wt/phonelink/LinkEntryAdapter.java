package com.wt.phonelink;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wt.phonelink.constant.Constants;

import java.util.ArrayList;

/**
 * @author renrui
 */
public class LinkEntryAdapter extends RecyclerView.Adapter<LinkEntryAdapter.EnterHolder> {
    private OnItemClickListener onItemClickListener;
    private ArrayList<MainItemBean> mainItemBeanArrayList;
    private int itemWidth;
    public static final int ITEM_WIDTH_BIG = 652;
    public static final int ITEM_WIDTH_SMALL = 540;

    @NonNull
    @Override
    public EnterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EnterHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.layout_item_link_enter, parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull EnterHolder holder, int position) {
        MainItemBean mainItemBean = mainItemBeanArrayList.get(holder.getAdapterPosition());
        holder.enterIcon.setImageDrawable(mainItemBean.getIcon());
        holder.enterTitle.setText(mainItemBean.getTitle());
        holder.enterSubTitle.setText(mainItemBean.getSubTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(mainItemBean.getType(), v);
                }
            }
        });
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.width = itemWidth;
        holder.itemView.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return mainItemBeanArrayList == null ? 0 : mainItemBeanArrayList.size();
    }

    public void setOnItemClickListener(OnItemClickListener icl) {
        this.onItemClickListener = icl;
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }

    static class EnterHolder extends RecyclerView.ViewHolder {
        private ImageView enterIcon;
        private TextView enterTitle;
        private TextView enterSubTitle;

        public EnterHolder(@NonNull View itemView) {
            super(itemView);
            enterIcon = itemView.findViewById(R.id.enter_item_icon_id);
            enterTitle = itemView.findViewById(R.id.enter_item_title_id);
            enterSubTitle = itemView.findViewById(R.id.enter_item_sub_title_id);
        }
    }


    public int initMainItemBeanArrayList(Context context) {
        if (mainItemBeanArrayList == null) {
            mainItemBeanArrayList = new ArrayList<>();
        }
        mainItemBeanArrayList.clear();
        Resources resources = context.getResources();
        MainItemBean mainItemBean = new MainItemBean(
                Constants.LINK_TYPE_TINNOVE_BOX,
                resources.getDrawable(R.drawable.image_icon_tlink, null),
                resources.getString(R.string.title_wu_tong),
                resources.getString(R.string.title_sub_wu_tong)
        );
        mainItemBeanArrayList.add(mainItemBean);
        mainItemBean = new MainItemBean(
                Constants.LINK_TYPE_HICAR,
                resources.getDrawable(R.drawable.image_icon_huaweihicar, null),
                resources.getString(R.string.title_hua_wei),
                resources.getString(R.string.title_sub_hua_wei)
        );
        mainItemBeanArrayList.add(mainItemBean);
        mainItemBean = new MainItemBean(
                Constants.LINK_TYPE_ICCOA,
                resources.getDrawable(R.drawable.image_icon_iccoa, null),
                resources.getString(R.string.title_car_link),
                resources.getString(R.string.title_sub_car_link)
        );
        mainItemBeanArrayList.add(mainItemBean);
        return mainItemBeanArrayList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String linkType, View view);
    }
}
