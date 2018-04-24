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

public class LoadingActivity extends AppCompatActivity {
    private AsyncTextPathView asyncTextPathView;
    private AsyncTextPathView sureTextPathView;
    private AnimatedSvgView svgView;


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
        asyncTextPathView = (AsyncTextPathView) findViewById(R.id.firstAsyncTextPathView);
        svgView = (AnimatedSvgView) findViewById(R.id.animated_svg_view);
        sureTextPathView=(AsyncTextPathView)findViewById(R.id.secondAsyncTextPathView);
        setSvg(SVGUtil.values()[0]);
    }

    public void setListener() {
        svgView.setOnStateChangeListener(new AnimatedSvgView.OnStateChangeListener() {
            @Override
            public void onStateChange(int state) {
                if(state==AnimatedSvgView.STATE_FINISHED){
                    Intent intent=new Intent(LoadingActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void setSvg(SVGUtil svg) {
        svgView.setGlyphStrings(svg.glyphs);
        svgView.setFillColors(svg.colors);
        svgView.setViewportSize(svg.width, svg.height);
        svgView.setTraceResidueColor(0x32000000);
        svgView.setTraceColors(svg.colors);
        svgView.rebuildGlyphData();
        svgView.start();

        //从无到显示
        asyncTextPathView.startAnimation(0,1);
        sureTextPathView.startAnimation(0,1);
    }
}
