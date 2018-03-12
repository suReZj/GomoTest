package sure.gomotest.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.widget.SpringView;
import com.previewlibrary.GPreviewBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import adapter.main_recycle_adapter;
import bean.pathInfo;
import gson.gson_result;
import gson.gson_welfare;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit.getData;
import retrofit2.Retrofit;
import sure.gomotest.R;
import util.RetrofitUtil;

import static util.Contants.url;
import static util.Contants.width;
import static util.ShowDialog.closeDisk;
import static util.ShowDialog.fluchCache;
import static util.ShowDialog.showImageDialog;


public class MainActivity extends AppCompatActivity {
    private File file;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private main_recycle_adapter adapter;
    private int page = 1;
    private List<String> list = new ArrayList<>();
    private List <pathInfo> pathInfolist=new ArrayList<>();
    private SpringView springView;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerView);
        layoutManager=new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new main_recycle_adapter(list,MainActivity.this);
        recyclerView.setAdapter(adapter);
        setImage(page);
        springView=(SpringView)findViewById(R.id.activity_main_frame);
        springView.setFooter(new DefaultFooter(MainActivity.this));
//        list.add("http://7xi8d6.com1.z0.glb.clouddn.com/2017-01-20-030332.jpg");
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
            public void onItemClick(View view, int position) {
//                GPreviewBuilder.from(MainActivity.this)//activity实例必须
//                        .setData(pathInfolist)//集合
//                        .setCurrentIndex(position)
//                        .setSingleFling(false)//是否在黑屏区域点击返回
//                        .setDrag(false)//是否禁用图片拖拽返回
//                        .start();//启动

                showImageDialog(MainActivity.this,list.get(position));
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });
    }

    /**
     * 使用相机
     */
    private void useCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/test/" + System.currentTimeMillis() + ".jpg");
        file.getParentFile().mkdirs();

        //改变Uri  com.xykj.customview.fileprovider注意和xml中的一致
        Uri uri = FileProvider.getUriForFile(this, "包名.fileprovider", file);
        //添加权限
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
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
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
//                Intent intent=new Intent(this,SelectActivity.class);
//                startActivity(intent);
                break;
            case R.id.photograph:
//                applyWritePermission();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setImage(final int page) {
        Retrofit retrofit = RetrofitUtil.getRetrofit(url+width);
        getData getData = retrofit.create(getData.class);
        getData.getWelfare("12", page)
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
                        List<gson_result> results = value.getResults();
                        int start = list.size();
                        int end = start;
                        for (int i = 0; i < results.size(); i++) {
                            list.add(results.get(i).getUrl());
                            adapter.notifyItemInserted(end);
                            pathInfo pathInfo=new pathInfo(results.get(i).getUrl());
                            pathInfolist.add(pathInfo);
                            end++;
                        }
//                        adapter.notifyItemRangeChanged(start,end);
                        if(page!=1){
                            springView.onFinishFreshAndLoad();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        disposable.dispose();
                        Log.e("error", e.toString());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        adapter.closeDisk();
        closeDisk();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        adapter.fluchCache();
        fluchCache();
        super.onPause();
    }
}
