package sure.gomotest.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.xinlan.imageeditlibrary.editimage.EditImageActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import adapter.album_fragment_adapter;
import adapter.album_viewPager_adapter;
import adapter.main_fragment_adapter;
import bean.AlbumBean;
import bean.showPath;
import event.saveImageEvent;
import event.showActivityEvent;
import event.updateAlbumEvent;
import fragment.showFragment;
import sure.gomotest.R;
import util.FileUtils;
import widght.MyViewPager;

import static util.Contants.albumPath;

public class AlbumDetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private String path;
    public MyViewPager viewPager;
    private album_viewPager_adapter adapter;
    private List<AlbumBean> list;
    private String albumName;
    private album_fragment_adapter fragmentAdapter;
    private List<showFragment> fragmentList = new ArrayList<>();
    private int firstIndex;
    private FrameLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumName = getIntent().getStringExtra("albumname");
        setContentView(R.layout.activity_album_detail);
        EventBus.getDefault().register(this);
        toolbar = (Toolbar) findViewById(R.id.activity_detail_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        viewPager = (MyViewPager) findViewById(R.id.activity_detail_viewPager);
        viewPager.setPageMargin((int)getResources().getDimensionPixelOffset(R.dimen.ui_5_dip));
        Intent intent = getIntent();
        if (intent.getStringArrayListExtra("list") != null) {
            AlbumBean albumBean = new AlbumBean();
            albumBean.setPath(intent.getStringArrayListExtra("list").get(0));
            list = new ArrayList<>();
            list.add(albumBean);
            adapter = new album_viewPager_adapter(list);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(intent.getIntExtra("position", 0));
        } else {
            list = DataSupport.where("albumName=?", albumName).find(AlbumBean.class);
            for (int i = 0; i < list.size(); i++) {
                Bundle bundle = new Bundle();
                bundle.putString("path", list.get(i).getPath());
                showFragment fragment = showFragment.newInstance(bundle);
                fragmentList.add(fragment);
            }
            fragmentAdapter = new album_fragment_adapter(getSupportFragmentManager(), fragmentList, list, getApplicationContext());
            viewPager.setAdapter(fragmentAdapter);
            viewPager.setPageMargin((int) getResources().getDimensionPixelOffset(R.dimen.ui_5_dip));
            viewPager.setCurrentItem(intent.getIntExtra("position", 0));
        }
        firstIndex = intent.getIntExtra("position", 0);


        layout=(FrameLayout) findViewById(R.id.activity_detail_layout);

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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        albumPath = "";
        EventBus.getDefault().unregister(this);
        DataSupport.deleteAll(showPath.class);
//        MyApplication.getRefWatcher(this).watch(this);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                // 获取该图片的父路径名
                String dirPath = new File(path).getParentFile().getAbsolutePath();
                AlbumBean bean = new AlbumBean();
                bean.setAlbumName(dirPath);
                bean.setPath(path);
                saveImageEvent event = new saveImageEvent(dirPath, path);
                EventBus.getDefault().post(event);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_toolbar_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.detail_edit:
                File outputFile = FileUtils.genEditFile();
                path = outputFile.getAbsolutePath();
                if (getIntent().getStringArrayListExtra("list") != null) {
                    EditImageActivity.start(AlbumDetailActivity.this, adapter.getUrl(viewPager.getCurrentItem()), outputFile.getAbsolutePath(), 9);
                } else {
                    EditImageActivity.start(AlbumDetailActivity.this, fragmentAdapter.getUrl(viewPager.getCurrentItem()), outputFile.getAbsolutePath(), 9);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshData(updateAlbumEvent messageEvent) {
        fragmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        showActivityEvent event = new showActivityEvent(viewPager.getCurrentItem());
        EventBus.getDefault().post(event);
        if (viewPager.getCurrentItem() != firstIndex) {
            finish();
        } else {
            ActivityCompat.finishAfterTransition(this);
        }
    }

    public void getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        layout.setBackgroundColor(a + rgb);
    }
}
