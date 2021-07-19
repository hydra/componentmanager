package com.seriouslypro.componentmanager.purchase.lcsc

import com.seriouslypro.csv.*

class LCSCPurchaseHeaderParser  implements CSVHeaderParser<LCSCPurchaseCSVHeaders> {

    private Map<LCSCPurchaseCSVHeaders, CSVHeader> createHeaderMappings(CSVInputContext context, String[] headerValues) {
        Map<LCSCPurchaseCSVHeaders, CSVHeader> headerMappings = [:]
        headerValues.eachWithIndex { String headerValue, Integer index ->
            try {
                LCSCPurchaseCSVHeaders LCSCPurchasesCSVHeader = parseHeader(context, headerValue)
                CSVHeader csvHeader = new CSVHeader()
                csvHeader.index = index
                headerMappings[LCSCPurchasesCSVHeader] = csvHeader
            } catch (IllegalArgumentException ignored) {
                // ignore unknown header
            }
        }
        headerMappings
    }

    @Override
    Map<LCSCPurchaseCSVHeaders, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
        Map<LCSCPurchaseCSVHeaders, CSVHeader> headerMappings = createHeaderMappings(context, headerValues)

        def requiredHeaders = [
            LCSCPurchaseCSVHeaders.PART,
            LCSCPurchaseCSVHeaders.MANUFACTURER,
            LCSCPurchaseCSVHeaders.MANUFACTURER_PART,
            LCSCPurchaseCSVHeaders.DESCRIPTION,
            LCSCPurchaseCSVHeaders.QUANTITY,
            LCSCPurchaseCSVHeaders.PRICE,
        ]

        RequiredHeadersVerifier.verifyRequiredHeadersPresent(requiredHeaders, headerMappings, headerValues)

        headerMappings
    }

    @Override
    LCSCPurchaseCSVHeaders parseHeader(CSVInputContext context, String headerValue) {
        def header = LCSCPurchaseCSVHeaders.fromString(LCSCPurchaseCSVHeaders, headerValue)
        header
    }
}
