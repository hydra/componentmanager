package com.seriouslypro.componentmanager.purchase.digikey

import java.time.LocalDate
import java.util.regex.Pattern

class DigikeyDataExtractor {
    String fileName

    LocalDate getOrderDate() {
        // Pattern specification: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
        // Named group example: https://e.printstacktrace.blog/groovy-regular-expressions-the-definitive-guide/
        Map<String, String> regexToDatePatternMap = [
                (~/^.*?_(?<date>\d{8})\..*/) : "yyyyMMdd",
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
            throw new IllegalArgumentException("unable to extract order date from filename, format should be 'DK_PRODUCTS_<order>_<YYYYMMDD>.csv', file: '$fileName'")
        }
        date
    }

    String getOrderNumber() {
        try {
            fileName.split(/_/)[2]
        } catch (e) {
            throw new IllegalArgumentException("unable to extract order number from filename, format should be 'DK_PRODUCTS_<order>_<YYYYMMDD>.csv', file: '$fileName'", e)
        }
    }
}
