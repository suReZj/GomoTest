package sure.gomotest.Activity;

import android.app.Application;

import com.previewlibrary.ZoomMediaLoader;

import org.litepal.LitePal;

import util.ImageLoader;

/**
 * Created by zhangzijian on 2018/03/16.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        ZoomMediaLoader.getInstance().init(new ImageLoader());
    }
}
