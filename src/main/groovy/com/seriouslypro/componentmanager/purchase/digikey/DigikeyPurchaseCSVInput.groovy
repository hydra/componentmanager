package com.seriouslypro.componentmanager.purchase.digikey

import com.seriouslypro.componentmanager.purchase.SupplierPurchase
import com.seriouslypro.csv.CSVColumn
import com.seriouslypro.csv.CSVInput
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Generate CSV from Digikey website.
 * "Dashboard -> Order History"
 * View an order number to view it.
 * Ensure you can see the 'Submitted date'.
 * Press 'Download'
 * Browser should prompt to download a .CSV file, DO NOT CLICK SAVE YET.
 * Append the submit date after the filename, before the '.csv' extension, in the format '_YYYYMMDD', e.g.  for a submitted date of 2024/03/22 and
 *   an order number of '12345678' change the filename from 'DK_PRODUCTS_12345678.csv' to 'DK_PRODUCTS_12345678_20240322.csv'
 * Repeat the search for each order.
 *
 * Point the tool at the folder containing the .CSV files.
 *
 * IMPORTANT: Digikey's export tool doesn't include the manufacturer of each part, so this has to be manually corrected after importing.
 */

@ToString(includeNames = true, includePackage = false, includeSuperProperties = true)
@EqualsAndHashCode(callSuper = true)
class DigikeyPurchase extends SupplierPurchase {
    String supplier = 'Digikey'
}

enum DigikeyPurchaseCSVHeaders implements CSVColumn<DigikeyPurchaseCSVHeaders> {
    PART(["DigiKey Part #"]),
    MANUFACTURER_PART(["Manufacturer Part Number"]),
    DESCRIPTION(["Description"]),
    QUANTITY(["Quantity"]),
    PRICE(["Unit Price"])

    DigikeyPurchaseCSVHeaders(List<String> aliases = []) {
        this.aliases = aliases
    }
}

class DigikeyPurchaseCSVInput extends CSVInput<DigikeyPurchase, DigikeyPurchaseCSVHeaders> {

    DigikeyPurchaseCSVInput(String reference, Reader reader) {
        super(reference, reader, new DigikeyPurchaseHeaderParser(), new DigikeyPurchaseLineParser())
    }
}
