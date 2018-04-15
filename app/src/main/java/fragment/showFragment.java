package fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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

public class showFragment extends Fragment {
    private SmoothImageView imageView;
    private String path;
    private Bundle arg;
    private RelativeLayout rootView;
    private String showActivity = "Activity.ShowActivity";
    private String albumDetailActivity = "Activity.AlbumDetailActivity";
    private ShowImageBean showBean;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arg = getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show, null);
        imageView = view.findViewById(R.id.fragment_show_photo);
        rootView = view.findViewById(R.id.fragment_show_layout);
        path = arg.getString("path");
        showBean=arg.getParcelable("imagePaths");
        if(showBean!=null){
            imageView.setThumbRect(showBean.getBounds());
        }

//        imageView.setTag(showBean.getPath());
        Glide.with(container.getContext()).load(path).into(imageView);

        imageView.setAlphaChangeListener(new SmoothImageView.OnAlphaChangeListener() {
            @Override
            public void onAlphaChange(int alpha) {
//                ((ShowActivity)(container.getContext())).setBackgroundColor(getColorWithAlpha(alpha / 255f, Color.BLACK));
//                getColorWithAlpha(alpha / 255f, Color.BLACK);

                if (((Activity) container.getContext()).getLocalClassName().equals(showActivity)) {
                    ((ShowActivity) container.getContext()).getColorWithAlpha(alpha / 255f, Color.BLACK);
                } else if (((Activity) container.getContext()).getLocalClassName().equals(albumDetailActivity)) {
                    ((AlbumDetailActivity) container.getContext()).getColorWithAlpha(alpha / 255f, Color.BLACK);
                }
            }
        });
        imageView.setTransformOutListener(new SmoothImageView.OnTransformOutListener() {
            @Override
            public void onTransformOut() {
                if (imageView.checkMinScale()) {
                    ((Activity) container.getContext()).onBackPressed();
//                    ((ShowActivity) container.getContext()).transformOut();
                }
            }
        });
        imageView.setOnScaleChangeListener(new PhotoViewAttacher.OnScaleChangeListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                if (((Activity) container.getContext()).getLocalClassName().equals(showActivity)) {
                    if (imageView.getScale() <= 1.1) {
                        ((ShowActivity) container.getContext()).viewPager.setScanScroll(true);
                    } else {
                        ((ShowActivity) container.getContext()).viewPager.setScanScroll(false);
                    }
                } else if (((Activity) container.getContext()).getLocalClassName().equals(albumDetailActivity)) {
                    if (imageView.getScale() <= 1.1) {
                        ((AlbumDetailActivity) container.getContext()).viewPager.setScanScroll(true);
                    } else {
                        ((AlbumDetailActivity) container.getContext()).viewPager.setScanScroll(false);
                    }
                }

            }
        });

        return view;
    }


    public static showFragment newInstance(Bundle args) {
        showFragment fragment = new showFragment();
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
        imageView.transformOut(listener);
    }


//
//    @Override
//    public void onDestroyView() {
//        setUserVisibleHint(false);
//        super.onDestroyView();
//    }
//
//    @Override
//    public void onStop() {
//        setUserVisibleHint(false);
//        super.onStop();
//    }
}
