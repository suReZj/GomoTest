package adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.List;

import bean.AlbumBean;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by dell88 on 2018/3/6 0006.
 * 用于拍照返回的图片viewpager的adapter
 */

public class PhotographAdapter extends PagerAdapter {
    private List<AlbumBean> mData;

    public PhotographAdapter(List<AlbumBean> list) {
        this.mData = list;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        Glide.with(container.getContext()).load(mData.get(position).getPhotoPath()).into(photoView);

        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public String getUrl(int position) {
        return mData.get(position).getPhotoPath();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
