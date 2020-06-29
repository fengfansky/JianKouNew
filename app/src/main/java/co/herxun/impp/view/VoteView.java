package co.herxun.impp.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import co.herxun.impp.R;
import co.herxun.impp.activity.VoteListActivity;
import co.herxun.impp.adapter.VoteListAdapter;
import co.herxun.impp.controller.VoteManager;
import co.herxun.impp.controller.VoteManager.FetchVotesCallback;
import co.herxun.impp.model.Vote;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;

public class VoteView extends SwipeRefreshLayout {
    private VoteListAdapter mVoteListAdapter;
    private ListView mListView;
    private FrameLayout headerView;
    private RelativeLayout footer;
    private VoteManager mVoteManager;
    private Map<String, Integer> voteIdIndexMap;
    private Context ct;
    private int fragType = 0;
    private VoteListActivity act;

    private boolean isListViewScrollButtom = false;

    public VoteView(Context context) {
        super(context);
        init(context);
    }

    public VoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context ct) {
        this.ct = ct;
        this.act = (VoteListActivity) ct;

        setColorSchemeColors(ct.getResources().getColor(R.color.no1));
        // setProgressViewOffset(true,mTA.mSr.mResolK.szPDtoPC(-84),mTA.mSr.mResolK.szPDtoPC(104));
        setOnRefreshListener(mOnRefreshListener);

        mListView = new ListView(ct);
        mListView.setDivider(null);
        Drawable drawable = getResources().getDrawable(R.drawable.click_selector);
        mListView.setSelector(drawable);
        mListView.setBackgroundColor(Color.rgb(237, 237, 237));
        addView(mListView);

        headerView = new FrameLayout(ct);
        headerView.setBackgroundColor(0xffff0000);
        headerView.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
        mListView.addHeaderView(headerView);

        footer = new RelativeLayout(ct);
        footer.setLayoutParams(new AbsListView.LayoutParams(-1, Utils.px2Dp(ct, 72)));
        ProgressBar mPb = new ProgressBar(ct);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(ct, 50), Utils.px2Dp(ct, 50));
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
        footer.addView(mPb, rlp);
        mListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (isListViewScrollButtom) {
                        if (mVoteManager.canLoadMore()) {
                            Log.e("onScrollStateChanged", "load more");
                            mVoteManager.loadMore(new FetchVotesCallback() {
                                @Override
                                public void onFailure(String errorMsg) {

                                }

                                @Override
                                public void onFinish(List<Vote> data) {
                                    voteIdIndexMap.clear();
                                    for (int i = 0; i < data.size(); i++) {
                                        voteIdIndexMap.put(data.get(i).voteId, i);
                                    }
                                    mVoteListAdapter.applyData(data);
                                }
                            });
                        } else {
                            if (mListView.getFooterViewsCount() > 0) {
                                mListView.removeFooterView(footer);
                            }
                        }
                    }
                }
            }

            @Override
            public void onScroll(final AbsListView view, int firstVisibleItem, final int visibleItemCount,
                    int totalItemCount) {
                isListViewScrollButtom = firstVisibleItem + visibleItemCount == totalItemCount;
                if (mVoteManager != null)
                    DBug.e("onScroll", mVoteManager.canLoadMore() + "?");
                if (mListView.getFooterViewsCount() == 0 && mVoteManager != null && mVoteManager.canLoadMore()) {
                    mListView.addFooterView(footer);
                }
            }
        });
    }

    public void setHeaderView(View view) {
        headerView.addView(view);
    }

    public void setVoteManager(VoteManager voteManager, int fragType) {
        this.mVoteManager = voteManager;
        this.fragType = fragType;

        mVoteListAdapter = new VoteListAdapter(ct, mVoteManager, fragType);
        mListView.setAdapter(mVoteListAdapter);

        if (fragType == Constant.VOTE_TYPE_ALL) {
            List<Vote> votes = mVoteManager.getLocalVotes();
            mVoteListAdapter.applyData(votes);
        } else if (fragType == Constant.VOTE_TYPE_MINE) {
            List<Vote> votes = mVoteManager.getMyLocalVotes();
            mVoteListAdapter.applyData(votes);
        } else {
            List<Vote> votes = mVoteManager.getJoinLocalVotes();
            mVoteListAdapter.applyData(votes);
        }

        initVoteData();
        // mVoteListAdapter.fillLocalData();
    }

    private void initVoteData() {
        setRefreshing(true);
        voteIdIndexMap = new HashMap<String, Integer>();
        mVoteManager.init(new FetchVotesCallback() {
            @Override
            public void onFailure(String errorMsg) {
                setRefreshing(false);
                DBug.e("mVoteManager.onFailure", errorMsg);

                if (mListView.getFooterViewsCount() > 0) {
                    mListView.removeFooterView(footer);
                }
            }

            @Override
            public void onFinish(List<Vote> data) {
                setRefreshing(false);
                for (int i = 0; i < data.size(); i++) {
                    voteIdIndexMap.put(data.get(i).voteId, i);
                }
                if (!data.isEmpty()) {
                    if (fragType == 1) {
                        act.hasData = true;
                        act.mVoteAllListFragment.setHasData();
                    } else if (fragType == 2) {
                        if (act.hasData) {
                            act.mVoteMineListFragment.setHasData();
                        }
                    } else {
                        if (act.hasData) {
                            act.mVoteJoinListFragment.setHasData();
                        }
                    }
                    mVoteListAdapter.applyData(data);
                } else {
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            if (fragType == 1) {
                                act.hasData = false;
                                act.mVoteAllListFragment.setNoData();
                            } else if (fragType == 2) {
                                if (act.hasData) {
                                    act.mVoteMineListFragment.setHasData();
                                } else {
                                    act.mVoteMineListFragment.setNoData();
                                }
                            } else {
                                if (act.hasData) {
                                    act.mVoteMineListFragment.setHasData();
                                } else {
                                    act.mVoteJoinListFragment.setNoData();
                                }
                            }
                        }
                    });
                }

                if (mListView.getFooterViewsCount() > 0) {
                    mListView.removeFooterView(footer);
                }
            }
        });
    }

    private OnRefreshListener mOnRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            initVoteData();
        }
    };
}
