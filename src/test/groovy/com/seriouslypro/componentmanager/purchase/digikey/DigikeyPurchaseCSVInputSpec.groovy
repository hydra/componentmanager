package com.seriouslypro.componentmanager.purchase.digikey

import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext
import spock.lang.Specification

class DigikeyPurchaseCSVInputSpec extends Specification {

    def 'parse'() {
        given:
            String inputFileName = "test"

            String content = 'Index,DigiKey Part #,Manufacturer Part Number,Description,Customer Reference,Quantity,Backorder,Unit Price,Extended Price\n' +
                    '1,296-LM60430DRPKRCT-ND,LM60430DRPKR,"3.8-V TO 36-V, 3-A, ULTRA-SMALL",,500,0,€1.15068,€575.34\n' +
                    '2,609-10132797-013100LFCT-ND,10132797-013100LF,HEIGHT PLUG CONNECTOR,,250,0,€0.35810,€179.05\n' +
                    ',,,,,,,Subtotal,€754.39\n'

            Reader reader = new StringReader(content)

        and:
            ArrayList<DigikeyPurchase> expectedPurchases = [
                new DigikeyPurchase(
                    supplierPart:'296-LM60430DRPKRCT-ND',
                    manufacturerPart: 'LM60430DRPKR',
                    manufacturer: "UNKNOWN",
                    description: '3.8-V TO 36-V, 3-A, ULTRA-SMALL',
                    quantity: 500,
                    price: 1.15068,
                    currency: 'EUR'
                ),
                new DigikeyPurchase(
                    supplierPart:'609-10132797-013100LFCT-ND',
                    manufacturerPart: '10132797-013100LF',
                    manufacturer: "UNKNOWN",
                    description: 'HEIGHT PLUG CONNECTOR',
                    quantity: 250,
                    price: 0.35810,
                    currency: 'EUR'
                )
            ]

        and:
            CSVInput csvInput = new DigikeyPurchaseCSVInput(inputFileName, reader)
            ArrayList<DigikeyPurchase> purchases = []

        when:
            csvInput.parseHeader()

            csvInput.parseLines { CSVInputContext context, DigikeyPurchase purchase, String[] line ->
                purchases << purchase
            }

        then:
            purchases == expectedPurchases
    }
}
