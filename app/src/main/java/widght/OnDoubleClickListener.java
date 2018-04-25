package widght;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhangzijian on 2018/03/13.
 */

public class OnDoubleClickListener implements View.OnTouchListener {
    private final String TAG = this.getClass().getSimpleName();
    private int mCount = 0;
    private long firClick = 0;
    private long secClick = 0;
    /**
     * 两次点击时间间隔，单位毫秒
     */
    private final int interval = 1500;
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
                firClick = System.currentTimeMillis();
            } else if (2 == mCount) {
                secClick = System.currentTimeMillis();
                if (secClick - firClick < interval) {
                    if (mCallback != null) {
                        mCallback.onDoubleClick();
                    } else {
                        Log.e(TAG, "请在构造方法中传入一个双击回调");
                    }
                    mCount = 0;
                    firClick = 0;
                } else {
                    firClick = secClick;
                    mCount = 1;
                }
                secClick = 0;
            }
        }
        return true;
    }
}
