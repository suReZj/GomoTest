package bean;

import android.util.Log;

import org.litepal.crud.DataSupport;

/**
 * Created by zhangzijian on 2018/03/13.
 */

public class AlbumBean extends DataSupport {
    private String albumName;
    private String path;

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
