package com.seriouslypro.componentmanager.bom

import com.seriouslypro.componentmanager.purchase.Purchase
import com.seriouslypro.eda.part.PartMapping
import spock.lang.Specification
import spock.lang.Unroll

class ExactOptionMatchingStrategySpec extends Specification {

    @Unroll
    def 'match with case-insensitivity on the manufacturer field - #purchasePartCode, #purchaseMfgr, #partMappingPartCode, #partMappingMfgr'() {
        given:
            def strategy = new ExactOptionMatchingStrategy()

            def purchase = new Purchase(
                partCode: purchasePartCode,
                manufacturer: purchaseMfgr,
            )

            def partMapping = new PartMapping(
                partCode: partMappingPartCode,
                manufacturer: partMappingMfgr
            )

        expect:
            expectResult == strategy.matches(purchase, partMapping)

        where:
            purchasePartCode|purchaseMfgr|partMappingPartCode|partMappingMfgr|expectResult
            "PART"|"MFGR"|"PART"|"MFGR"|true
            "PART"|"MFGR"|"PART"|"mfgr"|true
            "PART"|"mfgr"|"PART"|"mfgr"|true
            "PART"|"mfgr"|"part"|"mfgr"|false
            "PART"|"MFGR"|"part"|"mfgr"|false
    }
}
