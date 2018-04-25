package bean;

import android.util.Log;

import org.litepal.crud.DataSupport;

/**
 * Created by zhangzijian on 2018/03/13.
 * 相册照片实体类
 */


public class AlbumBean extends DataSupport {
    private String albumName;
    private String photoPath;

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
