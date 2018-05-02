package bean;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dell88 on 2018/3/11 0011.
 * 用于照片浏览的实例
 */


public class ShowImageBean implements Parcelable {
    private String mImagePath;
    private Rect mImageBounds;

    public ShowImageBean(String path) {
        this.mImagePath = path;
    }

    public String getPath() {
        return mImagePath;
    }

    public void setPath(String path) {
        this.mImagePath = path;
    }

    public Rect getBounds() {
        return mImageBounds;
    }

    public void setBounds(Rect bounds) {
        this.mImageBounds = bounds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mImagePath);
        dest.writeParcelable(this.mImageBounds, 0);
    }

    protected ShowImageBean(Parcel in) {
        this.mImagePath = in.readString();
        this.mImageBounds = in.readParcelable(Rect.class.getClassLoader());
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
