package com.wt.phonelink.decration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EntriesDecoration extends RecyclerView.ItemDecoration {

    public static final int MARGIN_BIG = 71;
    public static final int MARGIN_SMALL = 20;
    private int margin;


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = margin;
        outRect.right = margin;
    }


    public void setMargin(int margin) {
        this.margin = margin;
    }
}
