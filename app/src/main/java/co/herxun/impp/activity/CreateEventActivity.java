package co.herxun.impp.activity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import co.herxun.impp.controller.EventManager;
import co.herxun.impp.controller.EventManager.CreateEventCallback;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.model.Event;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.ImageUtility;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

public class CreateEventActivity extends Activity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {
    private AppBar appbar;
    private EditText etTitle, etInformation, etAddress, etCost, etUserLimit;
    private Dialog mActionDialog;
    private TextView tvStartdate, tvEnddate;
    private ImageView iv_cover, iv_click_off;
    private LinearLayout ll_startdate, ll_enddate;
    private RelativeLayout rl_cover;
    private byte[] data;
    private boolean isClickStart = false;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_create_event);

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
        appbar.getTextView().setText(R.string.annou_event_create_title);

        appbar.getMenuItemView().setVisibility(View.VISIBLE);
        appbar.getMenuItemView().setImageResource(R.drawable.menu_done);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56));
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        appbar.getMenuItemView().setLayoutParams(rlp);
        appbar.getMenuItemView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // showLoading();
                createEvent();
            }
        });
        iv_cover = (ImageView) findViewById(R.id.iv_cover);
        iv_click_off = (ImageView) findViewById(R.id.iv_click_off);
        rl_cover = (RelativeLayout) findViewById(R.id.rl_cover);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int newWidth = width - Utils.px2Dp(this, 20);
        int newHeight = newWidth / 2;
        rl_cover.setLayoutParams(new LinearLayout.LayoutParams(newWidth, newHeight));
        rl_cover.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionDialog.show();
            }
        });

        etTitle = (EditText) findViewById(R.id.et_event_title);
        etInformation = (EditText) findViewById(R.id.et_event_information);
        etAddress = (EditText) findViewById(R.id.et_event_address);
        etCost = (EditText) findViewById(R.id.et_event_cost);
        etUserLimit = (EditText) findViewById(R.id.et_event_user_limit);
        tvStartdate = (TextView) findViewById(R.id.tv_startdate);
        tvEnddate = (TextView) findViewById(R.id.tv_enddate);
        ll_startdate = (LinearLayout) findViewById(R.id.ll_startdate);
        ll_enddate = (LinearLayout) findViewById(R.id.ll_enddate);

        etInformation.setOnTouchListener(new OnTouchListener() {

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

        ll_startdate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                isClickStart = true;
                DatePickerDialog.newInstance(CreateEventActivity.this, startCalendar.get(Calendar.YEAR),
                        startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH)).show(
                        getFragmentManager(), "datePicker");
            }
        });
        ll_enddate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                isClickStart = false;
                DatePickerDialog.newInstance(CreateEventActivity.this, endCalendar.get(Calendar.YEAR),
                        endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH)).show(
                        getFragmentManager(), "datePicker");
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
                        mImageCaptureUri = Uri.fromFile(ImageUtility.getFileTemp(CreateEventActivity.this));
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

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        if (isClickStart) {
            startCalendar.set(year, monthOfYear, dayOfMonth);
            TimePickerDialog.newInstance(this, startCalendar.get(Calendar.HOUR_OF_DAY),
                    startCalendar.get(Calendar.MINUTE), true).show(getFragmentManager(), "timePicker");
        } else {
            endCalendar.set(year, monthOfYear, dayOfMonth);
            TimePickerDialog.newInstance(this, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE),
                    true).show(getFragmentManager(), "timePicker");
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        if (isClickStart) {
            startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            startCalendar.set(Calendar.MINUTE, minute);
            Date time = startCalendar.getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            tvStartdate.setText(df.format(time));
        } else {
            endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            endCalendar.set(Calendar.MINUTE, minute);
            Date time = endCalendar.getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            tvEnddate.setText(df.format(time));
        }
    }

    private void createEvent() {
        List<byte[]> dataList = new ArrayList<byte[]>();
        if (data != null) {
            dataList.add(data);
        }

        if (etTitle.getText().toString().length() == 0 || etInformation.getText().toString().length() == 0
                || tvStartdate.getText().toString().length() == 0 || tvEnddate.getText().toString().length() == 0
                || etAddress.getText().toString().length() == 0) {
            // dismissLoading();
            Toast.makeText(this, getString(R.string.annou_event_create_error), Toast.LENGTH_LONG).show();
            return;
        }
        if (endCalendar.before(startCalendar)) {
            // dismissLoading();
            Toast.makeText(this, getString(R.string.annou_event_endtime_starttime_error), Toast.LENGTH_LONG).show();
            return;
        }
        if (endCalendar.before(Calendar.getInstance())) {
            // dismissLoading();
            Toast.makeText(this, getString(R.string.annou_event_endtime_error), Toast.LENGTH_LONG).show();
            return;
        }
        long duration = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        BigDecimal cost = BigDecimal.ZERO;
        if (etCost.getText().toString().length() > 0) {
            try {
                cost = new BigDecimal(etCost.getText().toString());
            } catch (Exception e) {
                // dismissLoading();
                Toast.makeText(this, getString(R.string.annou_event_cost_error), Toast.LENGTH_LONG).show();
                return;
            }
        }
        int userLimit = 0;
        if (etUserLimit.getText().toString().length() > 0) {
            try {
                userLimit = Integer.parseInt(etUserLimit.getText().toString());
            } catch (Exception e) {
                // dismissLoading();
                Toast.makeText(this, getString(R.string.annou_event_user_limit_error), Toast.LENGTH_LONG).show();
                return;
            }
        }

        appbar.getMenuItemView().setEnabled(false);
        EventManager eventManager = new EventManager(this, 0);

        eventManager.createEvent(dataList, etTitle.getText().toString(), etInformation.getText().toString(), etAddress
                .getText().toString(), duration, tvStartdate.getText().toString(), cost, userLimit, UserManager
                .getInstance(this).getCurrentUser().userId, new CreateEventCallback() {

            @Override
            public void onSuccess(Event event) {
                DBug.e("createEvent.onSuccess", event.eventId);
                // dismissLoading();
                setResult(Activity.RESULT_OK);
                onBackPressed();
            }

            @Override
            public void onFailure(final String exception) {
                DBug.e("createEvent.onFailure", exception.toString());
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
                                CreateEventActivity.this, 40), Utils.px2Dp(CreateEventActivity.this, 40));
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
