package adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import bean.AlbumBean;
import fragment.showFragment;

public class album_fragment_adapter extends FragmentPagerAdapter {
    private FragmentManager mfragmentManager;
    private List<showFragment> mlist;
    private List<AlbumBean> albumList;
    private Context context;

    public album_fragment_adapter(FragmentManager fm, List<showFragment> list, List<AlbumBean> pathList, Context context) {
        super(fm);
        this.mfragmentManager=fm;
        this.mlist=list;
        this.albumList=pathList;
        this.context=context;
    }

    @Override
    public Fragment getItem(int position) {
        showFragment showFragment=mlist.get(position);
        return showFragment;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    public String getUrl(int position){
        return albumList.get(position).getPath();
    }
}
