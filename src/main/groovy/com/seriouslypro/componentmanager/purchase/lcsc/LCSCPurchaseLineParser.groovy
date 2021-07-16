package com.seriouslypro.componentmanager.purchase.lcsc

import com.seriouslypro.componentmanager.currency.Currency
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.csv.CSVLineParserBase

class LCSCPurchaseLineParser  extends CSVLineParserBase<LCSCPurchase, LCSCPurchasesCSVHeaders> {
    @Override
    LCSCPurchase parse(CSVInputContext context, String[] rowValues) {

        String priceWithCurrencySymbol = rowValues[columnIndex(context, LCSCPurchasesCSVHeaders.PRICE)]
        Currency currency = Currency.fromSymbol(Currency, priceWithCurrencySymbol.substring(0, 1))
        BigDecimal price = priceWithCurrencySymbol.substring(1) as BigDecimal

        LCSCPurchase lcscPurchase = new LCSCPurchase(
            lcscPart: rowValues[columnIndex(context, LCSCPurchasesCSVHeaders.PART)],
            manufacturerPart: rowValues[columnIndex(context, LCSCPurchasesCSVHeaders.MANUFACTURER_PART)],
            manufacturer: rowValues[columnIndex(context, LCSCPurchasesCSVHeaders.MANUFACTURER)],
            description: rowValues[columnIndex(context, LCSCPurchasesCSVHeaders.DESCRIPTION)],
            quantity: rowValues[columnIndex(context, LCSCPurchasesCSVHeaders.QUANTITY)] as Integer,
            price: price,
            currency: currency.toString()
        )
        return lcscPurchase
    }
}
