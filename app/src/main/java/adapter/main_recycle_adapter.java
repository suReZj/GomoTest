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
    private String error = "http://7xi8d6.com1.z0.glb.clouddn.com/2017-01-20-030332.jpg";
//    Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            ViewHolder holder = (ViewHolder) msg.obj;
//            String url = msg.getData().getString("url");
//            holder.imageView.setImageBitmap(imageCache.getBitmapFromCache(url));
//            notifyDataSetChanged();
//            return false;
//        }
//    });


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
//        holder.imageView.setTag(list.get(position));
//        holder.imageView.setImageResource(R.mipmap.ic_launcher);
        holder.imageView.setImageResource(R.mipmap.loadimage);

//        Glide.with(context).load(list.get(position)).into(holder.imageView);
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
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                byte[] bytes;
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
//                    list.remove(position);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemRemoved(position);
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


//第一次连接将图片保存为文件

//    public void getImageBitmap(final String url, final ViewHolder holder) {
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder()
//                .get()
//                .url(url + width)
//                .build();
//        client.newCall(request).enqueue(new okhttp3.Callback() {
//            @Override
//            public void onFailure(okhttp3.Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
//                String path = url;
//                int position = url.lastIndexOf("/");
//                path = path.substring(position, url.length());
//                String tempPath = Environment.getExternalStorageDirectory() + path;
//                File tempFile = new File(tempPath);
//                FileOutputStream fileOutputStream = null;
//                byte[] buffer = null;
//                try {
//                    try {
//                        if (tempFile.exists()) {
//                            tempFile.delete();
//                        }
//                        tempFile.createNewFile();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    fileOutputStream = new FileOutputStream(tempFile);
//                    buffer = new byte[1024];
//                    int len = 0;
//                    while ((len = response.body().byteStream().read(buffer)) != -1) {
//                        fileOutputStream.write(buffer, 0, len);
//                    }
//                    getFitSampleBitmap(tempPath, holder, url);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    response.body().byteStream().close();
//                    call.cancel();
//                    fileOutputStream.close();
//                    buffer = null;
//                }
//            }
//        });
//    }


    public void getFitSampleBitmap(String file_path, ViewHolder holder, String url) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes;
        BitmapFactory.Options options = new BitmapFactory.Options();
//        BitmapFactory.decodeFile(file_path, options);

        //采样率压缩
//        int inSampleSize = 1;
//        if (options.outWidth > holder.imageView.getWidth() || options.outHeight > holder.imageView.getHeight()) {
//            int widthRatio = Math.round((float) options.outWidth / (float) holder.imageView.getWidth());
//            int heightRatio = Math.round((float) options.outHeight / (float) holder.imageView.getHeight());
//            inSampleSize = Math.min(widthRatio, heightRatio);
//        }
//        options.inSampleSize = 2;

        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(file_path, options);

//        if (bitmap == null) {
////        if(url.equals(error)){
//            try {
//                ImageInfo info = new ImageInfo(file_path);
//                MagickImage magickImage = new MagickImage(info);
//                magickImage.transformRgbImage(ColorspaceType.RGBColorspace);
//                bitmap = MagickBitmap.ToBitmap(magickImage);
//                magickImage.destroyImages();
//            } catch (MagickException e) {
//                e.printStackTrace();
//            }
//        }

        if (bitmap != null && bitmap.getWidth() != 0) {
            //缩放法压缩
            Matrix matrix = new Matrix();
            matrix.setScale(0.5f, 0.5f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        if (bitmap != null && bitmap.getWidth() != 0) {
            //质量压缩
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            bytes = baos.toByteArray();

            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        setImage(holder, bitmap, url, file_path);

        if (baos != null) {
            try {
                baos.close();
                baos = null;
                bytes = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setImage(ViewHolder holder, Bitmap bitmap, String url, String path) {
        if (bitmap != null) {
            File tempFile = new File(path);
            if (tempFile.exists()) {
                tempFile.delete();
            }
            imageCache.addToDiskLruCache(url, bitmap);
            imageCache.addBitmapToCache(url, bitmap);
//            Message msg = handler.obtainMessage();
//            msg.obj = holder;
//            Bundle bundle = new Bundle();
//            bundle.putString("url", url);
//            msg.setData(bundle);
//            handler.sendMessage(msg);
            bitmap = null;
        }
    }
}
