package io.agora.chatdemo.group.adapter;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.databinding.EaseItemRowThreadListBinding;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chatdemo.R;
import io.agora.chatdemo.databinding.ItemHomeHeaderMenuBinding;

public class HomeHeaderMenuAdapter extends EaseBaseRecyclerViewAdapter<Pair<Integer, Integer>> {
    @Override
    public ViewHolder<Pair<Integer, Integer>> getViewHolder(ViewGroup parent, int viewType) {
        return new HomeMenuViewHolder(ItemHomeHeaderMenuBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public int getEmptyLayoutId() {
        return io.agora.chat.uikit.R.layout.ease_layout_no_data_show_nothing;
    }

    private class HomeMenuViewHolder extends ViewHolder<Pair<Integer, Integer>> {
        private ItemHomeHeaderMenuBinding binding;

        public HomeMenuViewHolder(@NonNull ItemHomeHeaderMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void setData(Pair<Integer, Integer> item, int position) {
            Pair<Integer, Integer> pair = mData.get(position);
            binding.itemMenu.setAvatar(pair.first);
            binding.itemMenu.setTitle(mContext.getString(pair.second));
            binding.tvContactTitle.setVisibility(View.GONE);

            if(position == mData.size() - 1) {
                binding.tvContactTitle.setVisibility(View.VISIBLE);
            }
        }
    }
}
