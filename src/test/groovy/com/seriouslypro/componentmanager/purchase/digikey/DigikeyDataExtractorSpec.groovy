package com.seriouslypro.componentmanager.purchase.digikey

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class DigikeyDataExtractorSpec extends Specification {

    @Unroll
    def 'extract date'() {
        given:
            DigikeyDataExtractor extractor = new DigikeyDataExtractor(fileName: fileName)

        expect:
            extractor.getOrderDate() == expectedOrderDate

        where:
            fileName                            | expectedOrderDate           | scenario
            // Actual real-world data
            "DK_PRODUCTS_70211052_20210615.csv" | new LocalDate(2021, 06, 15) | "YYYYMMDD 8 digit date format"
            "DK_PRODUCTS_85805679_20240310.csv" | new LocalDate(2024, 03, 10) | "YYYYMMDD 8 digit date format"
    }

    @Unroll
    def 'extract date - errors'() {
        given:
            DigikeyDataExtractor extractor = new DigikeyDataExtractor(fileName: fileName)

        when:
            extractor.getOrderDate()

        then:
            Exception thrown = thrown()
            thrown instanceof IllegalArgumentException
            thrown.message == "unable to extract order date from filename, format should be 'DK_PRODUCTS_<order>_<YYYYMMDD>.csv', file: '${fileName}'"

        where:
            fileName                            | scenario
            "DK_PRODUCTS_70211052_20241506.csv" | "incorrect YYYYDDMMD date format"
            "DK_PRODUCTS_85805679_240310.csv"   | "incorrect YYMMDD date format"
            "DK_PRODUCTS_85805679.csv"          | "missing date"
            "85805679_240310.csv"               | "missing prefix"
            "85805679.csv"                      | "missing prefix and date"
            null                                | "missing"
    }

    @Unroll
    def 'extract order number'() {
        given:
            DigikeyDataExtractor extractor = new DigikeyDataExtractor(fileName: fileName)

        expect:
            extractor.getOrderNumber() == expectedOrderNumber

        where:
            fileName                            | expectedOrderNumber
            // Actual real-world data
            "DK_PRODUCTS_70211052_20210615.csv" | "70211052"
            "DK_PRODUCTS_85805679_20240310.csv" | "85805679"
    }
}
