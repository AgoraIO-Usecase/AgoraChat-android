package io.agora.chatdemo.group.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import io.agora.chat.MucSharedFile;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.provider.EaseFileIconProvider;
import io.agora.chatdemo.R;
import io.agora.util.TextFormater;

public class GroupFilesAdapter extends EaseBaseRecyclerViewAdapter<MucSharedFile> {

    private OnListItemClickListener onItemClickListener;

    public void setOnListItemClickListener(OnListItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public FileViewHolder getViewHolder(ViewGroup parent, int viewType) {
        FileViewHolder viewHolder = new FileViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_item_group_file_row, parent, false));
        viewHolder.setContext(mContext);
        viewHolder.setListener(onItemClickListener);
        return viewHolder;
    }

    private static class FileViewHolder extends ViewHolder<MucSharedFile> {
        private Context context;
        private OnListItemClickListener listener;

        private ConstraintLayout itemLayout;
        private ImageView fileIcon;
        private TextView tvFileName;
        private TextView tvFileDesc;
        private TextView deleteTv;

        public void setContext(Context context) {
            this.context = context;
        }

        public void setListener(OnListItemClickListener listener) {
            this.listener = listener;
        }

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            itemLayout = findViewById(R.id.item_layout);
            fileIcon = findViewById(R.id.iv_file_icon);
            tvFileName = findViewById(R.id.tv_file_name);
            tvFileDesc = findViewById(R.id.tv_file_desc);
            deleteTv = findViewById(R.id.txt_delete);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onItemClick(v, getBindingAdapterPosition());
                    }
                }
            });
            deleteTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onDeleteClick(v, getBindingAdapterPosition());
                    }
                }
            });
        }

        @Override
        public void setData(MucSharedFile item, int position) {
            EaseFileIconProvider provider = EaseUIKit.getInstance().getFileIconProvider();
            if (provider != null) {
                Drawable icon = provider.getFileIcon(item.getFileName());
                if (icon != null) {
                    fileIcon.setImageDrawable(icon);
                }
            }

            tvFileName.setText(item.getFileName());
            tvFileDesc.setText(String.format(context.getResources().getString(R.string.group_file_desc), item.getFileOwner(), TextFormater.getDataSize(item.getFileSize())));
        }
    }

    public interface OnListItemClickListener {
        void onItemClick(View view, int position);

        void onDeleteClick(View view, int position);
    }
}
