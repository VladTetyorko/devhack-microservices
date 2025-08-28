package com.vladte.devhack.parser.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UaDateParsing {

    public static LocalDateTime parseUaToLdt(String dateText) {
        Locale uk = Locale.forLanguageTag("uk");
        String cutDate = trimToDate(dateText);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM uuuu", uk);
        return LocalDate.parse(cutDate.trim(), fmt).atStartOfDay();
    }

    public static String trimToDate(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        int i = s.length() - 1;
        while (i >= 0 && !Character.isDigit(s.charAt(i))) {
            i--;
        }
        return (i >= 0) ? s.substring(0, i + 1) : s;
    }


}
