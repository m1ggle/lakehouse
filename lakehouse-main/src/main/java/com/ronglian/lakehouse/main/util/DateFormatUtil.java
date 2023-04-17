package com.ronglian.lakehouse.main.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateFormatUtil {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dtfde = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Long toTs(String dates, boolean isFull){
        LocalDateTime localDateTime = null;
        if (!isFull) {
            dates = dates + " 00:00:00";

        }
        localDateTime = LocalDateTime.parse(dates,dtfde);
        return localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();

    }

    public static Long toTs(String dates){
        return toTs(dates,false);
    }

    public static String toDate(Long ts){
        Date date = new Date(ts);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return dtf.format(localDateTime);

    }

    public static String toYmdHms(Long ts) {
        Date dt = new Date(ts);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(dt.toInstant(), ZoneId.systemDefault());
        return dtfde.format(localDateTime);
    }
}
