package adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import bean.imagePathBean;
import bean.showImageBean;
import fragment.ShowFragment;

public class ShowFragmentAdapter extends FragmentPagerAdapter {
    private FragmentManager mfragmentManager;
    private List<ShowFragment> mlist;
    private List<showImageBean> pathList;
    private Context context;

    public ShowFragmentAdapter(FragmentManager fm, List<ShowFragment> list, List<showImageBean> pathList, Context context) {
        super(fm);
        this.mfragmentManager=fm;
        this.mlist=list;
        this.pathList=pathList;
        this.context=context;
    }

    @Override
    public ShowFragment getItem(int position) {
        ShowFragment showFragment=mlist.get(position);
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
