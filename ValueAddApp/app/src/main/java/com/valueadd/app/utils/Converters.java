package com.valueadd.app.utils;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null || value.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(value.split("\\|")));
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).trim());
            if (i < list.size() - 1) sb.append("|");
        }
        return sb.toString();
    }
}
