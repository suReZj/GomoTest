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
import bean.AlbumBean;
import bean.ShowImageBean;
import event.ShowActivityEvent;
import event.UpdateAlbumEvent;
import sure.gomotest.R;

/**
 * Created by dell88 on 2018/3/7 0007.
 * 展示相册里的照片activity
 */

public class AlbumActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private TextView mTextView;
    private AlbumAdapter albumAdapter;
    private String albumName;
    private Intent mIntent;
    private List<AlbumBean> albumList;
    private String intentAlbumName;
    private List<ShowImageBean> showList = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    private ShowImageBean showImageBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.album_activity);
        initView();
        setListener();
    }

    public void initView() {
        mIntent = getIntent();
        albumName = mIntent.getStringExtra("name");
        intentAlbumName = albumName;

        mToolbar = (Toolbar) findViewById(R.id.album_activity_toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.album_activity_rv);
        mTextView = (TextView) findViewById(R.id.album_activity_tv);

        albumList = DataSupport.where("albumName=?", albumName).find(AlbumBean.class);

        for (int i = 0; i < albumList.size(); i++) {
            showImageBean = new ShowImageBean(albumList.get(i).getPhotoPath());
            showList.add(showImageBean);
        }

        albumAdapter = new AlbumAdapter(albumList);
        layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(albumAdapter);

        int position = albumName.lastIndexOf("/");
        albumName = albumName.substring(position + 1, albumName.length());
        mTextView.setText(albumName);


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
        albumAdapter.setOnItemClickLitener(new AlbumAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(final View view, final int position) {
                Intent showItent = new Intent(AlbumActivity.this, AlbumDetailActivity.class);
                showItent.putExtra("albumname", intentAlbumName);
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
        if (String.valueOf(mTextView.getText()).equals(dirPath)) {
            albumList = DataSupport.where("albumName=?", albumName).find(AlbumBean.class);
            albumAdapter = new AlbumAdapter(albumList);
            layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(albumAdapter);
            albumAdapter.notifyDataSetChanged();
            showList.clear();
            for (int i = 0; i < albumList.size(); i++) {
                showImageBean = new ShowImageBean(albumList.get(i).getPhotoPath());
                showList.add(showImageBean);
            }
            setListener();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshData(UpdateAlbumEvent messageEvent) {
        albumName = messageEvent.getAlbumName();
        getData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changePosition(ShowActivityEvent messageEvent) {
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
