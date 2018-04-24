package event;

/**
 * Created by zhangzijian on 2018/03/20.
 */

//用于刷新AlbumActivity
public class updateAlbumEvent {
    private String albumName;

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
}
