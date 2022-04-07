package io.agora.chatdemo.me;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.databinding.ItemAvatarSelectBinding;
import io.agora.chatdemo.general.utils.UIUtils;

public class AvatarSelectAdapter extends EaseBaseRecyclerViewAdapter<Integer> {
    @Override
    public ViewHolder<Integer> getViewHolder(ViewGroup parent, int viewType) {

        return new AvatarViewHolder(ItemAvatarSelectBinding.inflate(LayoutInflater.from(mContext), parent, false));
    }
    class AvatarViewHolder extends ViewHolder<Integer>{
        private ItemAvatarSelectBinding mBinding;
        private final RequestOptions requestOptions;

        public AvatarViewHolder(@NonNull  ItemAvatarSelectBinding binding) {
            super( binding.getRoot());
            this.mBinding=binding;
            RoundedCorners roundedCorners = new RoundedCorners(UIUtils.dp2px(mContext, 5));
            requestOptions = new RequestOptions().transform(new FitCenter(), roundedCorners).placeholder(R.drawable.avatar_1);
        }

        @Override
        public void setData(Integer item, int position) {
           Glide.with(mContext)
                    .load(item)
                    .apply(requestOptions)
                    .error(R.drawable.avatar_1)
                    .into(mBinding.ivUserAvatar);
        }
    }
}
