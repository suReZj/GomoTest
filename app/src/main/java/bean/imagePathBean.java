package bean;

import org.litepal.crud.DataSupport;

//用于存储数据库的图片URL实例
public class imagePathBean extends DataSupport {
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
