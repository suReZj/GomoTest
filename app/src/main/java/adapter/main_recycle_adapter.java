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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import magick.ColorspaceType;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magick.util.MagickBitmap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import sure.gomotest.R;
import util.imageCache;

import static util.Contants.width;


/**
 * Created by dell88 on 2018/3/6 0006.
 */

public class main_recycle_adapter extends RecyclerView.Adapter<main_recycle_adapter.ViewHolder> {
    private List<String> list;
    private Context context;
    private imageCache imageCache;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            cardView = itemView.findViewById(R.id.item_cardView);
            int width =((Activity)itemView.getContext()).getWindowManager().getDefaultDisplay().getWidth();
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            //设置图片的相对于屏幕的宽高比
            params.width = width / 3;
            imageView.setLayoutParams(params);
        }
    }

    public main_recycle_adapter(List<String> list, Context context) {
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


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
//        holder.imageView.setTag(list.get(position));
        holder.imageView.setImageResource(R.mipmap.ic_launcher);

//        Glide.with(context).load(list.get(position)).into(holder.imageView);

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


//第一次连接将图片保存为文件

    public void getImageBitmap(final String url, final ViewHolder holder) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(url+width)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String path = url;
                int position = url.lastIndexOf("/");
                path = path.substring(position, url.length());
                String tempPath = Environment.getExternalStorageDirectory() + path;
                File tempFile = new File(tempPath);
                FileOutputStream fileOutputStream = null;
                byte[] buffer=null;
                try {
                    try {
                        if (tempFile.exists()) {
                            tempFile.delete();
                        }
                        tempFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    fileOutputStream = new FileOutputStream(tempFile);
                    buffer = new byte[1024];
                    int len = 0;
                    while ((len = response.body().byteStream().read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    getFitSampleBitmap(tempPath, holder, url);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    response.body().byteStream().close();
                    call.cancel();
                    fileOutputStream.close();
                    buffer=null;
                }
            }
        });
    }


    public void getFitSampleBitmap(String file_path, ViewHolder holder, String url) {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        byte[] bytes;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file_path, options);

        //采样率压缩
//        int inSampleSize = 1;
//        if (options.outWidth > holder.imageView.getWidth() || options.outHeight > holder.imageView.getHeight()) {
//            int widthRatio = Math.round((float) options.outWidth / (float) holder.imageView.getWidth());
//            int heightRatio = Math.round((float) options.outHeight / (float) holder.imageView.getHeight());
//            inSampleSize = Math.min(widthRatio, heightRatio);
//        }
        options.inSampleSize = 2;

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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            bytes = baos.toByteArray();

            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
        }

        setImage(holder,bitmap,url,file_path);
        if(baos!=null){
            try {
                baos.close();
                baos=null;
                bytes=null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setImage(ViewHolder holder,Bitmap bitmap,String url,String path){
        if (bitmap != null) {
            imageCache.addToDiskLruCache(url, bitmap);
            imageCache.addBitmapToCache(url, bitmap);
            holder.imageView.setImageBitmap(imageCache.getBitmapFromCache(url));
                bitmap = null;
            }
        File tempFile = new File(path);
        if (tempFile.exists()) {
            tempFile.delete();
        }
        System.gc();
    }
}
