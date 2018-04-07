package sure.gomotest.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.xinlan.imageeditlibrary.editimage.EditImageActivity;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import adapter.main_viewPager_adapter;
import bean.ImagePath;
import event.showActivityEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import sure.gomotest.R;
import util.FileUtils;
import widght.MyViewPager;


public class ShowActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private Random mRandom = new Random();
    final int downLoadImage=0;
    final int editImage=1;
    private List<ImagePath> list;
    private int position;
    private main_viewPager_adapter adapter;


    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Toast.makeText(ShowActivity.this, "该图片已经存在", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(ShowActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        toolbar = (Toolbar) findViewById(R.id.activity_show_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent=getIntent();
        list=DataSupport.limit(intent.getIntExtra("size",0)).find(ImagePath.class);
        position=intent.getIntExtra("position",0);

        viewPager=(MyViewPager)findViewById(R.id.activity_show_viewPager);
        adapter=new main_viewPager_adapter(list);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
    }

    public  void getImageBitmap(final String url, final Context context, final int type) {
        int index = url.lastIndexOf("/");
        String fileName = url;
        fileName = fileName.substring(index + 1, url.length());
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/beauty/" + fileName + ".jpg";

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/beauty/" + fileName + ".jpg");

        if(file.exists()){
            if(type==0){
                Message msg=handler.obtainMessage();
                msg.what=1;
                handler.sendMessage(msg);
                return;
            }else {
                File outputFile = FileUtils.genEditFile();
                EditImageActivity.start(ShowActivity.this,savePath,outputFile.getAbsolutePath(),9);
            }
        }else {
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
                        catchStreamToFile(response.body().byteStream(), url, context,type);
                        call.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    //将图片的InputStream转化为本地图片
    public void catchStreamToFile(InputStream inStream, String url, Context context,final int type) throws IOException {

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

            // 其次把文件插入到系统图库
//            try {
//                MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                        file.getAbsolutePath(), fileName, null);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
            // 最后通知图库更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + savePath)));
            if(type==0){
                Message msg=handler.obtainMessage();
                msg.what=2;
                handler.sendMessage(msg);
            }else {
                File outputFile = FileUtils.genEditFile();
                EditImageActivity.start(ShowActivity.this,savePath,outputFile.getAbsolutePath(),9);
            }
        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
//        DataSupport.deleteAllAsync(showPath.class).listen(new UpdateOrDeleteCallback() {
//            @Override
//            public void onFinish(int rowsAffected) {
//                Log.e("delete","delete");
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_show_toolbar_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.download:
                getImageBitmap(adapter.getUrl(viewPager.getCurrentItem()), ShowActivity.this,downLoadImage);
                break;
            case R.id.edit:
                getImageBitmap(adapter.getUrl(viewPager.getCurrentItem()), ShowActivity.this, editImage);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showActivityEvent event=new showActivityEvent(viewPager.getCurrentItem());
        EventBus.getDefault().post(event);
        finish();
    }


}


