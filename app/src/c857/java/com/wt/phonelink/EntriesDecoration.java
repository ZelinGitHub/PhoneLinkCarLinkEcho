package com.wt.phonelink;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author renrui
 */
public class EntriesDecoration extends RecyclerView.ItemDecoration {

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
