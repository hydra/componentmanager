package com.seriouslypro.componentmanager.bom

import com.seriouslypro.componentmanager.currency.Currency
import com.seriouslypro.componentmanager.purchase.Purchase
import com.seriouslypro.eda.BOMItemOption

class BOMCostResult {
    Map<Currency, BigDecimal> cost = [:]
    Map<BOMItemOption, Optional<Purchase>> purchaseMapping = [:]
}
