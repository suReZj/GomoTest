package sure.gomotest.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.jaredrummler.android.widget.AnimatedSvgView;

import sure.gomotest.R;
import utils.SVGUtil;
import yanzhikai.textpath.AsyncTextPathView;

/**
 * Created by dell88 on 2018/3/7 0007.
 * app打开时的加载动画
 */


public class LoadingActivity extends AppCompatActivity {
    private AsyncTextPathView mAsyncTextPathView;
    private AsyncTextPathView mSureTextPathView;
    private AnimatedSvgView mSvgView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*set it to be no title*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.loading_activity);
        initView();
        setListener();
    }

    public void initView() {
        mAsyncTextPathView = (AsyncTextPathView) findViewById(R.id.firstAsyncTextPathView);
        mSvgView = (AnimatedSvgView) findViewById(R.id.animated_svg_view);
        mSureTextPathView = (AsyncTextPathView) findViewById(R.id.secondAsyncTextPathView);
        setSvg(SVGUtil.values()[0]);
    }

    public void setListener() {
        mSvgView.setOnStateChangeListener(new AnimatedSvgView.OnStateChangeListener() {
            @Override
            public void onStateChange(int state) {
                if (state == AnimatedSvgView.STATE_FINISHED) {
                    Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void setSvg(SVGUtil svg) {
        mSvgView.setGlyphStrings(svg.glyphs);
        mSvgView.setFillColors(svg.colors);
        mSvgView.setViewportSize(svg.width, svg.height);
        mSvgView.setTraceResidueColor(0x32000000);
        mSvgView.setTraceColors(svg.colors);
        mSvgView.rebuildGlyphData();
        mSvgView.start();

        //从无到显示
        mAsyncTextPathView.startAnimation(0, 1);
        mSureTextPathView.startAnimation(0, 1);
    }
}
