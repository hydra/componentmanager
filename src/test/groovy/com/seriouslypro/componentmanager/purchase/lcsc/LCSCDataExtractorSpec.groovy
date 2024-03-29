package com.seriouslypro.componentmanager.purchase.lcsc

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class LCSCDataExtractorSpec extends Specification {

    @Unroll
    def 'extract date'() {
        given:
            LCSCDataExtractor extractor = new LCSCDataExtractor(fileName: fileName)

        expect:
            extractor.getOrderDate() == expectedOrderDate

        where:
            fileName | expectedOrderDate | Note
            // Actual real-world data
            "20210128VAXS.csv" | new LocalDate(2021,01,28) | "First 8 digits are the date"
            "WM210614632W.csv" | new LocalDate(2021,06,14) | "First 6 digits are the date, remaining digits must be ignored"
            // Edge cases
            "202101281234.csv" | new LocalDate(2021,01,28) | "First 8 digits are the date, remaining digits must be ignored"
            "20210128" | new LocalDate(2021,01,28) | "8 date digits"
    }

    @Unroll
    def 'extract date - errors'() {
        given:
            LCSCDataExtractor extractor = new LCSCDataExtractor(fileName: fileName)

        when:
            extractor.getOrderDate()

        then:
            Exception thrown = thrown()
            thrown instanceof IllegalArgumentException
            thrown.message == "Unable to extract order date from filename, ensure the filename is the same as the order number, which should contain a date, e.g. WM2401170052.csv = 2024/01/17, file: '$fileName'"

        where:
            fileName  | scenarion
            "foo.csv" | "bad filename"
            null      | "missing"
    }

    @Unroll
    def 'extract order number'() {
        given:
            LCSCDataExtractor extractor = new LCSCDataExtractor(fileName: fileName)

        expect:
            extractor.getOrderNumber() == expectedOrderNumber

        where:
            fileName | expectedOrderNumber
            // Actual real-world data
            "20210128VAXS.csv" | "20210128VAXS"
            "WM210614632W.csv" | "WM210614632W"
    }
}
