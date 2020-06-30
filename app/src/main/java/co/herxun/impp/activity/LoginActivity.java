package co.herxun.impp.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import co.herxun.impp.R;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.SpfHelper;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.MaterialEditText;

import com.arrownock.social.IAnSocialCallback;

public class LoginActivity extends Activity {
    private MaterialEditText etUsername, etPwd;
    private Button btnSignIn;
    private TextView btnRegister;
    private String payload;
    private boolean doubleBackToExistPressedOnce = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkBundle();
        autoSignIn();
        initView();
    }

    private void checkBundle() {
        if (getIntent().hasExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD)) {
            payload = getIntent().getStringExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD);
        }
    }

    private void autoSignIn() {
        if (SpfHelper.getInstance(this).hasSignIn()) {
            UserManager.getInstance(this).login(SpfHelper.getInstance(this).getMyUsername(),
                    SpfHelper.getInstance(this).getMyPwd(), new IAnSocialCallback() {
                        @Override
                        public void onFailure(JSONObject arg0) {
                            try {
                                String errorMsg = arg0.getJSONObject("meta").getString("message");
                                Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onSuccess(final JSONObject arg0) {
                            try {
                                JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
                                User user = new User(userJson);
                                afterLogin(user);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    private void initView() {
        setContentView(R.layout.activity_login);
        etUsername = (MaterialEditText) findViewById(R.id.et_username);
        etUsername.setLineFocusedColor(getResources().getColor(R.color.no5));
        etUsername.setLineUnFocusedColor(getResources().getColor(R.color.no5));
        etUsername.setLineFocusedHeight(4);
        etUsername.setLineUnFocusedHeight(1);
        etUsername.getEditText().setTextColor(getResources().getColor(R.color.no5));
        etUsername.getEditText().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        etUsername.getEditText().requestFocus();
        etUsername.getEditText().setHint(R.string.login_username);
        etUsername.getEditText().setHintTextColor(getResources().getColor(R.color.no7));
        etUsername.getEditText().setSingleLine();
        etUsername.getEditText().setText(SpfHelper.getInstance(this).getMyUsername());

        etPwd = (MaterialEditText) findViewById(R.id.et_pwd);
        etPwd.setLineFocusedColor(getResources().getColor(R.color.no5));
        etPwd.setLineUnFocusedColor(getResources().getColor(R.color.no5));
        etPwd.setLineFocusedHeight(4);
        etPwd.setLineUnFocusedHeight(1);
        etPwd.getEditText().setTextColor(getResources().getColor(R.color.no5));
        etPwd.getEditText().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        etPwd.getEditText().setHint(R.string.login_pwd);
        etPwd.getEditText().setHintTextColor(getResources().getColor(R.color.no7));
        etPwd.getEditText().setSingleLine();
        etPwd.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());
        etPwd.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);

        float corner = Utils.px2Dp(this, 2);
        GradientDrawable bgBtnSignUp = new GradientDrawable();
        bgBtnSignUp.setColor(getResources().getColor(R.color.no3));
        bgBtnSignUp.setCornerRadii(new float[] { corner, corner, corner, corner, corner, corner, corner, corner });
        GradientDrawable bgBtnSignIn = new GradientDrawable();
        bgBtnSignIn.setColor(getResources().getColor(R.color.no2));
        bgBtnSignIn.setCornerRadii(new float[] { corner, corner, corner, corner, corner, corner, corner, corner });

        // btnSignUp = (Button) findViewById(R.id.btn_sign_up);
        // btnSignUp.setBackgroundDrawable(bgBtnSignUp);
        // btnSignUp.setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // // showLoading();
        // UserManager.getInstance(LoginActivity.this).signUp(etUsername.getEditText().getText().toString(),
        // etPwd.getEditText().getText().toString(), new IAnSocialCallback() {
        // @Override
        // public void onFailure(JSONObject arg0) {
        // try {
        // // dismissLoading();
        // String errorMsg = arg0.getJSONObject("meta").getString("message");
        // int errorCode = arg0.getJSONObject("meta").getInt("errorCode");
        // if (errorCode == -101200) {
        // Toast.makeText(getBaseContext(),
        // getResources().getString(R.string.login_exist_name),
        // Toast.LENGTH_LONG)
        // .show();
        // } else {
        // Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
        // }
        // } catch (JSONException e) {
        // e.printStackTrace();
        // // dismissLoading();
        // }
        // }
        //
        // @Override
        // public void onSuccess(final JSONObject arg0) {
        // try {
        // JSONObject userJson =
        // arg0.getJSONObject("response").getJSONObject("user");
        // User user = new User(userJson);
        // afterLogin(user);
        // } catch (JSONException e) {
        // e.printStackTrace();
        // // dismissLoading();
        // }
        // }
        // });
        // }
        // });

        btnSignIn = (Button) findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                // showLoading();
                UserManager.getInstance(LoginActivity.this).login(etUsername.getEditText().getText().toString(),
                        etPwd.getEditText().getText().toString(), new IAnSocialCallback() {
                            @Override
                            public void onFailure(JSONObject arg0) {
                                try {
//                                    // dismissLoading();
                                    String errorMsg = arg0.getJSONObject("meta").getString("message");
                                    Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
//                                    // dismissLoading();
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onSuccess(final JSONObject arg0) {
                                try {
                                    JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
                                    User user = new User(userJson);
                                    afterLogin(user);
                                } catch (JSONException e) {
                                    e.printStackTrace();
//                                    // dismissLoading();
                                }
                            }
                        });
            }
        });

        btnRegister = (TextView) findViewById(R.id.register_btn);
        btnRegister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                goToRegisterActivity();
            }
        });

    }

    private void afterLogin(final User user) {
        if (!SpfHelper.getInstance(this).hasSignIn()) {
            SpfHelper.getInstance(this).saveUserInfo(etUsername.getEditText().getText().toString(),
                    etPwd.getEditText().getText().toString(), user.nickname, user.userId, user.clientId);
        }
        com.arrownock.appo.desk.User deskUser = new com.arrownock.appo.desk.User();
        deskUser.setId(user.userId);
        deskUser.setName(user.nickname == null ? user.userName : user.nickname);
        deskUser.setPhoto(user.userPhotoUrl);
        IMManager.getInstance(this).initDesk(deskUser);
        IMManager.getInstance(this).connect(user.clientId);
        UserManager.getInstance(this).setCurrentUser(user);

        IMManager.getInstance(this).fetchAllRemoteTopic();
        UserManager.getInstance(this).fetchMyRemoteFriend(null);

        IMManager.getInstance(this).bindAnPush();
        // sync the history on other devices
        IMManager.getInstance(this).syncHistory();
        
        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        if (payload != null) {
            i.putExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD, payload);
        }
        startActivity(i);
//        // dismissLoading();
        finish();
    }

    private void goToRegisterActivity() {
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                doubleBackToExistPressedOnce = false;
            }
        };
        if (!doubleBackToExistPressedOnce) {
            doubleBackToExistPressedOnce = true;
            Toast.makeText(this, getString(R.string.general_press_again_to_exit), Toast.LENGTH_SHORT).show();
            h.postDelayed(r, 2000);
        } else {
            h.removeCallbacks(r);
            super.onBackPressed();
        }
    }
}