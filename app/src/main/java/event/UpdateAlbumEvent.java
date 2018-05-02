package event;

/**
 * Created by zhangzijian on 2018/03/20.
 * 用于刷新AlbumActivity
 */

public class UpdateAlbumEvent {
    private String mAlbumName;

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        this.mAlbumName = albumName;
    }
}
