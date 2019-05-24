/**
 *         Go to Sleep is an open source app to manage a healthy sleep schedule
 *         Copyright (C) 2019 Cole Gerdemann
 *
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.qianzuncheng.nowsleep;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;
import static com.qianzuncheng.nowsleep.SettingsFragment.isUsageAccessGranted;
import static com.qianzuncheng.nowsleep.utilities.BedtimeUtilities.getBedtimeCal;
import static com.qianzuncheng.nowsleep.utilities.BedtimeUtilities.parseBedtime;
import static com.qianzuncheng.nowsleep.utilities.Constants.BEDTIME_CHANNEL_ID;
import static com.qianzuncheng.nowsleep.utilities.Constants.BEDTIME_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.CURRENT_NOTIFICATION_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.DND_DELAY_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.DND_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.DO_NOT_DISTURB_ALARM_REQUEST_CODE;
import static com.qianzuncheng.nowsleep.utilities.Constants.DO_NOT_DISTURB_REQUEST_CODE;
import static com.qianzuncheng.nowsleep.utilities.Constants.INACTIVITY_TIMER_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.LAST_NOTIFICATION_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.LAUNCH_APP_REQUEST_CODE;
import static com.qianzuncheng.nowsleep.utilities.Constants.NEXT_NOTIFICATION_ALARM_REQUEST_CODE;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIFICATION_REQUEST_CODE;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIFICATION_SOUND_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIF_AMOUNT_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIF_DELAY_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.ONE_HOUR_MILLIS;
import static com.qianzuncheng.nowsleep.utilities.Constants.ONE_MINUTE_MILLIS;
import static com.qianzuncheng.nowsleep.utilities.Constants.SEND_ONE_NOTIFICATION;
import static com.qianzuncheng.nowsleep.utilities.Constants.SMART_NOTIFICATIONS_KEY;
import static com.qianzuncheng.nowsleep.utilities.NotificationUtilites.createNotificationChannel;
import static com.qianzuncheng.nowsleep.utilities.NotificationUtilites.setNextDayNotification;

public class BedtimeNotificationReceiver extends BroadcastReceiver {


    private int DnD_delay = 2; //in minutes

    private Calendar bedtime;
    private int numNotifications;
    private int notificationDelay;
    private int userActiveMargin;
    private boolean adsEnabled;
    private boolean advancedOptionsPurchased;
    private boolean smartNotifications;
    private boolean autoDND;
    private boolean userActive = true;
    private final String TAG = "bedtimeNotifReceiver";
    private int currentNotification;

    private boolean notificationSoundsEnabled = false;
    private boolean sendOneNotification = false;
    private long lastNotification;
    private UsageStatsManager usageStatsManager;
    private String[] notifications = new String[5];

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        //bedtime = Calendar.getInstance();
        bedtime = getBedtimeCal(parseBedtime(settings.getString(BEDTIME_KEY, "22:00")));
        numNotifications = Integer.parseInt(settings.getString(NOTIF_AMOUNT_KEY, 3 + ""));
        notificationDelay = Integer.parseInt(settings.getString(NOTIF_DELAY_KEY, 15 + ""));

        autoDND = settings.getBoolean(DND_KEY, false);
        smartNotifications = settings.getBoolean(SMART_NOTIFICATIONS_KEY, false);
        currentNotification = settings.getInt(CURRENT_NOTIFICATION_KEY, 1);
        lastNotification = settings.getLong(LAST_NOTIFICATION_KEY, System.currentTimeMillis());
        userActiveMargin = Integer.parseInt(settings.getString(INACTIVITY_TIMER_KEY, "5"));
        DnD_delay = Integer.parseInt(settings.getString(DND_DELAY_KEY, "2"));
        notificationSoundsEnabled = settings.getBoolean(NOTIFICATION_SOUND_KEY, false);
        sendOneNotification = settings.getBoolean(SEND_ONE_NOTIFICATION, false);


        createNotificationChannel(context);

        for (int i = 0; i < notifications.length; i++) {
            notifications[i] = settings.getString("pref_notification" + (i + 1), "");
        }

        if (currentNotification == 1){
            lastNotification = System.currentTimeMillis();
        }

        if (isUsageAccessGranted(context) && smartNotifications) { //if any of these are not met, code will fall back to normal notifications
            //smart notification code block
            if (isUserActive(lastNotification, System.currentTimeMillis()) && (System.currentTimeMillis() - bedtime.getTimeInMillis() < 6 * ONE_HOUR_MILLIS)) {
                showNotification(context, getNotificationTitle(), getNotificationContent(context));
                settings.edit().putLong(LAST_NOTIFICATION_KEY, System.currentTimeMillis()).apply();
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply();
                setNextNotification(context);
                Log.d(TAG, "onReceive: SMART show notification!");
            } else {
                if (sendOneNotification && currentNotification == 1){
                    showNotification(context, getNotificationTitle(), getNotificationContent(context));
                    Log.d(TAG, "onReceive: SMART ALWAYS ONE show notification!");
                }
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
                if (autoDND) {
                    enableDoNotDisturb(context);
                    Log.d(TAG, "onReceive: SMART DND on!");
                }
                //cancelNextNotification(context);
                setNextDayNotification(context, bedtime, TAG);
                Log.d(TAG, "onReceive: SMART DONE!\n\n\n");
            }
        } else {
            //normal notification code block
            showNotification(context, getNotificationTitle(), getNotificationContent(context));
            if (currentNotification < numNotifications) {
                setNextNotification(context);
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, currentNotification + 1).apply();
                Log.d(TAG, "onReceive: NORMAL show notification!");
            } else if (currentNotification == numNotifications) {
                settings.edit().putInt(CURRENT_NOTIFICATION_KEY, 1).apply();
                if (autoDND) {
                    enableDoNotDisturb(context);
                    Log.d(TAG, "onReceive: NORMAL DND on!");
                }
                //cancelNextNotification(context);
                setNextDayNotification(context, bedtime, TAG);
                Log.d(TAG, "onReceive: NORMAL DONE!\n\n\n");
            }
        }
    }

    private boolean isUserActive(long startTime, long currentTime){
        String TAG = "isUserActive";
        if (currentNotification == 1){
            startTime = startTime - notificationDelay * ONE_MINUTE_MILLIS;
        }

        //#TODO experiment with using a daily interval (make sure it works past midnight)
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, startTime, currentTime);

        UsageStats minUsageStat = null;

        long min = Long.MAX_VALUE;
        for (UsageStats usageStat : queryUsageStats){
            if ((System.currentTimeMillis() - usageStat.getLastTimeStamp() < min) && (usageStat.getTotalTimeInForeground() > ONE_MINUTE_MILLIS) && !usageStat.getPackageName().equals("com.qianzuncheng.nowsleep")){  //make sure app has been in foreground for more than one minute to filter out background apps
                minUsageStat = usageStat;
                min = System.currentTimeMillis() - usageStat.getLastTimeStamp();
            }
        }

        if (minUsageStat != null) {
            Log.d(TAG, minUsageStat.getPackageName() + " last time used: " + minUsageStat.getLastTimeUsed() + " time in foreground: " + minUsageStat.getTotalTimeInForeground());
            Log.d(TAG, "getLastTimeStamp: " + minUsageStat.getLastTimeStamp() + " getLastUsed: " + minUsageStat.getLastTimeUsed() + " current time: " + System.currentTimeMillis());
            Log.d(TAG, (System.currentTimeMillis() - minUsageStat.getLastTimeUsed() <= userActiveMargin * ONE_MINUTE_MILLIS) + "");
            return System.currentTimeMillis() - minUsageStat.getLastTimeStamp() <= userActiveMargin * ONE_MINUTE_MILLIS;
        } else {
            Log.e(TAG, "minUsageStat was null!");
            Log.e(TAG, queryUsageStats.toString());
            return false;
        }
    }

    private void enableDoNotDisturb(Context context){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + (ONE_MINUTE_MILLIS * DnD_delay));
        Log.d(TAG, "Setting auto DND for " + DnD_delay + " minutes from now: " + calendar.getTime());

        Intent intent1 = new Intent(context, AutoDoNotDisturbReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                DO_NOT_DISTURB_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void showNotification(Context context, String title, String content) {
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, LAUNCH_APP_REQUEST_CODE, intent, 0);
        Intent snoozeIntent = new Intent(context, AutoDoNotDisturbReceiver.class);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, DO_NOT_DISTURB_REQUEST_CODE, snoozeIntent, 0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if the notification policy access has been granted for the app.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, BEDTIME_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_moon_notification)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setColorized(true)
                .setColor(context.getResources().getColor(R.color.moonPrimary));
                //.setSound(RingtoneManager
                //.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                //.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.addAction(R.drawable.ic_do_not_disturb, context.getString(R.string.notifAction), snoozePendingIntent);

        Notification notification = mBuilder.build();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (notificationSoundsEnabled) {  //if device does not support notification channels check if notification sound is enabled
                notification.defaults |= Notification.DEFAULT_SOUND;
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            } else {
                notification.defaults &= ~Notification.DEFAULT_SOUND;
                notification.defaults &= ~Notification.DEFAULT_VIBRATE;
            }
        }

        notificationManager.notify(NOTIFICATION_REQUEST_CODE, notification);

    }

    private String getNotificationContent(Context context) {
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(System.currentTimeMillis());

        Date endDate;
        Date startDate;

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        Log.d(TAG, "current second " + current.get(Calendar.SECOND));
        current.set(Calendar.SECOND, 0);

        startDate = bedtime.getTime();
        endDate = current.getTime();

        Log.d(TAG, bedtime.getTime() + " bedtime");

        Log.d(TAG, current.getTime() + " current time");


        long difference = endDate.getTime() - startDate.getTime();
        if (difference < 0) {
            try {
                Date dateMax = simpleDateFormat.parse("24:00");
                Date dateMin = simpleDateFormat.parse("00:00");
                difference = (dateMax.getTime() - startDate.getTime()) + (endDate.getTime() - dateMin.getTime());
            } catch (ParseException e) {
                Log.e(TAG, e + "");
            }
        }
        int min = (int) Math.round(((double) difference / 60000)); //divide time in milliseconds by 60 000 to get minutes



        Log.d(TAG, "currentNotification: " + currentNotification);
        if (Locale.getDefault().toString().toLowerCase().contains("pl")) {
            if (currentNotification == 1) {
                return context.getString(R.string.notifTitleFirst);
            } else if (min == 1) {
                return String.format(context.getString(R.string.notifTitleSingular), min);
            } else if(min >= 2 && min <= 4){
                return String.format(context.getString(R.string.notifTitleFunky), min);
            } else {
                return String.format(context.getString(R.string.notifTitlePlural), min);
            }
        } else {
            if (currentNotification == 1) {
                return context.getString(R.string.notifTitleFirst);
            } else if (min == 1) {
                return String.format(context.getString(R.string.notifTitleSingular), min);
            } else {
                return String.format(context.getString(R.string.notifTitlePlural), min);
            }
        }
    }

    private String getNotificationTitle(){
        int notificationTitleIndex = currentNotification - 1;
        if (notificationTitleIndex > notifications.length){
            while (notificationTitleIndex > notifications.length){
                notificationTitleIndex = notificationTitleIndex - 5;

            }
        }
        try {
            return notifications[notificationTitleIndex];
        } catch (ArrayIndexOutOfBoundsException e){
            Log.e(TAG, "getNotificationTitle: ",e);
            return  notifications[0];
        }
    }

    private void setNextNotification(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + notificationDelay * 60000);
        Log.d(TAG, "Setting next notification in " + notificationDelay + " minutes");

        Intent intent1 = new Intent(context, BedtimeNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                NEXT_NOTIFICATION_ALARM_REQUEST_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }




}
