package com.seriouslypro.componentmanager.purchase.lcsc

import com.seriouslypro.csv.*

class RequiredHeadersVerifier {
    static void verifyRequiredHeadersPresent(List<CSVColumn> requiredHeaders, Map<CSVColumn, CSVHeader> headerMap, String[] headerValues) {

        boolean haveRequiredHeaders = headerMap.keySet().containsAll(
            requiredHeaders
        )

        if (!haveRequiredHeaders) {
            String requiredHeadersString = requiredHeaders.collect {
                it.name()
            }.toArray().join(',')

            throw new CSVInput.CSVParseException("Input CSV file does not contain all required headers, required: '$requiredHeadersString', found: '$headerValues'")
        }
    }
}

class LCSCPurchaseHeaderParser  implements CSVHeaderParser<LCSCPurchasesCSVHeaders> {

    private Map<LCSCPurchasesCSVHeaders, CSVHeader> createHeaderMappings(CSVInputContext context, String[] headerValues) {
        Map<LCSCPurchasesCSVHeaders, CSVHeader> headerMappings = [:]
        headerValues.eachWithIndex { String headerValue, Integer index ->
            try {
                LCSCPurchasesCSVHeaders LCSCPurchasesCSVHeader = parseHeader(context, headerValue)
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
    Map<LCSCPurchasesCSVHeaders, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
        Map<LCSCPurchasesCSVHeaders, CSVHeader> headerMappings = createHeaderMappings(context, headerValues)

        def requiredHeaders = [
            LCSCPurchasesCSVHeaders.PART,
            LCSCPurchasesCSVHeaders.MANUFACTURER,
            LCSCPurchasesCSVHeaders.MANUFACTURER_PART,
            LCSCPurchasesCSVHeaders.DESCRIPTION,
            LCSCPurchasesCSVHeaders.QUANTITY,
            LCSCPurchasesCSVHeaders.PRICE,
        ]

        RequiredHeadersVerifier.verifyRequiredHeadersPresent(requiredHeaders, headerMappings, headerValues)

        headerMappings
    }

    @Override
    LCSCPurchasesCSVHeaders parseHeader(CSVInputContext context, String headerValue) {
        def header = LCSCPurchasesCSVHeaders.fromString(LCSCPurchasesCSVHeaders, headerValue)
        header
    }
}
