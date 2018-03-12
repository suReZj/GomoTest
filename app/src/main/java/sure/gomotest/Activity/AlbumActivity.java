package sure.gomotest.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.liaoinstan.springview.container.DefaultFooter;
import com.liaoinstan.springview.widget.SpringView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapter.album_recycle_adapter;
import bean.MediaBean;
import sure.gomotest.R;

public class AlbumActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TextView textView;
    private album_recycle_adapter adapter;
    private List<MediaBean> data;
    private String albumName;
    private Intent intent;
    private SpringView springView;
    private List<MediaBean> pageData=new ArrayList<>();
    private int position=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        initView();
        setListener();
    }

    public void initView() {
        intent=getIntent();
        data=(ArrayList<MediaBean>)intent.getSerializableExtra("data");
        albumName=intent.getStringExtra("name");

        toolbar = (Toolbar) findViewById(R.id.activity_album_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView=(RecyclerView) findViewById(R.id.activity_album_recyclerView);
        textView=(TextView) findViewById(R.id.activity_album_textView);

        int position=albumName.lastIndexOf("/");
        albumName=albumName.substring(position+1,albumName.length());
        textView.setText(albumName);

        if(data.size()>=15){
            int start=position;
            int end=position+15;
            for(int i=start;i<end&&i<data.size();i++){
                pageData.add(data.get(i));
                position++;
            }
            adapter=new album_recycle_adapter(pageData);
        }else {
            adapter=new album_recycle_adapter(data);
        }
//        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
//        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        springView=(SpringView) findViewById(R.id.activity_album_frame);
        springView.setFooter(new DefaultFooter(AlbumActivity.this));
    }

    public void setListener() {
        adapter.setOnItemClickLitener(new album_recycle_adapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        springView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadmore() {
                int start=position;
                int end=position+15;
                for(int i=start;i<end&&i<data.size();i++){
                    pageData.add(data.get(i));
                    adapter.notifyItemInserted(i);
                    position++;
                }
                springView.onFinishFreshAndLoad();
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



}
