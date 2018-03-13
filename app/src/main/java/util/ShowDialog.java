package util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import magick.ColorspaceType;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magick.util.MagickBitmap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import sure.gomotest.Activity.MainActivity;
import sure.gomotest.R;
import tyrantgit.widget.HeartLayout;

import static util.Contants.bigImage;


/**
 * Created by dell88 on 2018/1/23 0023.
 */

public class ShowDialog {
    static Random mRandom = new Random();
    static Dialog bottomDialog;
    static Context mContext;
    static imageCache imageCache;
    static String url;

    public static void showImageDialog(Context context, String path) {
        imageCache = new imageCache(context);
        mContext = context;
        url = path;
        View contentView = LayoutInflater.from(context).inflate(R.layout.show_image_dialog, null);
        bottomDialog = new Dialog(context, R.style.BottomDialog);
        ImageView imageView = contentView.findViewById(R.id.show_image);

        //获得屏幕的宽度

        int width = ((Activity) imageView.getContext()).getWindowManager().getDefaultDisplay().getWidth();

        if (imageCache.getBitmapFromCache(path + bigImage) != null) {
            setImage(imageCache.getBitmapFromCache(path + bigImage));
        } else {
            Bitmap bitmap = imageCache.getBitmapFromDisk(path + bigImage);
            if (bitmap != null) {
                imageCache.addBitmapToCache(path + bigImage, bitmap);
                setImage(imageCache.getBitmapFromCache(path + bigImage));
            } else {
                getImageBitmap(path, context, imageView, width);
            }
        }


    }


    //第一次网络请求获得图片的InputStream
    public static void getImageBitmap(final String url, final Context context, final ImageView imageView, final int width) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                try {
                    catchStreamToFile(response.body().byteStream(), url, context, imageView, width);
                    call.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    //将图片的InputStream转化为本地图片
    public static void catchStreamToFile(InputStream inStream, String url, Context context, ImageView imageView, int width) throws IOException {
        String path = url;
        int position = url.lastIndexOf("/");
        path = path.substring(position, url.length());
        String tempPath = Environment.getExternalStorageDirectory() + path;
        File tempFile = new File(tempPath);
        try {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, len);
        }
        inStream.close();
        fileOutputStream.close();
        getFitSampleBitmap(tempPath, tempPath, context, imageView, width);
    }


    //本地加载图片并进行比例缩放
    public static void getFitSampleBitmap(String file_path, String url, Context context, ImageView imageView, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file_path, options);

//        Log.e("options.outWidth", options.outWidth + "");
//        Log.e("imageView.getWidth()", imageView.getWidth() + "");

        int inSampleSize = 1;
        if (options.outWidth > width || options.outHeight > imageView.getHeight()) {
            int widthRatio = Math.round((float) options.outWidth / (float) width);
//            int heightRatio = Math.round((float) options.outHeight / (float) imageView.getHeight());
//            inSampleSize = Math.min(widthRatio, heightRatio);
            inSampleSize = widthRatio;
        }
        options.inSampleSize = inSampleSize;
//        Log.e("inSampleSize", inSampleSize + "");

        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;



        Bitmap bitmap = BitmapFactory.decodeFile(file_path, options);

        if (bitmap == null) {
            ImageInfo info = null;
            try {
                info = new ImageInfo(file_path);
                MagickImage magickImage = new MagickImage(info);
                magickImage.transformRgbImage(ColorspaceType.RGBColorspace);
                bitmap = MagickBitmap.ToBitmap(magickImage);
                magickImage.destroyImages();
            } catch (MagickException e) {
                e.printStackTrace();
            }
        }

        setImage(bitmap);

        File tempFile = new File(file_path);
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }


    public static void closeDisk() {
        imageCache.closeDiskLruCache();
    }

    public static void fluchCache() {
        imageCache.fluchCache();
    }

    public static void setImage(Bitmap bitmap){
        final View contentView = LayoutInflater.from(mContext).inflate(R.layout.show_image_dialog, null);
        ImageView imageView = (ImageView) contentView.findViewById(R.id.show_image);
        final HeartLayout layout = contentView.findViewById(R.id.periscope);

        if (bitmap == null) {
            Log.e("null", "null");
        }

        int width = ((Activity) imageView.getContext()).getWindowManager().getDefaultDisplay().getWidth();
        ViewGroup.LayoutParams param = imageView.getLayoutParams();
        param.width = width;
        imageView.setLayoutParams(param);


        imageView.setImageBitmap(bitmap);
        imageCache.addToDiskLruCache(url + bigImage, bitmap);
        imageCache.addBitmapToCache(url + bigImage, bitmap);


//                    imageView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
//                        @Override
//                        public void onDoubleClick() {
//                                                        layout.addHeart(Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255)));
//                        }
//                    }));


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.addHeart(Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255)));
            }
        });

        bottomDialog.setContentView(contentView);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
        params.width = mContext.getResources().getDisplayMetrics().widthPixels;
        params.bottomMargin = DensityUtil.dp2px(mContext, 8f);
        contentView.setLayoutParams(params);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);

        ((MainActivity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //此时已在主线程中，可以更新UI了
                bottomDialog.show();
            }
        });

    }
}
