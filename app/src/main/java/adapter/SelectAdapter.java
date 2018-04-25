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

import bean.MediaBean;
import sure.gomotest.R;


/**
 * Created by dell88 on 2018/3/11 0011.
 * 用于选择相册页的recyclerview的adapter
 */

public class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.ViewHolder> {
    private Context mContext;
    private HashMap<String, List<MediaBean>> mAllPhoto = new HashMap<>();
    private List<String> mAlbumName;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.item_select_image);
            mTextView = itemView.findViewById(R.id.item_select_textView);
        }
    }

    public SelectAdapter() {
    }

    public SelectAdapter(HashMap<String, List<MediaBean>> allPhotosTemp, List<String> albumName) {
        this.mAllPhoto = allPhotosTemp;
        this.mAlbumName = albumName;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.select_recycle_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String path = mAlbumName.get(position);
        int index = path.lastIndexOf("/");
        path = path.substring(index + 1, path.length());
        List<MediaBean> data = mAllPhoto.get(mAlbumName.get(position));
        int sum = data.size();
        holder.mTextView.setText(path + mContext.getResources().getString(R.string.select_adapter_head_brackets) + sum + mContext.getResources().getString(R.string.select_adapter_foot_brackets));

        String url = data.get(0).getLocalPath();
        Glide.with(mContext).load(url).into(holder.mImageView);

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
        return mAllPhoto.size();
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
