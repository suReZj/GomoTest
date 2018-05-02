package widght;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhangzijian on 2018/03/13.
 */

public class OnDoubleClickListener implements View.OnTouchListener {
    private final String mTAG = this.getClass().getSimpleName();
    private int mCount = 0;
    private long mFirClick = 0;
    private long mSecClick = 0;
    /**
     * 两次点击时间间隔，单位毫秒
     */
    private final int mInterval = 1500;
    private DoubleClickCallback mCallback;

    public interface DoubleClickCallback {
        void onDoubleClick();
    }

    public OnDoubleClickListener(DoubleClickCallback callback) {
        super();
        this.mCallback = callback;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            mCount++;
            if (1 == mCount) {
                mFirClick = System.currentTimeMillis();
            } else if (2 == mCount) {
                mSecClick = System.currentTimeMillis();
                if (mSecClick - mFirClick < mInterval) {
                    if (mCallback != null) {
                        mCallback.onDoubleClick();
                    } else {
                        Log.e(mTAG, "请在构造方法中传入一个双击回调");
                    }
                    mCount = 0;
                    mFirClick = 0;
                } else {
                    mFirClick = mSecClick;
                    mCount = 1;
                }
                mSecClick = 0;
            }
        }
        return true;
    }
}
