package com.seriouslypro.componentmanager.purchase

import com.seriouslypro.componentmanager.currency.Currency
import groovy.transform.ToString

import java.time.LocalDate

@ToString(includeNames = true, includePackage = false)
class Purchase {
    LocalDate date
    String orderReference
    String supplier
    int lineItem // 1-based
    String supplierReference
    String part
    String manufacturer
    String description
    int Quantity
    BigDecimal unitPrice
    BigDecimal linePrice
    Currency currency
}
