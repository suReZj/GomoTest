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
import bean.albumBean;
import bean.mediaBean;
import event.saveImageEvent;
import event.updateAlbumEvent;
import sure.gomotest.R;
import utils.MyDecorationUtil;

public class SelectActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SelectAdapter adapter = new SelectAdapter();
    private HashMap<String, List<mediaBean>> allPhotosTemp;//string 为父路径 List<mediaBean>为父路径下的图片实例list
    private Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private List<String> albumName;//用于存放不同相册的名字
    private boolean flag = false;
    private updateAlbumEvent event = new updateAlbumEvent();//用于更新相册的event

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
        toolbar = (Toolbar) findViewById(R.id.select_activity_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.select_activity_rv);
        recyclerView.addItemDecoration(new MyDecorationUtil(SelectActivity.this, MyDecorationUtil.VERTICAL_LIST, R.drawable.recyclerview_divider));

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
        adapter.setOnItemClickLitener(new SelectAdapter.OnItemClickLitener() {
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
        final List<albumBean> list = new ArrayList<>();//用于将相册中的每张图片路径存放到数据库中
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
                        //存储对应关系

                        albumBean bean = new albumBean();
                        if (allPhotosTemp.containsKey(dirPath)) {
                            List<mediaBean> data = allPhotosTemp.get(dirPath);
                            data.add(new mediaBean(path, size, displayName));
                            bean.setAlbumName(dirPath);
                            bean.setPath(path);
                            list.add(bean);
                            continue;
                        } else {
                            albumName.add(dirPath);
                            List<mediaBean> data = new ArrayList<>();
                            data.add(new mediaBean(path, size, displayName));
                            allPhotosTemp.put(dirPath, data);
                            bean.setAlbumName(dirPath);
                            bean.setPath(path);
                            list.add(bean);
                        }
                    }
                    mCursor.close();
                }
                //更新界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new SelectAdapter(allPhotosTemp, albumName);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(SelectActivity.this));
                        setListener();
                        DataSupport.deleteAll(albumBean.class);
                        DataSupport.saveAllAsync(list).listen(new SaveCallback() {
                            @Override
                            public void onFinish(boolean success) {
                                if (flag) {
                                    EventBus.getDefault().post(event);
                                    flag = false;
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
    public void refreshData(saveImageEvent messageEvent) {
        String path = messageEvent.getPath();
        String dirPath = new File(path).getParentFile().getAbsolutePath();
        event.setAlbumName(dirPath);
        getData();
        flag = true;
    }
}
