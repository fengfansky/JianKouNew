package co.herxun.impp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.User;

public class AttendedUserListAdapter extends BaseAdapter {
    private List<User> userList;
    private Context ct;

    public AttendedUserListAdapter(Context ct) {
        this.ct = ct;
        userList = new ArrayList<User>();
    }

    public List<User> getList() {
        return userList;
    }

    public void applyData(List<User> users) {
        userList.clear();
        userList.addAll(users);

        notifyDataSetChanged();
    }

    public void updateItem(int index, User user) {
        userList.remove(index);
        userList.add(index, user);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public User getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserListItem view = (UserListItem) convertView;
        if (convertView == null) {
            view = new UserListItem(parent.getContext());
        }

        view.setData(userList.get(position));

        return view;
    }

    public class UserListItem extends RelativeLayout {
        private ImageView iv_user_photo;
        private TextView tv_username;

        public UserListItem(Context ct) {
            super(ct);
            inflate(getContext(), R.layout.view_room_user_item, this);
            iv_user_photo = (ImageView) findViewById(R.id.iv_user_photo);
            tv_username = (TextView) findViewById(R.id.tv_username);
        }

        public void setData(final User user) {
            ImageLoader.getInstance(ct).DisplayImage(user.userPhotoUrl, iv_user_photo, R.drawable.friend_default, true);
            tv_username.setText(user.userName);
        }
    }
}
