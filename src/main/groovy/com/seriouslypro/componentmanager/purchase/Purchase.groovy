package com.seriouslypro.componentmanager.purchase

import com.seriouslypro.componentmanager.currency.Currency
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDate

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class Purchase {
    LocalDate date
    String orderReference
    String supplier
    int lineItem // 1-based
    String supplierReference
    String partCode
    String manufacturer
    String description
    int Quantity
    BigDecimal unitPrice
    BigDecimal linePrice
    Currency currency
}
