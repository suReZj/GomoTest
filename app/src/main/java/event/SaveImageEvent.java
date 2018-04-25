package event;

/**
 * Created by zhangzijian on 2018/03/19.
 * 用于图片编辑后产生新图片时刷新相册
 */

public class SaveImageEvent {
    private String albumName;
    private String imagePath;

    public SaveImageEvent(String albumName, String path) {
        this.albumName = albumName;
        this.imagePath = path;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getPath() {
        return imagePath;
    }

    public void setPath(String path) {
        this.imagePath = path;
    }
}
