package com.seriouslypro.componentmanager.purchase.farnell

import com.seriouslypro.componentmanager.currency.Currency
import com.seriouslypro.csv.*
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.math.RoundingMode
import java.time.LocalDate
import java.util.regex.Matcher

/**
 * The Farnell website, as of 2021/07/21 has some issues with the CSV export facility.
 *
 * 1) When clicking 'Export to CSV with Line Details' sometimes it gives you the .CSV file 'ORDERLINEOrderDetail.csv' and sometimes it gives you a file called 'OrderDetail.xls'
 *    One order needed about 50 retries before it generated the file.
 *
 * 2) Some of the files exported, have the DATA for the 'Order Status' field MISSING!  The header is there, but the data, and the ',' separator is completely missing for all rows.
 *
 * Example order: 67370810
 * First two lines from the file:
 * PO Number,Order Confirmation Number,Expected with You,Order Status,Tracking No.,Order Date,Currency,Goods Total,Shipping,Additional Charges,Tax,Order Total,Applied Vouchers,Order Source,Order Code,Your Part No,Line Note,Description,Manufacturer,Manufacturer Part Number,Quantity,Unit Price,Line Total
 * 6737-0810/00,67370810,-,1Z2X07F40405673222,2019.04.09,GBP,GBP 213.92,GBP 0,GBP 3.67,GBP 43.52,GBP 261.11,,WEB,1593427,,,,,,50.0,GBP 0.0246,GBP 1.23
 *
 * 3) Sometimes the CSV file has no data.
 *
 * Example order:  67202432
 * Entire contents of file:
 * PO Number,Order Confirmation Number,Expected with You,Order Status,Tracking No.,Order Date,Currency,Goods Total,Shipping,Additional Charges,Tax,Order Total,Applied Vouchers,Order Source,Order Code,Your Part No,Line Note,Description,Manufacturer,Manufacturer Part Number,Quantity,Unit Price,Line Total
 *
 * This implementation does not support the broken files and they must be manually fixed with a text editor first.
 *
 * Additionally:
 * - incredibly, the 'Quantity' field has a decimal point, though it's not possible to order less than one of anything...
 */

@ToString(includeNames = true, includePackage = false, includeSuperProperties = true)
@EqualsAndHashCode(callSuper = true)
class FarnellPurchase extends com.seriouslypro.componentmanager.purchase.SupplierPurchase {
    String supplier = 'Farnell'
}

enum FarnellPurchaseCSVHeaders implements CSVColumn<FarnellPurchaseCSVHeaders> {
    ORDER_CODE(["OrderCode"]),
    MANUFACTURER_PART(["Manufacturer Part Number"]),
    MANUFACTURER(["Manufacturer"]),
    DESCRIPTION(["Description"]),
    QUANTITY(["Quantity"]),
    UNIT_PRICE(["Unit Price"]),
    ORDER_CONFIRMATION_NUMBER(["Order Confirmation Number"]),
    ORDER_DATE(["Order Date"])

    FarnellPurchaseCSVHeaders(List<String> aliases = []){
        this.aliases = aliases
    }
}

class FarnellPurchaseCSVInput extends CSVInput<FarnellPurchase, FarnellPurchaseCSVHeaders> {

    static CSVLineParser<FarnellPurchase, FarnellPurchaseCSVHeaders> edaPartMappingParser = new CSVLineParserBase<FarnellPurchase, FarnellPurchaseCSVHeaders>() {

        @Override
        FarnellPurchase parse(CSVInputContext context, String[] rowValues) {

            String priceWithCurrencySymbol = rowValues[columnIndex(context, FarnellPurchaseCSVHeaders.UNIT_PRICE)]
            Matcher matcher = priceWithCurrencySymbol =~ /^(?<currency>\w{3}) (?<price>.*)$/
            if (!matcher.matches()) {
                throw new CSVInput.CSVParseException("invalid price format, expected: \"<currencyCode> <price>\", value: \"$priceWithCurrencySymbol\"")
            }
            Currency currency = Currency.fromSymbol(Currency, matcher.group('currency'))
            BigDecimal price = matcher.group('price').replace(',', '.') as BigDecimal

            LocalDate orderDate = LocalDate.parse(rowValues[columnIndex(context, FarnellPurchaseCSVHeaders.ORDER_DATE)], 'yyyy.MM.dd')

            BigDecimal decimalQuantity = rowValues[columnIndex(context, FarnellPurchaseCSVHeaders.QUANTITY)] as BigDecimal
            int quantity = decimalQuantity.setScale(0, RoundingMode.UP).intValue()

            return new FarnellPurchase(
                supplierPart: rowValues[columnIndex(context, FarnellPurchaseCSVHeaders.ORDER_CODE)].trim(),
                manufacturerPart: requireNonEmptyValue(context, rowValues, FarnellPurchaseCSVHeaders.MANUFACTURER_PART),
                manufacturer: requireNonEmptyValue(context, rowValues, FarnellPurchaseCSVHeaders.MANUFACTURER),
                description: requireNonEmptyValue(context, rowValues, FarnellPurchaseCSVHeaders.DESCRIPTION),
                quantity: quantity,
                price: price,
                currency: currency.toString(),
                orderDate: orderDate,
                orderNumber: rowValues[columnIndex(context, FarnellPurchaseCSVHeaders.ORDER_CONFIRMATION_NUMBER)] as Integer,
            )
        }

        String requireNonEmptyValue(CSVInputContext context, String[] rowValues, FarnellPurchaseCSVHeaders column) {
            String value = rowValues[columnIndex(context, column)].trim()
            if (value.empty) {
                throw new CSVInput.CSVParseException("Missing data in source file")
            }

            value
        }
    }

    static CSVHeaderParser<FarnellPurchaseCSVHeaders> edaPartMappingHeaderParser = new CSVHeaderParserBase<FarnellPurchaseCSVHeaders>() {
        @Override
        FarnellPurchaseCSVHeaders parseHeader(CSVInputContext context, String headerValue) {
            FarnellPurchaseCSVHeaders.fromString(FarnellPurchaseCSVHeaders, headerValue) as FarnellPurchaseCSVHeaders
        }

        @Override
        Map<FarnellPurchaseCSVHeaders, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
            Map<FarnellPurchaseCSVHeaders, CSVHeader> headerMappings = createHeaderMappings(context, headerValues)

            def requiredHeaders = [
                FarnellPurchaseCSVHeaders.ORDER_CODE,
                FarnellPurchaseCSVHeaders.MANUFACTURER,
                FarnellPurchaseCSVHeaders.MANUFACTURER_PART,
                FarnellPurchaseCSVHeaders.DESCRIPTION,
                FarnellPurchaseCSVHeaders.QUANTITY,
                FarnellPurchaseCSVHeaders.UNIT_PRICE,
                FarnellPurchaseCSVHeaders.ORDER_DATE,
                FarnellPurchaseCSVHeaders.ORDER_CONFIRMATION_NUMBER,
            ]

            RequiredHeadersVerifier.verifyRequiredHeadersPresent(requiredHeaders, headerMappings, headerValues)

            headerMappings
        }
    }

    FarnellPurchaseCSVInput(String reference, Reader reader) {
        super(reference, reader, edaPartMappingHeaderParser, edaPartMappingParser)
    }
}
