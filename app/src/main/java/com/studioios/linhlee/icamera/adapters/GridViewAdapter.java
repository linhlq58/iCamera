package com.studioios.linhlee.icamera.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.studioios.linhlee.icamera.R;
import com.studioios.linhlee.icamera.utils.GPUImageFilterTools;

/**
 * Created by lequy on 12/24/2016.
 */

public class GridViewAdapter extends BaseAdapter {
    private Context activity;
    private GPUImageFilterTools.FilterList filterList;

    public GridViewAdapter(Context activity, GPUImageFilterTools.FilterList listCountry) {
        super();
        this.filterList = listCountry;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return filterList.filters.size();
    }

    @Override
    public String getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public static class ViewHolder {
        public ImageView imgViewFlag;
        public TextView txtViewTitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder view;
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            view = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_grid, null);

            view.txtViewTitle = (TextView) convertView.findViewById(R.id.textView1);
            view.imgViewFlag = (ImageView) convertView.findViewById(R.id.imageView1);

            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        view.txtViewTitle.setText(filterList.getNames().get(position));
        view.imgViewFlag.setImageResource(filterList.getIntegerList().get(position));

        return convertView;
    }
}
