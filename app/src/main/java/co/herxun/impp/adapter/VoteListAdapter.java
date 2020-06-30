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
import co.herxun.impp.activity.VoteDetailActivity;
import co.herxun.impp.controller.VoteManager;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Vote;
import co.herxun.impp.utils.Constant;

public class VoteListAdapter extends BaseAdapter {
    private List<Vote> voteList;
    private Context ct;
    private int fragType = 0;

    public VoteListAdapter(Context ct, VoteManager voteManager, int fragType) {
        this.ct = ct;
        voteList = new ArrayList<Vote>();
        this.fragType = fragType;
    }

    public List<Vote> getList() {
        return voteList;
    }

    public void applyData(List<Vote> votes) {
        voteList.clear();
        voteList.addAll(votes);

        notifyDataSetChanged();
    }

    public void updateItem(int index, Vote vote) {
        voteList.remove(index);
        voteList.add(index, vote);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return voteList.size();
    }

    @Override
    public Vote getItem(int position) {
        return voteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VoteListItem view = (VoteListItem) convertView;
        if (convertView == null) {
            view = new VoteListItem(parent.getContext());
        }

        view.setData(voteList.get(position));

        return view;
    }

    public class VoteListItem extends RelativeLayout {
        private ImageView iv_vote;
        private CardView cardview;
        private TextView vote_title, vote_choice_type, vote_attend_users, vote_count;

        public VoteListItem(Context ct) {
            super(ct);
            inflate(getContext(), R.layout.view_vote_item, this);
            iv_vote = (ImageView) findViewById(R.id.iv_vote);
            vote_title = (TextView) findViewById(R.id.vote_title);
            vote_choice_type = (TextView) findViewById(R.id.vote_choice_type);
            vote_attend_users = (TextView) findViewById(R.id.vote_attend_users);
            vote_count = (TextView) findViewById(R.id.vote_count);
            cardview = (CardView) findViewById(R.id.cardview);
        }

        public void setData(final Vote vote) {
            if (vote.voteType.equals(Constant.VOTE_TYPE_SINGLE)) {
                vote_choice_type.setText(ct.getResources().getString(R.string.annou_vote_create_single));
            } else {
                vote_choice_type.setText(ct.getResources().getString(R.string.annou_vote_create_multiple));
            }
            // 票数
            int voteCount = 0;
            if (vote.voteResults.indexOf(",") != -1) {
                String[] voteResultsArray = vote.voteResults.split(",");
                for (int i = 0; i < voteResultsArray.length; i++) {
                    voteCount = voteCount + Integer.parseInt(voteResultsArray[i].split(":")[1]);
                }
            } else {
                voteCount = voteCount + Integer.parseInt(vote.voteResults.split(":")[1]);
            }
            if (voteCount > 1) {
                vote_count.setText("" + voteCount);
            } else {
                vote_count.setText("" + voteCount);
            }

            if (vote.votedUserCount > 1) {
                vote_attend_users.setText("" + vote.votedUserCount);
            } else {
                vote_attend_users.setText("" + vote.votedUserCount);
            }

            ImageLoader.getInstance(ct).DisplayImage(vote.votePhotoUrl, iv_vote, R.drawable.annou_default4, false);
            vote_title.setText(vote.voteTitle);

            cardview.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ct, VoteDetailActivity.class);
                    intent.putExtra("frag_type", fragType);
                    intent.putExtra("vote_id", vote.voteId);
                    ct.startActivity(intent);
                    ((Activity) ct).overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                }
            });
        }
    }
}
