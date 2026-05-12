package com.nci.fiuza.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class DurationUtils {

    private DurationUtils() {
    }

    //get duration description (eg. 75 = 1h 15min)
    public static String formatMinutes(Integer minutes) {

        if (minutes == null) {
            return "";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours > 0 && remainingMinutes > 0) {
            return hours + "h " + remainingMinutes + "min";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return remainingMinutes + "min";
        }

    }

    //returns a list of service duration options
    public static Map<Integer, String> getServiceDurationOptions() {

        //key,value would be duration and description
        Map<Integer, String> durations = new LinkedHashMap<>();

        //loops from the appointment interval minutes until the service max duration, step is appointment interval minutes (15)
        for (int minutes = BusinessHours.APPOINTMENT_INTERVAL_MINUTES; minutes <= BusinessHours.SERVICE_MAX_DURATION_MINUTES; minutes += BusinessHours.APPOINTMENT_INTERVAL_MINUTES) {
            durations.put(minutes, formatMinutes(minutes));
        }

        return durations;
    }

}
