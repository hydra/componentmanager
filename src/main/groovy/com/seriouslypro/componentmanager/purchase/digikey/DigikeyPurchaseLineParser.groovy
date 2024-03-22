package com.seriouslypro.componentmanager.purchase.digikey

import com.seriouslypro.componentmanager.currency.Currency
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.csv.CSVLineParserBase

class DigikeyPurchaseLineParser extends CSVLineParserBase<DigikeyPurchase, DigikeyPurchaseCSVHeaders> {
    @Override
    DigikeyPurchase parse(CSVInputContext context, String[] rowValues) {

        String priceWithCurrencySymbol = rowValues[columnIndex(context, DigikeyPurchaseCSVHeaders.PRICE)]
        Currency currency = Currency.fromSymbol(Currency, priceWithCurrencySymbol.substring(0, 1))
        BigDecimal price = priceWithCurrencySymbol.substring(1) as BigDecimal

        DigikeyPurchase DigikeyPurchase = new DigikeyPurchase(
            supplierPart: rowValues[columnIndex(context, DigikeyPurchaseCSVHeaders.PART)],
            manufacturerPart: rowValues[columnIndex(context, DigikeyPurchaseCSVHeaders.MANUFACTURER_PART)],
            manufacturer: "UNKNOWN",
            description: rowValues[columnIndex(context, DigikeyPurchaseCSVHeaders.DESCRIPTION)],
            quantity: rowValues[columnIndex(context, DigikeyPurchaseCSVHeaders.QUANTITY)] as Integer,
            price: price,
            currency: currency.toString()
        )
        return DigikeyPurchase
    }
}
