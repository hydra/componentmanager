package com.seriouslypro.componentmanager.purchase

import com.seriouslypro.componentmanager.currency.Currency
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.csv.CSVLineParserBase

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PurchaseLineParser extends CSVLineParserBase<Purchase, PurchaseCSVHeaders> {

    @Override
    Purchase parse(CSVInputContext context, String[] rowValues) {
        Purchase purchase = new Purchase(
            date: LocalDate.parse(rowValues[columnIndex(context, PurchaseCSVHeaders.DATE)], DateTimeFormatter.ofPattern("yyyy/M/d")),
            orderReference: rowValues[columnIndex(context, PurchaseCSVHeaders.ORDER)] as String,
            supplier: rowValues[columnIndex(context, PurchaseCSVHeaders.SUPPLIER)] as String,
            lineItem: rowValues[columnIndex(context, PurchaseCSVHeaders.LINE_ITEM)] as Integer,
            supplierReference: rowValues[columnIndex(context, PurchaseCSVHeaders.SUPPLIER_REFERENCE)] as String,
            part: rowValues[columnIndex(context, PurchaseCSVHeaders.PART)] as String,
            manufacturer: rowValues[columnIndex(context, PurchaseCSVHeaders.MANUFACTURER)] as String,
            description: rowValues[columnIndex(context, PurchaseCSVHeaders.DESCRIPTION)] as String,
            quantity: rowValues[columnIndex(context, PurchaseCSVHeaders.QUANTITY)] as Integer,
            unitPrice: rowValues[columnIndex(context, PurchaseCSVHeaders.UNIT_PRICE)] as BigDecimal,
            linePrice: rowValues[columnIndex(context, PurchaseCSVHeaders.LINE_PRICE)] as BigDecimal,
            currency: rowValues[columnIndex(context, PurchaseCSVHeaders.CURRENCY)] as Currency,
        )
        return purchase
    }
}
