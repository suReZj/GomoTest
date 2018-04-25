package sure.gomotest.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapter.SelectAdapter;
import bean.AlbumBean;
import bean.MediaBean;
import event.SaveImageEvent;
import event.UpdateAlbumEvent;
import sure.gomotest.R;
import utils.MyDecorationUtil;

/**
 * Created by zhangzijian on 2018/03/16.
 * 相册选择页activity
 */

public class SelectActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView recyclerView;
    private SelectAdapter selectAdapter = new SelectAdapter();
    private HashMap<String, List<MediaBean>> allPhotosTemp;
    private Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private List<String> albumName;
    private boolean mFlag = false;
    private UpdateAlbumEvent updateEvent = new UpdateAlbumEvent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_activity);
        EventBus.getDefault().register(this);
        initView();
        setListener();
        getData();
    }

    public void initView() {
        mToolbar = (Toolbar) findViewById(R.id.select_activity_toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.select_activity_rv);
        recyclerView.addItemDecoration(new MyDecorationUtil(SelectActivity.this, MyDecorationUtil.VERTICAL_LIST, R.drawable.activity_recyclerview_divider));

        Window window = this.getWindow();
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //如果为全透明模式，取消设置Window半透明的Flag
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //设置状态栏为透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        //设置window的状态栏不可见
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //view不根据系统窗口来调整自己的布局
        ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
            ViewCompat.requestApplyInsets(mChildView);
        }
    }

    public void setListener() {
        selectAdapter.setOnItemClickLitener(new SelectAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(SelectActivity.this, AlbumActivity.class);
                String name = albumName.get(position);
                intent.putExtra("name", name);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getData() {
        final List<AlbumBean> list = new ArrayList<>();//用于将相册中的每张图片路径存放到数据库中
        allPhotosTemp = new HashMap<>();
        albumName = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] projImage = {MediaStore.Images.Media._ID
                        , MediaStore.Images.Media.DATA
                        , MediaStore.Images.Media.SIZE
                        , MediaStore.Images.Media.DISPLAY_NAME};
                Cursor mCursor = getContentResolver().query(mImageUri,
                        projImage,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " +
                                MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png", "image/jpg", "image/bmp"},
                        MediaStore.Images.Media.DATE_MODIFIED + " desc");

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        // 获取图片的路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        int size = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media.SIZE)) / 1024;
                        String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                        // 获取该图片的父路径名
                        String dirPath = new File(path).getParentFile().getAbsolutePath();

                        AlbumBean bean = new AlbumBean();
                        if (allPhotosTemp.containsKey(dirPath)) {
                            List<MediaBean> data = allPhotosTemp.get(dirPath);
                            data.add(new MediaBean(path, size, displayName));
                            bean.setAlbumName(dirPath);
                            bean.setPhotoPath(path);
                            list.add(bean);
                            continue;
                        } else {
                            albumName.add(dirPath);
                            List<MediaBean> data = new ArrayList<>();
                            data.add(new MediaBean(path, size, displayName));
                            allPhotosTemp.put(dirPath, data);
                            bean.setAlbumName(dirPath);
                            bean.setPhotoPath(path);
                            list.add(bean);
                        }
                    }
                    mCursor.close();
                }
                //更新界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        selectAdapter = new SelectAdapter(allPhotosTemp, albumName);
                        recyclerView.setAdapter(selectAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(SelectActivity.this));
                        setListener();
                        DataSupport.deleteAll(AlbumBean.class);
                        DataSupport.saveAllAsync(list).listen(new SaveCallback() {
                            @Override
                            public void onFinish(boolean success) {
                                if (mFlag) {
                                    EventBus.getDefault().post(updateEvent);
                                    mFlag = false;
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
//        MyApplication.getRefWatcher(this).watch(this);
        System.gc();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshData(SaveImageEvent messageEvent) {
        String path = messageEvent.getPath();
        String dirPath = new File(path).getParentFile().getAbsolutePath();
        updateEvent.setAlbumName(dirPath);
        getData();
        mFlag = true;
    }
}
