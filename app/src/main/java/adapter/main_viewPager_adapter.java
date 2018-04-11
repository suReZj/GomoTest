package adapter;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.List;

import bean.ImagePath;
import sure.gomotest.Activity.ShowActivity;
import uk.co.senab.photoview.PhotoView;
import widght.SmoothImageView;

/**
 * Created by zhangzijian on 2018/03/27.
 */

public class main_viewPager_adapter extends PagerAdapter {
    private List<ImagePath> list;
//    SmoothImageView photoView;
    PhotoView photoView;

    public main_viewPager_adapter(List<ImagePath> list) {
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
    public View instantiateItem(final ViewGroup container, int position) {
//        photoView = new SmoothImageView(container.getContext());
        photoView = new PhotoView(container.getContext());
        Glide.with(container.getContext()).load(list.get(position).getPath()).into(photoView);

        // Now just add PhotoView to ViewPager and return it

        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        photoView.setAlphaChangeListener(new SmoothImageView.OnAlphaChangeListener() {
//            @Override
//            public void onAlphaChange(int alpha) {
////                Log.d("onAlphaChange", "alpha:" + alpha);
////                ((ShowActivity)(container.getContext())).setBackgroundColor(getColorWithAlpha(alpha / 255f, Color.BLACK));
//                ((ShowActivity)(container.getContext())).getColorWithAlpha(alpha / 255f, Color.BLACK);
//            }
//        });
//        photoView.setTransformOutListener(new SmoothImageView.OnTransformOutListener() {
//            @Override
//            public void onTransformOut() {
//                if (photoView.checkMinScale()) {
//                    ((ShowActivity)(container.getContext())).finish();
//                }
//            }
//        });
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

//    public int getColorWithAlpha(float alpha, int baseColor) {
//        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
//        int rgb = 0x00ffffff & baseColor;
//        return a + rgb;
//    }
}
