package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

import bean.mediaBean;
import sure.gomotest.R;


/**
 * Created by dell88 on 2018/3/11 0011.
 */

public class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.ViewHolder>{
    private Context context;
    HashMap<String,List<mediaBean>> allPhotosTemp = new HashMap<>();//所有照片
    private List<String> albumName;

    static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView textView;
        private RelativeLayout layout;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.item_select_image);
            textView=itemView.findViewById(R.id.item_select_textView);
            layout=itemView.findViewById(R.id.item_select_layout);
        }
    }

    public SelectAdapter() {
    }

    public SelectAdapter(HashMap<String, List<mediaBean>> allPhotosTemp, List<String> albumName) {
        this.allPhotosTemp = allPhotosTemp;
        this.albumName = albumName;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.select_recycle_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String path=albumName.get(position);
        int index=path.lastIndexOf("/");
        path=path.substring(index+1,path.length());
        List<mediaBean> data = allPhotosTemp.get(albumName.get(position));
        int sum=data.size();
        holder.textView.setText(path+context.getResources().getString(R.string.head)+sum+context.getResources().getString(R.string.foot));

        String url=data.get(0).getLocalPath();
        Glide.with(context).load(url).into(holder.imageView);

        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickLitener.onItemClick(holder.itemView, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return allPhotosTemp.size();
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }
}
