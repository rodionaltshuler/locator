package com.ottamotta.locator.appRater.apprater;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.ottamotta.locator.R;
import com.ottamotta.locator.ui.dialogs.RateAppDialogFragment;

public class AppRater {
    // Preference Constants
    private final static String PREF_NAME = "apprater";
    private final static String PREF_LAUNCH_COUNT = "launch_count";
    private final static String PREF_FIRST_LAUNCHED = "date_firstlaunch";
    private final static String PREF_DONT_SHOW_AGAIN = "dontshowagain";
    private final static String PREF_REMIND_LATER = "remindmelater";
    private final static String PREF_APP_VERSION_NAME = "app_version_name";
    private final static String PREF_APP_VERSION_CODE = "app_version_code";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;
    private static int DAYS_UNTIL_PROMPT_FOR_REMIND_LATER = 3;
    private static int LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER = 7;
    private static boolean isDark;
    private static boolean themeSet;
    private static boolean hideNoButton;
    private static boolean isVersionNameCheckEnabled;
    private static boolean isVersionCodeCheckEnabled;

    private static Market market = new GoogleMarket();

    /**
     * Decides if the version name check is active or not
     *
     * @param versionNameCheck
     */
    public static void setVersionNameCheckEnabled(boolean versionNameCheck) {
        isVersionNameCheckEnabled = versionNameCheck;
    }

    /**
     * Decides if the version code check is active or not
     *
     * @param versionCodeCheck
     */
    public static void setVersionCodeCheckEnabled(boolean versionCodeCheck) {
        isVersionCodeCheckEnabled = versionCodeCheck;
    }

    /**
     * sets number of day until rating dialog pops up for next time when remind
     * me later option is chosen
     *
     * @param daysUntilPromt
     */
    public static void setNumDaysForRemindLater(int daysUntilPromt) {
        DAYS_UNTIL_PROMPT_FOR_REMIND_LATER = daysUntilPromt;
    }

    /**
     * sets the number of launches until the rating dialog pops up for next time
     * when remind me later option is chosen
     *
     * @param launchesUntilPrompt
     */
    public static void setNumLaunchesForRemindLater(int launchesUntilPrompt) {

        LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER = launchesUntilPrompt;
    }

    /**
     * decides if No thanks button appear in dialog or not
     *
     * @param isNoButtonVisible
     */
    public static void setDontRemindButtonVisible(boolean isNoButtonVisible) {
        AppRater.hideNoButton = isNoButtonVisible;
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt using the specified or default day, launch count
     * values and checking if the version is changed or not
     *
     * @param context
     */
    public static void app_launched(FragmentActivity context) {
        app_launched(context, DAYS_UNTIL_PROMPT, LAUNCHES_UNTIL_PROMPT);
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt using the specified or default day, launch count
     * values with additional day and launch parameter for remind me later option
     * and checking if the version is changed or not
     *
     * @param context
     * @param daysUntilPrompt
     * @param launchesUntilPrompt
     * @param daysForRemind
     * @param launchesForRemind
     */
    public static void app_launched(FragmentActivity context, int daysUntilPrompt, int launchesUntilPrompt, int daysForRemind, int launchesForRemind) {
        setNumDaysForRemindLater(daysForRemind);
        setNumLaunchesForRemindLater(launchesForRemind);
        app_launched(context, daysUntilPrompt, launchesUntilPrompt);
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt
     *
     * @param context
     * @param daysUntilPrompt
     * @param launchesUntilPrompt
     */
    public static void app_launched(FragmentActivity context, int daysUntilPrompt, int launchesUntilPrompt) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
        int days;
        int launches;
        if (isVersionNameCheckEnabled) {
            if (!ratingInfo.getApplicationVersionName().equals(prefs.getString(PREF_APP_VERSION_NAME, "none"))) {
                editor.putString(PREF_APP_VERSION_NAME, ratingInfo.getApplicationVersionName());
                resetData(context);
                commitOrApply(editor);
            }
        }
        if (isVersionCodeCheckEnabled) {
            if (ratingInfo.getApplicationVersionCode() != (prefs.getInt(PREF_APP_VERSION_CODE, -1))) {
                editor.putInt(PREF_APP_VERSION_CODE, ratingInfo.getApplicationVersionCode());
                resetData(context);
                commitOrApply(editor);
            }
        }
        if (prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
            return;
        } else if (prefs.getBoolean(PREF_REMIND_LATER, false)) {
            days = DAYS_UNTIL_PROMPT_FOR_REMIND_LATER;
            launches = LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER;
        } else {
            days = daysUntilPrompt;
            launches = launchesUntilPrompt;
        }

        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);
        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(PREF_FIRST_LAUNCHED, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        }
        // Wait for at least the number of launches or the number of days used
        // until prompt
        if (launch_count >= launches) {
            if (System.currentTimeMillis() >= date_firstLaunch + (days * 24 * 60 * 601000)) {
                showRateAlertDialog(context, editor);
            }
        }
        commitOrApply(editor);
    }

    /**
     * Call this method directly if you want to force a rate prompt, useful for
     * testing purposes
     *
     * @param context
     */
    public static void showRateDialog(final FragmentActivity context) {
        showRateAlertDialog(context, null);
    }

    /**
     * Call this method directly to go straight to play store listing for rating
     *
     * @param context
     */
    public static void rateNow(final Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, market.getMarketURI(context)));
        } catch (ActivityNotFoundException activityNotFoundException1) {
            Log.e(AppRater.class.getSimpleName(), "Market Intent not found");
        }
    }

    /**
     * Set an alternate Market, defaults to Google Play
     *
     * @param market
     */
    public static void setMarket(Market market) {
        AppRater.market = market;
    }

    /**
     * Get the currently set Market
     *
     * @return market
     */
    public static Market getMarket() {
        return market;
    }

    /**
     * Sets dialog theme to dark
     */
    @TargetApi(11)
    public static void setDarkTheme() {
        isDark = true;
        themeSet = true;
    }

    /**
     * Sets dialog theme to light
     */
    @TargetApi(11)
    public static void setLightTheme() {
        isDark = false;
        themeSet = true;
    }

    /**
     * The meat of the library, actually shows the rate prompt dialog
     */
    private static void showRateAlertDialog(final FragmentActivity context, final SharedPreferences.Editor editor) {
        ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
        String title = String.format(context.getString(R.string.dialog_title), ratingInfo.getApplicationName());
        String message = context.getString(R.string.rate_message);
        String posButton = context.getString(R.string.rate);
        String negButton = context.getString(R.string.later);
        RateAppDialogFragment f = RateAppDialogFragment.newInstance(title, message, posButton, negButton);
        f.setEditor(editor);
        f.show(context.getSupportFragmentManager(), "rateApp");
    }

    @SuppressLint("NewApi")
    private static void commitOrApply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT > 8) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static void resetData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
        editor.putBoolean(PREF_REMIND_LATER, false);
        editor.putLong(PREF_LAUNCH_COUNT, 0);
        long date_firstLaunch = System.currentTimeMillis();
        editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        commitOrApply(editor);
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void onRateNowPressed(Context context) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(AppRater.PREF_DONT_SHOW_AGAIN, true);
        commitOrApply(editor);
    }

    public static void onRateLaterPressed(Context context) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        Long date_firstLaunch = System.currentTimeMillis();
        editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        editor.putLong(PREF_LAUNCH_COUNT, 0);
        editor.putBoolean(PREF_REMIND_LATER, true);
        editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
        commitOrApply(editor);
    }
}