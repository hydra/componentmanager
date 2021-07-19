package com.seriouslypro.componentmanager.purchase

import com.seriouslypro.csv.CSVInput

class PurchaseCSVInput extends CSVInput<Purchase, PurchaseCSVHeaders>  {
    PurchaseCSVInput(String reference, Reader reader) {
        super(reference, reader, new PurchaseHeaderParser(), new PurchaseLineParser(), ',' as char)
    }
}
