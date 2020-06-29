package co.herxun.impp.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import co.herxun.impp.R;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.SpfHelper;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.MaterialEditText;

import com.arrownock.social.IAnSocialCallback;

public class RegisterActivity extends BaseActivity {

    private Button btnRegister;
    private MaterialEditText etUsername, etPwd, etNickname;
    private String payload;
    private AppBar mAppBar;

    private void checkBundle() {
        if (getIntent().hasExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD)) {
            payload = getIntent().getStringExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        checkBundle();

        initAppbar();

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

        etNickname = (MaterialEditText) findViewById(R.id.et_nickName);
        etNickname.setLineFocusedColor(getResources().getColor(R.color.no5));
        etNickname.setLineUnFocusedColor(getResources().getColor(R.color.no5));
        etNickname.setLineFocusedHeight(4);
        etNickname.setLineUnFocusedHeight(1);
        etNickname.getEditText().setTextColor(getResources().getColor(R.color.no5));
        etNickname.getEditText().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        etNickname.getEditText().setHint(R.string.register_nickname);
        etNickname.getEditText().setHintTextColor(getResources().getColor(R.color.no7));
        etNickname.getEditText().setSingleLine();
        etNickname.getEditText().setText(SpfHelper.getInstance(this).getMyUsername());

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

        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showLoading();
                UserManager.getInstance(RegisterActivity.this).signUp(etUsername.getEditText().getText().toString(),
                        etPwd.getEditText().getText().toString(), etNickname.getEditText().getText().toString(),
                        new IAnSocialCallback() {
                            @Override
                            public void onFailure(JSONObject arg0) {
                                try {
                                    dismissLoading();
                                    String errorMsg = arg0.getJSONObject("meta").getString("message");
                                    Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    dismissLoading();
                                }
                            }

                            @Override
                            public void onSuccess(final JSONObject arg0) {
                                try {
                                    JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
                                    User user = new User(userJson);
                                    user.update();
                                    afterLogin(user);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    dismissLoading();
                                }
                            }
                        });
            }
        });
    }

    private void initAppbar() {
        mAppBar = (AppBar) findViewById(R.id.chat_app_bar);
        mAppBar.getTvReturn().setText(R.string.login_return);
        mAppBar.getTvReturn().setVisibility(View.VISIBLE);
        mAppBar.getTvReturn().setGravity(Gravity.LEFT);
        mAppBar.getTvReturn().setPadding(100, 0, 0, 0);

        mAppBar.getLogoView().setImageResource(R.drawable.menu_back);
        mAppBar.getLogoView().setLayoutParams(
                new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56)));
        mAppBar.getLogoView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void afterLogin(final User user) {
        if (!SpfHelper.getInstance(this).hasSignIn()) {
            SpfHelper.getInstance(this).saveUserInfo(etUsername.getEditText().getText().toString(),
                    etPwd.getEditText().getText().toString(), etNickname.getEditText().getText().toString(),
                    user.userId, user.clientId);
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
        
        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
        if (payload != null) {
            i.putExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD, payload);
        }
        startActivity(i);
        dismissLoading();
        finish();
    }

}
