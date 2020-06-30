package co.herxun.impp.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import co.herxun.impp.R;
import co.herxun.impp.adapter.FragmentPagerAdapter;
import co.herxun.impp.fragment.BaseFragment;
import co.herxun.impp.fragment.EventAllListFragment;
import co.herxun.impp.fragment.EventJoinListFragment;
import co.herxun.impp.fragment.EventMineListFragment;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.SlidingTabLayout;

public class EventListActivity extends BaseActivity {
    public EventAllListFragment mEventAllListFragment;
    public EventMineListFragment mEventMineListFragment;
    public EventJoinListFragment mEventJoinListFragment;
    public ViewPager mViewPager;
    private List<BaseFragment> fragList;
    private SlidingTabLayout mSlidingTabLayout;
    private AppBar appbar;
    private ImageView addBtn;
    public boolean hasData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        initView();
    }

    private void initView() {
        appbar = (AppBar) findViewById(R.id.community_app_bar);
        appbar.getLogoView().setImageResource(R.drawable.menu_back);
        appbar.getLogoView().setLayoutParams(
                new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56)));
        appbar.getLogoView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        appbar.getTextView().setVisibility(View.VISIBLE);
        appbar.getTextView().setText(R.string.annou_event_list_name);

        mEventAllListFragment = new EventAllListFragment(getString(R.string.annou_event_tab_title_all));
        mEventMineListFragment = new EventMineListFragment(getString(R.string.annou_event_tab_title_mine));
        mEventJoinListFragment = new EventJoinListFragment(getString(R.string.annou_event_tab_title_join));
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        fragList = new ArrayList<BaseFragment>();
        fragList.add(mEventAllListFragment);
        fragList.add(mEventMineListFragment);
        fragList.add(mEventJoinListFragment);
        FragmentPagerAdapter mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), fragList);
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.no13);
            }
        });
        mSlidingTabLayout.setOnPageChangeListener(mOnPageChangeListener);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mOnPageChangeListener.onPageSelected(0);
            }
        }, 300);

        addBtn = (ImageView) findViewById(R.id.wall_addBtn);
        addBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CreateEventActivity.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            }
        });
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        public void onPageScrollStateChanged(int arg0) {
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int location) {
            BaseFragment frag = fragList.get(location);
            frag.onViewShown();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // if (mOnPageChangeListener != null) {
        // mOnPageChangeListener.onPageSelected(mViewPager.getCurrentItem());
        // }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
