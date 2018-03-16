package sure.gomotest.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.previewlibrary.GPreviewActivity;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;

import java.io.File;

import sure.gomotest.R;
import util.FileUtils;

import static util.Contants.albumPath;

public class AlbumDetailActivity extends GPreviewActivity {
    private Toolbar toolbar;
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
//                    case R.id.detail_edit:
//                        File outputFile = FileUtils.genEditFile();
//                        EditImageActivity.start(AlbumDetailActivity.this,albumPath,outputFile.getAbsolutePath(),9);
                }
                return false;
            }
        });
    }
}
