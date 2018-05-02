package bean;

import java.io.Serializable;

/**
 * Created by dell88 on 2018/3/11 0011.
 * 查询本地相册存放照片信息的实例
 */

public class MediaBean implements Serializable {
    private String mLocalPath;
    private long mSize;
    private String mDisplayName;

    public MediaBean(String localPath, long size, String displayName) {
        this.mLocalPath = localPath;
        this.mSize = size;
        this.mDisplayName = displayName;
    }

    public String getLocalPath() {
        return mLocalPath;
    }

    public void setLocalPath(String localPath) {
        this.mLocalPath = localPath;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }
}
