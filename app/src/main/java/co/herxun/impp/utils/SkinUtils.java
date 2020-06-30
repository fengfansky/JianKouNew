package co.herxun.impp.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;

import co.herxun.impp.R;


public class SkinUtils {

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    //检查当前系统是否已开启暗黑模式
    public static boolean getDarkModeStatus(Context context) {
        int mode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static Drawable changeIconDark(Context context, int res) {
        Drawable drawable;
        if (SkinUtils.getDarkModeStatus(context)) {
            drawable = SkinUtils.tintDrawable(context.getResources().getDrawable(res), ColorStateList.valueOf(context.getResources().getColor(R.color.color_ffffff)));
        } else {
            drawable = context.getResources().getDrawable(res);
        }
        return drawable;
    }
}
