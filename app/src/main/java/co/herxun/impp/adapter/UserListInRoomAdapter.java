package co.herxun.impp.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.imageloader.MemoryCache;
import co.herxun.impp.model.User;

public class UserListInRoomAdapter extends ArrayAdapter {

    private List<User> data;
    private Context context;

    MemoryCache memoryCache = new MemoryCache();

    public UserListInRoomAdapter(Context context, List<User> data) {
        super(context, -1, data);
        this.context = context;
        this.data = data;
    }

    public void applyData(List<User> userList) {
        data.clear();
        data.addAll(userList);

        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.view_room_user_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tv_username = (TextView) view.findViewById(R.id.tv_username);
            viewHolder.iv_user_photo = (ImageView) view.findViewById(R.id.iv_user_photo);
            view.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.tv_username.setText(data.get(position).userName);
        ImageLoader.getInstance(getContext()).DisplayImage(data.get(position).userPhotoUrl, viewHolder.iv_user_photo,
                R.drawable.friend_default, true);
        return view;
    }

    class ViewHolder {
        private TextView tv_username;
        private ImageView iv_user_photo;
    }

}
