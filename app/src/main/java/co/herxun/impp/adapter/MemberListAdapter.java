package co.herxun.impp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.im.model.DeskGroup;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.view.UserListItem;

public class MemberListAdapter extends BaseAdapter {
    private List<DeskGroup> data;
    private Context ct;

    public MemberListAdapter(Context ct) {
        this.ct = ct;
        data = new ArrayList<DeskGroup>();
    }

    public void applyData(List<DeskGroup> requests) {
        data.clear();
        data.addAll(requests);
        notifyDataSetChanged();

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public DeskGroup getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeskGroupListItem view = (DeskGroupListItem) convertView;
        if (convertView == null) {
            view = new DeskGroupListItem(parent.getContext());
        }

        view.setData(getItem(position));

        return view;
    }

    private class DeskGroupListItem extends UserListItem {
        private ImageView iv_user_photo;
        private TextView tv_username;

        public DeskGroupListItem(Context ct) {
            super(ct);
            inflate(getContext(), R.layout.view_cs_user_item, this);
            iv_user_photo = (ImageView) findViewById(R.id.iv_user_photo);
            tv_username = (TextView) findViewById(R.id.tv_username);
//            setLayoutParams(new AbsListView.LayoutParams(-1, Utils.px2Dp(ct, 56)));
        }

        public void setData(DeskGroup group) {
            ImageLoader.getInstance(ct).DisplayImage(group.groupPhotoUrl, iv_user_photo, R.drawable.friend_group, true);
            tv_username.setText(group.groupName);
        }
    }
}