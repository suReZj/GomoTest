package fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import bean.ShowImageBean;
import sure.gomotest.Activity.AlbumDetailActivity;
import sure.gomotest.Activity.ShowActivity;
import sure.gomotest.R;
import uk.co.senab.photoview.PhotoViewAttacher;
import widght.SmoothImageView;

/**
 * Created by zhangzijian on 2018/03/20.
 * 用于viewpager的fragment
 */

public class ShowFragment extends Fragment {
    private SmoothImageView mImageView;
    private String imagePath;
    private Bundle mBundle;
    private RelativeLayout rootView;
    private String showActivity = "Activity.ShowActivity";
    private String albumDetailActivity = "Activity.AlbumDetailActivity";
    private ShowImageBean showBean;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_fragment, null);
        mImageView = view.findViewById(R.id.show_fragment_iv);
        rootView = view.findViewById(R.id.show_fragment_rl);
        imagePath = mBundle.getString("path");
        showBean=mBundle.getParcelable("imagePaths");
        if(showBean!=null){
            mImageView.setThumbRect(showBean.getBounds());
        }

        Glide.with(container.getContext()).load(imagePath).into(mImageView);

        mImageView.setAlphaChangeListener(new SmoothImageView.OnAlphaChangeListener() {
            @Override
            public void onAlphaChange(int alpha) {
                if (((Activity) container.getContext()).getLocalClassName().equals(showActivity)) {
                    ((ShowActivity) container.getContext()).getColorWithAlpha(alpha / 510f, Color.BLACK);
                } else if (((Activity) container.getContext()).getLocalClassName().equals(albumDetailActivity)) {
                    ((AlbumDetailActivity) container.getContext()).getColorWithAlpha(alpha / 510f, Color.BLACK);
                }
                getColorWithAlpha(alpha / 255f, Color.BLACK);
            }
        });
        mImageView.setTransformOutListener(new SmoothImageView.OnTransformOutListener() {
            @Override
            public void onTransformOut() {
                if (mImageView.checkMinScale()) {
                    ((Activity) container.getContext()).onBackPressed();
                }
            }
        });
        mImageView.setOnScaleChangeListener(new PhotoViewAttacher.OnScaleChangeListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                if (((Activity) container.getContext()).getLocalClassName().equals(showActivity)) {
                    if (mImageView.getScale() <= 1.1) {
                        ((ShowActivity) container.getContext()).viewPager.setScanScroll(true);
                    } else {
                        ((ShowActivity) container.getContext()).viewPager.setScanScroll(false);
                    }
                } else if (((Activity) container.getContext()).getLocalClassName().equals(albumDetailActivity)) {
                    if (mImageView.getScale() <= 1.1) {
                        ((AlbumDetailActivity) container.getContext()).viewPager.setScanScroll(true);
                    } else {
                        ((AlbumDetailActivity) container.getContext()).viewPager.setScanScroll(false);
                    }
                }
            }
        });

        return view;
    }


    public static ShowFragment newInstance(Bundle args) {
        ShowFragment fragment = new ShowFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void getColorWithAlpha(float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        rootView.setBackgroundColor(a + rgb);
    }

    public void changeBg(int color) {
        rootView.setBackgroundColor(color);
    }

    public void transformOut(SmoothImageView.onTransformListener listener) {
        mImageView.transformOut(listener);
    }

    public void transformIn() {
        mImageView.transformIn(new SmoothImageView.onTransformListener() {
            @Override
            public void onTransformCompleted(SmoothImageView.Status status) {
                if (((Activity) getContext()).getLocalClassName().equals(showActivity)) {
                    ((ShowActivity)getContext()).changeBg(Color.BLACK);
                } else if (((Activity)getContext()).getLocalClassName().equals(albumDetailActivity)) {
                    ((AlbumDetailActivity)getContext()).changeBg(Color.BLACK);
                }
            }
        });
    }
}
