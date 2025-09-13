package org.badmintonchain.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateBookingCode {
    public static String generate() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // YYYYMMDD
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000); // 4 số ngẫu nhiên
        return "BK-" + datePart + "-" + randomPart;
    }
}
