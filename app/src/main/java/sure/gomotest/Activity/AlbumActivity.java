package sure.gomotest.Activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.previewlibrary.GPreviewBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;


import java.util.ArrayList;
import java.util.List;

import adapter.album_recycle_adapter;
import bean.AlbumBean;
import bean.UserViewInfo;
import event.updateAlbumEvent;
import sure.gomotest.R;

import static util.Contants.albumPath;

public class AlbumActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TextView textView;
    private album_recycle_adapter adapter;
    private String albumName;
    private Intent intent;
    private List<AlbumBean> list;
    private ArrayList<String> urlList = new ArrayList<>();
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_album);
        initView();
        setListener();
    }

    public void initView() {
        intent = getIntent();
        albumName = intent.getStringExtra("name");

        toolbar = (Toolbar) findViewById(R.id.activity_album_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.activity_album_recyclerView);
        textView = (TextView) findViewById(R.id.activity_album_textView);

        list = DataSupport.where("albumName=?", albumName).find(AlbumBean.class);
        for(int i=0;i<list.size();i++){
            urlList.add(list.get(i).getPath());
        }

        adapter = new album_recycle_adapter(list);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        int position = albumName.lastIndexOf("/");
        albumName = albumName.substring(position + 1, albumName.length());
        textView.setText(albumName);


//        getData();
    }

    public void setListener() {
        adapter.setOnItemClickLitener(new album_recycle_adapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                albumPath = list.get(position).getPath();
                Intent showItent=new Intent(AlbumActivity.this,AlbumDetailActivity.class);
                showItent.putStringArrayListExtra("list",urlList);
                showItent.putExtra("position",position);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(showItent, ActivityOptions.makeSceneTransitionAnimation(AlbumActivity.this, view, "shareNames").toBundle());
                }else {
                    startActivity(showItent);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_album_toolbar_item, menu);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        System.gc();
    }

    public void getData() {
        int index = albumName.lastIndexOf("/");
        String dirPath = albumName.substring(index + 1, albumName.length());
        if (String.valueOf(textView.getText()).equals(dirPath)) {
            list = DataSupport.where("albumName=?", albumName).find(AlbumBean.class);
            for(int i=0;i<list.size();i++){
                urlList.add(list.get(i).getPath());
            }
            adapter = new album_recycle_adapter(list);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(adapter);
            setListener();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshData(updateAlbumEvent messageEvent) {
        albumName = messageEvent.getAlbumName();
        getData();
    }

}
