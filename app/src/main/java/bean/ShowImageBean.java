package bean;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class ShowImageBean implements Parcelable {
    //图片地址
    private String path;
    // 记录坐标
    private Rect bounds;

    public ShowImageBean(String path) {
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

    protected ShowImageBean(Parcel in) {
        this.path = in.readString();
        this.bounds = in.readParcelable(Rect.class.getClassLoader());
    }

    public static final Creator<ShowImageBean> CREATOR = new Creator<ShowImageBean>() {
        public ShowImageBean createFromParcel(Parcel source) {
            return new ShowImageBean(source);
        }

        public ShowImageBean[] newArray(int size) {
            return new ShowImageBean[size];
        }
    };
}
