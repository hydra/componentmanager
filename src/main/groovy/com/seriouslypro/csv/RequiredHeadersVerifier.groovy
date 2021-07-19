package com.seriouslypro.csv

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