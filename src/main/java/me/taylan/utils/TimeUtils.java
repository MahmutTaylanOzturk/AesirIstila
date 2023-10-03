package me.taylan.utils;

import me.taylan.AesirIstila;

public class TimeUtils {


    public static String getRemainingTime(long endDate) {
        long seconds1 = endDate - System.currentTimeMillis() / 1000;
        int days = (int) (seconds1 / 86400);
        int hours = (int) (seconds1 / 3600) % 24;
        int minutes = (int) (seconds1 / 60) % 60;
        int seconds = (int) (seconds1 % 60);
        return (days > 0 ? "" + days + " Gün," : "") + (hours > 0 ? " " + hours + " Saat," : "")
                + (minutes > 0 ? " " + minutes + " Dakika," : "") + (seconds > 0 ? " " + seconds + " Saniye" : "");

    }
}
