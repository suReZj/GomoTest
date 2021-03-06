package com.xinlan.imageeditlibrary.editimage.view;


import java.util.LinkedHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.xinlan.imageeditlibrary.editimage.EditImageActivity;

/**
 * 贴图操作控件
 *
 * @author panyi
 */
public class StickerView extends View {
    private static int STATUS_IDLE = 0;
    private static int STATUS_MOVE = 1;// 移动状态
    private static int STATUS_DELETE = 2;// 删除状态
    private static int STATUS_ROTATE = 3;// 图片旋转状态
    private static int STATUS_SCALE = 4;

    private int imageCount;// 已加入照片的数量
    private Context mContext;
    private int currentStatus;// 当前状态
    private StickerItem currentItem;// 当前操作的贴图数据
    private float oldx, oldy;
    private float oldx1, oldy1;

    private Paint rectPaint = new Paint();
    private Paint boxPaint = new Paint();
    int deleteId = -1;

    private LinkedHashMap<Integer, StickerItem> bank = new LinkedHashMap<Integer, StickerItem>();// 存贮每层贴图数据

    public StickerView(Context context) {
        super(context);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        currentStatus = STATUS_IDLE;

        rectPaint.setColor(Color.RED);
        rectPaint.setAlpha(100);

    }

    public void addBitImage(final Bitmap addBit) {
        StickerItem item = new StickerItem(this.getContext());
        item.init(addBit, this);
        if (currentItem != null) {
            currentItem.isDrawHelpTool = false;
        }
        bank.put(++imageCount, item);
        this.invalidate();// 重绘视图
    }

    /**
     * 绘制客户页面
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // System.out.println("on draw!!~");
        for (Integer id : bank.keySet()) {
            StickerItem item = bank.get(id);
            item.draw(canvas);
        }// end for each
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // System.out.println(w + "   " + h + "    " + oldw + "   " + oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);// 是否向下传递事件标志 true为消耗

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        int top;
        int bottom;
        int right;
        int left;
        RectF size;
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                for (Integer id : bank.keySet()) {
                    if(bank.size()==1){
                        currentItem=bank.get(id);
                    }
                    oldx = event.getX(0);
                    oldy = event.getY(0);
                    oldx1 = event.getX(1);
                    oldy1 = event.getY(1);
                    StickerItem item = bank.get(id);
//                    if(item.dstRect.contains(oldx,oldy)&&item.dstRect.contains(oldx1,oldy1)){
                    if(item==currentItem){
                        if (currentItem != null) {
                            currentItem.isDrawHelpTool = false;
                        }
                        currentStatus = STATUS_SCALE;
                        currentItem = item;
                        currentItem.isDrawHelpTool = true;
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:

                for (Integer id : bank.keySet()) {
                    StickerItem item = bank.get(id);


                    if (item.detectDeleteRect.contains(x, y)) {// 删除模式
                        // ret = true;
                        deleteId = id;
                        currentStatus = STATUS_DELETE;
                    } else if (item.detectRotateRect.contains(x, y)) {// 点击了旋转按钮
                        ret = true;
                        if (currentItem != null) {
                            currentItem.isDrawHelpTool = false;
                        }
                        currentItem = item;
                        currentItem.isDrawHelpTool = true;
                        currentStatus = STATUS_ROTATE;
                        oldx = x;
                        oldy = y;
                    } else if (item.dstRect.contains(x, y)) {// 移动模式
                        // 被选中一张贴图
                        ret = true;
                        if (currentItem != null) {
                            currentItem.isDrawHelpTool = false;
                        }
                        currentItem = item;
                        currentItem.isDrawHelpTool = true;
                        currentStatus = STATUS_MOVE;
                        oldx = x;
                        oldy = y;
                    }else {

                    }
                }// end for each
                break;


            case MotionEvent.ACTION_MOVE:
                top = this.getTop();
                bottom = this.getBottom();
                right = this.getRight();
                left = this.getLeft();
                size = new RectF(left, top, right, bottom);
                ret = true;
                if (currentStatus == STATUS_MOVE) {// 移动贴图

                    float dx = x - oldx;
                    float dy = y - oldy;
                    if (currentItem != null) {
                        currentItem.updatePos(dx, dy);
                            invalidate();
                    }// end if
                    oldx = x;
                    oldy = y;
                } else if (currentStatus == STATUS_ROTATE) {// 旋转 缩放图片操作
                    float dx = x - oldx;
                    float dy = y - oldy;
                    if (currentItem != null) {
                        currentItem.updateRotateAndScale(oldx, oldy, dx, dy);// 旋转
                        if(Math.max(currentItem.dstRect.right-currentItem.dstRect.left,currentItem.dstRect.bottom-currentItem.dstRect.top)>Math.max((bottom-top),right-left)){
                            currentItem.updateRotateAndScale(-oldx, -oldy, -dx, -dy);
                        }else {
                            invalidate();
                        }
                    }// end if
                    oldx = x;
                    oldy = y;
                } else if (currentStatus == STATUS_SCALE) {
                    try {
                        float x_1 = event.getX(0);
                        float x_2 = event.getX(1);
                        float y_1 = event.getY(0);
                        float y_2 = event.getY(1);
                        float x_3 = x_1 - x_2;
                        float y_3 = y_1 - y_2;
                        double second = Math.sqrt(x_3 * x_3 + y_3 * y_3);
                        x_3 = oldx - oldx1;
                        y_3 = oldy - oldy1;
                        double first = Math.sqrt(x_3 * x_3 + y_3 * y_3);
                        currentItem.updateScale((float) (second / first));
                        if(Math.max(currentItem.dstRect.right-currentItem.dstRect.left,currentItem.dstRect.bottom-currentItem.dstRect.top)>Math.max((bottom-top),right-left)){
                            currentItem.updateScale((float) (first / second));
                            oldx = x_1;
                            oldy = y_1;
                            oldx1 = x_2;
                            oldy1 = y_2;
                        }else {
                            invalidate();
                            oldx = x_1;
                            oldy = y_1;
                            oldx1 = x_2;
                            oldy1 = y_2;
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:

            case MotionEvent.ACTION_UP:
                if (deleteId > 0) {// 删除选定贴图
                    bank.remove(deleteId);
                    invalidate();
                }// end if
                if (!ret && currentItem != null && currentStatus == STATUS_IDLE) {// 没有贴图被选择
                    currentItem.isDrawHelpTool = false;
                    currentItem = null;
                    invalidate();
                }
                currentStatus = STATUS_IDLE;// 返回空闲状态
                deleteId = -1;
                ret = false;
                break;
        }// end switch
        return true;
    }

    public LinkedHashMap<Integer, StickerItem> getBank() {
        return bank;
    }

    public void clear() {
        bank.clear();
        this.invalidate();
    }
}// end class
