package com.seriouslypro.componentmanager.purchase.lcsc

import com.seriouslypro.componentmanager.currency.Currency
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.csv.CSVLineParserBase

class LCSCPurchaseLineParser  extends CSVLineParserBase<LCSCPurchase, LCSCPurchaseCSVHeaders> {
    @Override
    LCSCPurchase parse(CSVInputContext context, String[] rowValues) {

        String priceWithCurrencySymbol = rowValues[columnIndex(context, LCSCPurchaseCSVHeaders.PRICE)]
        Currency currency = Currency.fromSymbol(Currency, priceWithCurrencySymbol.substring(0, 1))
        BigDecimal price = priceWithCurrencySymbol.substring(1) as BigDecimal

        LCSCPurchase lcscPurchase = new LCSCPurchase(
            supplierPart: rowValues[columnIndex(context, LCSCPurchaseCSVHeaders.PART)],
            manufacturerPart: rowValues[columnIndex(context, LCSCPurchaseCSVHeaders.MANUFACTURER_PART)],
            manufacturer: rowValues[columnIndex(context, LCSCPurchaseCSVHeaders.MANUFACTURER)],
            description: rowValues[columnIndex(context, LCSCPurchaseCSVHeaders.DESCRIPTION)],
            quantity: rowValues[columnIndex(context, LCSCPurchaseCSVHeaders.QUANTITY)] as Integer,
            price: price,
            currency: currency.toString()
        )
        return lcscPurchase
    }
}
