package co.herxun.impp.view;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.herxun.impp.R;
import co.herxun.impp.activity.BulletinListActivity;
import co.herxun.impp.adapter.BulletinListAdapter;
import co.herxun.impp.controller.BulletinManager;
import co.herxun.impp.controller.BulletinManager.FetchBulletinsCallback;
import co.herxun.impp.model.Bulletin;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;

public class BulletinView extends SwipeRefreshLayout {
    private BulletinListAdapter mBulletinListAdapter;
    private ListView mListView;
    private FrameLayout headerView;
    private RelativeLayout footer;
    private BulletinManager mBulletinManager;
    private Map<String, Integer> bulletinIdIndexMap;
    private Context ct;
    private BulletinListActivity act;

    private boolean isListViewScrollButtom = false;

    public BulletinView(Context context) {
        super(context);
        init(context);
    }

    public BulletinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context ct) {
        this.ct = ct;
        this.act = (BulletinListActivity) ct;

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
                        if (mBulletinManager.canLoadMore()) {
                            Log.e("onScrollStateChanged", "load more");
                            mBulletinManager.loadMore(new FetchBulletinsCallback() {
                                @Override
                                public void onFailure(String errorMsg) {

                                }

                                @Override
                                public void onFinish(List<Bulletin> data) {
                                    bulletinIdIndexMap.clear();
                                    for (int i = 0; i < data.size(); i++) {
                                        bulletinIdIndexMap.put(data.get(i).bulletinId, i);
                                    }
                                    mBulletinListAdapter.applyData(data);
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
                if (mBulletinManager != null)
                    DBug.e("onScroll", mBulletinManager.canLoadMore() + "?");
                if (mListView.getFooterViewsCount() == 0 && mBulletinManager != null && mBulletinManager.canLoadMore()) {
                    mListView.addFooterView(footer);
                }
            }
        });
    }

    public void setHeaderView(View view) {
        headerView.addView(view);
    }

    public void setBulletinManager(BulletinManager bulletinManager) {
        this.mBulletinManager = bulletinManager;

        mBulletinListAdapter = new BulletinListAdapter(ct, mBulletinManager);
        mListView.setAdapter(mBulletinListAdapter);

        List<Bulletin> bulletins = mBulletinManager.getLocalBulletins();
        mBulletinListAdapter.applyData(bulletins);

        initBulletinData();
        // mBulletinListAdapter.fillLocalData();
    }

    public void initBulletinData() {
        setRefreshing(true);
        bulletinIdIndexMap = new HashMap<String, Integer>();
        mBulletinManager.init(new FetchBulletinsCallback() {
            @Override
            public void onFailure(String errorMsg) {
                setRefreshing(false);
                DBug.e("mWallManager.onFailure", errorMsg);

                if (mListView.getFooterViewsCount() > 0) {
                    mListView.removeFooterView(footer);
                }
            }

            @Override
            public void onFinish(List<Bulletin> data) {
                setRefreshing(false);
                for (int i = 0; i < data.size(); i++) {
                    bulletinIdIndexMap.put(data.get(i).bulletinId, i);
                }
                if (!data.isEmpty()) {
                    act.setHasData();
                    mBulletinListAdapter.applyData(data);
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
            initBulletinData();
        }
    };
}
