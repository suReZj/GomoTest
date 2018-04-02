package sure.gomotest.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.xinlan.imageeditlibrary.editimage.EditImageActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import adapter.viewPager_adapter;
import bean.AlbumBean;
import event.saveImageEvent;
import event.showActivityEvent;
import event.updateAlbumEvent;
import sure.gomotest.R;
import util.FileUtils;
import widght.MyViewPager;

import static util.Contants.albumPath;

public class AlbumDetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private String path;
    private MyViewPager viewPager;
    private viewPager_adapter adapter;
    private ArrayList<String> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        EventBus.getDefault().register(this);
        toolbar = (Toolbar) findViewById(R.id.activity_detail_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent=getIntent();
        viewPager=(MyViewPager)findViewById(R.id.activity_detail_viewPager);
        list=intent.getStringArrayListExtra("list");
        adapter=new viewPager_adapter(list);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(intent.getIntExtra("position",0));
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
                EditImageActivity.start(AlbumDetailActivity.this, adapter.getUrl(viewPager.getCurrentItem()), outputFile.getAbsolutePath(), 9);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshData(updateAlbumEvent messageEvent) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        showActivityEvent event=new showActivityEvent(viewPager.getCurrentItem());
        EventBus.getDefault().post(event);
        finish();
    }
}
