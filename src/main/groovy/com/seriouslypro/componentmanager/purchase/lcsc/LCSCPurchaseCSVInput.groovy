package com.seriouslypro.componentmanager.purchase.lcsc


import com.seriouslypro.csv.CSVColumn
import com.seriouslypro.csv.CSVInput
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDate

@ToString(includeNames = true, includePackage = false, includeSuperProperties = true)
@EqualsAndHashCode
class LCSCPurchase extends SupplierPurchase {
    String supplier = 'LCSC'
}

@ToString(includeNames = true, includePackage = false)
abstract class SupplierPurchase {
    String supplierPart
    String manufacturerPart
    String manufacturer
    String description
    Integer quantity
    BigDecimal price
    String currency
    LocalDate orderDate
    String orderNumber

    abstract String getSupplier()
}

enum LCSCPurchaseCSVHeaders implements CSVColumn<LCSCPurchaseCSVHeaders> {
    PART(["LCSC Part Number"]),
    MANUFACTURER_PART(["Manufacture Part Number"]),
    MANUFACTURER(["Manufacturer"]),
    DESCRIPTION(["Description"]),
    QUANTITY(["Order Qty."]),
    PRICE(["Unit Price"])

    LCSCPurchaseCSVHeaders(List<String> aliases = []) {
        this.aliases = aliases
    }
}

class LCSCPurchaseCSVInput extends CSVInput<LCSCPurchase, LCSCPurchaseCSVHeaders> {

    LCSCPurchaseCSVInput(String reference, Reader reader) {
        super(reference, reader, new LCSCPurchaseHeaderParser(), new LCSCPurchaseLineParser())
    }
}

