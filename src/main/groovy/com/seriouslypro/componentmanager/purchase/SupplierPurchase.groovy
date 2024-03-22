package com.seriouslypro.componentmanager.purchase

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDate

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
abstract class SupplierPurchase {
    String supplierPart
    String manufacturerPart
    String manufacturer
    String description
    Integer quantity
    BigDecimal price
    String currency
    LocalDate orderDate
    String orderNumber

    abstract String getSupplier()
}
