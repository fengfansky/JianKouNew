package co.herxun.impp.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.activity.BulletinBrowserActivity;
import co.herxun.impp.controller.BulletinManager;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Bulletin;
import co.herxun.impp.utils.Utils;

public class BulletinListAdapter extends BaseAdapter {
    private List<Bulletin> bulletinList;
    private Context ct;

    public BulletinListAdapter(Context ct, BulletinManager bulletinManager) {
        this.ct = ct;
        bulletinList = new ArrayList<Bulletin>();
    }

    public List<Bulletin> getList() {
        return bulletinList;
    }

    public void applyData(List<Bulletin> communities) {
        bulletinList.clear();
        bulletinList.addAll(communities);

        notifyDataSetChanged();
    }

    public void updateItem(int index, Bulletin bulletin) {
        bulletinList.remove(index);
        bulletinList.add(index, bulletin);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return bulletinList.size();
    }

    @Override
    public Bulletin getItem(int position) {
        return bulletinList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BulletinListItem view = (BulletinListItem) convertView;
        if (convertView == null) {
            view = new BulletinListItem(parent.getContext());
        }

        view.setData(bulletinList.get(position));

        return view;
    }

    public class BulletinListItem extends RelativeLayout {
        private CardView cardView;
        private ImageView iv_bulletin;
        private TextView bulletin_title, bulletin_date, bulletin_description;

        public BulletinListItem(Context ct) {
            super(ct);
            inflate(getContext(), R.layout.view_bulletin_item, this);
            cardView = (CardView) findViewById(R.id.cardview);
            iv_bulletin = (ImageView) findViewById(R.id.iv_bulletin);
            bulletin_title = (TextView) findViewById(R.id.bulletin_title);
            bulletin_date = (TextView) findViewById(R.id.bulletin_date);
            bulletin_description = (TextView) findViewById(R.id.bulletin_description);
        }

        public void setData(final Bulletin bulletin) {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            int newWidth = width - Utils.px2Dp(ct, 20);
            int newHeight = newWidth / 2;
            iv_bulletin.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
            ImageLoader.getInstance(ct).DisplayImage(bulletin.bulletinCoverUrl, iv_bulletin, R.drawable.annou_default3,
                    false);
            bulletin_description.setText(bulletin.bullectinDescription);
            bulletin_title.setText(bulletin.bulletinTitle);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(bulletin.createdAt);
            bulletin_date.setText(new SimpleDateFormat("yyyy-MM-dd kk:mm").format(c.getTime()));
            cardView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String content = bulletin.bulletinContent;
                    if (content != null && content.length() > 0) {
                        Intent intent = new Intent(ct, BulletinBrowserActivity.class);
                        intent.putExtra("bulletinId", bulletin.bulletinId);
                        ct.startActivity(intent);
                        ((Activity) ct).overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                    }
                }
            });
        }
    }
}
