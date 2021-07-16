package com.seriouslypro.componentmanager.purchase.lcsc

import com.seriouslypro.csv.CSVColumn
import com.seriouslypro.csv.CSVInput
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class LCSCPurchase {
    String lcscPart
    String manufacturerPart
    String manufacturer
    String description
    Integer quantity
    BigDecimal price
    String currency
}

enum LCSCPurchasesCSVHeaders implements CSVColumn<LCSCPurchasesCSVHeaders> {
    PART(["LCSC Part Number"]),
    MANUFACTURER_PART(["Manufacture Part Number"]),
    MANUFACTURER(["Manufacturer"]),
    DESCRIPTION(["Description"]),
    QUANTITY(["Order Qty."]),
    PRICE(["Unit Price"])

    LCSCPurchasesCSVHeaders(List<String> aliases = []) {
        this.aliases = aliases
    }
}

class LCSCPurchasesCSVInput extends CSVInput<LCSCPurchase, LCSCPurchasesCSVHeaders> {

    LCSCPurchasesCSVInput(String reference, Reader reader) {
        super(reference, reader, new LCSCPurchaseHeaderParser(), new LCSCPurchaseLineParser())
    }
}

