package co.herxun.impp.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
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
import co.herxun.impp.activity.CommunityListActivity;
import co.herxun.impp.adapter.CommunityListAdapter;
import co.herxun.impp.controller.CommunityManager;
import co.herxun.impp.controller.CommunityManager.FetchCommunitiesCallback;
import co.herxun.impp.model.Community;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;

public class CommunityView extends SwipeRefreshLayout {
    private CommunityListAdapter mCommunityListAdapter;
    private ListView mListView;
    private FrameLayout headerView;
    private RelativeLayout footer;
    private CommunityManager mCommunityManager;
    private Map<String, Integer> communityIdIndexMap;
    private Context ct;
    private CommunityListActivity act;

    private boolean isListViewScrollButtom = false;

    public CommunityView(Context context) {
        super(context);
        init(context);
    }

    public CommunityView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context ct) {
        this.ct = ct;
        this.act = (CommunityListActivity) ct;

        setColorSchemeColors(ct.getResources().getColor(R.color.no1));
        // setProgressViewOffset(true,mTA.mSr.mResolK.szPDtoPC(-84),mTA.mSr.mResolK.szPDtoPC(104));
        setOnRefreshListener(mOnRefreshListener);

        mListView = new ListView(ct);
        mListView.setDivider(null);
        Drawable drawable = getResources().getDrawable(R.drawable.click_selector);
        mListView.setSelector(drawable);
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
                        if (mCommunityManager.canLoadMore()) {
                            Log.e("onScrollStateChanged", "load more");
                            mCommunityManager.loadMore(new FetchCommunitiesCallback() {
                                @Override
                                public void onFailure(String errorMsg) {

                                }

                                @Override
                                public void onFinish(List<Community> data) {
                                    communityIdIndexMap.clear();
                                    for (int i = 0; i < data.size(); i++) {
                                        communityIdIndexMap.put(data.get(i).communityId, i);
                                    }
                                    mCommunityListAdapter.applyData(data);
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
                if (mCommunityManager != null)
                    DBug.e("onScroll", mCommunityManager.canLoadMore() + "?");
                if (mListView.getFooterViewsCount() == 0 && mCommunityManager != null
                        && mCommunityManager.canLoadMore()) {
                    mListView.addFooterView(footer);
                }
            }
        });
    }

    public void setHeaderView(View view) {
        headerView.addView(view);
    }

    public void setCommunityManager(CommunityManager communityManager) {
        this.mCommunityManager = communityManager;

        mCommunityListAdapter = new CommunityListAdapter(ct, mCommunityManager);
        mListView.setAdapter(mCommunityListAdapter);

        List<Community> communities = mCommunityManager.getLocalCommunities();
        mCommunityListAdapter.applyData(communities);

        initCommunityData();
        // mCommunityListAdapter.fillLocalData();
    }

    public void initCommunityData() {
        setRefreshing(true);
        communityIdIndexMap = new HashMap<String, Integer>();
        mCommunityManager.init(new FetchCommunitiesCallback() {
            @Override
            public void onFailure(String errorMsg) {
                setRefreshing(false);
                DBug.e("mWallManager.onFailure", errorMsg);

                if (mListView.getFooterViewsCount() > 0) {
                    mListView.removeFooterView(footer);
                }
            }

            @Override
            public void onFinish(List<Community> data) {
                setRefreshing(false);
                for (int i = 0; i < data.size(); i++) {
                    communityIdIndexMap.put(data.get(i).communityId, i);
                }
                if (!data.isEmpty()) {
                    act.setHasData();
                    mCommunityListAdapter.applyData(data);
                } else {
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            act.setNoData();
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
            initCommunityData();
        }
    };
}
