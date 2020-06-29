package co.herxun.impp.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Utils;

public class SettingView extends RelativeLayout {

    private ImageView imgUserIcon;
    private TextView textUserName, textBtn, changNameBtn, textNickName;
    private RelativeLayout ll;

    public SettingView(Context context) {
        super(context);
        init();
    }

    public SettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_user_setting, this);
        imgUserIcon = (ImageView) findViewById(R.id.user_detail_img);
        textUserName = (TextView) findViewById(R.id.user_detail_text_name);
        changNameBtn = (TextView) findViewById(R.id.user_detail_changename_btn);
        textBtn = (TextView) findViewById(R.id.user_detail_text_btn);
        textNickName = (TextView) findViewById(R.id.user_detail_text_nickname);
        ll = (RelativeLayout) findViewById(R.id.ll);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(getContext().getResources().getColor(R.color.no1));
        float corner = Utils.px2Dp(getContext(), 2);
        gd.setCornerRadii(new float[] { corner, corner, corner, corner, corner, corner, corner, corner });
    }

    public ImageView getImageView() {
        return imgUserIcon;
    }

    public RelativeLayout getLinearLayout() {
        return ll;
    }

    public TextView getTextButton() {
        return textBtn;
    }

    public void getNickName(String text) {
        textNickName.setText(text);
    }

    public void setButton(String text, OnClickListener lsr) {
        textBtn.setText(text);
        textBtn.setOnClickListener(lsr);
    }

    public void setNickname(OnClickListener lsr) {
        changNameBtn.setOnClickListener(lsr);
    }

    public void setLinearLayout(OnClickListener listener) {
        ll.setOnClickListener(listener);
    }

    public void setUserInfo(User user) {
        ImageLoader.getInstance(getContext()).DisplayImage(user.userPhotoUrl, imgUserIcon, R.drawable.friend_default,
                true);
        textUserName.setText(user.userName);
        textNickName.setText(user.nickname);
    }
}
