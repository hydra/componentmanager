package com.seriouslypro.componentmanager.purchase

import com.seriouslypro.csv.CSVColumn

enum PurchaseCSVHeaders implements CSVColumn<PurchaseCSVHeaders> {

    DATE(["Date"]),
    ORDER(["Order Reference"]),
    SUPPLIER(["Supplier"]),
    LINE_ITEM(["Line Item"]),
    SUPPLIER_REFERENCE(["Supplier Reference"]),
    PART(["Part"]),
    MANUFACTURER(["Manufacturer"]),
    DESCRIPTION(["Description"]),
    QUANTITY(["Quantity"]),
    UNIT_PRICE(["Unit Price"]),
    LINE_PRICE(["Line Price"]),
    CURRENCY(["Currency"])

    PurchaseCSVHeaders(List<String> aliases = []) {
        this.aliases = aliases
    }
}