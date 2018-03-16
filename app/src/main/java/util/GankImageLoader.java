package util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import adapter.main_recycle_adapter;
import magick.ColorspaceType;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magick.util.MagickBitmap;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by zhangzijian on 2018/03/16.
 */

public class GankImageLoader {
    public static void getBitmap(final String url, final main_recycle_adapter.ViewHolder holder, final int width, final int height){
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
                    catchStreamToFile(response.body().byteStream(), holder, url,width,height);
                    call.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static Bitmap catchStreamToFile(InputStream inStream, main_recycle_adapter.ViewHolder holder, String url,final int width, final int height) throws IOException {
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
        return getFitSampleBitmap(tempPath, holder, url,width,height);
    }


    public static Bitmap getFitSampleBitmap(String file_path, main_recycle_adapter.ViewHolder holder, String url, final int width, final int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file_path, options);

        int inSampleSize = 1;
        if (options.outWidth > width || options.outHeight > height) {
            int widthRatio = Math.round((float) options.outWidth / (float) width);
            int heightRatio = Math.round((float) options.outHeight / (float) height);
            inSampleSize = Math.min(widthRatio, heightRatio);
        }
        options.inSampleSize = inSampleSize;

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

        if (bitmap != null && bitmap.getWidth() != 0) {
            //缩放法压缩
            Matrix matrix = new Matrix();
            matrix.setScale(0.5f, 0.5f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
        }


        if (bitmap != null && bitmap.getWidth() != 0) {
            //质量压缩
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
            byte[] bytes = baos.toByteArray();

            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
        }

//        setImage(holder,bitmap,url);

        File tempFile = new File(file_path);
        if (tempFile.exists()) {
            tempFile.delete();
        }
        return bitmap;
    }

//    public static Bitmap setImage(main_recycle_adapter.ViewHolder holder, Bitmap bitmap, String url){
//        if (bitmap != null) {
////            imageCache.addToDiskLruCache(url, bitmap);
////            imageCache.addBitmapToCache(url, bitmap);
//            holder.imageView.setImageBitmap(bitmap);
//        }
//        return bitmap;
//    }
}
