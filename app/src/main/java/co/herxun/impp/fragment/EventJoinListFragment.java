package co.herxun.impp.fragment;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.activity.EventListActivity;
import co.herxun.impp.controller.EventManager;
import co.herxun.impp.model.Event;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.view.EventView;

public class EventJoinListFragment extends BaseFragment {
    private Context ct;
    private EventView eventView;
    private TextView noEventLabel;

    public EventJoinListFragment() {
        this("");
    }

    public EventJoinListFragment(String title) {
        super(title);
    }

    @Override
    public void onViewShown() {
        initData();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        onViewShown();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_join_event, container, false);
        ct = getActivity();

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(ct);
    }

    private void initView(final Context ct) {
        final EventListActivity act = (EventListActivity) ct;
        eventView = (EventView) act.findViewById(R.id.join_eventView);
        noEventLabel = (TextView) act.findViewById(R.id.noJoinEventLabel);
    }

    public void initData() {
        EventManager eventManager = new EventManager(getActivity(), Constant.VOTE_TYPE_JOIN);
        eventView.setEventManager(eventManager, Constant.VOTE_TYPE_JOIN);
        List<Event> events = eventManager.getLocalEvents();
        if (events.isEmpty()) {
            setNoData();
        } else {
            setHasData();
        }
    }
    
    public void setNoData() {
        eventView.setVisibility(View.GONE);
        noEventLabel.setVisibility(View.VISIBLE);
    }

    public void setHasData() {
        eventView.setVisibility(View.VISIBLE);
        noEventLabel.setVisibility(View.GONE);
    }

}
