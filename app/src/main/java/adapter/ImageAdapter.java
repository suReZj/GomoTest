package adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import sure.gomotest.Activity.MainActivity;
import sure.gomotest.R;
import utils.ImageCacheUtil;


/**
 * Created by dell88 on 2018/3/6 0006.
 * 用于首页展示图片recyclerview的adapter
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<String> mList;
    private Context mContext;
    private ImageCacheUtil mImageCache;
    private OkHttpClient mClient = new OkHttpClient();



    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView mCardView;
        public ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.item_image);
            mCardView = itemView.findViewById(R.id.item_cardView);
            int width = ((Activity) itemView.getContext()).getWindowManager().getDefaultDisplay().getWidth();
            ViewGroup.LayoutParams params = mImageView.getLayoutParams();
            //设置图片的相对于屏幕的宽高比
            params.width = (width - 6) / 3;
            mImageView.setLayoutParams(params);
        }
    }

    public ImageAdapter(List<String> list, Context context) {
        this.mList = list;
        this.mImageCache = new ImageCacheUtil(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_recycle_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public int getItemCount() {
        return mList.size();
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
        this.mImageCache.closeDiskLruCache();
    }

    public void fluchCache() {
        this.mImageCache.fluchCache();
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mImageView.setImageResource(R.mipmap.loadimage);
        Bitmap bitmap = mImageCache.getBitmapFromCache(mList.get(position));
        double scale = 0;
        if (bitmap != null) {
            scale = (double) (bitmap.getHeight()) / (double) (bitmap.getWidth());
            ViewGroup.LayoutParams params = holder.mImageView.getLayoutParams();
            params.height = (int) (params.width * scale);
            holder.mImageView.setLayoutParams(params);
            holder.mImageView.setImageBitmap(bitmap);
        } else {
            bitmap = mImageCache.getBitmapFromDisk(mList.get(position));
            if (bitmap != null) {
                mImageCache.addBitmapToCache(mList.get(position), bitmap);
                scale = (double) (bitmap.getHeight()) / (double) (bitmap.getWidth());
                ViewGroup.LayoutParams params = holder.mImageView.getLayoutParams();
                params.height = (int) (params.width * scale);
                holder.mImageView.setLayoutParams(params);
                holder.mImageView.setImageBitmap(bitmap);
            } else {
                getImageBitmap(mList.get(position), holder, position);
            }
        }

        if (mOnItemClickLitener != null) {
            holder.mCardView.setOnClickListener(new View.OnClickListener() {
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
        mClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mList.remove(position);
                        notifyItemRemoved(position);
                        ((MainActivity)mContext).removeShowList(position);
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
                    mList.remove(position);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemRemoved(position);
                            ((MainActivity)mContext).removeShowList(position);
                        }
                    });
                } else {
                    mImageCache.addToDiskLruCache(url, bitmap);
                    mImageCache.addBitmapToCache(url, bitmap);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = mImageCache.getBitmapFromCache(url);
                            if (bitmap != null) {
                                double scale = (double) (bitmap.getHeight()) / (double) (bitmap.getWidth());
                                ViewGroup.LayoutParams params = holder.mImageView.getLayoutParams();
                                params.height = (int) (params.width * scale);
                                holder.mImageView.setLayoutParams(params);
                            }
                            holder.mImageView.setImageBitmap(bitmap);
                        }
                    });
                    bitmap = null;
                }
            }
        });
        request = null;
    }
}
