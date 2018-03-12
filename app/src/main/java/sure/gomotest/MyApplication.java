package sure.gomotest;

import android.app.Application;
import android.content.Context;

import com.previewlibrary.ZoomMediaLoader;


/**
 * Created by zhangzijian on 2018/03/08.
 */

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getInstance() {
        return mContext;
    }
}
