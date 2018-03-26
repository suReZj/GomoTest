package sure.gomotest.Activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.previewlibrary.GPreviewActivity;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import bean.AlbumBean;
import event.saveImageEvent;
import sure.gomotest.R;
import util.FileUtils;

import static util.Contants.albumPath;

public class AlbumDetailActivity extends GPreviewActivity {
    private Toolbar toolbar;
    private String path;
    @Override
    public int setContentLayout() {
        return R.layout.activity_album_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar=findViewById(R.id.activity_detail_toolbar);
        toolbar.inflateMenu(R.menu.activity_detail_toolbar_item);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transformOut();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.detail_edit:
                        File outputFile = FileUtils.genEditFile();
                        path=outputFile.getAbsolutePath();
                        EditImageActivity.start(AlbumDetailActivity.this, albumPath, outputFile.getAbsolutePath(), 9);
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        albumPath="";
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(path!=null){
            File file=new File(path);
            if(file.exists()){
                // 获取该图片的父路径名
                String dirPath = new File(path).getParentFile().getAbsolutePath();
                AlbumBean bean=new AlbumBean();
                bean.setAlbumName(dirPath);
                bean.setPath(path);
                saveImageEvent event=new saveImageEvent(dirPath,path);
                EventBus.getDefault().post(event);
            }
        }
    }
}
