package sa.devming.realrank.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import sa.devming.realrank.Constants;
import sa.devming.realrank.MainActivity;
import sa.devming.realrank.R;
import sa.devming.realrank.Utils;
import sa.devming.realrank.adapter.RRListItem;
import sa.devming.realrank.network.RRAsyncTask;

public class WidgetProvider extends AppWidgetProvider {

    private WidgetConfig widgetConfig;

    private static PendingIntent mSender;
    private static AlarmManager mManager;

    private void removePreviousAlarm() {
        if (mManager != null && mSender != null) {
            mSender.cancel();
            mManager.cancel(mSender);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            removePreviousAlarm();
            mSender = PendingIntent.getBroadcast(context, 0, intent, 0);
        }
    }

    @Override
    public void onDisabled(Context context) {
        removePreviousAlarm();
        Utils.deleteWidgetPreference(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateRealRank(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateRealRank(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId){
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // progressbar show
        remoteViews.setViewVisibility(R.id.widget_progressbar, View.VISIBLE);

        // 위젯 클리시 mainActivity 호출
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_body, pendingIntent);

        // 위젯 상단 클릭시 onUpdate() 호출
        Intent intentR = new Intent(context, WidgetProvider.class);
        intentR.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = new int[]{appWidgetId};
        intentR.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        PendingIntent pendingIntentR = PendingIntent.getBroadcast(context, (appWidgetId * -1), intentR, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_title, pendingIntentR);

        // 위젯 정보 갱신
        final ArrayList<RRListItem> naverArr = new ArrayList<>();
        final ArrayList<RRListItem> daumArr = new ArrayList<>();

        // 방해금지 시간이라면 callback 처리만 한다
        if (isDisturbTime(context)) {
            new Handler(context.getMainLooper()).post(() -> onSuccessCallback(naverArr, daumArr, remoteViews, context, appWidgetManager, appWidgetId));
        } else {
            // remoteViews 업데이트 1차
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            new RRAsyncTask(context, naverArr, daumArr, () -> onSuccessCallback(naverArr, daumArr, remoteViews, context, appWidgetManager,appWidgetId)).execute();
        }
    }

    private boolean isDisturbTime(final Context context) {
        Locale current = context.getResources().getConfiguration().locale;
        SimpleDateFormat sdf = new SimpleDateFormat("HH", current);
        Date currentTime = Calendar.getInstance().getTime();
        int hour = Integer.parseInt(sdf.format(currentTime));

        if (widgetConfig == null) {
            widgetConfig = Utils.getWidgetPreference(context);
        }

        boolean ret = false;

        if (widgetConfig.isDisturb()) {
            int from = widgetConfig.getDisturbFrom();
            int to = widgetConfig.getDisturbTo();
            if (from <= to) {
                // case 1 : from < to => 사이에 존재하면 true
                if (hour >= from && hour < to)
                    ret = true;
            } else {
                // case 2 : from > to => from 보다 작고 && to 보다 크면 false
                if (!(hour < from && hour >= to))
                    ret = true;
            }
        }
        if (ret) Log.w(Constants.TAG, String.format(">>>>> Skip time : %d ~ %d", widgetConfig.getDisturbFrom(), widgetConfig.getDisturbTo()));
        return ret;
    }

    private void onSuccessCallback(
            final ArrayList<RRListItem> naverArr, final ArrayList<RRListItem> daumArr, final RemoteViews remoteViews,
            final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        for (int i=0 ; i<naverArr.size() ; i++) {
            remoteViews.setTextViewText(context.getResources().getIdentifier("naverTV" + (i + 1), "id", context.getPackageName()), naverArr.get(i).getTitle());
        }
        for (int i=0 ; i<daumArr.size() ; i++) {
            remoteViews.setTextViewText(context.getResources().getIdentifier("daumTV" + (i + 1), "id", context.getPackageName()), daumArr.get(i).getTitle());
        }

        // 업데이트 시간 표시
        String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());
        String widgetTimeString = context.getResources().getString(R.string.time, timeString);
        remoteViews.setTextViewText(R.id.widget_time, widgetTimeString);
        Intent intent = new Intent(context, WidgetConfigActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent configIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_time_layout, configIntent);

        // progressbar gone
        remoteViews.setViewVisibility(R.id.widget_progressbar, View.GONE);

        // remoteViews 2차 업데이트
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        // set alarm
        if (widgetConfig.getInterval() != 0) {
            long next = System.currentTimeMillis() + (widgetConfig.getInterval() * 60 * 1000);
            mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (widgetConfig.isUpdateScreenOff()) {
                mManager.set(AlarmManager.RTC_WAKEUP, next, mSender);
            } else {
                mManager.set(AlarmManager.RTC, next, mSender);
            }
            Log.w(Constants.TAG, String.format("Update during Screen off = %b", widgetConfig.isUpdateScreenOff()));
            Log.w(Constants.TAG, String.format("DONE Widget Update and Set Alarm (interval = %d)", widgetConfig.getInterval()));
        } else {
            Log.w(Constants.TAG, "DONE Widget Update and NO Set Alarm (interval = 0)");
        }
    }
}
