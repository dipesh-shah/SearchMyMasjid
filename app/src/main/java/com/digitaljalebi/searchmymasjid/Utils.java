package com.digitaljalebi.searchmymasjid;

import android.text.TextUtils;

/**
 * Created by dipesh on 09/08/17.
 */

public class Utils {

    public static void populateMasjidDisplayTimings(MasjidsModel model) {
        if (model.timings != null) {
            String na = "n/a";
            for (int i = 0; i < 6; i++) {

                if (!TextUtils.isEmpty(model.timings[i])) {
                    String timing;
                    String ampm;
                    int timingHourInt;
                    int timingMinInt;
                    try {
                        timingHourInt = Integer.parseInt(model.timings[i].substring(0, 2));
                        timingMinInt = Integer.parseInt(model.timings[i].substring(3, 5));
                    } catch (Exception e) {
                        timingHourInt = Integer.parseInt(model.timings[i].substring(0, 1));
                        timingMinInt = Integer.parseInt(model.timings[i].substring(2, 4));
                    }
                    ampm = "am";
                    if (timingHourInt >= 12) {
                        ampm = "pm";
                        if (timingHourInt > 12) {
                            timingHourInt -= 12;
                        }
                    }
                    timing = timingHourInt + ":" + timingMinInt;
                    model.displayTimings[i] = timing;
                    model.amPmArray[i] = ampm;
                }
                else {
                    model.displayTimings[i] = na;
                    model.amPmArray[i] = "";
                }
            }
        }
    }
}
