package sure.gomotest.Activity;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapter.select_recycle_adapter;
import bean.MediaBean;
import sure.gomotest.R;
import util.MyDecoration;

public class SelectActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView textView;
    private RecyclerView recyclerView;
    private select_recycle_adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        initView();
        setListener();
        getData();
    }

    public void initView(){
        toolbar = (Toolbar) findViewById(R.id.activity_select_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView=(RecyclerView) findViewById(R.id.activity_select_recyclerView);
        textView=(TextView) findViewById(R.id.activity_select_textView);
        recyclerView.addItemDecoration(new MyDecoration(SelectActivity.this, MyDecoration.VERTICAL_LIST, R.drawable.recyclerview_divider));
    }

    public void setListener(){

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

    public void getData(){
        new Thread(new Runnable() {
            List<MediaBean> mediaBeen = new ArrayList<>();
            HashMap<String,List<MediaBean>> allPhotosTemp = new HashMap<>();//所有照片
            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            List<String> albumName=new ArrayList<>();
            @Override
            public void run() {
                String[] projImage = { MediaStore.Images.Media._ID
                        , MediaStore.Images.Media.DATA
                        ,MediaStore.Images.Media.SIZE
                        ,MediaStore.Images.Media.DISPLAY_NAME};
                Cursor mCursor = getContentResolver().query(mImageUri,
                        projImage,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or "+
                                MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png","image/jpg","image/bmp"},
                        MediaStore.Images.Media.DATE_MODIFIED+" desc");

                if(mCursor!=null){
                    while (mCursor.moveToNext()) {
                        // 获取图片的路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        int size = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media.SIZE))/1024;
                        String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                        //用于展示相册初始化界面
                        mediaBeen.add(new MediaBean(path,size,displayName));
                        // 获取该图片的父路径名
                        String dirPath = new File(path).getParentFile().getAbsolutePath();
                        //存储对应关系
                        if (allPhotosTemp.containsKey(dirPath)) {
                            List<MediaBean> data = allPhotosTemp.get(dirPath);
                            data.add(new MediaBean(path,size,displayName));
                            continue;
                        } else {
                            albumName.add(dirPath);
                            List<MediaBean> data = new ArrayList<>();
                            data.add(new MediaBean(path,size,displayName));
                            allPhotosTemp.put(dirPath,data);
                        }
                    }
                    mCursor.close();
                }
                //更新界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("1111","1111111");
                        adapter=new select_recycle_adapter(allPhotosTemp,albumName);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(SelectActivity.this));
                    }
                });
            }
        }).start();
    }

}
