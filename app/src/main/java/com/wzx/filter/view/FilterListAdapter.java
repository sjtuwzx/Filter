package com.wzx.filter.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.wzx.filter.R;
import com.wzx.filter.library.AllFilterNode;
import com.wzx.filter.library.FilterGroup;
import com.wzx.filter.library.FilterNode;
import com.wzx.filter.library.FilterRoot;
import com.wzx.filter.library.UnlimitedFilterNode;
import com.wzx.filter.tools.ViewUtils;

public class FilterListAdapter extends BaseAdapter {

    private FilterGroup mFilterGroup;
    private List<FilterNode> mChildren = new ArrayList<FilterNode>();
    private FilterTreeView.TreeViewConfig mTreeViewConfig;

    private int mActivePosition = -1;

    private boolean mIndicateSelectState = true;

    private final Context mContext;
    private final LayoutInflater mInflater;

    public FilterListAdapter(Context context) {
        super();
        mContext = context;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setActivePosition(int position) {
        mActivePosition = position;
        notifyDataSetChanged();
    }

    public int getActivePosition() {
        return mActivePosition;
    }

    public void setFilterGroup(FilterGroup group, boolean showUnlimitedNode, boolean indicateSelectState) {
        if (mFilterGroup != group) {
            mActivePosition = -1;
        }
        mFilterGroup = group;
        mIndicateSelectState = indicateSelectState;

        mChildren.clear();
        if (mFilterGroup != null) {
            mTreeViewConfig = mFilterGroup.getTag();
            mChildren = group.getChildren(showUnlimitedNode);
        }
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        if (position < mChildren.size()) {
            return mChildren.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mChildren.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.view_tree_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        FilterNode node = mChildren.get(position);
        boolean isActive = position == mActivePosition;
        String label = node.getDisplayName();
        int maxLen = parent.getMeasuredWidth()
                - parent.getPaddingLeft() - parent.getPaddingRight()
                - ViewUtils.dip2px(mContext, 24);

        viewHolder.mDisplayNameText.setText(label);
        viewHolder.mDisplayNameText.setMaxWidth(maxLen);

        if (!mIndicateSelectState) {
            viewHolder.mSelectIndicator.setVisibility(View.GONE);
            viewHolder.mCheckbox.setVisibility(View.GONE);
        }

        int textColor = R.color.filter_item_text_color;
        LinearLayout container = (LinearLayout) viewHolder.mDisplayNameText.getParent();
        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) container
                .getLayoutParams();
        boolean selected = mIndicateSelectState && node.isSelected();
        FilterTreeView.TreeViewConfig config = mFilterGroup.getTag();
        if (config == null) {
            return convertView;
        }

        if (!node.isLeaf()) {
            if (mIndicateSelectState) {
                viewHolder.mSelectIndicator.setVisibility(View.VISIBLE);
                viewHolder.mSelectIndicator.setSelected(selected);
            }

            param.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            viewHolder.mDisplayNameText.setGravity(Gravity.CENTER);
            container.setGravity(Gravity.CENTER);
            viewHolder.mDisplayNameText.setPadding(0, 0, 0, 0);
            if (mFilterGroup instanceof FilterRoot) {
                textColor = R.color.filter_root_text_color;
            }
            viewHolder.mDisplayNameText.setTextColor(mContext.getResources().getColorStateList(
                    textColor));
            viewHolder.mDisplayNameText.setSelected(isActive);

            viewHolder.mCheckbox.setVisibility(View.GONE);
        } else {
            viewHolder.mSelectIndicator.setVisibility(View.GONE);

            param.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                    RelativeLayout.TRUE);
            viewHolder.mDisplayNameText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            container.setGravity(Gravity.LEFT);
            viewHolder.mDisplayNameText.setPadding(ViewUtils.dip2px(mContext, 11), 0, 0, 0);
            viewHolder.mDisplayNameText.setSelected(selected);

            if (mIndicateSelectState) {
                viewHolder.mCheckbox.setVisibility(View.VISIBLE);
                int checkboxRes = R.drawable.checkbox_selector_new;
                if ((mFilterGroup != null && mFilterGroup.isSingleChoice()) || node instanceof UnlimitedFilterNode
                        || node instanceof AllFilterNode) {
                    checkboxRes = R.drawable.filter_radio_button;
                }
                viewHolder.mCheckbox.setImageResource(checkboxRes);
                viewHolder.mCheckbox.setSelected(selected);
            }
        }
        if (isActive && !node.isLeaf()) {
            convertView.setBackgroundColor(Color.WHITE);
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        int height = mTreeViewConfig.mItemMinHeight;
        if (height > 0) {
            convertView.setMinimumHeight(height);
        }
        return convertView;
    }


    private static class ViewHolder {
        TextView mDisplayNameText;
        ImageView mSelectIndicator;
        ImageView mCheckbox;

        public ViewHolder(View v) {
            mDisplayNameText = (TextView) v.findViewById(R.id.text_name);
            mSelectIndicator = (ImageView) v.findViewById(R.id.icon_selected);
            mCheckbox = (ImageView) v.findViewById(R.id.checkbox);
        }
    }

}
