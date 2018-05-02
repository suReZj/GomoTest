package adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import bean.ShowImageBean;
import fragment.ShowFragment;

/**
 * Created by dell88 on 2018/3/11 0011.
 * 用于首页预览图片的viewpager的fragmentAdapter
 */

public class ShowFragmentAdapter extends FragmentPagerAdapter {
    private FragmentManager mFragmentManager;
    private List<ShowFragment> mFragmentList;
    private List<ShowImageBean> mPathList;
    private Context mContext;

    public ShowFragmentAdapter(FragmentManager fm, List<ShowFragment> list, List<ShowImageBean> pathList, Context context) {
        super(fm);
        this.mFragmentManager = fm;
        this.mFragmentList = list;
        this.mPathList = pathList;
        this.mContext = context;
    }

    @Override
    public ShowFragment getItem(int position) {
        ShowFragment showFragment = mFragmentList.get(position);
        return showFragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public String getUrl(int position) {
        return mPathList.get(position).getPath();
    }
}
