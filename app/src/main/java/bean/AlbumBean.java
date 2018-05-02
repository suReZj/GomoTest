package bean;


import org.litepal.crud.DataSupport;

/**
 * Created by zhangzijian on 2018/03/13.
 * 相册照片实体类
 */


public class AlbumBean extends DataSupport {
    private String mAlbumName;
    private String mPhotoPath;

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        this.mAlbumName = albumName;
    }

    public String getPhotoPath() {
        return mPhotoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.mPhotoPath = photoPath;
    }
}
