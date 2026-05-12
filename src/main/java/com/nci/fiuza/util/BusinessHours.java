package com.nci.fiuza.util;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

//utility class to set up the business opening days and hours
public class BusinessHours {

    //setting the business opening and closing time
    public static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    public static final LocalTime CLOSING_TIME = LocalTime.of(18, 0);

    //setting the appointment interval in minutes
    public static final int APPOINTMENT_INTERVAL_MINUTES = 15;

    public static final int SERVICE_MAX_DURATION_MINUTES = 720; //12 hours

    //setting the business opening days
    public static final Set<DayOfWeek> OPEN_DAYS = Set.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
    );

    //constructor
    private BusinessHours() {
    }

    //method to return opening days and hours as text
    public static String getOpeningHoursText(Locale locale) {

        //get time format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        //get all the open days from the set, sort, and get the display name, short and translated
        String daysText = OPEN_DAYS.stream()
                .sorted()
                .map(day -> day.getDisplayName(TextStyle.SHORT, locale))
                .collect(Collectors.joining(", "));

        //return the text
        return daysText
                + " - "
                + OPENING_TIME.format(formatter)
                + "-"
                + CLOSING_TIME.format(formatter);

    }

}
