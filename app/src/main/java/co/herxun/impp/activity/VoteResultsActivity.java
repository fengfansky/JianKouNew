package co.herxun.impp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.herxun.impp.R;
import co.herxun.impp.adapter.VoteResultsChoiceListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.VoteManager;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Choice;
import co.herxun.impp.model.Vote;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.ListViewForScrollView;

public class VoteResultsActivity extends BaseActivity {
    private AppBar appbar;
    private Vote vote;
    private ImageView ivVotePhoto, iv_user_photo;
    private TextView tvVoteUsername, tvVoteDate, tvVoteDesc, tvVoteType, tv_vote_title;
    private ListViewForScrollView listviewVoteChocie;
    private LinearLayout llVote;
    private String fragType = "";
    private PieChart pieChart;
    private ScrollView svVote;
    private Map<Integer, String> chocieMap = new HashMap<Integer, String>();
    private List<Integer> colorsTemplate = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_results);
        checkBundle();
        initView();
        initData();
    }

    private void initView() {
        svVote = (ScrollView) findViewById(R.id.sv_vote);
        svVote.smoothScrollTo(0, 0);
        appbar = (AppBar) findViewById(R.id.wall_app_bar);
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
        appbar.getTextView().setText(vote.voteTitle);
        appbar.getMenuItemView().setImageResource(R.drawable.menu_chat);

        ivVotePhoto = (ImageView) findViewById(R.id.iv_vote_photo);
        iv_user_photo = (ImageView) findViewById(R.id.iv_user_photo);
        tvVoteUsername = (TextView) findViewById(R.id.tv_vote_username);
        tv_vote_title = (TextView) findViewById(R.id.tv_vote_title);
        tvVoteDate = (TextView) findViewById(R.id.tv_vote_date);
        tvVoteDesc = (TextView) findViewById(R.id.tv_vote_desc);
        tvVoteType = (TextView) findViewById(R.id.tv_vote_type);
        listviewVoteChocie = (ListViewForScrollView) findViewById(R.id.listview_vote_chocie);
        llVote = (LinearLayout) findViewById(R.id.ll_vote);
    }

    private void checkBundle() {
        fragType = getIntent().getStringExtra("frag_type");
        String voteId = getIntent().getStringExtra("vote_id");
        VoteManager voteManager = new VoteManager(this, 0);
        vote = voteManager.getVoteByVoteId(voteId);
    }

    private void initData() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth() - Utils.px2Dp(this, 20);
        int newHeight = width / 2;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, newHeight);
        p.setMargins(Utils.px2Dp(this, 10), 0, Utils.px2Dp(this, 10), 0);
        ivVotePhoto.setLayoutParams(p);
        ImageLoader.getInstance(this).DisplayImage(vote.votePhotoUrl, ivVotePhoto, R.drawable.annou_default3, false);
        ImageLoader.getInstance(this).DisplayImage(
                UserManager.getInstance(this).getUserByUserId(vote.userId).userPhotoUrl, iv_user_photo,
                R.drawable.friend_default, true);
        tvVoteUsername.setText(UserManager.getInstance(this).getUserByUserId(vote.userId).userName);
        tv_vote_title.setText(vote.voteTitle);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(vote.createdAt);
        tvVoteDate.setText(new SimpleDateFormat("yyyy-MM-dd kk:mm").format(c.getTime()));
        tvVoteDesc.setText(vote.voteContent);

        final VoteResultsChoiceListAdapter adapter = new VoteResultsChoiceListAdapter(this, fragType,
                vote.voteSelectedChoices);
        listviewVoteChocie.setAdapter(adapter);
        List<Choice> choiceList = new ArrayList<Choice>();
        String choices = vote.voteChoices;
        String[] choiceArr;
        String choiceKey = "";
        String choiceValue = "";
        if (choices.indexOf(",") != -1) {
            String[] choicesArr = choices.split(",");
            Arrays.sort(choicesArr);
            for (int i = 0; i < choicesArr.length; i++) {
                choiceArr = choicesArr[i].split(":");
                choiceKey = choiceArr[0];
                choiceValue = choiceArr[1];
                Choice choice = new Choice();
                choice.setKey(choiceKey);
                choice.setValue(choiceValue);
                choiceList.add(choice);
            }
        } else {
            choiceArr = choices.split(":");
            choiceKey = choiceArr[0];
            choiceValue = choiceArr[1];
            Choice choice = new Choice();
            choice.setKey(choiceKey);
            choice.setValue(choiceValue);
            choiceList.add(choice);
        }
        adapter.applyData(choiceList);
        if (vote.voteType.equals(Constant.VOTE_TYPE_SINGLE)) {
            tvVoteType.setText(getResources().getString(R.string.annou_vote_create_single));
        } else {
            tvVoteType.setText(getResources().getString(R.string.annou_vote_create_multiple));
        }

        pieChart = (PieChart) findViewById(R.id.piechart_vote);
        // int width = wm.getDefaultDisplay().getWidth() - Utils.px2Dp(this,
        // 20);
        // int newHeight = width / 2;
        //
        // p.setMargins(Utils.px2Dp(this, 10), 0, Utils.px2Dp(this, 10), 0);
        // ivVotePhoto.setLayoutParams(p);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(width, width);
        p.setMargins(Utils.px2Dp(this, 10), 0, Utils.px2Dp(this, 10), 0);
        pieChart.setLayoutParams(p2);
        pieChart.setHoleColorTransparent(true);
        pieChart.setHoleRadius(55f); // 半径
        pieChart.setTransparentCircleRadius(60f); // 半透明圈
        // pieChart.setHoleRadius(0); // 实心圆
        pieChart.setDescription("");
        // mChart.setDrawYValues(true);
        pieChart.setDrawCenterText(true); // 饼状图中间可以添加文字
        pieChart.setDrawHoleEnabled(true);
        pieChart.setRotationAngle(90); // 初始旋转角度
        // draws the corresponding description value into the slice
        // mChart.setDrawXValues(true);
        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true); // 可以手动旋转
        // display percentage values
        pieChart.setUsePercentValues(false); // 显示成百分比
        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);
        // add a selection listener
        // mChart.setOnChartValueSelectedListener(this);
        // mChart.setTouchEnabled(false);
        // mChart.setOnAnimationListener(this);
        pieChart.setCenterTextWordWrapEnabled(true);
        pieChart.setCenterTextSize(18f);
        pieChart.setCenterTextColor(getResources().getColor(R.color.no1));
        // pieChart.setCenterText("你好你好你好你好你好你好你好你好你好你好你好你好你好你好你好"); // 饼状图中间的文字
        // 设置数据
        pieChart.setData(getPieData());

        pieChart.setDrawSliceText(!pieChart.isDrawSliceTextEnabled());
        pieChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
        // undo all highlights
        // pieChart.highlightValues(null);
        // pieChart.invalidate();
        Legend mLegend = pieChart.getLegend(); // 设置比例图
        mLegend.setEnabled(true);
        mLegend.setPosition(LegendPosition.BELOW_CHART_LEFT);
        mLegend.setXEntrySpace(0f);
        mLegend.setYEntrySpace(7f);
        mLegend.setYOffset(0f);
        mLegend.setForm(LegendForm.CIRCLE); // 设置比例图的形状，默认是方形
        // mLegend.setDirection(LegendDirection.LEFT_TO_RIGHT);
        mLegend.setWordWrapEnabled(true);
        // mLegend.setMaxSizePercent(maxSize)
        // mLegend.setXEntrySpace(7f);
        // mLegend.setYEntrySpace(5f);
        // mChart.spin(2000, 0, 360);
    }

    /**
     * 
     * @param count
     *            分成几部分
     * @param range
     */
    private PieData getPieData() {
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colorsTemplate.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colorsTemplate.add(c);
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colorsTemplate.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colorsTemplate.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colorsTemplate.add(c);
        colorsTemplate.add(ColorTemplate.getHoloBlue());
        List<Integer> colors = new ArrayList<Integer>();
        ArrayList<String> xValues = new ArrayList<String>(); // xVals用来表示每个饼块上的内容
        ArrayList<Entry> yValues = new ArrayList<Entry>(); // yVals用来表示封装每个饼块的实际数据
        String[] choiceArray, resultArray;
        String choice = "";
        String result = "";
        int length = 0;
        if (vote.voteChoices.indexOf(",") != -1) {
            String[] choicesArray = vote.voteChoices.split(",");
            String[] resultsArray = vote.voteResults.split(",");
            for (int i = 0; i < choicesArray.length; i++) {
                choice = choicesArray[i];
                result = resultsArray[i];
                choiceArray = choice.split(":");
                resultArray = result.split(":");
                if (Integer.parseInt(resultArray[1]) > 0) {
                    length++;
                    // xValues.add(choiceArray[1]);
                    String choiceKey = resultArray[0];
                    choiceKey = choiceKey.substring(choiceKey.indexOf("_") + 1, choiceKey.length());
                    int choiceIndex = Integer.parseInt(choiceKey);
                    choiceIndex--;
                    colors.add(colorsTemplate.get(choiceIndex % 26));
                    chocieMap.put(Integer.parseInt(resultArray[1]),
                            getResources().getString(R.string.annou_vote_result_choice) + " " + (i + 1));
                    xValues.add(getResources().getString(R.string.annou_vote_result_choice) + " " + (i + 1));
                    yValues.add(new Entry(Integer.parseInt(resultArray[1]), i));
                }
            }
        } else {
            choiceArray = vote.voteChoices.split(":");
            resultArray = vote.voteResults.split(":");
            // xValues.add(choiceArray[1]);
            xValues.add(getResources().getString(R.string.annou_vote_result_choice) + " " + 1);
            yValues.add(new Entry(Float.parseFloat(resultArray[1]), 0));
            length = 1;
            colors.add(colorsTemplate.get(0));
        }

        PieDataSet pieDataSet = new PieDataSet(yValues, "");
        // 饼图颜色
        pieDataSet.setColors(colors);

        // y轴的集合
        pieDataSet.setSliceSpace(3f); // 设置个饼状图之间的距离
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = 5 * (metrics.densityDpi / 160f);
        pieDataSet.setSelectionShift(0f); // 选中态多出的长度
        PieData pieData = new PieData(xValues, pieDataSet);
        pieData.setValueTextColor(getResources().getColor(R.color.no5));
        pieData.setValueTextSize(11f);
        pieData.setHighlightEnabled(true);
        pieData.setValueFormatter(new ValueFormatter() {

            @Override
            public String getFormattedValue(float value) {
                int v = (int) value;
                String choice = chocieMap.get(v);
                if (v <= 1) {
                    // return choice + " (" + v +
                    // getResources().getString(R.string.annou_piechart_vote) +
                    // ")";
                    return v + getResources().getString(R.string.annou_piechart_vote);
                } else {
                    return v + getResources().getString(R.string.annou_piechart_votes);
                    // return choice + " (" + v +
                    // getResources().getString(R.string.annou_piechart_votes) +
                    // ")";
                }

            }
        });
        return pieData;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == AppCompatActivity.RESULT_OK) {
            initData();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
