package sure.gomotest.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

import adapter.AlbumFragmentAdapter;
import adapter.PhotographAdapter;
import bean.AlbumBean;
import bean.ShowImageBean;
import event.SaveImageEvent;
import event.ShowActivityEvent;
import event.UpdateAlbumEvent;
import fragment.ShowFragment;
import sure.gomotest.R;
import utils.FileUtil;
import widght.DepthPageTransformer;
import widght.MyViewPager;
import widght.SmoothImageView;

/**
 * Created by dell88 on 2018/3/7 0007.
 * 预览相册图片的activity
 */


public class AlbumDetailActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private String editPath;
    public MyViewPager viewPager;
    private PhotographAdapter photographAdapter;
    private List<AlbumBean> albumList;
    private String albumName;
    private AlbumFragmentAdapter fragmentAdapter;
    private List<ShowFragment> fragmentList = new ArrayList<>();
    private FrameLayout rootFLayout;
    private ArrayList<ShowImageBean> showList = new ArrayList<>();
    private boolean isTransformOut = false;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumName = getIntent().getStringExtra("albumname");
        setContentView(R.layout.detail_activity);
        EventBus.getDefault().register(this);
        rootFLayout = (FrameLayout) findViewById(R.id.detail_activity_fl);
        mToolbar = (Toolbar) findViewById(R.id.detail_activity_toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        viewPager = (MyViewPager) findViewById(R.id.detail_activity_viewPager);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
        mIntent = getIntent();
        showList = mIntent.getParcelableArrayListExtra("imagePaths");

        if (mIntent.getStringArrayListExtra("list") != null) {//相机拍照后的展示
            AlbumBean albumBean = new AlbumBean();
            albumBean.setPhotoPath(mIntent.getStringArrayListExtra("list").get(0));
            albumList = new ArrayList<>();
            albumList.add(albumBean);
            photographAdapter = new PhotographAdapter(albumList);
            viewPager.setAdapter(photographAdapter);
            viewPager.setCurrentItem(mIntent.getIntExtra("position", 0));
            changeBg(Color.BLACK);
        } else {//点击相册后的展示相册图片
            albumList = DataSupport.where("albumName=?", albumName).find(AlbumBean.class);
            for (int i = 0; i < albumList.size(); i++) {
                Bundle bundle = new Bundle();
                bundle.putString("path", albumList.get(i).getPhotoPath());
                bundle.putParcelable("imagePaths", showList.get(i));
                ShowFragment fragment = ShowFragment.newInstance(bundle);
                fragmentList.add(fragment);
            }

            fragmentAdapter = new AlbumFragmentAdapter(getSupportFragmentManager(), fragmentList, albumList, getApplicationContext());
            viewPager.setAdapter(fragmentAdapter);
            final int position = mIntent.getIntExtra("position", 0);
            viewPager.setCurrentItem(position);
            viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    viewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    ShowFragment fragment = fragmentList.get(position);
                    fragment.transformIn();
                }
            });
        }


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
        EventBus.getDefault().unregister(this);
//        MyApplication.getRefWatcher(this).watch(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (editPath != null) {
            File file = new File(editPath);
            if (file.exists()) {
                // 获取该图片的父路径名
                String dirPath = new File(editPath).getParentFile().getAbsolutePath();
                AlbumBean bean = new AlbumBean();
                bean.setAlbumName(dirPath);
                bean.setPhotoPath(editPath);
                SaveImageEvent event = new SaveImageEvent(dirPath, editPath);
                EventBus.getDefault().post(event);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.detail_edit:
                File outputFile = FileUtil.genEditFile();
                editPath = outputFile.getAbsolutePath();
                if (getIntent().getStringArrayListExtra("list") != null) {
                    EditImageActivity.start(AlbumDetailActivity.this, photographAdapter.getUrl(viewPager.getCurrentItem()), outputFile.getAbsolutePath(), 9);
                } else {
                    EditImageActivity.start(AlbumDetailActivity.this, fragmentAdapter.getUrl(viewPager.getCurrentItem()), outputFile.getAbsolutePath(), 9);
                }
                if (mIntent.getStringArrayListExtra("list") != null) {
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshData(UpdateAlbumEvent messageEvent) {
        fragmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        ShowActivityEvent event = new ShowActivityEvent(viewPager.getCurrentItem());
        EventBus.getDefault().post(event);
        if (mIntent.getStringArrayListExtra("list") != null) {
            finish();
        } else {
            transformOut();
        }
    }

    public void getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        rootFLayout.setBackgroundColor(a + rgb);
        mToolbar.setAlpha(alpha * 510f / 255f);
    }

    public void transformOut() {
        if (isTransformOut) {
            return;
        }
        isTransformOut = true;
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < albumList.size()) {
            ShowFragment fragment = fragmentList.get(currentItem);

            fragment.changeBg(Color.TRANSPARENT);
            changeBg(Color.TRANSPARENT);
            fragment.transformOut(new SmoothImageView.onTransformListener() {
                @Override
                public void onTransformCompleted(SmoothImageView.Status status) {
                    exit();
                }
            });
        } else {
            exit();
        }
    }

    private void exit() {
        finish();
        overridePendingTransition(0, 0);
    }


    public void changeBg(int color) {
        rootFLayout.setBackgroundColor(color);
    }
}
