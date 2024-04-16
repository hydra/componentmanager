package com.seriouslypro.componentmanager.purchasecombiner

import spock.lang.Ignore
import spock.lang.Specification

class PurchaseCSVProcessorSpec extends Specification {
    @Ignore
    def 'ensure correct columns are updated'() {
        // Date	Order Reference	Supplier	Line Item	Supplier Reference	Part	Manufacturer	Description	Quantity	Unit Price	Line Price	Currency
        false
    }

    @Ignore
    def 'ensure correct columns are updated when rearranged in spreadsheet'() {
        // Date	Order Reference	Line Item	Quantity	Supplier	Supplier Reference	Part	Manufacturer	Description	Unit Price	Line Price	Currency
        // See the TODO in `updateRows`
        false
    }
}
