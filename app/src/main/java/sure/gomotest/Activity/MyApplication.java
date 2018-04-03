package sure.gomotest.Activity;

import android.app.Application;


import org.litepal.LitePal;

import util.CrashHandler;

/**
 * Created by zhangzijian on 2018/03/16.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        CrashHandler.getInstance().init(this);
    }
}
