package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

import bean.MediaBean;
import sure.gomotest.R;

import static util.Contants.foot;
import static util.Contants.head;

/**
 * Created by dell88 on 2018/3/11 0011.
 */

public class select_recycle_adapter extends RecyclerView.Adapter<select_recycle_adapter.ViewHolder>{
    private Context context;
    HashMap<String,List<MediaBean>> allPhotosTemp = new HashMap<>();//所有照片
    private List<String> albumName;

    static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView textView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.item_select_image);
            textView=itemView.findViewById(R.id.item_select_textView);
        }
    }

    public select_recycle_adapter(HashMap<String, List<MediaBean>> allPhotosTemp, List<String> albumName) {
        this.allPhotosTemp = allPhotosTemp;
        this.albumName = albumName;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_select, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String path=albumName.get(position);
        int index=path.lastIndexOf("/");
        path=path.substring(index+1,path.length());
        List<MediaBean> data = allPhotosTemp.get(albumName.get(position));
        int sum=data.size();
        holder.textView.setText(path+head+sum+foot);

        String url=data.get(0).getLocalPath();
        Glide.with(context).load(url).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return allPhotosTemp.size();
    }
}
