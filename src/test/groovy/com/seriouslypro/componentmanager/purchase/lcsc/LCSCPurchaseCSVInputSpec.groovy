package com.seriouslypro.componentmanager.purchase.lcsc

import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext
import spock.lang.Specification

class LCSCPurchaseCSVInputSpec extends Specification {

    def 'parse'() {
        given:
            String inputFileName = "test"

            String content = '"LCSC Part Number","Manufacture Part Number","Manufacturer","Customer NO.","Package","Description","RoHS","Order Qty.","Min\\Mult Order Qty.","Unit Price","Order Price"\n' +
                '"C75549","MMBT3906,215","Nexperia","","SOT-23(SOT-23-3)","PNP 200mA 40V 250mW SOT-23(SOT-23-3) Bipolar Transistors - BJT RoHS","YES","50","50\\50","$0.014485","$0.72"\n'

            Reader reader = new StringReader(content)

        and:
            ArrayList<LCSCPurchase> expectedPurchases = [
                new LCSCPurchase(
                    supplierPart:'C75549',
                    manufacturerPart: 'MMBT3906,215',
                    manufacturer: 'Nexperia',
                    description: 'PNP 200mA 40V 250mW SOT-23(SOT-23-3) Bipolar Transistors - BJT RoHS',
                    quantity: 50,
                    price: 0.014485,
                    currency: 'USD'
                )
            ]

        and:
            CSVInput csvInput = new LCSCPurchaseCSVInput(inputFileName, reader)
            ArrayList<LCSCPurchase> purchases = []

        when:
            csvInput.parseHeader()

            csvInput.parseLines { CSVInputContext context, LCSCPurchase purchase, String[] line ->
                purchases << purchase
            }

        then:
            purchases == expectedPurchases
    }
}
