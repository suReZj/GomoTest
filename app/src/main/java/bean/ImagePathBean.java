package bean;

import org.litepal.crud.DataSupport;


/**
 * Created by zhangzijian on 2018/03/13.
 * 用于存储数据库的图片URL实例
 */



public class ImagePathBean extends DataSupport {
    private String imagePath;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
