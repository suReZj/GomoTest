package adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import bean.AlbumBean;
import fragment.ShowFragment;

/**
 * Created by zhangzijian on 2018/03/12.
 * 用于相册页照片预览的viewpager的fragmentAdapter
 */

public class AlbumFragmentAdapter extends FragmentPagerAdapter {
    private FragmentManager mFragmentManager;
    private List<ShowFragment> mFragmentList;
    private List<AlbumBean> mAlbumList;
    private Context mContext;

    public AlbumFragmentAdapter(FragmentManager fm, List<ShowFragment> list, List<AlbumBean> pathList, Context context) {
        super(fm);
        this.mFragmentManager = fm;
        this.mFragmentList = list;
        this.mAlbumList = pathList;
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        ShowFragment showFragment = mFragmentList.get(position);
        return showFragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public String getUrl(int position) {
        return mAlbumList.get(position).getPhotoPath();
    }
}
