package com.seriouslypro.componentmanager.purchase.mouser

import com.seriouslypro.componentmanager.currency.Currency
import com.seriouslypro.csv.*
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDate
import java.util.regex.Matcher

/**
 * Generate CSV from mouser website.
 * "Order History - Part History / Search"
 * Filter the results by "Sales order number" (maybe open the order history in a new browser tab)
 * Set page to 100 per page, or more if you can.
 * Click "Export this page to Excel" ON EACH PAGE.
 * Save each file to a folder.
 * For each file, load into Excel the save as "CSV UTF-8 (Comma delimited) (*.csv)".  Note: it's important to use UTF-8 so that the currency symbol is exported correctly.
 * Repeat the search for each order.
 *
 * Point the tool at the folder containing the .CSV files.
 */
@ToString(includeNames = true, includePackage = false, includeSuperProperties = true)
@EqualsAndHashCode(callSuper = true)
class MouserPurchase extends com.seriouslypro.componentmanager.purchase.SupplierPurchase {
    String supplier = 'Mouser'
}

enum MouserPurchaseCSVHeaders implements CSVColumn<MouserPurchaseCSVHeaders> {
    PART(["Mouser Part No"]),
    MANUFACTURER_PART(["Mfr. Part No."]),
    MANUFACTURER(["Mfr."]),
    DESCRIPTION(["Description"]),
    QUANTITY(["Qty.", "Qty Shipped"]),
    PRICE(["Price"]),
    SALES_ORDER(["Sales Order No."]),
    ORDER_DATE(["Order Date"])

    MouserPurchaseCSVHeaders(List<String> aliases = []){
        this.aliases = aliases
    }
}

class MouserPurchaseCSVInput extends CSVInput<MouserPurchase, MouserPurchaseCSVHeaders> {

    static CSVLineParser<MouserPurchase, MouserPurchaseCSVHeaders> edaPartMappingParser = new CSVLineParserBase<MouserPurchase, MouserPurchaseCSVHeaders>() {

        @Override
        MouserPurchase parse(CSVInputContext context, String[] rowValues) {

            String priceWithCurrencySymbol = rowValues[columnIndex(context, MouserPurchaseCSVHeaders.PRICE)]
            Matcher matcher = priceWithCurrencySymbol =~ /^(?<price>.{2,}) +(?<currency>\S{1})$/
            if (!matcher.matches()) {
                matcher = priceWithCurrencySymbol =~ /^(?<currency>\S{1}) +(?<price>.{2,})$/
                if (!matcher.matches()) {
                    throw new CSVInput.CSVParseException("invalid price format, expected: \"<price> <currencySymbol>\" or \"<currencySymbol> <price>\", value: \"$priceWithCurrencySymbol\"")
                }
            }
            Currency currency = Currency.fromSymbol(Currency, matcher.group('currency'))
            BigDecimal price = matcher.group('price').replace(',', '.') as BigDecimal

            LocalDate orderDate = LocalDate.parse(rowValues[columnIndex(context, MouserPurchaseCSVHeaders.ORDER_DATE)], 'dd-MMM-yy')
            return new MouserPurchase(
                supplierPart: rowValues[columnIndex(context, MouserPurchaseCSVHeaders.PART)].trim(),
                manufacturerPart: rowValues[columnIndex(context, MouserPurchaseCSVHeaders.MANUFACTURER_PART)].trim(),
                manufacturer: rowValues[columnIndex(context, MouserPurchaseCSVHeaders.MANUFACTURER)].trim(),
                description: rowValues[columnIndex(context, MouserPurchaseCSVHeaders.DESCRIPTION)].trim(),
                quantity: rowValues[columnIndex(context, MouserPurchaseCSVHeaders.QUANTITY)] as Integer,
                price: price,
                currency: currency.toString(),
                orderDate: orderDate,
                orderNumber: rowValues[columnIndex(context, MouserPurchaseCSVHeaders.SALES_ORDER)] as Integer,
            )
        }
    }

    static CSVHeaderParser<MouserPurchaseCSVHeaders> edaPartMappingHeaderParser = new CSVHeaderParserBase<MouserPurchaseCSVHeaders>() {
        @Override
        MouserPurchaseCSVHeaders parseHeader(CSVInputContext context, String headerValue) {
            MouserPurchaseCSVHeaders.fromString(MouserPurchaseCSVHeaders, headerValue) as MouserPurchaseCSVHeaders
        }

        @Override
        Map<MouserPurchaseCSVHeaders, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
            Map<MouserPurchaseCSVHeaders, CSVHeader> headerMappings = createHeaderMappings(context, headerValues)

            def requiredHeaders = [
                MouserPurchaseCSVHeaders.PART,
                MouserPurchaseCSVHeaders.MANUFACTURER,
                MouserPurchaseCSVHeaders.MANUFACTURER_PART,
                MouserPurchaseCSVHeaders.DESCRIPTION,
                MouserPurchaseCSVHeaders.QUANTITY,
                MouserPurchaseCSVHeaders.PRICE,
            ]

            RequiredHeadersVerifier.verifyRequiredHeadersPresent(requiredHeaders, headerMappings, headerValues)

            headerMappings
        }
    }

    MouserPurchaseCSVInput(String reference, Reader reader) {
        super(reference, reader, edaPartMappingHeaderParser, edaPartMappingParser)
    }
}
