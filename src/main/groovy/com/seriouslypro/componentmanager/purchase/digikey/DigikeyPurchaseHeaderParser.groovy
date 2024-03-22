package com.seriouslypro.componentmanager.purchase.digikey

import com.seriouslypro.csv.CSVHeader
import com.seriouslypro.csv.CSVHeaderParserBase
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.csv.RequiredHeadersVerifier

class DigikeyPurchaseHeaderParser extends CSVHeaderParserBase<DigikeyPurchaseCSVHeaders> {

    @Override
    Map<DigikeyPurchaseCSVHeaders, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
        Map<DigikeyPurchaseCSVHeaders, CSVHeader> headerMappings = createHeaderMappings(context, headerValues)

        def requiredHeaders = [
            DigikeyPurchaseCSVHeaders.PART,
            DigikeyPurchaseCSVHeaders.MANUFACTURER_PART,
            DigikeyPurchaseCSVHeaders.DESCRIPTION,
            DigikeyPurchaseCSVHeaders.QUANTITY,
            DigikeyPurchaseCSVHeaders.PRICE,
        ]

        RequiredHeadersVerifier.verifyRequiredHeadersPresent(requiredHeaders, headerMappings, headerValues)

        headerMappings
    }

    @Override
    DigikeyPurchaseCSVHeaders parseHeader(CSVInputContext context, String headerValue) {
        def header = DigikeyPurchaseCSVHeaders.fromString(DigikeyPurchaseCSVHeaders, headerValue)
        header
    }
}
