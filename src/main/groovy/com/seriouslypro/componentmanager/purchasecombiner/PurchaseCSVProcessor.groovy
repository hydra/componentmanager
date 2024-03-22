package com.seriouslypro.componentmanager.purchasecombiner

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import com.seriouslypro.componentmanager.bom.Debug
import com.seriouslypro.componentmanager.purchase.farnell.FarnellPurchaseCSVInput
import com.seriouslypro.componentmanager.purchase.lcsc.LCSCDataExtractor
import com.seriouslypro.componentmanager.purchase.lcsc.LCSCPurchaseCSVInput
import com.seriouslypro.componentmanager.purchase.lcsc.SupplierPurchase
import com.seriouslypro.componentmanager.purchase.mouser.MouserPurchaseCSVInput
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInput.CSVParseException
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.googlesheets.GridRangeConverter

class PurchaseCSVProcessor {
    Sheets service
    Spreadsheet spreadsheet
    Sheet sheet

    int updatedRowCount = 0

    private static final int HEADER_ROW_COUNT = 1

    void process(File sourceFile) {
        System.out.println("Processing file: ${sourceFile.absolutePath}")
        ArrayList<SupplierPurchase> purchases = readPurchases(sourceFile)
        System.out.println(purchases)

        if (purchases.empty) {
            return
        }

        updateSheet(purchases)
    }

    void updateSheet( ArrayList<SupplierPurchase> purchases) {

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

        insertRows(spreadsheetId, HEADER_ROW_COUNT, purchases.size())
        updateRows(spreadsheetId, sheetTitle, HEADER_ROW_COUNT, columnCount, purchases)
    }

    void updateRows(String spreadsheetId, String sheetTitle, Integer start, Integer columnCount, List<SupplierPurchase> purchases) {

        ValueRange valueRange = new ValueRange()
        List<List<Object>> rows = []


        purchases.eachWithIndex { SupplierPurchase purchase, int i ->
            List<Object> row = [null] * columnCount

            // TODO use a header to column index mapping, fixed index for now.
            row[0] = purchase.orderDate.format("yyyy/MM/dd")
            row[1] = purchase.orderNumber
            row[2] = purchase.supplier
            row[3] = indexToLineItem(i)
            row[4] = purchase.supplierPart
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
            Debug.trace(rowValues as String)
        }
    }

    enum Supplier {
        LCSC,
        MOUSER,
        FARNELL
    }

    private ArrayList<SupplierPurchase> readPurchases(File sourceFile) {

        List<Exception> exceptions = []
        CSVInput csvInput

        Supplier supplier = Supplier.find { supplier ->
            switch (supplier) {
                case Supplier.LCSC:
                    Reader reader = makeGarbageSkippingFileReader(sourceFile)
                    csvInput = new LCSCPurchaseCSVInput(sourceFile.name, reader)
                    break;
                case Supplier.MOUSER:
                    Reader reader = makeGarbageSkippingFileReader(sourceFile)
                    csvInput = new MouserPurchaseCSVInput(sourceFile.name, reader)
                    break
                case Supplier.FARNELL:
                    Reader reader =  makeUTF8Reader(sourceFile)
                    csvInput = new FarnellPurchaseCSVInput(sourceFile.name, reader)
                    break
            }

            try {
                csvInput.parseHeader()
            } catch (CSVParseException parseException) {
                exceptions << parseException
                return false
            }

            return true
        }

        if (!supplier) {
            throw new RuntimeException("No parsers were able to parse the file '$sourceFile', exceptions: '$exceptions'")
        }


        ArrayList<SupplierPurchase> purchases = []
        csvInput.parseLines { CSVInputContext context, SupplierPurchase purchase, String[] line ->

            switch(supplier) {
                case Supplier.LCSC:
                    // For now, we repeat this for every purchase, a later refactoring should seek to remove both the switch statements in this method.
                    LCSCDataExtractor lcscDataExtractor = new LCSCDataExtractor(fileName: sourceFile.name)

                    purchase.orderNumber = lcscDataExtractor.getOrderNumber()
                    if (!purchase.orderNumber) {
                        throw new RuntimeException("Unable to extract order number from filename, ensure the filename is the same as the order number, file: '$sourceFile'")
                    }

                    purchase.orderDate = lcscDataExtractor.getOrderDate()
                    if (!purchase.orderDate) {
                        throw new RuntimeException("Unable to extract order date from filename, ensure the filename is the same as the order number, which should contain a date, e.g. WM2401170052.csv = 2024/01/17, file: '$sourceFile'")
                    }

                    break;
            }

            purchases << purchase
        }

        csvInput.close()


        purchases
    }

    private InputStreamReader makeUTF8Reader(File sourceFile) {
        new InputStreamReader(new FileInputStream(sourceFile), "UTF-8")
    }

    private Reader makeGarbageSkippingFileReader(File sourceFile) {
        /*
            The CSV files that LCSC's website generates files like this:

            <Header="0xEFBBBF"><CSV data...><EOF>

            The header causes problems for the CSV reader implementation and must be skipped.

            The same was observed in a Mouser XLS file saved as CSV by Excel 2016...
         */

        Reader headerReader = makeUTF8Reader(sourceFile)

        int unprintableCharacterCount = 0
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

            if (c[0] < 32 || c[0] > 127) {
                unprintableCharacterCount ++
            }

            boolean quoteNotFound = headerLength >= 3
            if (quoteNotFound) {
                headerLength = unprintableCharacterCount
                break
            }
        } while (true)

        headerReader.close()

        Reader reader = makeUTF8Reader(sourceFile)
        reader.skip(headerLength)

        return reader
    }
}


