package io.agora.chatdemo.me;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import io.agora.chat.Language;
import io.agora.chatdemo.R;

public class DoNotDisturbAdapter extends ArrayAdapter<DoNotDisturbAdapter.SelectItem> {
    private long mSelectedIndex = 0;

    public DoNotDisturbAdapter(@NonNull Context context, @NonNull List<SelectItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertedView, ViewGroup parent) {
        if(convertedView == null) {
            convertedView = LayoutInflater.from(getContext()).inflate(R.layout.select_item, parent, false);
        }

        TextView textView = (TextView) convertedView.findViewById(R.id.select_content);
        RadioButton button = (RadioButton) convertedView.findViewById(R.id.select_button);

        SelectItem item = getItem(position);
        textView.setText(item.content);
        long id = getItemId(position);
        if (mSelectedIndex == id) {
            button.setChecked(true);
        }else {
            button.setChecked(false);
        }

        return convertedView;
    }

    public void setSelectedIndex(long index) {
        mSelectedIndex = index;
    }

    public long getSelectedIndex() {
        return mSelectedIndex;
    }

    public static class SelectItem{
        String content;
        int duration;
        public SelectItem(String content, int duration){
            this.content = content;
            this.duration = duration;
        }

    }
}

