package sure.gomotest.Activity;

import android.app.Application;


import com.squareup.leakcanary.RefWatcher;

import org.litepal.LitePal;

import utils.CrashHandlerUtil;

/**
 * Created by zhangzijian on 2018/03/16.
 */

public class MyApplication extends Application {
    private static MyApplication instance;
    private RefWatcher mRefWatcher;
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        CrashHandlerUtil.getInstance().init(this);
//        mRefWatcher = LeakCanary.install(this);
    }
    public static MyApplication getInstance() {
        return instance;
    }
//    public static RefWatcher getRefWatcher(Context context){
//        MyApplication baseApplication = (MyApplication) context.getApplicationContext();
//        return baseApplication.mRefWatcher;
//    }

}
