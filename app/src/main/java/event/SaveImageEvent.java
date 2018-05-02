package event;

/**
 * Created by zhangzijian on 2018/03/19.
 * 用于图片编辑后产生新图片时刷新相册
 */

public class SaveImageEvent {
    private String mAlbumName;
    private String mImagePath;

    public SaveImageEvent(String albumName, String path) {
        this.mAlbumName = albumName;
        this.mImagePath = path;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        this.mAlbumName = albumName;
    }

    public String getPath() {
        return mImagePath;
    }

    public void setPath(String path) {
        this.mImagePath = path;
    }
}
