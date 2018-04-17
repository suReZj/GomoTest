package adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import magick.ColorspaceType;
import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import magick.util.MagickBitmap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import sure.gomotest.Activity.MainActivity;
import sure.gomotest.R;
import util.imageCache;


/**
 * Created by dell88 on 2018/3/6 0006.
 */

public class main_recycle_adapter extends RecyclerView.Adapter<main_recycle_adapter.ViewHolder> {
    private List<String> list;
    private Context context;
    private imageCache imageCache;
    private OkHttpClient client = new OkHttpClient();



    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            cardView = itemView.findViewById(R.id.item_cardView);
            int width = ((Activity) itemView.getContext()).getWindowManager().getDefaultDisplay().getWidth();
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            //设置图片的相对于屏幕的宽高比
            params.width = (width - 6) / 3;
//            params.width=width/2;
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
        holder.imageView.setImageResource(R.mipmap.loadimage);

        Bitmap bitmap = imageCache.getBitmapFromCache(list.get(position));
        double scale = 0;
        if (bitmap != null) {
            scale = (double) (bitmap.getHeight()) / (double) (bitmap.getWidth());
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();

            params.height = (int) (params.width * scale);
            holder.imageView.setLayoutParams(params);
            holder.imageView.setImageBitmap(bitmap);
        } else {
            bitmap = imageCache.getBitmapFromDisk(list.get(position));
            if (bitmap != null) {
                imageCache.addBitmapToCache(list.get(position), bitmap);
                scale = (double) (bitmap.getHeight()) / (double) (bitmap.getWidth());
                ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
                params.height = (int) (params.width * scale);
                holder.imageView.setLayoutParams(params);
                holder.imageView.setImageBitmap(bitmap);
            } else {
                getImageBitmap(list.get(position), holder, position);
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


    public void getImageBitmap(final String url, final ViewHolder holder, final int position) {
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemRemoved(position);
                        ((MainActivity)context).removeShowList(position);
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream(), null, options);

                if (bitmap != null && bitmap.getWidth() != 0) {
                    //缩放法压缩
                    Matrix matrix = new Matrix();
                    matrix.setScale(0.5f, 0.5f);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
                Bitmap newBitmap = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
                if ((baos != null) && (bitmap != null)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    int option = 100;
                    //循环判断如果压缩后图片是否大于50kb,大于继续压缩
                    while (baos.toByteArray().length / 1024 > 50) {
                        //清空baos
                        baos.reset();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, option, baos);
                        option -= 10;//每次都减少10
                    }
                    //把压缩后的数据baos存放到ByteArrayInputStream中
                    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
                    //把ByteArrayInputStream数据生成图片
                    newBitmap = BitmapFactory.decodeStream(isBm, null, null);
                    bitmap = newBitmap;
                }

                if (bitmap == null) {
                    list.remove(position);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemRemoved(position);
                            ((MainActivity)context).removeShowList(position);
                        }
                    });
                } else {
                    imageCache.addToDiskLruCache(url, bitmap);
                    imageCache.addBitmapToCache(url, bitmap);


                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = imageCache.getBitmapFromCache(url);
                            if (bitmap != null) {
                                double scale = (double) (bitmap.getHeight()) / (double) (bitmap.getWidth());
                                ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
                                params.height = (int) (params.width * scale);
                                holder.imageView.setLayoutParams(params);
                            }
                            holder.imageView.setImageBitmap(bitmap);
                        }
                    });
                    bitmap = null;
                }
            }
        });
        request = null;
    }
}
