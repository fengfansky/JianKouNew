package co.herxun.impp.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.utils.Utils;

public class AppBar extends RelativeLayout {
    private ImageView logo, menuItem;
    private EditText etSearch;
    private TextView textTitle, tvReturn, textClose;
    private int id = 1;

    public AppBar(Context ct) {
        super(ct);
        init(ct);
    }

    public AppBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public AppBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context ct) {
        logo = new ImageView(ct);
        logo.setId(id++);
        addView(logo);

        etSearch = new EditText(ct);
        etSearch.setSingleLine();
        etSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etSearch.setBackgroundColor(Color.TRANSPARENT);
        etSearch.setGravity(Gravity.CENTER_VERTICAL);
        etSearch.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        etSearch.setTextColor(ct.getResources().getColor(R.color.no5));
        etSearch.setHintTextColor(0x4cffffff);
        etSearch.setPadding(0, 0, 0, 0);
        etSearch.setHint(R.string.friend_search_username);
        addView(etSearch);

        textClose = new TextView(ct);
        textClose.setSingleLine();
        textClose.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        textClose.setEllipsize(TruncateAt.MIDDLE);
        textClose.setTextColor(ct.getResources().getColor(R.color.no5));
        textClose.setText(getResources().getString(R.string.general_close));
        addView(textClose);


        textTitle = new TextView(ct);
        textTitle.setSingleLine();
        textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textTitle.setEllipsize(TruncateAt.MIDDLE);
        textTitle.setTextColor(ct.getResources().getColor(R.color.no5));
        addView(textTitle);

        tvReturn = new TextView(ct);
        tvReturn.setSingleLine();
        tvReturn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        tvReturn.setEllipsize(TruncateAt.MIDDLE);
        tvReturn.setVisibility(View.GONE);
        tvReturn.setTextColor(ct.getResources().getColor(R.color.no5));
        addView(tvReturn);

        menuItem = new ImageView(ct);
        menuItem.setId(id++);
        addView(menuItem);

        initLayout();
    }

    public void initLayout() {
        LayoutParams rlpLogo = new LayoutParams(Utils.px2Dp(getContext(), 54),
                Utils.px2Dp(getContext(), 26));
        rlpLogo.leftMargin = Utils.px2Dp(getContext(), 16);
        rlpLogo.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlpLogo.addRule(RelativeLayout.CENTER_VERTICAL);
        logo.setLayoutParams(rlpLogo);

        LayoutParams rlpEt = new LayoutParams(-1, -2);
        rlpEt.leftMargin = Utils.px2Dp(getContext(), 16);
        rlpEt.addRule(RelativeLayout.RIGHT_OF, logo.getId());
        rlpEt.addRule(RelativeLayout.CENTER_VERTICAL);
        etSearch.setLayoutParams(rlpEt);
        etSearch.setVisibility(View.GONE);
        etSearch.setText("");

        LayoutParams rlpTt = new LayoutParams(-1, -2);
        rlpTt.leftMargin = Utils.px2Dp(getContext(), 16);
        rlpTt.addRule(RelativeLayout.RIGHT_OF, logo.getId());
        rlpTt.addRule(RelativeLayout.LEFT_OF, menuItem.getId());
        rlpTt.addRule(RelativeLayout.CENTER_VERTICAL);
        textTitle.setLayoutParams(rlpTt);
        textTitle.setVisibility(View.GONE);
        textTitle.setText("");

        LayoutParams rlpTt2 = new LayoutParams(-1, -2);
        rlpTt2.leftMargin = Utils.px2Dp(getContext(), 16);
        rlpTt2.addRule(RelativeLayout.LEFT_OF, menuItem.getId());
        rlpTt2.addRule(RelativeLayout.CENTER_VERTICAL);
        tvReturn.setLayoutParams(rlpTt2);
        tvReturn.setVisibility(View.GONE);
        tvReturn.setText("");

        LayoutParams rlpMenuItem = new LayoutParams(Utils.px2Dp(getContext(), 56),
                Utils.px2Dp(getContext(), 56));
        rlpMenuItem.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        menuItem.setLayoutParams(rlpMenuItem);
        menuItem.setVisibility(View.GONE);
        
        LayoutParams rlpTextClose = new LayoutParams(Utils.px2Dp(getContext(), 56),
                Utils.px2Dp(getContext(), 56));
        rlpTextClose.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        textClose.setGravity(Gravity.CENTER_VERTICAL);
        textClose.setLayoutParams(rlpTextClose);
        textClose.setVisibility(View.GONE);
    }

    public ImageView getLogoView() {
        return logo;
    }

    public ImageView getMenuItemView() {
        return menuItem;
    }

    public EditText getEditText() {
        return etSearch;
    }

    public TextView getTextView() {
        return textTitle;
    }

    public TextView getTvReturn() {
        return tvReturn;
    }

    public TextView getTextViewClose() {
        return textClose;
    }
}
