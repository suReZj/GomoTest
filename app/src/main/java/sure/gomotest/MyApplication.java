package sure.gomotest;

import android.app.Application;
import android.content.Context;

import com.previewlibrary.ZoomMediaLoader;

import util.imageLoader;

/**
 * Created by zhangzijian on 2018/03/08.
 */

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        ZoomMediaLoader.getInstance().init(new imageLoader());
    }

    public static Context getInstance() {
        return mContext;
    }
}
