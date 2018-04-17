package adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import bean.ImagePath;
import fragment.showFragment;

public class main_fragment_adapter extends FragmentPagerAdapter {
    private FragmentManager mfragmentManager;
    private List<showFragment> mlist;
    private List<ImagePath> pathList;
    private Context context;

    public main_fragment_adapter(FragmentManager fm, List<showFragment> list, List<ImagePath> pathList, Context context) {
        super(fm);
        this.mfragmentManager=fm;
        this.mlist=list;
        this.pathList=pathList;
        this.context=context;
    }

    @Override
    public showFragment getItem(int position) {
        showFragment showFragment=mlist.get(position);
        return showFragment;
    }

    @Override
    public int getCount() {
        return mlist.size();
    }

    public String getUrl(int position){
        return pathList.get(position).getPath();
    }
}
