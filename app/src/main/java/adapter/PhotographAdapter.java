package adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.List;

import bean.albumBean;
import uk.co.senab.photoview.PhotoView;

public class PhotographAdapter extends PagerAdapter {
    private List<albumBean> list;

    public PhotographAdapter(List<albumBean> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        Glide.with(container.getContext()).load(list.get(position).getPath()).into(photoView);

        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public String getUrl(int position){
        return list.get(position).getPath();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
