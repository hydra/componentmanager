package com.seriouslypro.eda.diptrace.bom

import com.seriouslypro.csv.CSVHeader
import com.seriouslypro.csv.CSVHeaderParser
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext

class DipTraceBOMHeaderParser implements CSVHeaderParser<DipTraceBOMCSVHeaders> {

    private Map<DipTraceBOMCSVHeaders, CSVHeader> createHeaderMappings(CSVInputContext context, String[] headerValues) {
        Map<DipTraceBOMCSVHeaders, CSVHeader> headerMappings = [:]
        headerValues.eachWithIndex { String headerValue, Integer index ->
            try {
                DipTraceBOMCSVHeaders dipTraceCSVHeader = parseHeader(context, headerValue)
                CSVHeader csvHeader = new CSVHeader()
                csvHeader.index = index
                headerMappings[dipTraceCSVHeader] = csvHeader
            } catch (IllegalArgumentException ignored) {
                // ignore unknown header
            }
        }
        headerMappings
    }

    @Override
    DipTraceBOMCSVHeaders parseHeader(CSVInputContext context, String headerValue) {
        DipTraceBOMCSVHeaders dipTraceCSVHeader = DipTraceBOMCSVHeaders.fromString(DipTraceBOMCSVHeaders, headerValue)
        dipTraceCSVHeader
    }

    private void verifyRequiredHeadersPresent(Map<DipTraceBOMCSVHeaders, CSVHeader> dipTraceCSVHeadersCSVHeaderMap, String[] headerValues) {
        def requiredDipTraceCSVHeaders = [
            DipTraceBOMCSVHeaders.REFDES_LIST,
            DipTraceBOMCSVHeaders.VALUE,
            DipTraceBOMCSVHeaders.NAME,
            DipTraceBOMCSVHeaders.QUANTITY
        ]

        boolean haveRequiredHeaders = dipTraceCSVHeadersCSVHeaderMap.keySet().containsAll(
            requiredDipTraceCSVHeaders
        )

        if (!haveRequiredHeaders) {
            String requiredHeaders = requiredDipTraceCSVHeaders.collect {
                it.name()
            }.toArray().join(',')

            throw new CSVInput.CSVParseException("Input CSV file does not contain all required headers, required: '$requiredHeaders', found: '$headerValues'")
        }
    }

    @Override
    Map<DipTraceBOMCSVHeaders, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
        Map<DipTraceBOMCSVHeaders, CSVHeader> headerMappings = createHeaderMappings(context, headerValues)

        verifyRequiredHeadersPresent(headerMappings, headerValues)

        headerMappings
    }
}
