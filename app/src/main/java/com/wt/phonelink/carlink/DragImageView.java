package com.wt.phonelink.carlink;

import android.content.Context;
import android.util.AttributeSet;

/*
    private IGestureCallback mGestureCallback = new IGestureCallback.Stub() {
        @Override
        public void onGestureEvent(int gestureEvent) throws RemoteException {
            if(!isMusicPlaying){
                return;
            }

            Log.d(TAG, "onGestureEvent, gestureEvent:" + gestureEvent);
            switch (gestureEvent) {
                case IGestureConstants.GESTURE_WAVE_LEFT:
                    //上一曲
                    UCarAdapter.getInstance().sendKeyEvent(UCarCommon.KeyEventActionType.KEY_EVENT_ACTION_DOWN,
                            UCarCommon.KeyCodeType.KEY_CODE_MEDIA_PREVIOUS, 0);
                    UCarAdapter.getInstance().sendKeyEvent(UCarCommon.KeyEventActionType.KEY_EVENT_ACTION_UP,
                            UCarCommon.KeyCodeType.KEY_CODE_MEDIA_PREVIOUS, 0);
                    break;
                case IGestureConstants.GESTURE_WAVE_RIGHT:
                    //下一曲
                    UCarAdapter.getInstance().sendKeyEvent(UCarCommon.KeyEventActionType.KEY_EVENT_ACTION_DOWN,
                            UCarCommon.KeyCodeType.KEY_CODE_MEDIA_NEXT, 0);
                    UCarAdapter.getInstance().sendKeyEvent(UCarCommon.KeyEventActionType.KEY_EVENT_ACTION_UP,
                            UCarCommon.KeyCodeType.KEY_CODE_MEDIA_NEXT, 0);
                    break;
            }
        }

        @Override
        public void onError(int errorEvent, String errorString) throws RemoteException {
            Log.d(TAG, "onError: errorEvent:" + errorEvent + ", errorString:" + errorString);
        }
    };
*/
public class DragImageView extends androidx.appcompat.widget.AppCompatImageView {

    public DragImageView(Context context) {
        this(context, null);
    }

    public DragImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
