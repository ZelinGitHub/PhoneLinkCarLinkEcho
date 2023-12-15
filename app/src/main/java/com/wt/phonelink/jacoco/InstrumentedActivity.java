package com.wt.phonelink.jacoco;

import com.wt.phonelink.MainActivity;

public class InstrumentedActivity extends MainActivity {
    public FinishListener finishListener;

    public void setFinishListener(FinishListener finishListener) {
        this.finishListener = finishListener;
    }

    @Override
    public void onDestroy() {
        if (this.finishListener != null) {
            finishListener.onActivityFinished();
        }
        super.onDestroy();
    }
}