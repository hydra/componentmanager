package com.seriouslypro.componentmanager.purchase.lcsc

import com.seriouslypro.csv.*

class LCSCPurchaseHeaderParser extends CSVHeaderParserBase<LCSCPurchaseCSVHeaders> {

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
