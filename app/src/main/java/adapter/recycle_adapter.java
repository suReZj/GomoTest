package adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import sure.gomotest.R;

/**
 * Created by dell88 on 2018/3/6 0006.
 */

public class recycle_adapter extends RecyclerView.Adapter<recycle_adapter.ViewHolder> {
    private List<String> list;
    private Context context;
    static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.item_image);
            int width = ((Activity) imageView.getContext()).getWindowManager().getDefaultDisplay().getWidth();
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            //设置图片的相对于屏幕的宽高比
            params.width = width/2;
//            params.height =  (int) (200 + Math.random() * 400) ;
            imageView.setLayoutParams(params);
        }
    }

    public recycle_adapter(List<String> list) {
        this.list = list;
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(context).load(list.get(position)).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
