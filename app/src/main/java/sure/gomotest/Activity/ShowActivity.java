package sure.gomotest.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.xinlan.imageeditlibrary.editimage.EditImageActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import adapter.ShowFragmentAdapter;
import bean.ShowImageBean;
import event.ShowActivityEvent;
import fragment.ShowFragment;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import utils.FileUtil;
import sure.gomotest.R;
import widght.DepthPageTransformer;
import widght.MyViewPager;
import widght.SmoothImageView;

/**
 * Created by zhangzijian on 2018/03/16.
 * 图片浏览activity
 */

public class ShowActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    public MyViewPager viewPager;
    private final int mDownLoadImage = 0;
    private final int mEditImage = 1;
    private int mImagePosition;
    private FrameLayout mFrameLayout;
    private List<ShowFragment> mFragmentList = new ArrayList<>();
    private ShowFragmentAdapter mFragmentAdapter;
    private boolean mIsTransformOut = false;
    private ArrayList<ShowImageBean> mShowList = new ArrayList<>();

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(ShowActivity.this, getResources().getString(R.string.select_activity_toast_exist), Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(ShowActivity.this, getResources().getString(R.string.select_activity_toast_success), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_activity);
        mToolbar = (Toolbar) findViewById(R.id.show_activity_toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        mFrameLayout = (FrameLayout) findViewById(R.id.show_activity_fl);
        Intent intent = getIntent();
        mImagePosition = intent.getIntExtra("position", 0);

        if (intent.getParcelableArrayListExtra("imagePaths") != null) {
            mShowList = intent.getParcelableArrayListExtra("imagePaths");
            for (int i = 0; i < mShowList.size(); i++) {
                Bundle bundle = new Bundle();
                bundle.putString("path", mShowList.get(i).getPath());
                bundle.putParcelable("imagePaths", mShowList.get(i));
                ShowFragment fragment = ShowFragment.newInstance(bundle);
                fragment.setUserVisibleHint(false);
                mFragmentList.add(fragment);
            }
        }


        viewPager = (MyViewPager) findViewById(R.id.show_activity_viewPager);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
        viewPager.setOffscreenPageLimit(0);
        mFragmentAdapter = new ShowFragmentAdapter(getSupportFragmentManager(), mFragmentList, mShowList, getApplicationContext());
        viewPager.setAdapter(mFragmentAdapter);
        viewPager.setCurrentItem(mImagePosition);
        viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                ShowFragment fragment = mFragmentList.get(mImagePosition);
                fragment.transformIn();
            }
        });


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

    public void getImageBitmap(final String url, final Context context, final int type) {
        int index = url.lastIndexOf("/");
        String fileName = url;
        fileName = fileName.substring(index + 1, url.length());
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/beauty/" + fileName + ".jpg";

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/beauty/" + fileName + ".jpg");
        File outputFile = FileUtil.genEditFile();
        if (file.exists()) {
            if (type == 0) {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                mHandler.sendMessage(msg);
                return;
            } else {
                EditImageActivity.start(ShowActivity.this, url, outputFile.getAbsolutePath(), 0);
            }
        } else {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {

                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    try {
                        catchStreamToFile(response.body().byteStream(), url, context, type);
                        call.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    //将图片的InputStream转化为本地图片
    public void catchStreamToFile(InputStream inStream, String url, Context context, final int type) throws IOException {

        Bitmap bitmap = BitmapFactory.decodeStream(inStream);


        int index = url.lastIndexOf("/");
        url = url.substring(index + 1, url.length());
        String fileName = url;
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/beauty/" + url + ".jpg";

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/beauty/" + url + ".jpg");

        file.getParentFile().mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + savePath)));
        if (type == 0) {
            Message msg = mHandler.obtainMessage();
            msg.what = 2;
            mHandler.sendMessage(msg);
        } else {
            File outputFile = FileUtil.genEditFile();
            EditImageActivity.start(ShowActivity.this, savePath, outputFile.getAbsolutePath(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.download:
                getImageBitmap(mFragmentAdapter.getUrl(viewPager.getCurrentItem()), ShowActivity.this, mDownLoadImage);
                break;
            case R.id.edit:
                getImageBitmap(mFragmentAdapter.getUrl(viewPager.getCurrentItem()), ShowActivity.this, mEditImage);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ShowActivityEvent event = new ShowActivityEvent(viewPager.getCurrentItem());
        EventBus.getDefault().post(event);
        transformOut();
    }


    public void getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        mFrameLayout.setBackgroundColor(a + rgb);
        mToolbar.setAlpha(alpha * 510f / 255f);
    }

    public void transformOut() {
        if (mIsTransformOut) {
            return;
        }
        mIsTransformOut = true;
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < mShowList.size()) {
            ShowFragment fragment = mFragmentList.get(currentItem);
            fragment.changeBg(Color.TRANSPARENT);
            changeBg(Color.TRANSPARENT);
            fragment.transformOut(new SmoothImageView.OnTransformListener() {
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
        mFrameLayout.setBackgroundColor(color);
    }
}


