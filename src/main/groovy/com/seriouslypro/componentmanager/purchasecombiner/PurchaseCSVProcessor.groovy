package com.seriouslypro.componentmanager.purchasecombiner

import com.google.api.services.sheets.v4.model.Sheet
import com.seriouslypro.componentmanager.purchase.lcsc.LCSCPurchase
import com.seriouslypro.componentmanager.purchase.lcsc.LCSCPurchasesCSVInput
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext

class PurchaseCSVProcessor {
    Sheet sheet

    void process(File sourceFile) {
        ArrayList<LCSCPurchase> purchases = readPurchases(sourceFile)
        System.out.println(purchases)
    }

    private ArrayList<LCSCPurchase> readPurchases(File sourceFile) {
        Reader reader = makeLCSCFileReader(sourceFile)

        CSVInput csvInput = new LCSCPurchasesCSVInput(sourceFile.name, reader)

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


