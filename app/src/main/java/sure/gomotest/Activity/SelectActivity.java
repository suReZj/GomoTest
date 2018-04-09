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
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapter.select_recycle_adapter;
import bean.AlbumBean;
import bean.MediaBean;
import event.saveImageEvent;
import event.updateAlbumEvent;
import sure.gomotest.R;
import util.MyDecoration;

public class SelectActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView textView;
    private RecyclerView recyclerView;
    private select_recycle_adapter adapter;
    List<MediaBean> mediaBeen = new ArrayList<>();
    HashMap<String, List<MediaBean>> allPhotosTemp;//所有照片
    Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    List<String> albumName;
    boolean flag = false;
    updateAlbumEvent event = new updateAlbumEvent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        EventBus.getDefault().register(this);
        initView();
        setListener();
        getData();
    }

    public void initView() {
        toolbar = (Toolbar) findViewById(R.id.activity_select_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.activity_select_recyclerView);
        textView = (TextView) findViewById(R.id.activity_select_textView);
        recyclerView.addItemDecoration(new MyDecoration(SelectActivity.this, MyDecoration.VERTICAL_LIST, R.drawable.recyclerview_divider));

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_toolbar_item, menu);
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
        final List<AlbumBean> list = new ArrayList<>();
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
                        //用于展示相册初始化界面
                        mediaBeen.add(new MediaBean(path, size, displayName));
                        // 获取该图片的父路径名
                        String dirPath = new File(path).getParentFile().getAbsolutePath();
                        //存储对应关系

                        AlbumBean bean = new AlbumBean();
                        if (allPhotosTemp.containsKey(dirPath)) {
                            List<MediaBean> data = allPhotosTemp.get(dirPath);
                            data.add(new MediaBean(path, size, displayName));

                            bean.setAlbumName(dirPath);
                            bean.setPath(path);
                            list.add(bean);
                            continue;
                        } else {
                            albumName.add(dirPath);
                            List<MediaBean> data = new ArrayList<>();
                            data.add(new MediaBean(path, size, displayName));
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
                        adapter = new select_recycle_adapter(allPhotosTemp, albumName);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(SelectActivity.this));

                        DataSupport.deleteAll(AlbumBean.class);
                        DataSupport.saveAllAsync(list).listen(new SaveCallback() {
                            @Override
                            public void onFinish(boolean success) {
                                if (flag) {
                                    EventBus.getDefault().post(event);
                                    flag = false;
                                }
                            }
                        });

                        adapter.setOnItemClickLitener(new select_recycle_adapter.OnItemClickLitener() {
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
