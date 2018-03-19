package event;

/**
 * Created by zhangzijian on 2018/03/19.
 */

public class saveImageEvent {
    private String albumName;
    private String path;

    public saveImageEvent(String albumName, String path) {
        this.albumName = albumName;
        this.path = path;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
