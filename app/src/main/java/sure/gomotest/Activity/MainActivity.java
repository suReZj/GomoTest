package sure.gomotest.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.widget.SpringView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import adapter.ImageAdapter;
import bean.ImagePathBean;
import bean.ShowImageBean;
import event.ShowActivityEvent;
import gson.ResultGson;
import gson.WelfareGson;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit.GetData;
import retrofit2.Retrofit;
import sure.gomotest.R;
import widght.OnDoubleClickListener;
import utils.RetrofitUtil;
import widght.MyLayoutManager;

/**
 * Created by dell88 on 2018/3/7 0007.
 * app首页
 */

public class MainActivity extends AppCompatActivity {
    private File mPhotoFile;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ImageAdapter mImageAdapter;
    private int mPage = 1;
    private ArrayList<String> mImageList = new ArrayList<>();
    private SpringView mSpringView;
    private MyLayoutManager mLayoutManager;
    private long mBackLastPressedTimestamp = 0;
    private List<ResultGson> mResultList;
    private Retrofit mRetrofit;
    private GetData mGetData;
    private List<ImagePathBean> mPathList = new ArrayList<>();
    private ImagePathBean mImagePath;
    private ArrayList<ShowImageBean> mShowList = new ArrayList<>();
    private int mStartPos;
    private int mEndPos;
    private int mIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mRetrofit = RetrofitUtil.getRetrofit(getResources().getString(R.string.main_activity_url));
        mGetData = mRetrofit.create(GetData.class);
        setContentView(R.layout.main_activity);
        initView();
        setListener();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_activity_rv);
        mLayoutManager = new MyLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mLayoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mLayoutManager.invalidateSpanAssignments(); //防止第一行到顶部有空白区域
            }
        });
        mImageAdapter = new ImageAdapter(mImageList, MainActivity.this);
        mRecyclerView.setAdapter(mImageAdapter);
        mPathList = DataSupport.findAll(ImagePathBean.class);
        setImage(mPage);
        mSpringView = (SpringView) findViewById(R.id.main_activity_sv);
        mSpringView.setFooter(new DefaultFooter(MainActivity.this));


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


    private void setListener() {
        mSpringView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
            }

            @Override
            public void onLoadmore() {
                setImage(++mPage);
            }
        });

        mImageAdapter.setOnItemClickLitener(new ImageAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(final View view, final int position) {
                Intent intent = new Intent(MainActivity.this, ShowActivity.class);
                mShowList.clear();
                for (int i = 0; i < mImageList.size(); i++) {
                    ShowImageBean showImageBean = new ShowImageBean(mImageList.get(i));
                    mShowList.add(showImageBean);
                }
                int into[] = new int[3];
                computeBoundsBackward(mLayoutManager.findFirstVisibleItemPositions(into));
                intent.putExtra("size", mShowList.size());
                intent.putExtra("position", position);
                intent.putParcelableArrayListExtra("imagePaths", mShowList);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });
        mToolbar.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                mRecyclerView.scrollToPosition(0);
            }
        }));
    }

    /**
     * 使用相机
     */
    private void useCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/beauty/" + System.currentTimeMillis() + ".jpg");
        mPhotoFile.getParentFile().mkdirs();

        Uri uri = FileProvider.getUriForFile(this, "包名.fileprovider", mPhotoFile);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 1);
    }


    public void applyWritePermission() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= 23) {
            int check = ContextCompat.checkSelfPermission(this, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (check == PackageManager.PERMISSION_GRANTED) {
                //调用相机
                useCamera();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            useCamera();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            useCamera();
        } else {
            // 没有获取 到权限，从新请求，或者关闭app
            Toast.makeText(this, getResources().getString(R.string.main_activity_toast_permission), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //在手机相册中显示刚拍摄的图片
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(mPhotoFile);
            String uri = contentUri.toString();
            int index = uri.indexOf("s");
            uri = uri.substring(index, uri.length());
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            Intent intent = new Intent(MainActivity.this, AlbumDetailActivity.class);
            ArrayList<String> photoList = new ArrayList<>();
            photoList.add(uri);
            intent.putStringArrayListExtra("list", photoList);
            intent.putExtra("position", 0);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_toolbar_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.album:
                Intent intent = new Intent(this, SelectActivity.class);
                startActivity(intent);
                break;
            case R.id.photograph:
                applyWritePermission();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setImage(final int page) {
        if ((mPathList.size() != 0) && (mImageList.size() < mPathList.size())) {
            if (mImageList.size() % 15 == 0) {
                System.gc();
            }
            mIndex = mImageList.size() + 15;
            mStartPos = mImageList.size();
            mEndPos = mStartPos;
            for (int i = mImageList.size(); i < mIndex; i++) {
                mImageList.add(mPathList.get(i).getImagePath());
                mImageAdapter.notifyItemInserted(mEndPos);
                mEndPos++;
            }
            if (page != 1) {
                mSpringView.onFinishFreshAndLoad();
            }
        } else {
            mGetData = mRetrofit.create(GetData.class);
            mGetData.getWelfare("15", page)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<WelfareGson>() {
                        private Disposable mDisposable;

                        @Override
                        public void onSubscribe(Disposable d) {
                            mDisposable = d;
                        }

                        @Override
                        public void onNext(WelfareGson value) {
                            if (mImageList.size() % 30 == 0) {
                                System.gc();
                            }
                            mResultList = value.getmResults();
                            mStartPos = mImageList.size();
                            mEndPos = mStartPos;
                            for (int i = 0; i < mResultList.size(); i++) {
                                mImagePath = new ImagePathBean();
                                mImageList.add(mResultList.get(i).getmUrl());
                                mImagePath.setImagePath(mResultList.get(i).getmUrl());
                                mImagePath.save();
                                mImageAdapter.notifyItemInserted(mEndPos);
                                mEndPos++;
                                mImagePath = null;
                            }
                            if (page != 1) {
                                mSpringView.onFinishFreshAndLoad();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            mDisposable.dispose();
                            if (page != 1) {
                                mSpringView.onFinishFreshAndLoad();
                            }
                            Toast.makeText(MainActivity.this, "网络出现问题", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        mImageAdapter.closeDisk();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
//        MyApplication.getRefWatcher(this).watch(this);
    }

    @Override
    protected void onPause() {
        mImageAdapter.fluchCache();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mBackLastPressedTimestamp > 2 * 1000) {
            Toast.makeText(MainActivity.this, R.string.main_activity_press_to_exit, Toast.LENGTH_SHORT).show();
            mBackLastPressedTimestamp = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshData(ShowActivityEvent messageEvent) {
//        recyclerView.smoothScrollToPosition(messageEvent.getPosition());
    }


    /**
     * 计算recyclerview的每个item的位置
     */
    private void computeBoundsBackward(int firstCompletelyVisiblePos[]) {
        for (int i = firstCompletelyVisiblePos[0]; i < mShowList.size(); i++) {
            View itemView = mLayoutManager.findViewByPosition(i);
            Rect bounds = new Rect();
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            if (itemView != null) {
                ImageView imageView = itemView.findViewById(R.id.item_image);
                imageView.getGlobalVisibleRect(bounds);
                bounds.top = bounds.top + rect.top;
            }
            mShowList.get(i).setBounds(bounds);
        }
    }

    /**
     * 移除list中加载错误的图片
     */
    public void removeShowList(int position) {
        mImageList.remove(position);
        mImageAdapter.notifyDataSetChanged();
    }
}
