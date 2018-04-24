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
import bean.albumBean;
import bean.showImageBean;
import event.saveImageEvent;
import event.showActivityEvent;
import event.updateAlbumEvent;
import fragment.ShowFragment;
import sure.gomotest.R;
import utils.FileUtil;
import widght.DepthPageTransformer;
import widght.MyViewPager;
import widght.SmoothImageView;


public class AlbumDetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private String path;//图片编辑后的存放路径
    public MyViewPager viewPager;
    private PhotographAdapter adapter;
    private List<albumBean> list;//根据albumName在数据库查询改相册名下的所有照片的list
    private String albumName;//相册名
    private AlbumFragmentAdapter fragmentAdapter;
    private List<ShowFragment> fragmentList = new ArrayList<>();//图片展示的fragment的list
    private FrameLayout rootFLayout;//界面的根布局
    private ArrayList<showImageBean> showList = new ArrayList<>();//用于展示的图片实例list
    private boolean isTransformOut = false;//用于判断当前是否在执行退出动画
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumName = getIntent().getStringExtra("albumname");
        setContentView(R.layout.detail_activity);
        EventBus.getDefault().register(this);
        rootFLayout = (FrameLayout) findViewById(R.id.detail_activity_fl);
        toolbar = (Toolbar) findViewById(R.id.detail_activity_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        viewPager = (MyViewPager) findViewById(R.id.detail_activity_viewPager);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
        intent = getIntent();
        showList = intent.getParcelableArrayListExtra("imagePaths");

        if (intent.getStringArrayListExtra("list") != null) {//相机拍照后的展示
            albumBean albumBean = new albumBean();
            albumBean.setPath(intent.getStringArrayListExtra("list").get(0));
            list = new ArrayList<>();
            list.add(albumBean);
            adapter = new PhotographAdapter(list);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(intent.getIntExtra("position", 0));
            changeBg(Color.BLACK);
        } else {//点击相册后的展示相册图片
            list = DataSupport.where("albumName=?", albumName).find(albumBean.class);
            for (int i = 0; i < list.size(); i++) {
                Bundle bundle = new Bundle();
                bundle.putString("path", list.get(i).getPath());
                bundle.putParcelable("imagePaths", showList.get(i));
                ShowFragment fragment = ShowFragment.newInstance(bundle);
                fragmentList.add(fragment);
            }

            fragmentAdapter = new AlbumFragmentAdapter(getSupportFragmentManager(), fragmentList, list, getApplicationContext());
            viewPager.setAdapter(fragmentAdapter);
            viewPager.setPageMargin((int) getResources().getDimensionPixelOffset(R.dimen.ui_5_dip));
            final int position = intent.getIntExtra("position", 0);
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
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                // 获取该图片的父路径名
                String dirPath = new File(path).getParentFile().getAbsolutePath();
                albumBean bean = new albumBean();
                bean.setAlbumName(dirPath);
                bean.setPath(path);
                saveImageEvent event = new saveImageEvent(dirPath, path);
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
                path = outputFile.getAbsolutePath();
                if (getIntent().getStringArrayListExtra("list") != null) {
                    EditImageActivity.start(AlbumDetailActivity.this, adapter.getUrl(viewPager.getCurrentItem()), outputFile.getAbsolutePath(), 9);
                } else {
                    EditImageActivity.start(AlbumDetailActivity.this, fragmentAdapter.getUrl(viewPager.getCurrentItem()), outputFile.getAbsolutePath(), 9);
                }
                if (intent.getStringArrayListExtra("list") != null) {
                    finish();
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
        if (intent.getStringArrayListExtra("list") != null) {
            finish();
        } else {
            transformOut();
        }
    }

    public void getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        rootFLayout.setBackgroundColor(a + rgb);
        toolbar.setAlpha(alpha * 510f / 255f);
    }

    public void transformOut() {
        if (isTransformOut) {
            return;
        }
        isTransformOut = true;
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < list.size()) {
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
