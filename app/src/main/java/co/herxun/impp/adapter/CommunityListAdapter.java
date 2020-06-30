package co.herxun.impp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.activity.BrowserActivity;
import co.herxun.impp.controller.CommunityManager;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Community;

public class CommunityListAdapter extends BaseAdapter {
    private List<Community> communityList;
    private Context ct;

    public CommunityListAdapter(Context ct, CommunityManager communityManager) {
        this.ct = ct;
        communityList = new ArrayList<Community>();
    }

    public List<Community> getList() {
        return communityList;
    }

    public void applyData(List<Community> communities) {
        communityList.clear();
        communityList.addAll(communities);

        notifyDataSetChanged();
    }

    public void updateItem(int index, Community community) {
        communityList.remove(index);
        communityList.add(index, community);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return communityList.size();
    }

    @Override
    public Community getItem(int position) {
        return communityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommunityListItem view = (CommunityListItem) convertView;
        if (convertView == null) {
            view = new CommunityListItem(parent.getContext());
        }

        view.setData(communityList.get(position));

        return view;
    }

    public class CommunityListItem extends RelativeLayout {
        private CardView cardView;
        private ImageView iv_community;
        private TextView community_name, community_desc;

        public CommunityListItem(Context ct) {
            super(ct);
            inflate(getContext(), R.layout.view_community_item, this);
            cardView = (CardView) findViewById(R.id.cardview);
            iv_community = (ImageView) findViewById(R.id.iv_community);
            community_name = (TextView) findViewById(R.id.community_name);
            community_desc = (TextView) findViewById(R.id.community_desc);
        }

        public void setData(final Community community) {
            ImageLoader.getInstance(ct).DisplayImage(community.communityPhotoUrl, iv_community,
                    R.drawable.annou_default4, false);
            community_name.setText(community.communityName);
            community_desc.setText(community.communityDesc);
            cardView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String url = community.communityUrl;
                    if (url != null && url.length() > 0) {
                        Intent intent = new Intent(ct, BrowserActivity.class);
                        intent.putExtra("browser_name", community.communityName);
                        intent.putExtra("browser_url", community.communityUrl);
                        ct.startActivity(intent);
                        ((Activity) ct).overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                    }
                }
            });
        }
    }
}
