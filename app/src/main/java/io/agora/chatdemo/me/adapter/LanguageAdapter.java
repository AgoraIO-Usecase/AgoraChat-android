package io.agora.chatdemo.me.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import io.agora.chat.Language;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chatdemo.R;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {
    private List<Language> data;
    private SparseBooleanArray selectedItems;
    private int maxSelectionCount;

    public LanguageAdapter(@NonNull Context context, @NonNull List<Language> dataList,int maxSelection) {
        this.data = dataList;
        this.maxSelectionCount = maxSelection;
        this.selectedItems = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.demo_item_language, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Language item = data.get(position);
        holder.textView.setText(item.LanguageLocalName);
        holder.selectIcon.setImageResource(0);

        for (Integer selectedPosition : getSelectedPositions()) {
            if (selectedPosition == position) {
                holder.selectIcon.setImageResource(R.drawable.check_yes);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isSelected = selectedItems.get(holder.getBindingAdapterPosition());
                setSelectedIndex(holder.getBindingAdapterPosition(),!isSelected);
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void refreshData(List<Language> value){
        EaseThreadManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                data = value;
                notifyDataSetChanged();
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView selectIcon;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            selectIcon = itemView.findViewById(R.id.language_select);
            textView = itemView.findViewById(R.id.language_content);
        }
    }

    public List<Integer> getSelectedPositions() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            positions.add(selectedItems.keyAt(i));
        }
        return positions;
    }

    public void setSelectedIndex(int selectedIndex,boolean isSelect){
        if (maxSelectionCount == 1){
            selectedItems.clear();
            if (isSelect){
                selectedItems.put(selectedIndex, true);
            }
        }else {
            if (!isSelect){
                selectedItems.delete(selectedIndex);
            }else {
                if (selectedItems.size() < maxSelectionCount){
                    selectedItems.put(selectedIndex, true);
                }
            }
        }
        EaseThreadManager.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

}
