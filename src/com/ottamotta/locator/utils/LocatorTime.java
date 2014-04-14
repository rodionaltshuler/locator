package com.ottamotta.locator.utils;

import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class LocatorTime implements Comparable<LocatorTime> {

    public enum DaysAgoGroup {
        TODAY, YESTERDAY, EARLIER
    }

    public DaysAgoGroup daysAgoGroup;
    public String dateFormatted;
    public String daysAgoString;
    long originalTime;

    public LocatorTime(long time) {
        this.originalTime = time;
    }

    public String getTimeElapsedFormatted() {
        return (String) DateUtils.getRelativeTimeSpanString(originalTime, System.currentTimeMillis(), 0);
    }

    public DaysAgoGroup getDaysAgoGroup() {

        long now = System.currentTimeMillis();

        Date prevDate = new Date(originalTime);
        Date nowDate = new Date(now);

        if (originalTime > now)
            return DaysAgoGroup.TODAY; //Message from FUTURE!

        if (nowDate.getDay() == prevDate.getDay() && nowDate.getYear() == prevDate.getYear()) {
            return DaysAgoGroup.TODAY;
        }

        if (prevDate.getDay() + 1 == nowDate.getDay() && nowDate.getYear() == prevDate.getYear()) {
            return DaysAgoGroup.YESTERDAY;
        }

        if (nowDate.getYear() == prevDate.getYear() + 1 && nowDate.getDay() == 1 && prevDate.getDay() == getDaysInYear(originalTime)) {
            return DaysAgoGroup.YESTERDAY;
        }

        return DaysAgoGroup.EARLIER;

    }

    private int getDaysInYear(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(time));
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int compareTo(LocatorTime another) {
        if (originalTime > another.originalTime)
            return 1;
        else
            return -1;
    }

}
