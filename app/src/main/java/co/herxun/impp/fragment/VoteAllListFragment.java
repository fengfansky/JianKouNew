package co.herxun.impp.fragment;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.activity.VoteListActivity;
import co.herxun.impp.controller.VoteManager;
import co.herxun.impp.model.Vote;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.view.VoteView;

public class VoteAllListFragment extends BaseFragment {
    private Context ct;
    private VoteView voteView;
    private TextView noVoteLabel;

    public VoteAllListFragment() {
        this("");
    }

    public VoteAllListFragment(String title) {
        super(title);
    }

    @Override
    public void onViewShown() {
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_vote, container, false);
        ct = getActivity();

        return rootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        onViewShown();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(ct);
    }

    private void initView(final Context ct) {
        final VoteListActivity act = (VoteListActivity) ct;
        voteView = (VoteView) act.findViewById(R.id.all_voteView);
        noVoteLabel = (TextView) act.findViewById(R.id.noAllVoteLabel);
    }

    public void initData() {
        VoteManager voteManager = new VoteManager(getActivity(), Constant.VOTE_TYPE_ALL);
        voteView.setVoteManager(voteManager, Constant.VOTE_TYPE_ALL);
        List<Vote> votes = voteManager.getLocalVotes();
        if (votes.isEmpty()) {
            setNoData();
        } else {
            setHasData();
        }
    }
    
    public void setNoData() {
        voteView.setVisibility(View.GONE);
        noVoteLabel.setVisibility(View.VISIBLE);
    }
    
    public void setHasData() {
        voteView.setVisibility(View.VISIBLE);
        noVoteLabel.setVisibility(View.GONE);
    }

}
