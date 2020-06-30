package co.herxun.impp.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import co.herxun.impp.R;
import co.herxun.impp.activity.EventListActivity;
import co.herxun.impp.adapter.EventListAdapter;
import co.herxun.impp.controller.EventManager;
import co.herxun.impp.controller.EventManager.FetchEventsCallback;
import co.herxun.impp.model.Event;
import co.herxun.impp.model.EventItem;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;

public class EventView extends SwipeRefreshLayout {
    private EventListAdapter mEventListAdapter;
    private ListView mListView;
    private FrameLayout headerView;
    private RelativeLayout footer;
    private EventManager mEventManager;
    private Map<String, Integer> eventIdIndexMap;
    private Context ct;
    private int fragType = 0;
    private EventListActivity act;

    private boolean isListViewScrollButtom = false;

    public EventView(Context context) {
        super(context);
        init(context);
    }

    public EventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context ct) {
        this.ct = ct;
        this.act = (EventListActivity) ct;

        setColorSchemeColors(ct.getResources().getColor(R.color.no1));
        // setProgressViewOffset(true,mTA.mSr.mResolK.szPDtoPC(-84),mTA.mSr.mResolK.szPDtoPC(104));
        setOnRefreshListener(mOnRefreshListener);

        mListView = new ListView(ct);
        mListView.setDivider(null);
        Drawable drawable = getResources().getDrawable(R.drawable.click_selector);
        mListView.setSelector(drawable);
        addView(mListView);
        mListView.setBackgroundColor(Color.rgb(237, 237, 237));
        //
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
                        if (mEventManager.canLoadMore()) {
                            Log.e("onScrollStateChanged", "load more");
                            mEventManager.loadMore(new FetchEventsCallback() {
                                @Override
                                public void onFailure(String errorMsg) {

                                }

                                @Override
                                public void onFinish(List<Event> data) {
                                    eventIdIndexMap.clear();
                                    List<EventItem> eventItems = new ArrayList<EventItem>();
                                    EventItem item = null;
                                    for (int i = 0; i < data.size(); i++) {
                                        eventIdIndexMap.put(data.get(i).eventId, i);
                                        if (i % 2 == 0) {
                                            item = new EventItem();
                                            item.setLeftEvent(data.get(i));
                                            if (i == data.size() - 1) {
                                                eventItems.add(item);
                                            }
                                        } else {
                                            item.setRightEvent(data.get(i));
                                            eventItems.add(item);
                                        }

                                    }
                                    mEventListAdapter.applyData(eventItems);
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
                if (mEventManager != null)
                    DBug.e("onScroll", mEventManager.canLoadMore() + "?");
                if (mListView.getFooterViewsCount() == 0 && mEventManager != null && mEventManager.canLoadMore()) {
                    mListView.addFooterView(footer);
                }
            }
        });

        // mListView.setOnItemClickListener(new OnItemClickListener() {
        //
        // @Override
        // public void onItemClick(AdapterView<?> parent, View view, int
        // position, long id) {
        // Event event = mEventListAdapter.getList().get(position - 1);
        // Intent intent = new Intent(ct, EventDetailActivity.class);
        // intent.putExtra("frag_type", fragType);
        // intent.putExtra("event_id", event.eventId);
        // ct.startActivity(intent);
        // ((Activity) ct).overridePendingTransition(R.anim.slide_in_right,
        // android.R.anim.fade_out);
        // }
        // });
    }

    public void setHeaderView(View view) {
        headerView.addView(view);
    }

    public void setEventManager(EventManager eventManager, int fragType) {
        this.mEventManager = eventManager;
        this.fragType = fragType;
        eventIdIndexMap = new HashMap<String, Integer>();
        mEventListAdapter = new EventListAdapter(ct, mEventManager);
        mListView.setAdapter(mEventListAdapter);

        if (fragType == Constant.VOTE_TYPE_ALL) {
            List<Event> data = mEventManager.getLocalEvents();
            List<EventItem> eventItems = new ArrayList<EventItem>();
            EventItem item = null;
            for (int i = 0; i < data.size(); i++) {
                eventIdIndexMap.put(data.get(i).eventId, i);
                if (i % 2 == 0) {
                    item = new EventItem();
                    item.setLeftEvent(data.get(i));
                    if (i == data.size() - 1) {
                        eventItems.add(item);
                    }
                } else {
                    item.setRightEvent(data.get(i));
                    eventItems.add(item);
                }

            }
            mEventListAdapter.applyData(eventItems);
        } else if (fragType == Constant.VOTE_TYPE_MINE) {
            List<Event> data = mEventManager.getMyLocalEvents();
            List<EventItem> eventItems = new ArrayList<EventItem>();
            EventItem item = null;
            for (int i = 0; i < data.size(); i++) {
                eventIdIndexMap.put(data.get(i).eventId, i);
                if (i % 2 == 0) {
                    item = new EventItem();
                    item.setLeftEvent(data.get(i));
                    if (i == data.size() - 1) {
                        eventItems.add(item);
                    }
                } else {
                    item.setRightEvent(data.get(i));
                    eventItems.add(item);
                }

            }
            mEventListAdapter.applyData(eventItems);
        } else {
            List<Event> data = mEventManager.getJoinLocalEvents();
            List<EventItem> eventItems = new ArrayList<EventItem>();
            EventItem item = null;
            for (int i = 0; i < data.size(); i++) {
                eventIdIndexMap.put(data.get(i).eventId, i);
                if (i % 2 == 0) {
                    item = new EventItem();
                    item.setLeftEvent(data.get(i));
                    if (i == data.size() - 1) {
                        eventItems.add(item);
                    }
                } else {
                    item.setRightEvent(data.get(i));
                    eventItems.add(item);
                }

            }
            mEventListAdapter.applyData(eventItems);
        }

        initEventData();
        // mEventListAdapter.fillLocalData();
    }

    private void initEventData() {
        setRefreshing(true);

        mEventManager.init(new FetchEventsCallback() {
            @Override
            public void onFailure(String errorMsg) {
                setRefreshing(false);
                DBug.e("mEventManager.onFailure", errorMsg);

                if (mListView.getFooterViewsCount() > 0) {
                    mListView.removeFooterView(footer);
                }
            }

            @Override
            public void onFinish(List<Event> data) {
                setRefreshing(false);
                if (!data.isEmpty()) {
                    if (fragType == 1) {
                        act.hasData = true;
                        act.mEventAllListFragment.setHasData();
                    } else if (fragType == 2) {
                        if (act.hasData) {
                            act.mEventMineListFragment.setHasData();
                        }
                    } else {
                        if (act.hasData) {
                            act.mEventJoinListFragment.setHasData();
                        }
                    }
                    List<EventItem> eventItems = new ArrayList<EventItem>();
                    EventItem item = null;
                    for (int i = 0; i < data.size(); i++) {
                        eventIdIndexMap.put(data.get(i).eventId, i);
                        if (i % 2 == 0) {
                            item = new EventItem();
                            item.setLeftEvent(data.get(i));
                            if (i == data.size() - 1) {
                                eventItems.add(item);
                            }
                        } else {
                            item.setRightEvent(data.get(i));
                            eventItems.add(item);
                        }

                    }
                    mEventListAdapter.applyData(eventItems);
                } else {
                    act.runOnUiThread(new Runnable() {
                        public void run() {
                            if (fragType == 1) {
                                act.hasData = false;
                                act.mEventAllListFragment.setNoData();
                            } else if (fragType == 2) {
                                if (act.hasData) {
                                    act.mEventMineListFragment.setHasData();
                                } else {
                                    act.mEventMineListFragment.setNoData();
                                }
                            } else {
                                if (act.hasData) {
                                    act.mEventJoinListFragment.setHasData();
                                } else {
                                    act.mEventJoinListFragment.setNoData();
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
            initEventData();
        }
    };
}
