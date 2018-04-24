package adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import bean.albumBean;
import fragment.ShowFragment;

public class AlbumFragmentAdapter extends FragmentPagerAdapter {
    private FragmentManager mfragmentManager;
    private List<ShowFragment> mlist;
    private List<albumBean> albumList;
    private Context context;

    public AlbumFragmentAdapter(FragmentManager fm, List<ShowFragment> list, List<albumBean> pathList, Context context) {
        super(fm);
        this.mfragmentManager=fm;
        this.mlist=list;
        this.albumList=pathList;
        this.context=context;
    }

    @Override
    public Fragment getItem(int position) {
        ShowFragment showFragment=mlist.get(position);
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
