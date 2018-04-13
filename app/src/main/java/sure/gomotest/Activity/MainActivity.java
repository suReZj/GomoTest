package sure.gomotest.Activity;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.widget.SpringView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import adapter.main_recycle_adapter;
import bean.ImagePath;
import event.showActivityEvent;
import fragment.showView;
import gson.gson_result;
import gson.gson_welfare;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit.getData;
import retrofit2.Retrofit;
import sure.gomotest.R;
import util.OnDoubleClickListener;
import util.RetrofitUtil;

import static util.Contants.url;
import static util.Contants.imageUrl;
import static util.ShowDialog.closeDisk;
import static util.ShowDialog.fluchCache;


public class MainActivity extends AppCompatActivity {
    private File file;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private main_recycle_adapter adapter;
    private int page = 1;
    private ArrayList<String> list = new ArrayList<>();
    private SpringView springView;
    private RecyclerView.LayoutManager layoutManager;
    private boolean flag = false;
    private String error = "https://img.gank.io/anri.kumaki_23_10_2017_12_27_30_151.jpg";
    private long backLastPressedTimestamp = 0;
    private List<gson_result> results;
    private int start;
    private int end;
    private Retrofit retrofit = RetrofitUtil.getRetrofit(url);
    private getData getData = retrofit.create(getData.class);
    private int index = 0;
    private List<ImagePath> pathList = new ArrayList<>();
    private ImagePath imagePath;
    private int getNum=18;
    private FrameLayout frameLayout;
    private FragmentManager fm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);
        fm=this.getSupportFragmentManager();
        initView();
        setListener();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerView);
        layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new main_recycle_adapter(list, MainActivity.this);
        recyclerView.setAdapter(adapter);
        pathList = DataSupport.findAll(ImagePath.class);
        setImage(page);
        springView = (SpringView) findViewById(R.id.activity_main_frame);
        springView.setFooter(new DefaultFooter(MainActivity.this));
//        list.add("http://7xi8d6.com1.z0.glb.clouddn.com/2017-01-20-030332.jpg");
//        list.add("http://7xi8d6.com1.z0.glb.clouddn.com/2017-02-27-tumblr_om1aowIoKa1qbw5qso1_540.jpg");

//        frameLayout=(FrameLayout) findViewById(R.id.activity_main_frameLayout);
//        frameLayout.setVisibility(View.GONE);

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
        springView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
            }

            @Override
            public void onLoadmore() {
                setImage(++page);
            }
        });

        adapter.setOnItemClickLitener(new main_recycle_adapter.OnItemClickLitener() {
            @Override
            public void onItemClick(final View view, final int position) {
                imageUrl = list.get(position);
                Log.e("url", list.get(position));
                Intent intent = new Intent(MainActivity.this, ShowActivity.class);
                intent.putExtra("size", list.size());
                intent.putExtra("position", position);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, view, "shareNames").toBundle());
                } else {
                    startActivity(intent);
                }
//                Log.e("url", list.get(position));
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.replace(R.id.activity_main_frameLayout, new showView());
//                ft.commit();
//                frameLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });
        toolbar.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                recyclerView.scrollToPosition(0);
            }
        }));
    }

    /**
     * 使用相机
     */
    private void useCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/beauty/" + System.currentTimeMillis() + ".jpg");
        file.getParentFile().mkdirs();

        Uri uri = FileProvider.getUriForFile(this, "包名.fileprovider", file);
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
            Toast.makeText(this, "需要存储权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //在手机相册中显示刚拍摄的图片
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
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
        getMenuInflater().inflate(R.menu.activity_main_toolbar_item, menu);
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
        if ((pathList.size() != 0) && (list.size() < pathList.size())) {
            if (list.size() % 15 == 0) {
                System.gc();
            }
            if(page==1){
                index=list.size()+15;
            }else {
                index = list.size() + 15;
            }
            start = list.size();
            end = start;
            for (int i = list.size(); i < index; i++) {
                list.add(pathList.get(i).getPath());
                adapter.notifyItemInserted(end);
                end++;
            }
            if (page != 1) {
                springView.onFinishFreshAndLoad();
            }
        } else {
            getData = retrofit.create(getData.class);
//            int requestSum;
//            if(page==1){
//                requestSum=18;
//            }else {
//                requestSum=9;
//            }
//            int requestPage=page;
//            if(page==2){
//                requestPage++;
//            }
            getData.getWelfare("15", page)
//            getData.getWelfare(String.valueOf(requestSum), requestPage)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<gson_welfare>() {
                        private Disposable disposable;

                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        public void onNext(gson_welfare value) {
                            if (list.size() % 18 == 0) {
                                System.gc();
                            }
                            results = value.getResults();
                            start = list.size();
                            end = start;
                            for (int i = 0; i < results.size(); i++) {
                                imagePath = new ImagePath();
                                if (error.equals(results.get(i).getUrl())) {
                                    list.add("http://img.gank.io/anri.kumaki_23_10_2017_12_27_30_151.jpg");
                                    imagePath.setPath("http://img.gank.io/anri.kumaki_23_10_2017_12_27_30_151.jpg");
                                    imagePath.save();
                                } else if (results.get(i).getUrl().equals("https://ws1.sinaimg.cn/large/610dc034ly1fhfmsbxvllj20u00u0q80.jpg")) {
                                    list.add("http://ww2.sinaimg.cn/large/7a8aed7bgw1esbmanpn0tj20hr0qo0w8.jpg");
                                    imagePath.setPath("http://ww2.sinaimg.cn/large/7a8aed7bgw1esbmanpn0tj20hr0qo0w8.jpg");
                                    imagePath.save();
                                } else {
                                    list.add(results.get(i).getUrl());
                                    imagePath.setPath(results.get(i).getUrl());
                                    imagePath.save();
                                }
                                adapter.notifyItemInserted(end);
                                end++;
                                imagePath = null;
                            }
                            if (page != 1) {
                                springView.onFinishFreshAndLoad();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            disposable.dispose();
                            if (page != 1) {
                                springView.onFinishFreshAndLoad();
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
        adapter.closeDisk();
        if (flag) {
            closeDisk();
        }
        super.onDestroy();
        EventBus.getDefault().unregister(this);
//        MyApplication.getRefWatcher(this).watch(this);
    }

    @Override
    protected void onPause() {
        adapter.fluchCache();
        if (flag) {
            fluchCache();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - backLastPressedTimestamp > 2 * 1000) {
            Toast.makeText(MainActivity.this, R.string.press_back_again_to_exit, Toast.LENGTH_SHORT).show();
            backLastPressedTimestamp = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshData(showActivityEvent messageEvent) {
        recyclerView.smoothScrollToPosition(messageEvent.getPosition());
    }


    public int[] getXY(int position){
        int xy[]=new int[2];
        View view=recyclerView.getChildAt(position);
        xy[0]=(int)view.getX();
        xy[1]=(int)view.getY();
        return xy;
    }
}
