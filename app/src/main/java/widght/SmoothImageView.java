package widght;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.previewlibrary.view.ImageUtils;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by zhangzijian on 2018/03/13.
 */

public class SmoothImageView extends PhotoView {

    public enum Status {
        STATE_NORMAL,
        STATE_IN,
        STATE_OUT,
        STATE_MOVE,
    }

    private Status mStatus = Status.STATE_NORMAL;
    private static int sTRANSFORM_DURATION = 400;
    private Paint mPaint;
    private Matrix matrix;
    private Transform mStartTransform;
    private Transform mEndTransform;
    private Transform mAnimTransform;
    private Rect mThumbRect;
    private boolean mTransformStart;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private boolean mIsDrag;
    ValueAnimator mAnimator;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBitmapWidth = 0;
        mBitmapHeight = 0;
        mThumbRect = null;
        mPaint = null;
        matrix = null;
        mStartTransform = null;
        mEndTransform = null;
        mAnimTransform = null;
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator.clone();
            mAnimator = null;
        }
    }


    private void initSmoothImageView() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xFF000000);
        matrix = new Matrix();
        setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    public boolean checkMinScale() {
        if (getScale() != 1) {
            setScale(1, true);
            return false;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }

        if (mStatus == Status.STATE_OUT || mStatus == Status.STATE_IN) {
            if (mStartTransform == null || mEndTransform == null || mAnimTransform == null) {
                initTransform();
            }

            if (mAnimTransform == null) {
                super.onDraw(canvas);
                return;
            }

            mPaint.setAlpha(mAnimTransform.alpha);
            canvas.drawPaint(mPaint);
            int saveCount = canvas.getSaveCount();
            matrix.setScale(mAnimTransform.scale, mAnimTransform.scale);
            float translateX = -(mBitmapWidth * mAnimTransform.scale - mAnimTransform.width) / 2;
            float translateY = -(mBitmapHeight * mAnimTransform.scale - mAnimTransform.height) / 2;
            matrix.postTranslate(translateX, translateY);
            canvas.translate(mAnimTransform.left, mAnimTransform.top);
            canvas.clipRect(0, 0, mAnimTransform.width, mAnimTransform.height);
            canvas.concat(matrix);
            getDrawable().draw(canvas);
            canvas.restoreToCount(saveCount);

            if (mTransformStart) {
                startTransform();
            }
        } else if (mStatus == Status.STATE_MOVE) {
            mPaint.setAlpha(0);
            canvas.drawPaint(mPaint);
            super.onDraw(canvas);
        } else {
            mPaint.setAlpha(255);
            canvas.drawPaint(mPaint);
            super.onDraw(canvas);
        }
    }

    private int mDownX, mDownY;
    private boolean mIsMoved = false;
    private boolean mIsDownPhoto = false;
    private int mAlpha = 0;
    private static final int MIN_TRANS_DEST = 5;
    private static final float MAX_TRANS_SCALE = 0.5f;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getScale() == 1) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = (int) event.getX();
                    mDownY = (int) event.getY();
                    if (markTransform == null) {
                        initTransform();
                    }
                    mIsDownPhoto = false;
                    if (markTransform != null) {
                        int startY = (int) markTransform.top;
                        int endY = (int) (markTransform.height + markTransform.top);
                        if (mDownY >= startY && endY >= mDownY) {
                            mIsDownPhoto = true;
                        }
                    }
                    mIsMoved = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mIsDownPhoto && event.getPointerCount() == 1) {
                        break;
                    }
                    int mx = (int) event.getX();
                    int my = (int) event.getY();

                    int offsetX = mx - mDownX;
                    int offsetY = my - mDownY;

                    // 水平方向移动不予处理
                    boolean s = !mIsMoved && (Math.abs(offsetX) > Math.abs(offsetY) || Math.abs(offsetY) < MIN_TRANS_DEST);
                    Matrix matrix = new Matrix();
                    matrix.setScale(1, 1);
                    if (s) {
                        return super.dispatchTouchEvent(event);
                    } else {
                        if (mIsDrag) {
                            return super.dispatchTouchEvent(event);
                        }
                        // 一指滑动时，才对图片进行移动缩放处理
                        if (event.getPointerCount() == 1) {
                            mStatus = Status.STATE_MOVE;
                            offsetLeftAndRight(offsetX);
                            offsetTopAndBottom(offsetY);
                            float scale = moveScale();
                            float scaleXY = 1 - scale * 0.1f;
                            setScaleY(scaleXY);
                            setScaleX(scaleXY);
                            mIsMoved = true;
                            mAlpha = (int) (255 * (1 - scale * 0.5f));
                            invalidate();
                            if (mAlpha < 0) {
                                mAlpha = 0;
                            }
                            if (mAlphaChangeListener != null) {
                                mAlphaChangeListener.onAlphaChange(mAlpha);
                            }
                            return true;
                        }
                        // 多指滑动，直接屏蔽事件
                        else {
                            return true;
                        }
                    }
                case MotionEvent.ACTION_UP:
//                        if (isMoved) {
//                            if (moveScale() <= MAX_TRANS_SCALE) {
//                                moveToOldPosition();
//                            } else {
//                                changeTransform();
//                                setTag(com.previewlibrary.R.id.item_image_key,true);
//                                if (transformOutListener != null) {
//                                    transformOutListener.onTransformOut();
//                                }
//                            }
//                            return true;
//                        }
//                        break;
                case MotionEvent.ACTION_CANCEL:
                    if (mIsMoved) {
                        if (moveScale() <= MAX_TRANS_SCALE) {
                            moveToOldPosition();
                        } else {
                            changeTransform();
                            setTag(com.previewlibrary.R.id.item_image_key, true);
                            if (mTransformOutListener != null) {
                                mTransformOutListener.onTransformOut();
                            }
                        }
                        return true;
                    }
                    break;
                default: {

                }
            }
        }
//        }
        return super.dispatchTouchEvent(event);

    }

    /**
     * 未达到关闭的阈值松手时，返回到初始位置
     */
    private void moveToOldPosition() {
        ValueAnimator va = ValueAnimator.ofInt(getTop(), 0);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int mStartValue = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                if (mStartValue != 0) {
                    offsetTopAndBottom(value - mStartValue);
                }
                mStartValue = value;
            }
        });

        ValueAnimator leftAnim = ValueAnimator.ofInt(getLeft(), 0);
        leftAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int mStartValue = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                if (mStartValue != 0) {
                    offsetLeftAndRight(value - mStartValue);
                }
                mStartValue = value;
            }
        });

        ValueAnimator alphaAnim = ValueAnimator.ofInt(mAlpha, 255);
        alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mAlphaChangeListener != null) {
                    mAlphaChangeListener.onAlphaChange((Integer) animation.getAnimatedValue());
                }
            }
        });

        ValueAnimator scaleAnim = ValueAnimator.ofFloat(getScaleX(), 1);
        scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                setScaleX(scale);
                setScaleY(scale);
            }
        });

        AnimatorSet as = new AnimatorSet();
        as.setDuration(sTRANSFORM_DURATION);
        as.setInterpolator(new AccelerateDecelerateInterpolator());
        as.playTogether(va, leftAnim, scaleAnim, alphaAnim);
        as.start();
    }

    private float moveScale() {
        if (markTransform == null) {
            initTransform();
        }
        return Math.abs(getTop() / markTransform.height);
    }

    private OnAlphaChangeListener mAlphaChangeListener;
    private OnTransformOutListener mTransformOutListener;

    public void setTransformOutListener(OnTransformOutListener transformOutListener) {
        this.mTransformOutListener = transformOutListener;
    }

    public void setAlphaChangeListener(OnAlphaChangeListener alphaChangeListener) {
        this.mAlphaChangeListener = alphaChangeListener;
    }

    public interface OnTransformOutListener {
        void onTransformOut();
    }

    public interface OnAlphaChangeListener {
        void onAlphaChange(int alpha);
    }

    private Transform markTransform;

    private void changeTransform() {
        if (markTransform != null) {
            Transform tempTransform = markTransform.clone();
            tempTransform.top = markTransform.top + getTop();
            tempTransform.left = markTransform.left + getLeft();
            tempTransform.alpha = mAlpha;
            tempTransform.scale = markTransform.scale - (1 - getScaleX()) * markTransform.scale;
            mAnimTransform = tempTransform.clone();
            mEndTransform = tempTransform.clone();
        }
    }

    private void startTransform() {
        mTransformStart = false;
        if (mAnimTransform == null) {
            return;
        }

        mAnimator = new ValueAnimator();
        mAnimator.setDuration(sTRANSFORM_DURATION);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        if (mStatus == Status.STATE_IN) {
            PropertyValuesHolder scaleHolder = PropertyValuesHolder.ofFloat("animScale", mStartTransform.scale, mEndTransform.scale);
            PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("animAlpha", mStartTransform.alpha, mEndTransform.alpha);
            PropertyValuesHolder leftHolder = PropertyValuesHolder.ofFloat("animLeft", mStartTransform.left, mEndTransform.left);
            PropertyValuesHolder topHolder = PropertyValuesHolder.ofFloat("animTop", mStartTransform.top, mEndTransform.top);
            PropertyValuesHolder widthHolder = PropertyValuesHolder.ofFloat("animWidth", mStartTransform.width, mEndTransform.width);
            PropertyValuesHolder heightHolder = PropertyValuesHolder.ofFloat("animHeight", mStartTransform.height, mEndTransform.height);
            mAnimator.setValues(scaleHolder, alphaHolder, leftHolder, topHolder, widthHolder, heightHolder);
        } else if (mStatus == Status.STATE_OUT) {
            PropertyValuesHolder scaleHolder = PropertyValuesHolder.ofFloat("animScale", mEndTransform.scale, mStartTransform.scale);
            PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("animAlpha", mEndTransform.alpha, mStartTransform.alpha);
            PropertyValuesHolder leftHolder = PropertyValuesHolder.ofFloat("animLeft", mEndTransform.left, mStartTransform.left);
            PropertyValuesHolder topHolder = PropertyValuesHolder.ofFloat("animTop", mEndTransform.top, mStartTransform.top);
            PropertyValuesHolder widthHolder = PropertyValuesHolder.ofFloat("animWidth", mEndTransform.width, mStartTransform.width);
            PropertyValuesHolder heightHolder = PropertyValuesHolder.ofFloat("animHeight", mEndTransform.height, mStartTransform.height);
            mAnimator.setValues(scaleHolder, alphaHolder, leftHolder, topHolder, widthHolder, heightHolder);
        }
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimTransform.alpha = (Integer) animation.getAnimatedValue("animAlpha");
                mAnimTransform.scale = (float) animation.getAnimatedValue("animScale");
                mAnimTransform.left = (float) animation.getAnimatedValue("animLeft");
                mAnimTransform.top = (float) animation.getAnimatedValue("animTop");
                mAnimTransform.width = (float) animation.getAnimatedValue("animWidth");
                mAnimTransform.height = (float) animation.getAnimatedValue("animHeight");
                invalidate();
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (getTag(com.previewlibrary.R.id.item_image_key) != null) {
                    setTag(com.previewlibrary.R.id.item_image_key, null);
                    setOnLongClickListener(null);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                /*
                 * 如果是进入的话，当然是希望最后停留在center_crop的区域。但是如果是out的话，就不应该是center_crop的位置了
                 * ， 而应该是最后变化的位置，因为当out的时候结束时，不回复视图是Normal，要不然会有一个突然闪动回去的bug
                 */
                if (mOnTransformListener != null) {
                    mOnTransformListener.onTransformCompleted(mStatus);
                }
                if (mStatus == Status.STATE_IN) {
                    mStatus = Status.STATE_NORMAL;
                }
            }
        });
        mAnimator.start();

    }

    public void transformIn(OnTransformListener listener) {
        setOnTransformListener(listener);
        mTransformStart = true;
        mStatus = Status.STATE_IN;
        invalidate();
    }

    public void transformOut(OnTransformListener listener) {
        if (getTop() != 0) {
            offsetTopAndBottom(-getTop());
        }
        if (getLeft() != 0) {
            offsetLeftAndRight(-getLeft());
        }
        if (getScaleX() != 1) {
            setScaleX(1);
            setScaleY(1);
        }
        setOnTransformListener(listener);
        mTransformStart = true;
        mStatus = Status.STATE_OUT;
        invalidate();
    }

    /**
     * 设置起始位置图片的Rect
     * g
     *
     * @param thumbRect 参数
     */
    public void setThumbRect(Rect thumbRect) {
        this.mThumbRect = thumbRect;
    }

    private void initTransform() {
        if (getDrawable() == null) {
            return;
        }
        if (mStartTransform != null && mEndTransform != null && mAnimTransform != null) {
            return;
        }
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        if (getDrawable() instanceof BitmapDrawable) {
            Bitmap mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            mBitmapWidth = mBitmap.getWidth();
            mBitmapHeight = mBitmap.getHeight();
        } else {
            Bitmap mBitmap = Bitmap.createBitmap(getDrawable().getIntrinsicWidth(),
                    getDrawable().getIntrinsicHeight(), Bitmap.Config.RGB_565);
            mBitmapWidth = mBitmap.getWidth();
            mBitmapHeight = mBitmap.getHeight();
        }
        mStartTransform = new Transform();
        mStartTransform.alpha = 0;
        if (mThumbRect == null) {
            mThumbRect = new Rect();
        }
        mStartTransform.left = mThumbRect.left;
        mStartTransform.top = mThumbRect.top - ImageUtils.getStatusBarHeight(getContext().getApplicationContext());
        mStartTransform.width = mThumbRect.width();
        mStartTransform.height = mThumbRect.height();
        //开始时以CenterCrop方式显示，缩放图片使图片的一边等于起始区域的一边，另一边大于起始区域
        float startScaleX = (float) mThumbRect.width() / mBitmapWidth;
        float startScaleY = (float) mThumbRect.height() / mBitmapHeight;
        mStartTransform.scale = startScaleX > startScaleY ? startScaleX : startScaleY;

        //结束时以fitCenter方式显示，缩放图片使图片的一边等于View的一边，另一边大于View
        float endScaleX = (float) getWidth() / mBitmapWidth;
        float endScaleY = (float) getHeight() / mBitmapHeight;
        mEndTransform = new Transform();
        mEndTransform.scale = endScaleX < endScaleY ? endScaleX : endScaleY;
        mEndTransform.alpha = 255;
        int endBitmapWidth = (int) (mEndTransform.scale * mBitmapWidth);
        int endBitmapHeight = (int) (mEndTransform.scale * mBitmapHeight);
        mEndTransform.left = (getWidth() - endBitmapWidth) / 2;
        mEndTransform.top = (getHeight() - endBitmapHeight) / 2;
        mEndTransform.width = endBitmapWidth;
        mEndTransform.height = endBitmapHeight;

        if (mStatus == Status.STATE_IN) {
            mAnimTransform = mStartTransform.clone();
        } else if (mStatus == Status.STATE_OUT) {
            mAnimTransform = mEndTransform.clone();
        }
        markTransform = mEndTransform;
    }

    private OnTransformListener mOnTransformListener;

    public void setOnTransformListener(SmoothImageView.OnTransformListener onTransformListener) {
        this.mOnTransformListener = onTransformListener;
    }

    public interface OnTransformListener {
        void onTransformCompleted(Status status);
    }

    private class Transform implements Cloneable {
        public float left, top, width, height;
        public int alpha;
        public float scale;

        @Override
        public Transform clone() {
            Transform obj = null;
            try {
                obj = (Transform) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return obj;
        }
    }

    public SmoothImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSmoothImageView();
    }

    public SmoothImageView(Context context) {
        super(context);
        initSmoothImageView();
    }

    /***
     * 设置图片拖拽返回
     * @param isDrag  true  可以 false 默认 true
     * **/
    public void setDrag(boolean isDrag) {
        this.mIsDrag = isDrag;
    }

    /***
     *  设置动画的时长
     * @param duration  单位毫秒
     * **/
    public static void setDuration(int duration) {
        sTRANSFORM_DURATION = duration;
    }

    @Override
    public void setOnScaleChangeListener(PhotoViewAttacher.OnScaleChangeListener onScaleChangeListener) {
        super.setOnScaleChangeListener(onScaleChangeListener);

    }
}
