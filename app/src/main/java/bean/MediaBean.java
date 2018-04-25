package bean;

import java.io.Serializable;

/**
 * Created by dell88 on 2018/3/11 0011.
 * 查询本地相册存放照片信息的实例
 */

public class MediaBean implements Serializable {
    private String localPath;
    private long mSize;
    private String displayName;

    public MediaBean(String localPath, long size, String displayName) {
        this.localPath = localPath;
        this.mSize = size;
        this.displayName = displayName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
