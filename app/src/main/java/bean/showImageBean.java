package bean;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;


//用于照片浏览的实例
public class showImageBean implements Parcelable {
    //图片地址
    private String path;
    // 记录坐标
    private Rect bounds;

    public showImageBean(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Rect getBounds() {
        return bounds;
    }

    public void setBounds(Rect bounds) {
        this.bounds = bounds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeParcelable(this.bounds, 0);
    }

    protected showImageBean(Parcel in) {
        this.path = in.readString();
        this.bounds = in.readParcelable(Rect.class.getClassLoader());
    }

    public static final Creator<showImageBean> CREATOR = new Creator<showImageBean>() {
        public showImageBean createFromParcel(Parcel source) {
            return new showImageBean(source);
        }

        public showImageBean[] newArray(int size) {
            return new showImageBean[size];
        }
    };
}
