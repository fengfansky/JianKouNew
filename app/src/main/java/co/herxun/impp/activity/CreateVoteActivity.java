package co.herxun.impp.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import co.herxun.impp.R;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.VoteManager;
import co.herxun.impp.controller.VoteManager.CreateVoteCallback;
import co.herxun.impp.model.Vote;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.ImageUtility;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class CreateVoteActivity extends BaseActivity {
    private AppBar appbar;
    private EditText etTitle, etDescription;
    private List<EditText> etChoiceList = new ArrayList<EditText>();
    private List<ImageView> ivDeleteChoiceList = new ArrayList<ImageView>();
    private List<RelativeLayout> rlChoiceList = new ArrayList<RelativeLayout>();
    private List<ImageView> ivLineList = new ArrayList<ImageView>();
    private LinearLayout llChoiceMain;
    private Switch sb;
    private Dialog mActionDialog;
    private ImageView iv_cover, iv_click_off;
    private RelativeLayout rl_cover;
    private byte[] data;
    private int ivIds = 0;
    private boolean isSingle = true;
    private TextView tv_choice_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_create_vote);

        appbar = (AppBar) findViewById(R.id.create_post_app_bar);
        appbar.getLogoView().setImageResource(R.drawable.menu_back);
        appbar.getLogoView().setLayoutParams(
                new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56)));
        appbar.getLogoView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                onBackPressed();
            }
        });
        appbar.getTextView().setVisibility(View.VISIBLE);
        appbar.getTextView().setText(R.string.annou_vote_create_title);

        appbar.getMenuItemView().setVisibility(View.VISIBLE);
        appbar.getMenuItemView().setImageResource(R.drawable.menu_done);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56));
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        appbar.getMenuItemView().setLayoutParams(rlp);
        appbar.getMenuItemView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                createVote();
            }
        });

        etTitle = (EditText) findViewById(R.id.et_vote_title);
        tv_choice_type = (TextView) findViewById(R.id.tv_choice_type);
        etDescription = (EditText) findViewById(R.id.et_vote_desc);

        etDescription.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                }
                return false;
            }
        });
        sb = (Switch) findViewById(R.id.wiperSwitch1);
        sb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSingle = !isChecked;
                if (isSingle) {
                    tv_choice_type.setText(getResources().getString(R.string.annou_vote_create_single));
                } else {
                    tv_choice_type.setText(getResources().getString(R.string.annou_vote_create_multiple));
                }
            }
        });

        llChoiceMain = (LinearLayout) findViewById(R.id.ll_choice);
        autoAddChoice();

        iv_cover = (ImageView) findViewById(R.id.iv_cover);
        iv_click_off = (ImageView) findViewById(R.id.iv_click_off);
        rl_cover = (RelativeLayout) findViewById(R.id.rl_cover);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int newWidth = width - Utils.px2Dp(this, 20);
        int newHeight = newWidth / 2;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(newWidth, newHeight);
        p.setMargins(Utils.px2Dp(this, 10), 0, Utils.px2Dp(this, 10), 0);
        rl_cover.setLayoutParams(p);
        rl_cover.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.show();
            }
        });

        AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.view_friend_alert, null);
        ImageView imgTakePic = (ImageView) view.findViewById(R.id.dialog_img1);
        imgTakePic.setImageResource(R.drawable.dialog_camera);
        ImageView imgPickPic = (ImageView) view.findViewById(R.id.dialog_img2);
        imgPickPic.setImageResource(R.drawable.dialog_upload);
        TextView textTakePic = (TextView) view.findViewById(R.id.dialog_text1);
        textTakePic.setText(R.string.chat_take_picture);
        TextView textPickPic = (TextView) view.findViewById(R.id.dialog_text2);
        textPickPic.setText(R.string.chat_pick_picture);
        view.findViewById(R.id.action_dialog_friend_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.dismiss();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    Uri mImageCaptureUri = null;
                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        mImageCaptureUri = Uri.fromFile(ImageUtility.getFileTemp(CreateVoteActivity.this));
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    intent.putExtra("return-data", true);
                    startActivityForResult(intent, Constant.REQUESTCODE_PHOTO_TAKE);
                } catch (ActivityNotFoundException e) {
                    Log.d("", "cannot take picture", e);
                }
            }
        });
        view.findViewById(R.id.action_dialog_topic_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.dismiss();

                // Intent intent = new Intent();
                // intent.setType("image/*");
                // intent.setAction(Intent.ACTION_GET_CONTENT);
                // startActivityForResult(Intent.createChooser(intent, ""),
                // Constant.REQUESTCODE_PHOTO_PICK);
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, Constant.REQUESTCODE_PHOTO_PICK);
            }
        });
        dialogBuiler.setView(view);
        mActionDialog = dialogBuiler.create();

    }

    private void createVote() {
        List<byte[]> dataList = new ArrayList<byte[]>();
        if (data != null) {
            dataList.add(data);
        }
        String choices = "";
        for (EditText etChoice : etChoiceList) {
            choices = choices + etChoice.getText().toString() + ",";
        }
        if (etTitle.getText().toString().length() == 0 || etDescription.getText().toString().length() == 0
                || choices.length() == 0) {
            dismissLoading();
            Toast.makeText(this, getString(R.string.annou_vote_create_error), Toast.LENGTH_LONG).show();
            return;
        }
        choices = choices.substring(0, choices.length() - 1);

        appbar.getMenuItemView().setEnabled(false);

        VoteManager voteManager = new VoteManager(this, 0);
        voteManager.createVote(dataList, etTitle.getText().toString(), etDescription.getText().toString(), choices,
                isSingle, UserManager.getInstance(this).getCurrentUser().userId, new CreateVoteCallback() {

                    @Override
                    public void onSuccess(Vote vote) {
                        DBug.e("createVote.onSuccess", vote.voteId);
                        dismissLoading();
                        setResult(Activity.RESULT_OK);
                        onBackPressed();
                    }

                    @Override
                    public void onFailure(final String exception) {
                        DBug.e("createVote.onFailure", exception.toString());
                        appbar.getMenuItemView().setEnabled(true);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                dismissLoading();
                                Toast.makeText(getBaseContext(), exception, Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int newWidth = width - Utils.px2Dp(this, 20);
        int newHeight = newWidth / 2;
        String imageFilePath = null;
        if (requestCode == Constant.REQUESTCODE_PHOTO_PICK) {
            imageFilePath = ImageUtility.getFilePathFromGallery(this, intent);
        } else if (requestCode == Constant.REQUESTCODE_PHOTO_TAKE) {
            imageFilePath = ImageUtility.getFileTemp(this).getPath();
        }
        if (imageFilePath != null) {
            data = ImageUtility.getDataFromFilePath(imageFilePath);
            if (data == null) {
                // Toast.makeText(context, resId, duration)
                return;
            } else {
                iv_cover.setLayoutParams(new RelativeLayout.LayoutParams(newWidth, newHeight));
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                iv_cover.setImageBitmap(bitmap);
                rl_cover.setBackground(null);
                rl_cover.setOnClickListener(null);
                iv_click_off.setVisibility(View.VISIBLE);
                iv_click_off.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        iv_click_off.setVisibility(View.GONE);
                        rl_cover.setBackgroundResource(R.drawable.dot_line);
                        rl_cover.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mActionDialog.show();
                            }
                        });
                        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(Utils.px2Dp(
                                CreateVoteActivity.this, 40), Utils.px2Dp(CreateVoteActivity.this, 40));
                        p.addRule(RelativeLayout.CENTER_IN_PARENT);
                        iv_cover.setLayoutParams(p);
                        iv_cover.setImageBitmap(null);
                    }
                });
            }
        }
    }

    private void autoAddChoice() {
        RelativeLayout rlChoice = new RelativeLayout(this);
        rlChoice.setTag(++ivIds + "");
        rlChoiceList.add(rlChoice);
        RelativeLayout.LayoutParams rlChoiceParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlChoiceParams.topMargin = Utils.px2Dp(this, 10);
        ImageView ivDeleteChocie_1 = new ImageView(this);
        ivDeleteChoiceList.add(ivDeleteChocie_1);
        ivDeleteChocie_1.setImageResource(R.drawable.friend_decline);
        ivDeleteChocie_1.setId(ivIds);
        ivDeleteChocie_1.setTag(ivIds + "");
        RelativeLayout.LayoutParams ivDeleteChoiceParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ivDeleteChoiceParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivDeleteChoiceParams.addRule(RelativeLayout.CENTER_VERTICAL);
        ivDeleteChoiceParams.leftMargin = Utils.px2Dp(this, 10);
        ivDeleteChoiceParams.rightMargin = Utils.px2Dp(this, 10);
        if (rlChoiceList.size() == 1) {
            ivDeleteChocie_1.setVisibility(View.INVISIBLE);
        }
        ivDeleteChocie_1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String currentTag = (String) v.getTag();
                for (int i = 0; i < etChoiceList.size(); i++) {
                    EditText et = etChoiceList.get(i);
                    if (et.getTag().equals(currentTag)) {
                        etChoiceList.remove(et);
                        break;
                    }
                }

                for (int i = 0; i < ivDeleteChoiceList.size(); i++) {
                    ImageView iv = ivDeleteChoiceList.get(i);
                    if (iv.getTag().equals(currentTag)) {
                        ivDeleteChoiceList.remove(iv);
                        break;
                    }
                }

                for (int i = 0; i < ivLineList.size(); i++) {
                    ImageView iv = ivLineList.get(i);
                    if (iv.getTag().equals(currentTag)) {
                        llChoiceMain.removeView(iv);
                        ivLineList.remove(iv);
                        break;
                    }
                }

                for (int i = 0; i < rlChoiceList.size(); i++) {
                    RelativeLayout rl = rlChoiceList.get(i);
                    if (rl.getTag().equals(currentTag)) {
                        llChoiceMain.removeView(rl);
                        rlChoiceList.remove(rl);
                        break;
                    }
                }
            }
        });
        rlChoice.addView(ivDeleteChocie_1, ivDeleteChoiceParams);

        EditText etChoice_1 = new EditText(this);
        etChoice_1.setTag(ivIds + "");
        etChoiceList.add(etChoice_1);
        etChoice_1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        etChoice_1.setTextSize(18);
        etChoice_1.setTextColor(getResources().getColor(R.color.textColor));
        etChoice_1.setBackgroundColor(Color.TRANSPARENT);
        etChoice_1.setSingleLine(true);
        etChoice_1.setEms(10);
        etChoice_1.setHintTextColor(getResources().getColor(R.color.textHintColor));
        etChoice_1.setHint(R.string.annou_vote_create_choices);
        etChoice_1.setPadding(Utils.px2Dp(this, 20), Utils.px2Dp(this, 20), Utils.px2Dp(this, 20),
                Utils.px2Dp(this, 20));
        RelativeLayout.LayoutParams etChoiceParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        etChoiceParams.addRule(RelativeLayout.LEFT_OF, ivDeleteChocie_1.getId());
        rlChoice.addView(etChoice_1, etChoiceParams);
        llChoiceMain.addView(rlChoice, rlChoiceParams);

        ImageView line = new ImageView(this);
        line.setTag(ivIds + "");
        ivLineList.add(line);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                Utils.px2Dp(this, 1));
        line.setBackgroundColor(getResources().getColor(R.color.no7));
        llChoiceMain.addView(line, lineParams);

        final TextView btnAddChoice = new TextView(this);
        btnAddChoice.setTextSize(18);
        btnAddChoice.setGravity(Gravity.CENTER);
        btnAddChoice.setText(R.string.annou_vote_create_add_choices);
        btnAddChoice.setTextColor(getResources().getColor(R.color.no1));
        btnAddChoice.setBackgroundColor(getResources().getColor(R.color.no5));
        LinearLayout.LayoutParams btnAddChoiceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnAddChoiceParams.gravity = Gravity.CENTER;
        btnAddChoiceParams.leftMargin = Utils.px2Dp(this, 10);
        btnAddChoiceParams.rightMargin = Utils.px2Dp(this, 10);
        btnAddChoiceParams.topMargin = Utils.px2Dp(this, 10);
        btnAddChoiceParams.bottomMargin = Utils.px2Dp(this, 10);
        llChoiceMain.addView(btnAddChoice, btnAddChoiceParams);

        btnAddChoice.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                llChoiceMain.removeView(btnAddChoice);
                autoAddChoice();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
