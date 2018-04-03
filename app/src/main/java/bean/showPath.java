package bean;

import org.litepal.crud.DataSupport;

public class showPath extends DataSupport{
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
