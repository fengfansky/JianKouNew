package co.herxun.impp.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import co.herxun.impp.R;
import co.herxun.impp.adapter.VoteChoiceListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.VoteManager;
import co.herxun.impp.controller.VoteManager.VoteCallback;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Choice;
import co.herxun.impp.model.Vote;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.ListViewForScrollView;

public class VoteDetailActivity extends Activity {
    private AppBar appbar;
    private Vote vote;
    private ImageView ivVotePhoto, iv_user_photo;
    private TextView tvVoteUsername, tvVoteDate, tvVoteDesc, tvVoteType, tvVoteTitle;
    private ListViewForScrollView listviewVoteChocie;
    private int fragType;
    private VoteManager voteManager;
    private ScrollView svVote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        checkBundle();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        appbar.getMenuItemView().setVisibility(View.VISIBLE);
        appbar.getTextView().setVisibility(View.VISIBLE);
        appbar.getTextView().setText(vote.voteTitle);
        appbar.getMenuItemView().setImageResource(R.drawable.menu_done);
        ivVotePhoto = (ImageView) findViewById(R.id.iv_vote_photo);
        iv_user_photo = (ImageView) findViewById(R.id.iv_user_photo);
        tvVoteUsername = (TextView) findViewById(R.id.tv_vote_username);
        tvVoteTitle = (TextView) findViewById(R.id.tv_vote_title);
        tvVoteDate = (TextView) findViewById(R.id.tv_vote_date);
        tvVoteDesc = (TextView) findViewById(R.id.tv_vote_desc);
        tvVoteType = (TextView) findViewById(R.id.tv_vote_type);
        listviewVoteChocie = (ListViewForScrollView) findViewById(R.id.listview_vote_chocie);
    }

    private void checkBundle() {
        fragType = getIntent().getIntExtra("frag_type", 0);
        String voteId = getIntent().getStringExtra("vote_id");
        voteManager = new VoteManager(this, 0);
        vote = voteManager.getVoteByVoteId(voteId);

        if (vote.isVoted()) {
            Intent intent = new Intent(this, VoteResultsActivity.class);
            intent.putExtra("vote_id", vote.voteId);
            startActivityForResult(intent, 0);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            finish();
        }
    }

    private void initData() {
        tvVoteTitle.setText(vote.voteTitle);
        tvVoteUsername.setText(UserManager.getInstance(this).getUserByUserId(vote.userId).userName);
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

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(vote.createdAt);
        tvVoteDate.setText(new SimpleDateFormat("yyyy-MM-dd kk:mm").format(c.getTime()));
        tvVoteDesc.setText(vote.voteContent);

        final VoteChoiceListAdapter adapter = new VoteChoiceListAdapter(this, fragType);
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
        listviewVoteChocie.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Choice choice = adapter.getList().get(position);
                if (vote.voteType.equals(Constant.VOTE_TYPE_SINGLE)) {
                    for (CheckBox cb : adapter.getCheckBoxList()) {
                        Choice c = (Choice) cb.getTag();
                        if (c.getValue().equals(choice.getValue())) {
                            cb.setChecked(true);
                        } else {
                            cb.setChecked(false);
                        }
                    }
                } else {
                    for (CheckBox cb : adapter.getCheckBoxList()) {
                        Choice c = (Choice) cb.getTag();
                        if (c.getValue().equals(choice.getValue())) {
                            cb.setChecked(!cb.isChecked());
                        }
                    }
                }
            }
        });

        if (vote.voteType.equals(Constant.VOTE_TYPE_SINGLE)) {
            tvVoteType.setText(getResources().getString(R.string.annou_vote_create_single));
        } else {
            tvVoteType.setText(getResources().getString(R.string.annou_vote_create_multiple));
        }
        if (vote.isVoted()) {
            appbar.getMenuItemView().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), VoteResultsActivity.class);
                    intent.putExtra("vote_id", vote.voteId);
                    startActivityForResult(intent, 0);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                }
            });
        } else {
            appbar.getMenuItemView().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // showLoading();
                    String choice = "";
                    if (vote.voteType.equals(Constant.VOTE_TYPE_SINGLE)) {
                        for (CheckBox cb : adapter.getCheckBoxList()) {
                            if (cb.isChecked()) {
                                Choice c = (Choice) cb.getTag();
                                choice = c.getKey();
                            }
                        }
                    } else {
                        for (CheckBox cb : adapter.getCheckBoxList()) {
                            if (cb.isChecked()) {
                                Choice c = (Choice) cb.getTag();
                                choice = choice + c.getKey() + ",";
                            }
                        }
                        if (choice.length() > 0) {
                            choice = choice.substring(0, choice.length() - 1);
                        }
                    }

                    if (choice.length() == 0) {
                        Toast.makeText(VoteDetailActivity.this,
                                getResources().getString(R.string.annou_vote_no_choice), Toast.LENGTH_LONG).show();
                        // dismissLoading();
                        return;
                    }

                    voteManager.vote(vote, choice, new VoteCallback() {
                        @Override
                        public void onFailure(String errorMsg) {
                            appbar.getMenuItemView().setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(v.getContext(), VoteResultsActivity.class);
                                    intent.putExtra("vote_id", vote.voteId);
                                    startActivityForResult(intent, 0);
                                    finish();
                                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                                }
                            });
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    // dismissLoading();
                                    Toast.makeText(VoteDetailActivity.this,
                                            getResources().getString(R.string.annou_vote_vote_error),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFinish(Vote vote) {
                            // dismissLoading();
                            Intent intent = new Intent(VoteDetailActivity.this, VoteResultsActivity.class);
                            intent.putExtra("vote_id", vote.voteId);
                            startActivityForResult(intent, 0);
                            finish();
                            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
