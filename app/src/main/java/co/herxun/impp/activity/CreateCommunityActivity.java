package co.herxun.impp.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.herxun.impp.R;
import co.herxun.impp.controller.CommunityManager;
import co.herxun.impp.controller.CommunityManager.CreateCommunityCallback;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.model.Community;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.ImageUtility;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class CreateCommunityActivity extends Activity {
    private AppBar appbar;
    private EditText etContent, etDescription, etUrl;
    private Dialog mActionDialog;
    private ImageView iv_cover, iv_click_off;
    private RelativeLayout rl_cover;
    private byte[] data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_create_community);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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
        appbar.getTextView().setText(R.string.annou_comunity_create_title);

        appbar.getMenuItemView().setVisibility(View.VISIBLE);
        appbar.getMenuItemView().setImageResource(R.drawable.menu_done);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56));
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        appbar.getMenuItemView().setLayoutParams(rlp);
        appbar.getMenuItemView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // showLoading();
                createCommunity();
            }
        });

        etContent = (EditText) findViewById(R.id.et_community_name);
        etDescription = (EditText) findViewById(R.id.et_community_desc);
        etUrl = (EditText) findViewById(R.id.et_community_url);
        iv_cover = (ImageView) findViewById(R.id.iv_cover);
        iv_click_off = (ImageView) findViewById(R.id.iv_click_off);
        rl_cover = (RelativeLayout) findViewById(R.id.rl_cover);

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
                        mImageCaptureUri = Uri.fromFile(ImageUtility.getFileTemp(CreateCommunityActivity.this));
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

    private void createCommunity() {
        List<byte[]> dataList = new ArrayList<byte[]>();
        if (data != null) {
            dataList.add(data);
        }
        if (dataList.isEmpty() || etContent.getText().toString().length() == 0
                || etDescription.getText().toString().length() == 0 || etUrl.getText().toString().length() == 0) {
            // dismissLoading();
            Toast.makeText(this, getString(R.string.annou_comunity_create_error), Toast.LENGTH_LONG).show();
            return;
        }

        if (!Utils.isTopURL(etUrl.getText().toString())) {
            // dismissLoading();
            Toast.makeText(this, getString(R.string.annou_comunity_create_url_error), Toast.LENGTH_LONG).show();
            return;
        }

        if (!Utils.isConnect(etUrl.getText().toString())) {
            // dismissLoading();
            Toast.makeText(this, getString(R.string.annou_comunity_create_url_error), Toast.LENGTH_LONG).show();
            return;
        }

        appbar.getMenuItemView().setEnabled(false);

        CommunityManager communityManager = new CommunityManager(this);
        communityManager.createCommunity(dataList, etContent.getText().toString(), etDescription.getText().toString(),
                etUrl.getText().toString(), UserManager.getInstance(this).getCurrentUser().userId,
                new CreateCommunityCallback() {

                    @Override
                    public void onSuccess(Community community) {
                        DBug.e("createCommunity.onSuccess", community.communityId);
                        // dismissLoading();
                        setResult(Activity.RESULT_OK);
                        onBackPressed();
                    }

                    @Override
                    public void onFailure(final String exception) {
                        DBug.e("createCommunity.onFailure", exception.toString());
                        appbar.getMenuItemView().setEnabled(true);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                // dismissLoading();
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
                                CreateCommunityActivity.this, 40), Utils.px2Dp(CreateCommunityActivity.this, 40));
                        p.addRule(RelativeLayout.CENTER_IN_PARENT);
                        iv_cover.setLayoutParams(p);
                        iv_cover.setImageBitmap(null);
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
