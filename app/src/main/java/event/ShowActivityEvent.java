package event;


/**
 * Created by zhangzijian on 2018/03/19.
 * 用于recyclerview滑动到当前浏览图片所在的位置
 */


public class ShowActivityEvent {
    private int mPosition;

    public ShowActivityEvent(int position) {
        this.mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }
}
