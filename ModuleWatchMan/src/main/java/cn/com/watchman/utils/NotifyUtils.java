package cn.com.watchman.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import cn.com.watchman.R;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.alibaba.sdk.android.ams.common.global.AmsGlobalHolder.getPackageName;
import static com.linked.erfli.library.okhttps.log.LoggerInterceptor.TAG;

/**
 * 文件名：NotifyUtils
 * 描    述：通知栏的工具类
 * 作    者：stt
 * 时    间：2017.4.25
 * 版    本：V1.0.0
 */

public class NotifyUtils {
    /**
     * 是否在播放
     */
    public boolean isPlay = false;
    /**
     * 通知栏按钮广播
     */
    public ButtonBroadcastReceiver bReceiver;
    /**
     * 通知栏按钮点击事件对应的ACTION
     */
    public final static String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";
    private Activity activity;
    /**
     * Notification管理
     */
    public NotificationManager mNotificationManager;

    public NotifyUtils(Activity activity) {
        this.activity = activity;
        mNotificationManager = (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);
        initButtonReceiver();
    }

    public void showButtonNotify() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity);
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.view_custom_button);
        mRemoteViews.setImageViewResource(R.id.custom_song_icon, R.mipmap.watch_logo);
        //API3.0 以上的时候显示按钮，否则消失
        mRemoteViews.setTextViewText(R.id.tv_custom_song_singer, "独立巡更");
        mRemoteViews.setTextViewText(R.id.tv_custom_song_name, "上传数据");
        //如果版本号低于（3。0），那么不显示按钮
        if (getSystemVersion() <= 9) {
            mRemoteViews.setViewVisibility(R.id.ll_custom_button, View.GONE);
        } else {
            mRemoteViews.setViewVisibility(R.id.ll_custom_button, View.VISIBLE);
            //
            if (isPlay) {
                mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.btn_pause);
            } else {
                mRemoteViews.setImageViewResource(R.id.btn_custom_play, R.drawable.btn_play);
            }
        }

        //点击的事件处理
        Intent buttonIntent = new Intent(ACTION_BUTTON);
        /* 上一首按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PREV_ID);
        //这里加了广播，所及INTENT的必须用getBroadcast方法
        PendingIntent intent_prev = PendingIntent.getBroadcast(activity, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_prev, intent_prev);
        /* 播放/暂停  按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PALY_ID);
        PendingIntent intent_paly = PendingIntent.getBroadcast(activity, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_play, intent_paly);
        /* 下一首 按钮  */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
        PendingIntent intent_next = PendingIntent.getBroadcast(activity, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_custom_next, intent_next);
        mBuilder.setContent(mRemoteViews)
                .setContentIntent(getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("正在播放")
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                .setSmallIcon(R.mipmap.watch_logo);
        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        //会报错，还在找解决思路
//		notify.contentView = mRemoteViews;
//		notify.contentIntent = PendingIntent.getActivity(activity, 0, new Intent(), 0);
        mNotificationManager.notify(200, notify);
    }

    /**
     * 获取当前应用版本号
     *
     * @param context
     * @return version
     * @throws Exception
     */
    public static String getAppVersion(Context context) throws Exception {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        String versionName = packInfo.versionName;
        return versionName;
    }

    /**
     * 获取当前系统SDK版本号
     */
    public static int getSystemVersion() {
        /*获取当前系统的android版本号*/
        int version = android.os.Build.VERSION.SDK_INT;
        return version;
    }

    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    /**
     * 上一首 按钮点击 ID
     */
    public final static int BUTTON_PREV_ID = 1;
    /**
     * 播放/暂停 按钮点击 ID
     */
    public final static int BUTTON_PALY_ID = 2;
    /**
     * 下一首 按钮点击 ID
     */
    public final static int BUTTON_NEXT_ID = 3;

    /**
     * 带按钮的通知栏点击广播接收
     */
    public void initButtonReceiver() {
        bReceiver = new ButtonBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BUTTON);
        activity.registerReceiver(bReceiver, intentFilter);
    }

    /**
     * 广播监听按钮点击时间
     */
    public class ButtonBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equals(ACTION_BUTTON)) {
                //通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
                int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
                switch (buttonId) {
                    case BUTTON_PREV_ID:
                        Log.d(TAG, "上一首");
                        Toast.makeText(activity, "上一首", Toast.LENGTH_SHORT).show();
                        break;
                    case BUTTON_PALY_ID:
                        String play_status = "";
                        isPlay = !isPlay;
                        if (isPlay) {
                            play_status = "开始播放";
                        } else {
                            play_status = "已暂停";
                        }
                        showButtonNotify();
                        Log.d(TAG, play_status);
                        Toast.makeText(activity, play_status, Toast.LENGTH_SHORT).show();
                        break;
                    case BUTTON_NEXT_ID:
                        Log.d(TAG, "下一首");
                        Toast.makeText(activity, "下一首", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    }


    /**
     * 清除所有通知栏
     */
    public void clearAllNotify() {
        mNotificationManager.cancelAll();// 删除你发的所有通知
    }

    /**
     * @获取默认的pendingIntent,为了防止2.3及以下版本报错
     * @flags属性: 在顶部常驻:Notification.FLAG_ONGOING_EVENT
     * 点击去除： Notification.FLAG_AUTO_CANCEL
     */
    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 1, new Intent(), flags);
        return pendingIntent;
    }
}
