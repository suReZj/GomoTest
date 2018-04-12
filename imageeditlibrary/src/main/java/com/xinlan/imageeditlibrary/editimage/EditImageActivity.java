package com.xinlan.imageeditlibrary.editimage;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.xinlan.imageeditlibrary.BaseActivity;
import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.fragment.AddTextFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.BeautyFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.CropFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.FilterListFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.MainMenuFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.PaintFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.RotateFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.StickerFragment;
import com.xinlan.imageeditlibrary.editimage.utils.FileUtil;
import com.xinlan.imageeditlibrary.editimage.view.CropImageView;
import com.xinlan.imageeditlibrary.editimage.view.CustomPaintView;
import com.xinlan.imageeditlibrary.editimage.view.CustomViewPager;
import com.xinlan.imageeditlibrary.editimage.view.RotateImageView;
import com.xinlan.imageeditlibrary.editimage.view.StickerView;
import com.xinlan.imageeditlibrary.editimage.view.TextStickerView;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouch;
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouchBase;
import com.xinlan.imageeditlibrary.editimage.widget.EditCache;
import com.xinlan.imageeditlibrary.editimage.widget.RedoUndoController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 * 图片编辑 主页面
 *
 * @author panyi
 * <p>
 * 包含 1.贴图 2.滤镜 3.剪裁 4.底图旋转 功能
 * add new modules
 */
public class EditImageActivity extends BaseActivity {
    public static final String FILE_PATH = "file_path";
    public static final String EXTRA_OUTPUT = "extra_output";
    public static final String SAVE_FILE_PATH = "save_file_path";

    public static final String IMAGE_IS_EDIT = "image_is_edit";

    public static final int MODE_NONE = 0;
    public static final int MODE_STICKERS = 1;// 贴图模式
    public static final int MODE_FILTER = 2;// 滤镜模式
    public static final int MODE_CROP = 3;// 剪裁模式
    public static final int MODE_ROTATE = 4;// 旋转模式
    public static final int MODE_TEXT = 5;// 文字模式
    public static final int MODE_PAINT = 6;//绘制模式
    public static final int MODE_BEAUTY = 7;//美颜模式

    public String filePath;// 需要编辑图片路径
    public String saveFilePath;// 生成的新图片路径
    private int imageWidth, imageHeight;// 展示图片控件 宽 高
    private LoadImageTask mLoadImageTask;

    public int mode = MODE_NONE;// 当前操作模式

    protected int mOpTimes = 0;
    protected boolean isBeenSaved = false;

    private EditImageActivity mContext;
    private Bitmap mainBitmap;// 底层显示Bitmap
    public ImageViewTouch mainImage;
    private View backBtn;

    public ViewFlipper bannerFlipper;
    private View applyBtn;// 应用按钮
    private View saveBtn;// 保存按钮

    public StickerView mStickerView;// 贴图层View
    public CropImageView mCropPanel;// 剪切操作控件
    public RotateImageView mRotatePanel;// 旋转操作控件
    public TextStickerView mTextStickerView;//文本贴图显示View
    public CustomPaintView mPaintView;//涂鸦模式画板

    public CustomViewPager bottomGallery;// 底部gallery
    private BottomGalleryAdapter mBottomGalleryAdapter;// 底部gallery
    private MainMenuFragment mMainMenuFragment;// Menu
    public StickerFragment mStickerFragment;// 贴图Fragment
    public FilterListFragment mFilterListFragment;// 滤镜FliterListFragment
    public CropFragment mCropFragment;// 图片剪裁Fragment
    public RotateFragment mRotateFragment;// 图片旋转Fragment
    public AddTextFragment mAddTextFragment;//图片添加文字
    public PaintFragment mPaintFragment;//绘制模式Fragment
    public BeautyFragment mBeautyFragment;//美颜模式Fragment
    private SaveImageTask mSaveImageTask;

    public RedoUndoController mRedoUndoController;//撤销操作

    public LinearLayout undoLayout;

    /**
     * @param context
     * @param editImagePath
     * @param outputPath
     * @param requestCode
     */
    public static void start(Activity context, final String editImagePath, final String outputPath, final int requestCode) {
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, EditImageActivity.class);
        it.putExtra(EditImageActivity.FILE_PATH, editImagePath);
        it.putExtra(EditImageActivity.EXTRA_OUTPUT, outputPath);
        context.startActivityForResult(it, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {


        checkInitImageLoader();
        setContentView(R.layout.activity_image_edit);
        initView();
        getData();
        super.onCreate(savedInstanceState);
    }

    private void getData() {
        filePath = getIntent().getStringExtra(FILE_PATH);
        saveFilePath = getIntent().getStringExtra(EXTRA_OUTPUT);// 保存图片路径
        loadImage(filePath);
    }

    private void initView() {
        mContext = this;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels / 2;
        imageHeight = metrics.heightPixels / 2;

        bannerFlipper = (ViewFlipper) findViewById(R.id.banner_flipper);
        bannerFlipper.setInAnimation(this, R.anim.in_bottom_to_top);
        bannerFlipper.setOutAnimation(this, R.anim.out_bottom_to_top);
        applyBtn = findViewById(R.id.apply);
        applyBtn.setOnClickListener(new ApplyBtnClick());
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new SaveBtnClick());

        mainImage = (ImageViewTouch) findViewById(R.id.main_image);
        backBtn = findViewById(R.id.back_btn);// 退出按钮
        backBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                onBackPressed();
                finish();
            }
        });

        mStickerView = (StickerView) findViewById(R.id.sticker_panel);
        mCropPanel = (CropImageView) findViewById(R.id.crop_panel);
        mRotatePanel = (RotateImageView) findViewById(R.id.rotate_panel);
        mTextStickerView = (TextStickerView) findViewById(R.id.text_sticker_panel);
        mPaintView = (CustomPaintView) findViewById(R.id.custom_paint_view);

        // 底部gallery
        bottomGallery = (CustomViewPager) findViewById(R.id.bottom_gallery);
        //bottomGallery.setOffscreenPageLimit(7);
        mMainMenuFragment = MainMenuFragment.newInstance(getApplicationContext());
        mBottomGalleryAdapter = new BottomGalleryAdapter(
                this.getSupportFragmentManager());
        mStickerFragment = StickerFragment.newInstance();
        mFilterListFragment = FilterListFragment.newInstance();
        mCropFragment = CropFragment.newInstance();
        mRotateFragment = RotateFragment.newInstance();
        mAddTextFragment = AddTextFragment.newInstance();
        mPaintFragment = PaintFragment.newInstance();
        mBeautyFragment = BeautyFragment.newInstance();

        bottomGallery.setAdapter(mBottomGalleryAdapter);


        mainImage.setFlingListener(new ImageViewTouch.OnImageFlingListener() {
            @Override
            public void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //System.out.println(e1.getAction() + " " + e2.getAction() + " " + velocityX + "  " + velocityY);
                if (velocityY > 1) {
                    closeInputMethod();
                }
            }
        });

        mRedoUndoController = new RedoUndoController(this, findViewById(R.id.redo_uodo_panel));
        undoLayout=findViewById(R.id.redo_uodo_panel);
    }

    /**
     * 关闭输入法
     */
    private void closeInputMethod() {
        if (mAddTextFragment.isAdded()) {
            mAddTextFragment.hideInput();
        }
    }

    /**
     * @author panyi
     */
    private final class BottomGalleryAdapter extends FragmentPagerAdapter {
        public BottomGalleryAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            // System.out.println("createFragment-->"+index);
            switch (index) {
                case MainMenuFragment.INDEX:// 主菜单
                    return mMainMenuFragment;
                case StickerFragment.INDEX:// 贴图
                    return mStickerFragment;
                case FilterListFragment.INDEX:// 滤镜
                    return mFilterListFragment;
                case CropFragment.INDEX://剪裁
                    return mCropFragment;
                case RotateFragment.INDEX://旋转
                    return mRotateFragment;
                case AddTextFragment.INDEX://添加文字
                    return mAddTextFragment;
                case PaintFragment.INDEX:
                    return mPaintFragment;//绘制
                case BeautyFragment.INDEX://美颜
                    return mBeautyFragment;
            }//end switch
            return MainMenuFragment.newInstance(EditImageActivity.this);
        }

        @Override
        public int getCount() {
            return 8;
        }
    }// end inner class

    /**
     * 异步载入编辑图片
     *
     * @param filepath
     */
    public void loadImage(String filepath) {
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }
        mLoadImageTask = new LoadImageTask();
        mLoadImageTask.execute(filepath);
    }


    @Override
    public void onBackPressed() {
        switch (mode) {
            case MODE_STICKERS:
                mStickerFragment.backToMain(EditImageActivity.this);
                return;
            case MODE_FILTER:// 滤镜编辑状态
                mFilterListFragment.backToMain(EditImageActivity.this);// 保存滤镜贴图
                return;
            case MODE_CROP:// 剪切图片保存
                mCropFragment.backToMain(EditImageActivity.this);
                return;
            case MODE_ROTATE:// 旋转图片保存
                mRotateFragment.backToMain(EditImageActivity.this);
                return;
            case MODE_TEXT:
                mAddTextFragment.backToMain(EditImageActivity.this);
                return;
            case MODE_PAINT:
                mPaintFragment.backToMain(EditImageActivity.this);
                return;
            case MODE_BEAUTY://从美颜模式中返回
                mBeautyFragment.backToMain(EditImageActivity.this);
                return;
        }// end switch

        if (canAutoExit()) {
            onSaveTaskDone();
        } else {//图片还未被保存    弹出提示框确认
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage(R.string.exit_without_save)
                    .setCancelable(false).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mContext.finish();
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    /**
     * 应用按钮点击
     *
     * @author panyi
     */
    private final class ApplyBtnClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (mode) {
                case MODE_STICKERS:
                    mStickerFragment.applyStickers(EditImageActivity.this, mainBitmap);// 保存贴图
                    break;
                case MODE_FILTER:// 滤镜编辑状态
                    mFilterListFragment.applyFilterImage();// 保存滤镜贴图
                    break;
                case MODE_CROP:// 剪切图片保存
                    mCropFragment.applyCropImage();
                    break;
                case MODE_ROTATE:// 旋转图片保存
                    mRotateFragment.applyRotateImage();
                    break;
                case MODE_TEXT://文字贴图 图片保存
                    mAddTextFragment.applyTextImage();
                    break;
                case MODE_PAINT://保存涂鸦
                    mPaintFragment.savePaintImage(EditImageActivity.this);
                    break;
                case MODE_BEAUTY://保存美颜后的图片
                    mBeautyFragment.applyBeauty();
                    break;
                default:
                    break;
            }// end switch
        }
    }// end inner class

    /**
     * 保存按钮 点击退出
     *
     * @author panyi
     */
    private final class SaveBtnClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (mOpTimes == 0) {//并未修改图片
                onSaveTaskDone();
            } else {
                doSaveImage();
            }
        }
    }// end inner class

    protected void doSaveImage() {
        if (mOpTimes <= 0)
            return;

        if (mSaveImageTask != null) {
            mSaveImageTask.cancel(true);
        }

        mSaveImageTask = new SaveImageTask();
        mSaveImageTask.execute(mainBitmap);
    }

    /**
     * @param newBit
     * @param needPushUndoStack
     */
    public void changeMainBitmap(String path, Bitmap newBit, boolean needPushUndoStack) {
        if (path != null) {
//            try {
//                newBit=Glide.with(getApplicationContext()).load(filePath).asBitmap().fitCenter().into(imageWidth,imageHeight).get();
            if (mainBitmap == null || mainBitmap != newBit) {
                if (needPushUndoStack) {
                    mRedoUndoController.switchMainBit(mainBitmap, newBit);
                    increaseOpTimes();
                }
                mainBitmap = newBit;
                mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if(!mainBitmap.isRecycled()){
                    mainBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] bytes=baos.toByteArray();
                    Glide.with(getApplicationContext()).load(bytes).into(mainImage);
                }

//                Glide.with(getApplicationContext()).load(path).into(mainImage);
            }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
        } else {
            if (newBit == null)
                return;

            if (mainBitmap == null || mainBitmap != newBit) {
                if (needPushUndoStack) {
                    mRedoUndoController.switchMainBit(mainBitmap, newBit);
                    increaseOpTimes();
                }
                mainBitmap = newBit;
                mainImage.setImageBitmap(mainBitmap);
                mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
            }
        }

    }

    public int getBitmapRotateAngle(String path) {
        Log.e("path", path);
        int digree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();

            exif = null;
        }
        if (exif != null) {
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }

        }
        Log.e("digree", digree + "");
        return digree;
    }

    /**
     * 导入文件图片任务
     */
    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        String path = null;

        @Override
        protected Bitmap doInBackground(String... params) {
            path = params[0];
            Bitmap bitmap = null;
            try {
                bitmap=Glide.with(getApplicationContext()).load(filePath).asBitmap().fitCenter().into(imageWidth, imageHeight).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
//            return BitmapUtils.getSampledBitmap(params[0], imageWidth,
//                    imageHeight, EditImageActivity.this);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
//            getBitmapRotateAngle(getRealPathFromUri(getApplicationContext(),Uri.parse("file://"+path)));

//            changeMainBitmap( rotateBitmap(getBitmapRotateAngle(path),result), false);
            changeMainBitmap(path, result, false);
        }
    }// end inner class

    public Bitmap rotateBitmap(int digree, Bitmap bitmap) {
        Bitmap newBitmap = null;
        if (digree == 0) {
            newBitmap = bitmap;
        } else {
            Matrix m = new Matrix();
            m.setRotate(digree, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            Log.e("digree", digree + "");
//            newBitmap=adjustPhotoRotation(bitmap,digree);
            if (bitmap != newBitmap) {
//                bitmap.recycle();
            }
        }
        return newBitmap;
    }

    Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {

        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);


        return bm1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }

        if (mSaveImageTask != null) {
            mSaveImageTask.cancel(true);
        }

        if (mRedoUndoController != null) {
            mRedoUndoController.onDestroy();
        }
    }

    public void increaseOpTimes() {
        mOpTimes++;
        isBeenSaved = false;
    }

    public void resetOpTimes() {
        isBeenSaved = true;
    }

    public boolean canAutoExit() {
        return isBeenSaved || mOpTimes == 0;
    }

    protected void onSaveTaskDone() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(FILE_PATH, filePath);
        returnIntent.putExtra(EXTRA_OUTPUT, saveFilePath);
        returnIntent.putExtra(IMAGE_IS_EDIT, mOpTimes > 0);

        FileUtil.ablumUpdate(this, saveFilePath);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    /**
     * 保存图像
     * 完成后退出
     */
    private final class SaveImageTask extends AsyncTask<Bitmap, Void, Boolean> {
        private Dialog dialog;

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            if (TextUtils.isEmpty(saveFilePath))
                return false;

            return BitmapUtils.saveBitmap(params[0], saveFilePath);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
        }

        @Override
        protected void onCancelled(Boolean result) {
            super.onCancelled(result);
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = EditImageActivity.getLoadingDialog(mContext, R.string.saving_image, false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dialog.dismiss();

            if (result) {
                resetOpTimes();
                onSaveTaskDone();
            } else {
                Toast.makeText(mContext, R.string.save_error, Toast.LENGTH_SHORT).show();
            }
        }
    }//end inner class

    public Bitmap getMainBit() {
        return mainBitmap;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState.putInt("mode",mode);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        mainImage.clear();
        switch (mode) {
            case MODE_STICKERS:
//                mStickerFragment.applyStickers(EditImageActivity.this,mainBitmap);// 保存贴图
                mStickerFragment.backToMain(EditImageActivity.this);
                break;
            case MODE_FILTER:// 滤镜编辑状态
//                mFilterListFragment.applyFilterImage();// 保存滤镜贴图
                mFilterListFragment.backToMain(EditImageActivity.this);
                break;
            case MODE_CROP:// 剪切图片保存
//                mCropFragment.applyCropImage();
                mCropFragment.backToMain(EditImageActivity.this);
                break;
            case MODE_ROTATE:// 旋转图片保存
//                mRotateFragment.applyRotateImage();
                mRotateFragment.backToMain(EditImageActivity.this);
                break;
            case MODE_TEXT://文字贴图 图片保存
//                mAddTextFragment.applyTextImage();
//                mAddTextFragment=AddTextFragment.newInstance();
                mTextStickerView.setVisibility(View.GONE);
                mAddTextFragment.backToMain(EditImageActivity.this);
                break;
            case MODE_PAINT://保存涂鸦
//                mPaintFragment.savePaintImage(EditImageActivity.this);
//                mPaintView.invalidate();
//                mPaintView.generatorBit();
//                mPaintView.setVisibility(View.GONE);
                mPaintFragment.backToMain(EditImageActivity.this);
                break;
            case MODE_BEAUTY://保存美颜后的图片
//                mBeautyFragment.applyBeauty();

                mBeautyFragment.backToMain(EditImageActivity.this);
                break;
            default:
                break;
        }// end switch
        super.onConfigurationChanged(newConfig);
    }
}// end class
