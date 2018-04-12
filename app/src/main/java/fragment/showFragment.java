package fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import sure.gomotest.Activity.ShowActivity;
import sure.gomotest.R;
import widght.SmoothImageView;

public class showFragment extends LazyLoadFragment {
    private SmoothImageView imageView;
    private String path;
    private Bundle arg;
    private RelativeLayout rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arg=getArguments();
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_show, null);
//        imageView=view.findViewById(R.id.fragment_show_photo);
//        rootView=view.findViewById(R.id.fragment_show_layout);
//        path=arg.getString("path");
//        Glide.with(container.getContext()).load(path).into(imageView);

////        imageView.setAlphaChangeListener(new SmoothImageView.OnAlphaChangeListener() {
////            @Override
////            public void onAlphaChange(int alpha) {
//////                Log.d("onAlphaChange", "alpha:" + alpha);
//////                ((ShowActivity)(container.getContext())).setBackgroundColor(getColorWithAlpha(alpha / 255f, Color.BLACK));
////                getColorWithAlpha(alpha / 255f, Color.BLACK);
////            }
////        });
//        return view;
//    }

    public void setImage(String path, Context context){
        this.path=path;
        Glide.with(context).load(this.path).into(imageView);
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


    @Override
    protected int setContentView() {
        path=arg.getString("path");
        return R.layout.fragment_show;
    }

    @Override
    protected void lazyLoad() {
        shouImage(path,getContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        setUserVisibleHint(false);
    }
}
