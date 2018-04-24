package event;

//用于recyclerview滑动到当前浏览图片所在的位置
public class showActivityEvent {
    private int position;

    public showActivityEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
