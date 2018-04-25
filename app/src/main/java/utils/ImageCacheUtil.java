package utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zhangzijian on 2018/03/08.
 */

public class ImageCacheUtil {
    private Context mContext;
    private LruCache<String, Bitmap> mCache;


    /**
     * 图片硬盘缓存核心类。
     */
    private DiskLruCache mDiskLruCache = null;


    public ImageCacheUtil(Context context) {
        this.mContext = context;
        //获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        //设置缓存的大小
        int cacheSize = maxMemory / 8;
        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 重写此方法来衡量每张图片的大小，默认返回图片数量。
                return bitmap.getByteCount() / 1024;
            }
        };


        //首先调用getDiskCacheDir()方法获取到缓存地址的路径，然后判断一下该路径是否存在，
        // 如果不存在就创建一下。接着调用DiskLruCache的open()方法来创建实例，并把四个参数传入即可。
        try {
            File cacheDir = getDiskCacheDir(context, "bitmap");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 30 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 将bitmap加入到LruCache缓存中
     *
     * @param url    LruCache的键，即图片的下载路径
     * @param bitmap LruCache的值，即图片的Bitmap对象
     */
    public void addBitmapToCache(String url, Bitmap bitmap) {
        if(url!=null&&bitmap!=null){
            mCache.put(url, bitmap);
        }
//        bitmap=null;
//        Log.e("add",url);
    }

    /**
     * 从LruCache缓存中获取bitmap
     *
     * @param url LruCache的键，即图片的下载路径
     * @return 对应传入键的Bitmap对象，或者null
     */
    public Bitmap getBitmapFromCache(String url) {
        Bitmap bitmap = mCache.get(url);
        return bitmap;
    }


    //DiskLruCache获得当前应用程序版本号
    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }


    //DiskLruCache获得缓存路径
    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }


    //将图片的URL进行MD5编码，编码后的字符串肯定是唯一的，并且只会包含0-F这样的字符，完全符合文件的命名规则
    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }


    //添加bitmap到磁盘缓存
    public void addToDiskLruCache(String url, Bitmap bitmap) {
        String key = hashKeyForDisk(url);
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream;
                outputStream = editor.newOutputStream(0);
                if(outputStream!=null&&bitmap!=null){
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                }
                if (outputStream != null) {
                    editor.commit();
                } else {
                    editor.abort();
                }
            }
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //从磁盘缓存获取bitmap
    public Bitmap getBitmapFromDisk(String url) {
        String key = hashKeyForDisk(url);
//        Log.e("get",url);
        Bitmap bitmap = null;
        try {
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
            if (snapShot != null) {
                InputStream is = snapShot.getInputStream(0);
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
                snapShot.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //移除磁盘缓存
    public void removeBitmapFromDisk(String url) {
        String key = hashKeyForDisk(url);
        try {
            mDiskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeDiskLruCache() {
        try {
            mDiskLruCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将缓存记录同步到journal文件中。
     */
    public void fluchCache() {
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
