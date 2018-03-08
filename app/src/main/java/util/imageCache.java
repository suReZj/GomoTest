package util;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

/**
 * Created by zhangzijian on 2018/03/08.
 */

public class imageCache {
    private LruCache<String, Bitmap> mCache;//LruCache缓存对象
    public imageCache(){
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
    }
    /**
     * 将bitmap加入到缓存中
     *
     * @param url LruCache的键，即图片的下载路径
     * @param bitmap LruCache的值，即图片的Bitmap对象
     */
    public void addBitmapToCache(String url, Bitmap bitmap) {
            mCache.put(url, bitmap);
        Log.e("aaaa","aa");
    }

    /**
     * 从缓存中获取bitmap
     *
     * @param url LruCache的键，即图片的下载路径
     * @return 对应传入键的Bitmap对象，或者null
     */
    public Bitmap getBitmapFromCache(String url) {
        Bitmap bitmap = mCache.get(url);
        Log.e("bbbb","bb");
        return bitmap;
    }
}
