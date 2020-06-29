package co.herxun.impp.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.widget.Toast;
import co.herxun.impp.IMppApp;
import co.herxun.impp.activity.SplashActivity;

public class IMppHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler mDefaultHandler;
    public static final String TAG = "IMppApp";
    IMppApp application;

    public IMppHandler(IMppApp application) {
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.application = application;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            Intent intent = new Intent(application.getApplicationContext(), SplashActivity.class);
            PendingIntent restartIntent = PendingIntent.getActivity(application.getApplicationContext(), 0, intent,
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            AlarmManager mgr = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);

            // 立即重启应用
            mgr.set(AlarmManager.RTC, System.currentTimeMillis(), restartIntent);
            // 退出程序，需要开发者手动管理所有的Activity
            application.finishActivity();
        }
    }

    // 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 这里可以使用Toast来显示异常，也可以记录异常信息并发出
        // TODO
        return true;
    }
}
