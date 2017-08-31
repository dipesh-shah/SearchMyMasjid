package com.digitaljalebi.searchmymasjid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by dipesh on 25/10/16.
 */

public class CityAreaExpandableListAdapter<String> extends BaseExpandableListAdapter {

    private List<String>  mHeaders;
    private Map<String, List<Areas.Area>> mMapping;
    private Context mContext;
    private LayoutInflater mInflater;

    public CityAreaExpandableListAdapter(Context context, List<String> headers, Map<String, List<Areas.Area>> mapping) {
        mHeaders = headers;
        mMapping = mapping;
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return mHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (mMapping != null) {
            return mMapping.get(mHeaders.get(groupPosition)).size();
        }
        return 0;
    }

    @Override
    public String getGroup(int groupPosition) {
        return mHeaders.get(groupPosition);
    }

    @Override
    public Areas.Area getChild(int groupPosition, int childPosition) {
        if (mMapping != null) {
            return mMapping.get(mHeaders.get(groupPosition)).get(childPosition);
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupName = getGroup(groupPosition);
        TextView textView = (TextView) mInflater.inflate(R.layout.item_header_layout, parent, false);
        textView.setText("" + groupName);
        return textView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Areas.Area childName = getChild(groupPosition, childPosition);
        View view =  mInflater.inflate(R.layout.item_child_layout, parent, false);
        TextView areaName = (TextView)view.findViewById(R.id.txt_area_name);
        TextView pincodeName = (TextView)view.findViewById(R.id.txt_area_pincode);
        areaName.setText("" + childName.name);
        pincodeName.setText("" + childName.pincode);
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
