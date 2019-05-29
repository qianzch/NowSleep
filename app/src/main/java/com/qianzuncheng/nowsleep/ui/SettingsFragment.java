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

package com.qianzuncheng.nowsleep.ui;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.qianzuncheng.nowsleep.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.qianzuncheng.nowsleep.utilities.BedtimeUtilities.parseBedtime;
import static com.qianzuncheng.nowsleep.utilities.Constants.ADDITIONAL_NOTIFICATION_SETTINGS_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.ADVANCED_OPTIONS_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.BEDTIME_CHANNEL_ID;
import static com.qianzuncheng.nowsleep.utilities.Constants.BEDTIME_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.CUSTOM_NOTIFICATIONS_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.DEBUG;
import static com.qianzuncheng.nowsleep.utilities.Constants.DEBUG_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.DND_DELAY_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.DND_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.INACTIVITY_TIMER_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIFICATION_1_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIFICATION_2_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIFICATION_3_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIFICATION_4_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIFICATION_5_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIFICATION_SOUND_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIF_AMOUNT_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIF_DELAY_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.NOTIF_ENABLE_KEY;
import static com.qianzuncheng.nowsleep.utilities.Constants.SMART_NOTIFICATIONS_KEY;
import static com.qianzuncheng.nowsleep.utilities.NotificationUtilites.setNotifications;
import static com.qianzuncheng.nowsleep.utilities.NotificationUtilites.setNotifications_DEBUG;


public class SettingsFragment extends BasePreferenceFragmentCompat {

    private boolean smartNotificationsEnabled;
    private NotificationManager notificationManager;
    private UsageStatsManager usageStatsManager;
    private SharedPreferences sharedPreferences;
    private final String TAG = "SettingsFragment";

    private boolean desiredSmartNotificationValue = false;
    private boolean desiredDoNotDisturbValue = false;

    private String rootKey;

    private PreferenceScreen preferenceScreen;

    private boolean usageSettingsOpened = false;
    private boolean doNotDisturbSettingsOpened = false;

    private Preference customNotificationsPref;
    private Preference autoDnDPref;
    private Preference smartNotificationsPref;
    private Preference inactivityTimerPref;
    private Preference notificationAmount;
    private Preference notificationDelay;
    private Preference delayDnDPref;
    private Preference notificationChannel;
    private Preference notificationSound;
    private Preference bedtime;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        this.rootKey = rootKey;
        sharedPreferences = getPreferenceManager().getSharedPreferences();
        notificationManager = (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.NOTIFICATION_SERVICE);
        usageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);

        customNotificationsPref = this.findPreference(CUSTOM_NOTIFICATIONS_KEY);
        autoDnDPref = this.findPreference(DND_KEY);
        smartNotificationsPref = this.findPreference(SMART_NOTIFICATIONS_KEY);
        inactivityTimerPref = this.findPreference(INACTIVITY_TIMER_KEY);
        notificationAmount = this.findPreference(NOTIF_AMOUNT_KEY);
        notificationDelay = this.findPreference(NOTIF_DELAY_KEY);
        delayDnDPref = this.findPreference(DND_DELAY_KEY);
        notificationChannel = this.findPreference(ADDITIONAL_NOTIFICATION_SETTINGS_KEY);
        notificationSound = this.findPreference(NOTIFICATION_SOUND_KEY);
        bedtime = this.findPreference(BEDTIME_KEY);

        // pre-req
        smartNotificationsEnabled = sharedPreferences.getBoolean(SMART_NOTIFICATIONS_KEY, false);

        // root settings
        if (rootKey == null) {
            // DEBUG
            if(!DEBUG) {
                PreferenceCategory advancedOptions = (PreferenceCategory) findPreference(ADVANCED_OPTIONS_KEY);
                advancedOptions.removePreference(findPreference(DEBUG_KEY));
            } else  { // DEBUG
                findPreference(DEBUG_KEY).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        setNotifications_DEBUG(false, sharedPreferences.getBoolean(NOTIF_ENABLE_KEY, false),
                                parseBedtime(sharedPreferences.getString(BEDTIME_KEY, "22:00")),
                                Integer.parseInt(sharedPreferences.getString(NOTIF_DELAY_KEY, "10")),
                                Integer.parseInt(sharedPreferences.getString(NOTIF_AMOUNT_KEY, "1")), getContext());
                        return false;
                    }
                });
            }
            createPrefCommon();
            createPrefNotification();
            createPrefAdvanced();
        }
        // custom notification setting (nested setting)
        else if(rootKey.equals(CUSTOM_NOTIFICATIONS_KEY)) {
           startCustomNotificationsScreen();
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.categoryCustomNotificationsTitle));
        }
    }

    private void createPrefCommon() {
        // bedtime
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            Date time = simpleDateFormat.parse(sharedPreferences.getString(BEDTIME_KEY, "22:00"));
            bedtime.setSummary(String.format(Locale.US, getString(R.string.settingsBedtimeSummary), DateFormat.getTimeInstance(DateFormat.SHORT).format(time)));
        } catch (ParseException e) {
            e.printStackTrace();
            bedtime.setSummary(String.format(Locale.US, getString(R.string.settingsBedtimeSummary), sharedPreferences.getString(BEDTIME_KEY, "22:00")));
        }

        // auto DND
        delayDnDPref.setEnabled(sharedPreferences.getBoolean(DND_KEY, false));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.findPreference(DND_KEY).setEnabled(false);
            this.findPreference(DND_KEY).setSummary(R.string.settingsAutoDnDLowAndroidVersionMessage);
        } else if (Build.MANUFACTURER.equalsIgnoreCase("lg") && Build.MODEL.equalsIgnoreCase("g4")){
            this.findPreference(DND_KEY).setEnabled(false);
            this.findPreference(DND_KEY).setSummary(R.string.settingsAutoDnD_LG_G4_message);
        } else {
            autoDnDPref.setOnPreferenceChangeListener((preference, newValue) -> {
                // TODO: what's this?
                if (!desiredDoNotDisturbValue) {
                    if ((boolean) newValue) {
                        if (!notificationManager.isNotificationPolicyAccessGranted()) {
                            doNotDisturbSettingsOpened = true;
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(intent);
                            return false;
                        } else  {
                            delayDnDPref.setEnabled(true);
                        }
                        return true;
                    } else if (!(boolean) newValue) {
                        delayDnDPref.setEnabled(false);
                        return true;
                    }
                } else if ((boolean) newValue) {
                    delayDnDPref.setEnabled(true);
                    doNotDisturbSettingsOpened = false;
                    return true;
                } else {
                    delayDnDPref.setEnabled(false);
                    doNotDisturbSettingsOpened = false;
                    desiredDoNotDisturbValue = false;
                    return true;
                }
                return true;
            });
        }

        // tap to hide
    }

    private void createPrefNotification() {
        // enable notification

        // notification sound & channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationSound.setVisible(false);
            notificationChannel.setVisible(true);
            notificationChannel.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName())
                        .putExtra(Settings.EXTRA_CHANNEL_ID, BEDTIME_CHANNEL_ID);
                startActivity(intent);
                return true;
            });
        } else {
            notificationSound.setVisible(true);
            notificationChannel.setVisible(false);
        }

        // notification interval
        notificationDelay.setSummary(String.format(Locale.US, getString(R.string.settingsNotificationDelaySummary), sharedPreferences.getString(NOTIF_DELAY_KEY, "15")));
        notificationDelay.setOnPreferenceChangeListener((preference, newValue) -> {
            notificationDelay.setSummary(String.format(Locale.US, getString(R.string.settingsNotificationDelaySummary), newValue));
            return true;
        });

        // notification amount
        notificationAmount.setEnabled(!smartNotificationsEnabled);
        setNotificationAmountSummary(Integer.parseInt(sharedPreferences.getString(NOTIF_AMOUNT_KEY, "3")));
        notificationAmount.setOnPreferenceChangeListener((preference, newValue) -> {
            setNotificationAmountSummary(Integer.parseInt((String)newValue));
            return true;
        });

    }

    private void createPrefAdvanced() {
        // smart notification
        smartNotificationsPref.setSummary(R.string.settingsSmartNotificationsSummaryEnabled);
        smartNotificationsPref.setOnPreferenceChangeListener((preference, newValue) -> {
            // TODO: what's this?
            if (!desiredSmartNotificationValue) {
                if ((boolean) newValue) {
                    if (!isUsageAccessGranted(getContext())) {
                        usageSettingsOpened = true;
                        Intent usageSettings = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        startActivity(usageSettings);
                        return false;
                    } else {
                        notificationAmount.setEnabled(false);
                    }
                    return true;
                } else if (!(boolean) newValue) {
                    notificationAmount.setEnabled(true);
                    return true;
                }
            } else if ((boolean) newValue) {
                notificationAmount.setEnabled(false);
                usageSettingsOpened = false;
                return true;

            } else {
                notificationAmount.setEnabled(true);
                usageSettingsOpened = false;
                desiredSmartNotificationValue = false;
                return true;
            }
            return true;
        });

        // inactive
        setInactivityTimerSummary(Integer.parseInt(sharedPreferences.getString(INACTIVITY_TIMER_KEY, "5")));
        inactivityTimerPref.setOnPreferenceChangeListener(((preference, newValue) -> {
            setInactivityTimerSummary(Integer.parseInt((String) newValue));
            return true;
        }));

        // always notification

        // DND delay
        setDNDSummary(Integer.parseInt(sharedPreferences.getString(DND_DELAY_KEY, "2")));
        delayDnDPref.setOnPreferenceChangeListener((preference, newValue) -> {
            setDNDSummary(Integer.parseInt((String)newValue));
            return true;
        });

        // customized notification
    }

    private void setDNDSummary(int DNDDelay) {
        if (DNDDelay == 1) {
            autoDnDPref.setSummary(R.string.settingsAutoDnDSummarySingular);
            delayDnDPref.setSummary(R.string.settingsDelayDnDSummarySingular);
        } else {
            autoDnDPref.setSummary(String.format(getString(R.string.settingsAutoDnDSummaryPlural), sharedPreferences.getString(DND_DELAY_KEY, "2")));
            delayDnDPref.setSummary(String.format(getString(R.string.settingsDelayDnDSummaryPlural), sharedPreferences.getString(DND_DELAY_KEY, "2")));
        }
    }

    private void setNotificationAmountSummary(int notificationAmountNum) {
        if (notificationAmountNum == 1) {
            notificationAmount.setSummary(R.string.settingsNotificationAmountSingular);
        } else {
            notificationAmount.setSummary(String.format(Locale.getDefault(), getString(R.string.settingsNotificationAmountPlural), "" + notificationAmountNum));
        }
    }

    private void setInactivityTimerSummary(int inactivityTimer) {
        if (inactivityTimer == 1) {
            inactivityTimerPref.setSummary(R.string.settingsInactivityTimerSummarySingular);
        } else {
            inactivityTimerPref.setSummary(String.format(Locale.getDefault(), getString(R.string.settingsInactivityTimerSummaryPlural), "" + inactivityTimer));
        }
    }



    public static boolean isUsageAccessGranted(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void startCustomNotificationsScreen(){


        final Preference notification1 = this.findPreference(NOTIFICATION_1_KEY);
        final Preference notification2 = this.findPreference(NOTIFICATION_2_KEY);
        final Preference notification3 = this.findPreference(NOTIFICATION_3_KEY);
        final Preference notification4 = this.findPreference(NOTIFICATION_4_KEY);
        final Preference notification5 = this.findPreference(NOTIFICATION_5_KEY);

        notification1.setSummary(sharedPreferences.getString(NOTIFICATION_1_KEY, getString(R.string.notification1)));
        notification2.setSummary(sharedPreferences.getString(NOTIFICATION_2_KEY, getString(R.string.notification2)));
        notification3.setSummary(sharedPreferences.getString(NOTIFICATION_3_KEY, getString(R.string.notification3)));
        notification4.setSummary(sharedPreferences.getString(NOTIFICATION_4_KEY, getString(R.string.notification4)));
        notification5.setSummary(sharedPreferences.getString(NOTIFICATION_5_KEY, getString(R.string.notification5)));

        notification1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                notification1.setSummary((String) newValue);
                return true;
            }
        });

        notification2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                notification2.setSummary((String) newValue);
                return true;
            }
        });

        notification3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                notification3.setSummary((String) newValue);
                return true;
            }
        });

        notification4.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                notification4.setSummary((String) newValue);
                return true;
            }
        });

        notification5.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                notification5.setSummary((String) newValue);
                return true;
            }
        });
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
        // or you can return the parent fragment if it's handling the screen navigation,
        // however, in that case you need to traverse to the implementing parent fragment
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onResume(){
        Log.d("settings", "onResume called!");
        //COMPILE INSTRUCTIONS: comment out the following line
        //bp.loadOwnedPurchasesFromGoogle();

        if (rootKey == null){
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.settings));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (sharedPreferences.getBoolean(DND_KEY, false) && !notificationManager.isNotificationPolicyAccessGranted()){
                sharedPreferences.edit().putBoolean(DND_KEY, false).apply();
                desiredDoNotDisturbValue = false;
                Toast.makeText(getContext(), getString(R.string.settingsDnDPermDeniedToast), Toast.LENGTH_LONG).show();
                getPreferenceScreen().findPreference(DND_KEY).performClick();
            } else if (notificationManager.isNotificationPolicyAccessGranted() && doNotDisturbSettingsOpened){
                desiredDoNotDisturbValue = true;
                getPreferenceScreen().findPreference(DND_KEY).performClick();
            }
        }

        if (sharedPreferences.getBoolean(SMART_NOTIFICATIONS_KEY, false) && !isUsageAccessGranted(getContext())) {
            sharedPreferences.edit().putBoolean(SMART_NOTIFICATIONS_KEY, false).apply();
            desiredSmartNotificationValue = false;
            Toast.makeText(getContext(), getString(R.string.settingsUsagePermDeniedToast), Toast.LENGTH_LONG).show();
            getPreferenceScreen().findPreference(SMART_NOTIFICATIONS_KEY).performClick();
        } else if (isUsageAccessGranted(getContext()) && usageSettingsOpened){
            desiredSmartNotificationValue = true;
            getPreferenceScreen().findPreference(SMART_NOTIFICATIONS_KEY).performClick();
        }

        super.onResume();
    }

    @Override
    public void onPause(){
        setNotifications(false, sharedPreferences.getBoolean(NOTIF_ENABLE_KEY, false),
                parseBedtime(sharedPreferences.getString(BEDTIME_KEY, "22:00")),
                Integer.parseInt(sharedPreferences.getString(NOTIF_DELAY_KEY, "10")),
                Integer.parseInt(sharedPreferences.getString(NOTIF_AMOUNT_KEY, "1")), getContext());
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

