package com.ottamotta.locator.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ottamotta.locator.R;
import com.ottamotta.locator.actions.LocatorMenuItem;

import java.util.List;

public class ContextMenuAdapter extends ArrayAdapter<LocatorMenuItem> {

    public ContextMenuAdapter(Context context, List<LocatorMenuItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.context_menu_item, null);
        final LocatorMenuItem item = getItem(position);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(item.getTitle());
        if (item.hasImage()) {
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageResource(item.getImageResourceId());
        }
        return convertView;
    }
}
