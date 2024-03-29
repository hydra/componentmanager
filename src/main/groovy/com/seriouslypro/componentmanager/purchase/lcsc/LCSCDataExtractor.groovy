package com.seriouslypro.componentmanager.purchase.lcsc

import java.time.LocalDate
import java.util.regex.Pattern

class LCSCDataExtractor {
    String fileName

    LocalDate getOrderDate() {

        // Pattern specification: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
        // Named group example: https://e.printstacktrace.blog/groovy-regular-expressions-the-definitive-guide/
        Map<String, String> regexToDatePatternMap = [
            (~/^(?<date>\d{8}).*/) : "yyyyMMdd",
            (~/^.*?(?<date>\d{6}).*/) : "yyMMdd"
        ]

        LocalDate date = regexToDatePatternMap.findResult { Pattern regex, String datePattern ->
            try {
                def matcher = fileName =~ regex
                if (!matcher.find()) {
                    return null
                }
                return LocalDate.parse(matcher.group('date'), datePattern)
            } catch (ignored) {
                return null
            }
        }
        if (!date) {
            throw new IllegalArgumentException("Unable to extract order date from filename, ensure the filename is the same as the order number, which should contain a date, e.g. WM2401170052.csv = 2024/01/17, file: '$fileName'")
        }
        date
    }

    String getOrderNumber() {
        try {
            fileName.split(/\./).first()
        } catch (e) {
            throw new IllegalArgumentException("Unable to extract order number from filename, ensure the filename is the same as the order number, file: '$fileName'", e)
        }
    }
}
