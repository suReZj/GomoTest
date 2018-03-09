package adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import sure.gomotest.R;
import util.imageCache;


/**
 * Created by dell88 on 2018/3/6 0006.
 */

public class recycle_adapter extends RecyclerView.Adapter<recycle_adapter.ViewHolder> {
    private List<String> list;
    private Context context;
    private imageCache imageCache;
    private List<Integer> Livelist = new ArrayList<>();

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ViewHolder holder = (ViewHolder) msg.obj;
                    Bitmap bitmap = (Bitmap) msg.getData().get("bitmap");
                    if (bitmap != null) {
                        String url = (String) msg.getData().get("url");
                        imageCache.addToDiskLruCache(url, bitmap);

                        imageCache.addBitmapToCache(url, bitmap);
                        holder.imageView.setImageBitmap(bitmap);
                    }
            }
            return true;
        }
    });

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            cardView = itemView.findViewById(R.id.item_cardView);
            int width = ((Activity) imageView.getContext()).getWindowManager().getDefaultDisplay().getWidth();
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            //设置图片的相对于屏幕的宽高比
            params.width = width / 3;
            imageView.setLayoutParams(params);
        }
    }

    public recycle_adapter(List<String> list, Context context) {
        this.list = list;
        this.imageCache = new imageCache(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
//        Glide.with(context).load(list.get(position)).into(holder.imageView);
        holder.imageView.setTag(list.get(position));
        holder.imageView.setImageResource(R.mipmap.ic_launcher);


        if (imageCache.getBitmapFromCache(list.get(position)) != null) {
            holder.imageView.setImageBitmap(imageCache.getBitmapFromCache(list.get(position)));
        } else {
            Bitmap bitmap = imageCache.getBitmapFromDisk(list.get(position));
            if (bitmap != null) {
                imageCache.addBitmapToCache(list.get(position), bitmap);
                holder.imageView.setImageBitmap(bitmap);
            } else {
                getImageBitmap(list.get(position), holder);
            }
        }


        if (mOnItemClickLitener != null) {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickLitener.onItemClick(holder.itemView, position);
                }
            });
        }

    }


//第一次连接获取图片大小计算缩放比例

    public void getImageBitmap(final String url, final ViewHolder holder) {
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
                    //获得图片尺寸进行压缩
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inJustDecodeBounds = true;

//                    if(response.body().byteStream()==null){
//                        Log.e("url",url);
//                        Log.e("response",response.toString());
//                    }


//                    BitmapFactory.decodeStream(response.body().byteStream(), null, options);
//
//
//                    int inSampleSize = 1;
//                    if (options.outWidth > holder.imageView.getWidth() || options.outHeight > holder.imageView.getHeight()) {
//                        int widthRatio = Math.round((float) options.outWidth / (float) holder.imageView.getWidth());
//                        int heightRatio = Math.round((float) options.outHeight / (float) holder.imageView.getHeight());
//                        inSampleSize = Math.min(widthRatio, heightRatio);
//                    }
//                    getImageBitmapDouble(url, inSampleSize, holder);

                    catchStreamToFile(response.body().byteStream(), holder, url);
                    call.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //第二次进行网络连接获取图片

    public void getImageBitmapDouble(final String url, final int inSampleSize, final ViewHolder holder) {

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
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;

                //采样率压缩
                options.inSampleSize = inSampleSize;

                //RGB_565法
                options.inPreferredConfig = Bitmap.Config.RGB_565;


                Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream(), null, options);

                if (bitmap == null) {

                }


                if (bitmap != null && bitmap.getWidth() != 0) {
                    //缩放法压缩
                    Matrix matrix = new Matrix();
                    matrix.setScale(0.5f, 0.5f);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
//                Log.e("url", url);


                if (bitmap != null && bitmap.getWidth() != 0) {
                    //质量压缩
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                    byte[] bytes = baos.toByteArray();

                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    baos.close();
                    response.body().byteStream().close();
                } else {
//                    Log.e("url",url);
                }


                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = holder;
                Bundle bundle = new Bundle();
                bundle.putParcelable("bitmap", bitmap);
                bundle.putString("url", url);
                msg.setData(bundle);
                handler.sendMessage(msg);
                call.cancel();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public void closeDisk() {
        this.imageCache.closeDiskLruCache();
    }

    public void fluchCache() {
        this.imageCache.fluchCache();
    }

    public void catchStreamToFile(InputStream inStream, ViewHolder holder, String url) throws IOException {
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
        getFitSampleBitmap(tempPath, holder, url);
    }

    public void getFitSampleBitmap(String file_path, ViewHolder holder, String url) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file_path, options);

        int inSampleSize = 1;
        if (options.outWidth > holder.imageView.getWidth() || options.outHeight > holder.imageView.getHeight()) {
            int widthRatio = Math.round((float) options.outWidth / (float) holder.imageView.getWidth());
            int heightRatio = Math.round((float) options.outHeight / (float) holder.imageView.getHeight());
            inSampleSize = Math.min(widthRatio, heightRatio);
        }
        options.inSampleSize = inSampleSize;

        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bitmap = BitmapFactory.decodeFile(file_path, options);

        if (bitmap != null && bitmap.getWidth() != 0) {
            //缩放法压缩
            Matrix matrix = new Matrix();
            matrix.setScale(0.5f, 0.5f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }else {
        }


        if (bitmap != null && bitmap.getWidth() != 0) {
            //质量压缩
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
            byte[] bytes = baos.toByteArray();

            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
        }

        Message msg = handler.obtainMessage();
        msg.what = 1;
        msg.obj = holder;
        Bundle bundle = new Bundle();
        bundle.putParcelable("bitmap", bitmap);
        bundle.putString("url", url);
        msg.setData(bundle);
        handler.sendMessage(msg);

        File tempFile = new File(file_path);
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}
