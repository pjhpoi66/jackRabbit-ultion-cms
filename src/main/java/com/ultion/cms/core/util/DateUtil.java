package com.ultion.cms.core.util;

import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Component
public class DateUtil {

    /**
     * 현재일자 구하기
     *
     * @return yyyyMMdd
     */
    public static String getNowDate() {

        return calDate("yyyy-MM-dd");
    }

    public static String getNowDateTime() {

        return calDate("yyyy-MM-dd HH:mm:ss");
    }

    public static String getNowDateTime(String format) {

        return calDate(format);
    }

    /**
     * 현재년도 구하기
     *
     * @return yyyy
     */
    public static String getNowYear() {
        Date nowDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
        String strNowDate = simpleDateFormat.format(nowDate);

        return strNowDate;
    }

    /**
     * 현재년도, 월 구하기
     *
     * @return yyyy-MM
     */
    public static String getNowMonth() {
        Date nowDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String strNowDate = simpleDateFormat.format(nowDate);

        return strNowDate;
    }

    /**
     * 일자 계산
     *
     * @param date_format
     * @return
     */
    public static String calDate(String date_format) {

        DateFormat df = new SimpleDateFormat(date_format);

        Calendar calendar = Calendar.getInstance();

        return df.format(calendar.getTime());
    }

}

