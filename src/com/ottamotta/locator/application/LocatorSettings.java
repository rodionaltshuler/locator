package com.ottamotta.locator.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.greenrobot.event.EventBus;

@Singleton
public class LocatorSettings {

    public static final int AUTO_ANSWER_ON = 0;
    public static final int AUTO_ANSWER_OFF = 1;

    @Inject
    private Context context;

    @Inject
    EventBus bus;

    private final String KEY_AUTO_ANSWER = "autoAnswer";
    private final int KEY_AUTO_ANSWER_DEFAULT_VALUE = 0;

    public int getAutoAnswerSetting() {
        SharedPreferences prefs = getPrefs();
        int result = prefs.getInt(KEY_AUTO_ANSWER, KEY_AUTO_ANSWER_DEFAULT_VALUE);
        return result;
    }

    public void setAutoAnswerSetting(int newSetting) {
        SharedPreferences prefs = getPrefs();
        int oldSetting = prefs.getInt(KEY_AUTO_ANSWER, KEY_AUTO_ANSWER_DEFAULT_VALUE);
        if (oldSetting != newSetting) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_AUTO_ANSWER, newSetting);
            editor.commit();
            bus.post(new AutoAnswerSettingChangedEvent(newSetting));
        }

    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static class AutoAnswerSettingChangedEvent {
        public int autoAnswer;

        public AutoAnswerSettingChangedEvent(int newSetting) {
            autoAnswer = newSetting;
        }
    }

}
