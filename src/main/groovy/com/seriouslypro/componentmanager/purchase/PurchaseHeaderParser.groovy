package com.seriouslypro.componentmanager.purchase

import com.seriouslypro.csv.CSVHeader
import com.seriouslypro.csv.CSVHeaderParser
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.csv.RequiredHeadersVerifier

class PurchaseHeaderParser implements CSVHeaderParser<PurchaseCSVHeaders> {

    Map<PurchaseCSVHeaders, CSVHeader> createHeaderMappings(CSVInputContext context, String[] headerValues) {
        Map<PurchaseCSVHeaders, CSVHeader> headerMappings = [:]
        headerValues.eachWithIndex { String headerValue, Integer index ->
            try {
                PurchaseCSVHeaders purchaseCSVHeader = parseHeader(context, headerValue)
                CSVHeader csvHeader = new CSVHeader()
                csvHeader.index = index
                headerMappings[purchaseCSVHeader] = csvHeader
            } catch (IllegalArgumentException ignored) {
                // ignore unknown header
            }
        }
        headerMappings
    }

    @Override
    Map<PurchaseCSVHeaders, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
        Map<PurchaseCSVHeaders, CSVHeader> headerMappings = createHeaderMappings(context, headerValues)

        def requiredHeaders = [
            PurchaseCSVHeaders.DATE,
            PurchaseCSVHeaders.ORDER,
            PurchaseCSVHeaders.SUPPLIER,
            PurchaseCSVHeaders.LINE_ITEM,
            PurchaseCSVHeaders.SUPPLIER_REFERENCE,
            PurchaseCSVHeaders.PART,
            PurchaseCSVHeaders.MANUFACTURER,
            PurchaseCSVHeaders.DESCRIPTION,
            PurchaseCSVHeaders.QUANTITY,
            PurchaseCSVHeaders.UNIT_PRICE,
            PurchaseCSVHeaders.LINE_PRICE,
            PurchaseCSVHeaders.CURRENCY,
        ]

        RequiredHeadersVerifier.verifyRequiredHeadersPresent(requiredHeaders, headerMappings, headerValues)

        headerMappings
    }

    @Override
    PurchaseCSVHeaders parseHeader(CSVInputContext context, String headerValue) {
        def header = PurchaseCSVHeaders.fromString(PurchaseCSVHeaders, headerValue)
        header
    }
}
