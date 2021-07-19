package com.seriouslypro.componentmanager.purchasecombiner

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.InsertDimensionRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import com.seriouslypro.componentmanager.purchase.lcsc.LCSCDataExtractor
import com.seriouslypro.componentmanager.purchase.lcsc.LCSCPurchase
import com.seriouslypro.componentmanager.purchase.lcsc.LCSCPurchaseCSVInput
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.googlesheets.GridRangeConverter

import java.time.LocalDate

class PurchaseCSVProcessor {
    Sheets service
    Spreadsheet spreadsheet
    Sheet sheet

    int updatedRowCount = 0

    private static final int HEADER_ROW_COUNT = 1

    void process(File sourceFile) {
        ArrayList<LCSCPurchase> purchases = readPurchases(sourceFile)
        System.out.println(purchases)

        if (purchases.empty) {
            return
        }

        LCSCDataExtractor lcscDataExtractor = new LCSCDataExtractor(fileName: sourceFile.name)
        LocalDate orderDate = lcscDataExtractor.getOrderDate()
        String orderNumber = lcscDataExtractor.getOrderNumber()

        updateSheet(orderDate, orderNumber, purchases)
    }

    void updateSheet(LocalDate orderDate, String orderNumber, ArrayList<LCSCPurchase> lcscPurchases) {

        String spreadsheetId = spreadsheet.getSpreadsheetId()
        String sheetTitle = sheet.getProperties().getTitle()

        int columnCount = sheet.getProperties().getGridProperties().getColumnCount()
        int rowCount = sheet.getProperties().getGridProperties().getRowCount()

        if (rowCount <= HEADER_ROW_COUNT) {
            // need header row + data rows to do something useful
            return
        }

        GridRange gridRange = new GridRange()
        gridRange.setStartColumnIndex(0)
        gridRange.setEndColumnIndex(columnCount - 1)

        gridRange.setStartRowIndex(0)
        gridRange.setEndRowIndex(HEADER_ROW_COUNT - 1)

        String headersRange = sheetTitle + '!' + GridRangeConverter.toString(gridRange)
        ValueRange headersValuesRangeResponse = service.spreadsheets().values().get(spreadsheetId, headersRange).execute()
        List<List<Object>> headersValues = headersValuesRangeResponse.getValues()
        dumpRows(headersValues)

        insertRows(spreadsheetId, HEADER_ROW_COUNT, lcscPurchases.size())
        updateRows(spreadsheetId, sheetTitle, HEADER_ROW_COUNT, columnCount, orderDate, orderNumber, lcscPurchases)
    }

    void updateRows(String spreadsheetId, String sheetTitle, Integer start, Integer columnCount, LocalDate orderDate, String orderNumber, ArrayList<LCSCPurchase> purchases) {

        ValueRange valueRange = new ValueRange()
        List<List<Object>> rows = []


        purchases.eachWithIndex { LCSCPurchase purchase, int i ->
            List<Object> row = [null] * columnCount

            // TODO use a header to column index mapping, fixed index for now.
            row[0] = orderDate.format("yyyy/MM/dd")
            row[1] = orderNumber
            row[2] = "LCSC"
            row[3] = indexToLineItem(i)
            row[4] = purchase.lcscPart
            row[5] = purchase.manufacturerPart
            row[6] = purchase.manufacturer
            row[7] = purchase.description
            row[8] = purchase.quantity
            row[9] = purchase.price
            row[10] = purchase.price * purchase.quantity
            row[11] = purchase.currency.toString()

            rows << row
        }

        valueRange.setValues(rows)

        GridRange gridRange = new GridRange()
        gridRange.setStartColumnIndex(0)
        gridRange.setEndColumnIndex(columnCount - 1)

        gridRange.setStartRowIndex(start)
        gridRange.setEndRowIndex(start + purchases.size() - 1)

        String range = sheetTitle + '!' + GridRangeConverter.toString(gridRange)

        UpdateValuesResponse updateValuesResponse = service.spreadsheets().values().update(spreadsheetId, range, valueRange)
            .setValueInputOption('USER_ENTERED') // TODO feels like there should be an enum for this value
            .execute()

        updatedRowCount += updateValuesResponse.getUpdatedRows()

    }

    private int indexToLineItem(int i) {
        i + 1
    }

    void insertRows(String spreadsheetId, Integer start, Integer count) throws IOException{
        InsertDimensionRequest insertDimensionRequest = new InsertDimensionRequest();
        insertDimensionRequest.setRange(new DimensionRange()
            .setDimension("ROWS")
            .setStartIndex(start)
            .setEndIndex(start + count)
            .setSheetId(0)
        );

        BatchUpdateSpreadsheetRequest updateRequest = new BatchUpdateSpreadsheetRequest().setRequests(Arrays.asList(
            new Request().setInsertDimension(insertDimensionRequest)
        ));

        service.spreadsheets().batchUpdate(spreadsheetId, updateRequest).execute();
    }

    static void dumpRows(List<List<Object>> rowsValues) {
        rowsValues.each { rowValues ->
            println(rowValues)
        }
    }

    private ArrayList<LCSCPurchase> readPurchases(File sourceFile) {
        Reader reader = makeLCSCFileReader(sourceFile)

        CSVInput csvInput = new LCSCPurchaseCSVInput(sourceFile.name, reader)

        csvInput.parseHeader()

        ArrayList<LCSCPurchase> purchases = []
        csvInput.parseLines { CSVInputContext context, LCSCPurchase purchase, String[] line ->
            purchases << purchase
        }

        csvInput.close()

        purchases
    }

    private Reader makeLCSCFileReader(File sourceFile) {
        /*
            The CSV files that LCSC's website generates files like this:

            <Header="0xEFBBBF"><CSV data...><EOF>

            The header causes problems for the CSV reader implementation and must be skipped.
         */

        Reader headerReader = new FileReader(sourceFile)

        int headerLength = 0
        do {
            char[] c = new char[1]
            int charsRead = headerReader.read(c)
            if (charsRead != 1) {
                break
            }
            if (c[0] == '"') {
                break
            }
            headerLength++
        } while (true)

        headerReader.close()

        Reader reader = new FileReader(sourceFile)
        reader.skip(headerLength)

        return reader
    }
}


