package sure.gomotest.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;


import java.util.ArrayList;
import java.util.List;

import adapter.AlbumAdapter;
import bean.albumBean;
import bean.showImageBean;
import event.showActivityEvent;
import event.updateAlbumEvent;
import sure.gomotest.R;


public class AlbumActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TextView textView;//用于展示相册名
    private AlbumAdapter adapter;
    private String albumName;//相册名
    private Intent intent;
    private List<albumBean> list;//根据相册名从数据库中获取照片实例的list
    private String album;//用于存放相册名
    private List<showImageBean> showList = new ArrayList<>();//用于展示的图片list
    private RecyclerView.LayoutManager layoutManager;
    private showImageBean showImageBean;//展示的图片实例

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.album_activity);
        initView();
        setListener();
    }

    public void initView() {
        intent = getIntent();
        albumName = intent.getStringExtra("name");
        album = albumName;

        toolbar = (Toolbar) findViewById(R.id.album_activity_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.album_activity_rv);
        textView = (TextView) findViewById(R.id.album_activity_tv);

        list = DataSupport.where("albumName=?", albumName).find(albumBean.class);

        for (int i = 0; i < list.size(); i++) {
            showImageBean = new showImageBean(list.get(i).getPath());
            showList.add(showImageBean);
        }

        adapter = new AlbumAdapter(list);
        layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        int position = albumName.lastIndexOf("/");
        albumName = albumName.substring(position + 1, albumName.length());
        textView.setText(albumName);


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

    public void setListener() {
        adapter.setOnItemClickLitener(new AlbumAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(final View view, final int position) {
                Intent showItent = new Intent(AlbumActivity.this, AlbumDetailActivity.class);
                showItent.putExtra("albumname", album);
                showItent.putExtra("position", position);
                int into[] = new int[3];
                computeBoundsBackward(((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(into));
                showItent.putParcelableArrayListExtra("imagePaths", (ArrayList<? extends Parcelable>) showList);
                startActivity(showItent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
//        MyApplication.getRefWatcher(this).watch(this);
        System.gc();
    }

    public void getData() {
        int index = albumName.lastIndexOf("/");
        String dirPath = albumName.substring(index + 1, albumName.length());
        if (String.valueOf(textView.getText()).equals(dirPath)) {
            list = DataSupport.where("albumName=?", albumName).find(albumBean.class);
            adapter = new AlbumAdapter(list);
            layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            showList.clear();
            for (int i = 0; i < list.size(); i++) {
                showImageBean = new showImageBean(list.get(i).getPath());
                showList.add(showImageBean);
            }
            setListener();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshData(updateAlbumEvent messageEvent) {
        albumName = messageEvent.getAlbumName();
        getData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changePosition(showActivityEvent messageEvent) {
//        recyclerView.smoothScrollToPosition(messageEvent.getPosition());
    }

    private void computeBoundsBackward(int firstCompletelyVisiblePos[]) {
        for (int i = firstCompletelyVisiblePos[0]; i < showList.size(); i++) {
            View itemView = layoutManager.findViewByPosition(i);
            Rect bounds = new Rect();
            Rect rect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            if (itemView != null) {
                ImageView imageView = itemView.findViewById(R.id.item_album_image);
                imageView.getGlobalVisibleRect(bounds);
                bounds.top = bounds.top + rect.top;
            }
            showList.get(i).setBounds(bounds);
        }
    }

}
